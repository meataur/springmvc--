package com.gomeplus.im.api.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import redis.clients.jedis.JedisCluster;

import com.gomeplus.im.api.dao.UserMapper;
import com.gomeplus.im.api.global.Global;
import com.gomeplus.im.api.model.User;
import com.gomeplus.im.api.request.ReqFindUser;
import com.gomeplus.im.api.request.response.ResultModel;
import com.gomeplus.im.api.service.UserService;
import com.gomeplus.im.api.utils.JedisClusterClient;
import com.gomeplus.im.api.utils.PasswordEncode;

@Service
public class UserServiceImpl implements UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
    
    @Autowired
    private UserMapper userMapper;
    @Override
    public ResultModel<Object> saveUser(final User user,String appId) {
    	if (user==null) {
    		logger.error("参数为空");
    		return new ResultModel<Object>(ResultModel.RESULT_FAILURE, "参数为空", new HashMap<>());
    	}
    	String userName = user.getUserName();
    	long phoneNumber = user.getPhoneNumber();
    	String nickName = user.getNickName();
    	if (StringUtils.isBlank(userName)) {
    		userName="gome_im_"+phoneNumber;
    		logger.info("用户名不能为空,生成默认用户名");
    	}
    	if (StringUtils.isBlank(nickName)) {
    		nickName="gome_im_"+phoneNumber;
    		logger.info("用户名不能为空,生成默认昵称");
    	}
    	
    	if (phoneNumber < 10000000000L) {
    		logger.error("手机号码不正确");
    		return new ResultModel<Object>(ResultModel.RESULT_FAILURE, "手机号码不正确", new HashMap<>());
    	}
    	long time = System.currentTimeMillis();
    	String pwd = user.getPassword();
    	if (StringUtils.isBlank(pwd)) {
    		logger.error("密码为空");
    		return new ResultModel<Object>(ResultModel.RESULT_FAILURE, "密码为空", new HashMap<>());
    	}
        try {
            User oldUser = userMapper.getUserInfoByPhoneNumber(phoneNumber);
            if (oldUser != null) {
            	logger.error("手机号已注册");
                return new ResultModel<Object>(ResultModel.RESULT_FAILURE, "手机号已注册", new HashMap<>());
            }
            User oldUser2=userMapper.getUserInfoByUserName(userName);
            if (oldUser2!=null) {
            	logger.error("用户名已注册");
            	return new ResultModel<Object>(ResultModel.RESULT_FAILURE, "用户名已注册", new HashMap<>());
			}

            //ZHTODO 暂时使用SHA-256,盐值为空 liuzhenhuann 20160603
            pwd = PasswordEncode.encode(pwd, "");
            //ZHTODO 用户名的hash值是用来做数据库索引时用的，当根据用户名查询时，可以生成hashCode进行查询，速度快
            //但是hash值尽量保证不同的值生成的hashCode不同，所以，暂时在使用查询的时候，不使用用户名的hashCode查询 liuzhenhuan 20160606
            user.setUserName(userName);
            if (StringUtils.isNotBlank(userName)) {
            	user.setUserNameHashId(userName.hashCode());
			}
            user.setNickName(nickName);
            user.setPassword(pwd);
            user.setCreateTime(time);
            user.setUpdateTime(time);
    		
            String token = com.gomeplus.im.api.utils.StringUtils.getUuid();
            long tokenExpires = time + Global.TOKEN_EXPIRE_TIME;
            user.setToken(token);
            user.setTokenValidity(tokenExpires);
            
            long saveRow = userMapper.saveUser(user);
            logger.debug("saveRow: "+saveRow);
            logger.info("注册成功userName{}",user.getUserName());
            return new ResultModel<Object>(ResultModel.RESULT_OK, "注册成功",new HashMap<>());
        } catch (Exception e) {
            logger.error("注册失败，出现异常",e);
            return new ResultModel<Object>(ResultModel.RESULT_FAILURE, "注册失败", new HashMap<>());
        }
    }

    @Override
    public ResultModel<Object> updateUserInfo(User reqUser,String appId,long userId) {
    	if (reqUser==null) {
			logger.error("参数为空");
			return new ResultModel<Object>(ResultModel.RESULT_FAILURE, "参数为空", new HashMap<>());
		}
        try {
            User user = new User();
            user.setId(userId);
            user.setNickName(reqUser.getNickName());
            
//            if (reqUser.getPhoneNumber()> 10000000000L&&reqUser.getPhoneNumber()<100000000000L) {
//            	user.setPhoneNumber(reqUser.getPhoneNumber());
//			}
            if(StringUtils.isNotBlank(reqUser.getAutograph())){
            	user.setAutograph(reqUser.getAutograph());
            }
            if(StringUtils.isNotBlank(reqUser.getAvatar())){
            	user.setAvatar(reqUser.getAvatar());
            }
            
            if(reqUser.getBirthday()>0){
            	user.setBirthday(reqUser.getBirthday());
            }
            if(StringUtils.isNotBlank(reqUser.getHardwareIndentifier())){
            	user.setAvatar(reqUser.getHardwareIndentifier());
            }
            if(StringUtils.isNotBlank(reqUser.getOsIdentifier())){
            	user.setAvatar(reqUser.getOsIdentifier());
            }
            
            if (reqUser.getGender()>= 1 && reqUser.getGender() <= 2) {
            	user.setGender(reqUser.getGender());
			}
            
            if(StringUtils.isNotBlank(reqUser.getNickName())){
            	user.setNickName(reqUser.getNickName());
            	//TODO 同时更新好友中的昵称 liuzhenhuan 20160614
            	//1、放入redis中 userIds，即有修改昵称的用户ID的集合
            	JedisCluster jedis = JedisClusterClient.INSTANCE.getJedisCluster();
            	jedis.sadd(Global.ALL_APP_KEY, appId);
            	String key = appId + Global.TEMP_CHANGE_NIKENAME_IDS;
            	jedis.sadd(key,userId+"");
            	//2、放入redis中userId_nickName String类型
            	jedis.set(userId+Global.TEMP_CHANGE_USERID_NICKNAME_SUFFIX, reqUser.getNickName());
            }
            
            if(StringUtils.isNotBlank(reqUser.getPassword())){
            	user.setPassword(reqUser.getPassword());
            }
            
            long now = System.currentTimeMillis();
            user.setUpdateTime(now);
            userMapper.updateUserInfo(user);
            logger.info("用户信息更新成功 id:{}",userId);
            
            return new ResultModel<Object>(ResultModel.RESULT_OK, "更新用户信息成功", new HashMap<>());
			
        } catch (Exception e) {
            logger.error("更新用户信息失败,出现异常",e);
            return new ResultModel<Object>(ResultModel.RESULT_FAILURE, "更新用户信息失败", new HashMap<>());
        }
    }
    
//    private void addIdToRedis(String userId){
//    	
//    }
//    
//    private void setNickNameToRedis(){
//    	
//    }
    
    @Override
    public ResultModel<Object> getUserInfo(String appId,User reqUser) {
        try {
            long userId=reqUser.getId();
			User user =userMapper.getUserInfoById(userId);
            HashMap<String, Object> map = getInfo(user);
            logger.info("获取用户信息成功,id:{},userName:{}",user.getId(),user.getUserName());
            return new ResultModel<Object>(ResultModel.RESULT_OK, "获取信息成功", map);
        } catch (Exception e) {
            logger.error("获取信息失败，出现异常",e);
            return new ResultModel<Object>(ResultModel.RESULT_FAILURE, "获取信息失败", new HashMap<>());
        }
    }

    @Override
	public ResultModel<Object> findUser(String keyWord,String appId) {
		if (StringUtils.isBlank(keyWord)) {
			logger.error("参数为空");
			return new ResultModel<Object>(ResultModel.RESULT_FAILURE, "参数为空", new HashMap<>());
		}
		try {
			List<User> userList = userMapper.findUser(keyWord);
			if (CollectionUtils.isEmpty(userList)) {
				logger.error("没有符合此查询条件的用户");
				return new ResultModel<Object>(ResultModel.RESULT_FAILURE,"没有符合此查询条件的用户", new HashMap<>());
			}
			List<Map<String, Object>> resultUserList=new ArrayList<Map<String,Object>>();
			for (User user : userList) {
				resultUserList.add(getInfo(user));
			}
			logger.info("根据关键字模糊查询成功,keyWord: {}",keyWord);
			return new ResultModel<Object>(ResultModel.RESULT_OK, "获取信息成功",resultUserList);
		} catch (Exception e) {
			logger.error("获取信息失败，出现异常", e);
			return new ResultModel<Object>(ResultModel.RESULT_FAILURE,"获取信息失败", new HashMap<>());
		}
	}
    @Override
    public ResultModel<Object> login(User reqUser,String appId) {
    	if (reqUser==null) {
    		logger.error("参数为空");
    		return new ResultModel<Object>(ResultModel.RESULT_FAILURE, "参数为空", new HashMap<>());
		}
//    	String userName = reqUser.getUserName();
    	long phoneNumber = reqUser.getPhoneNumber();
    	String password = reqUser.getPassword();
    	if (phoneNumber <0||(phoneNumber>0&&phoneNumber<10000000000L)) {
    		logger.error("手机号码不正确，phoneNumber:{}",phoneNumber);
            return new ResultModel<Object>(ResultModel.RESULT_FAILURE, "手机号码不正确", new HashMap<>());
        }
    	
    	if (StringUtils.isBlank(password)) {
    		logger.error("密码为空");
    		return new ResultModel<Object>(ResultModel.RESULT_FAILURE, "密码为空", new HashMap<>());
    	}
    	
        try {
//            User user = getUserInfoByCondition(userName, phoneNumber);
            User user = userMapper.getUserInfoByPhoneNumber(phoneNumber);
//            User user = userMapper.getUserInfoByUserName(userName);
            if (user==null) {
            	logger.error("用户不存在");
            	return new ResultModel<Object>(ResultModel.RESULT_FAILURE, "用户不存在", new HashMap<>());
            }
            long nowTime = System.currentTimeMillis();
			if (System.currentTimeMillis() > user.getTokenValidity()) {
				String token = com.gomeplus.im.api.utils.StringUtils.getUuid();
				long tokenExpires = nowTime + Global.TOKEN_EXPIRE_TIME;
				user.setToken(token);
				user.setTokenValidity(tokenExpires);
				userMapper.updateUserInfo(user);
			}
            
            JedisCluster jedis = JedisClusterClient.INSTANCE.getJedisCluster();
            String key = appId + "_"+user.getId()+ Global.TOKEN_SUFFIX;
            jedis.setex(key, Global.TOKEN_EXPIRES_REDIS, user.getToken());
            //ZHTODO 使用和注册用户相同的加密方式，如有修改，请保持一致的加密方法 liuzhenhuan 20160603
            String encodePwd = PasswordEncode.encode(reqUser.getPassword(),"");
            if (!StringUtils.equals(encodePwd, user.getPassword())) {
            	logger.error("密码不正确");
            	return new ResultModel<Object>(ResultModel.RESULT_FAILURE, "密码不正确", new HashMap<>());
			}
            HashMap<String,Object> resultUser = getInfo(user);
            resultUser.put("token", user.getToken());
            resultUser.put("tokenValidity", user.getTokenValidity());
            logger.debug("======================================登录成功==");
            
            return new ResultModel<Object>(ResultModel.RESULT_OK, "登录成功", resultUser);
            
        } catch (Exception e) {
            logger.error("登录失败,出现异常",e);
            return new ResultModel<Object>(ResultModel.RESULT_FAILURE, "登录失败", new HashMap<>());
        }
    }
    
    @Override
    public ResultModel<Object> modifyPwd(ReqFindUser reqFindUser, String appId,long userId) {
    	//1、得到旧密码和新密码
    	String oldPwd=reqFindUser.getOldPwd();
    	String newPwd=reqFindUser.getNewPwd();
    	if (StringUtils.isBlank(oldPwd)) {
    		logger.error("当前密码为空");
            return new ResultModel<Object>(ResultModel.RESULT_FAILURE, "当前密码为空", new HashMap<>());
		}
    	if (StringUtils.isBlank(newPwd)) {
    		logger.error("新密码为空");
    		return new ResultModel<Object>(ResultModel.RESULT_FAILURE, "新密码为空", new HashMap<>());
    	}
    	if (StringUtils.endsWith(oldPwd, newPwd)) {
    		logger.error("新密码与之前密码相同");
    		return new ResultModel<Object>(ResultModel.RESULT_FAILURE, "新密码与之前密码相同", new HashMap<>());
		}
    	//2、根据userId查询出旧密码
    	User user = userMapper.getUserInfoById(userId);
    	if (user==null) {
    		logger.error("没有此用户,userId:{}",userId);
    		return new ResultModel<Object>(ResultModel.RESULT_FAILURE, "不存在此用户", new HashMap<>());
		}
    	String dbPwd = user.getPassword();
    	//3、比较密码是否一致
        String encodeOldPwd = PasswordEncode.encode(oldPwd,"");
    	if (!StringUtils.equals(encodeOldPwd, dbPwd)) {
    		logger.error("当前密码输入有误",userId);
    		return new ResultModel<Object>(ResultModel.RESULT_FAILURE, "当前密码输入有误", new HashMap<>());
		}
    	//4、修改密码
    	try {
    		String encodeNewPwd = PasswordEncode.encode(newPwd,"");
    		user.setPassword(encodeNewPwd);
    		user.setUpdateTime(System.currentTimeMillis());
    		userMapper.updateUserInfo(user);
			logger.info("密码修改成功");
			return new ResultModel<Object>(ResultModel.RESULT_OK, "密码修改成功", new HashMap<>());
		} catch (Exception e) {
			logger.error("密码修改失败,出现异常",e);
			return new ResultModel<Object>(ResultModel.RESULT_FAILURE, "密码修改失败", new HashMap<>());
		}
    }

    private HashMap<String, Object> getInfo(User user) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("userId", user.getId());
        if (StringUtils.isBlank(user.getUserName())) {
        	map.put("userName", StringUtils.EMPTY);
		}else {
			map.put("userName", user.getUserName());
		}
        
        if (StringUtils.isBlank(user.getNickName())) {
        	map.put("nickName", StringUtils.EMPTY);
        }else {
        	map.put("nickName", user.getNickName());
        }
        
        if (StringUtils.isBlank(user.getAvatar())) {
        	map.put("avatar",StringUtils.EMPTY);
        }else {
        	map.put("avatar", user.getAvatar());
        }
        if (StringUtils.isBlank(user.getRegion())) {
        	map.put("region", StringUtils.EMPTY);
			
		}else {
			map.put("region", user.getRegion());
		}
        if (StringUtils.isBlank( user.getAutograph())) {
        	map.put("autograph", StringUtils.EMPTY);
		}else {
			map.put("autograph", user.getAutograph());
		}
        map.put("gender", user.getGender());
        map.put("phoneNumber", user.getPhoneNumber());
        return map;
    }
    
    
    /**
     * ZHTODO v1.0根据用户的id，手机号或者是用户名得到用户信息
     *  之后可能会有邮箱或者是其他的查询条件 liuzhenhuan 20160603 
     * @param user 用户信息
     * @return
     */
    @Deprecated
    private User getUserInfoByCondition(long id,String userName,long phoneNumber){
    	User userInfo = userMapper.getUserInfoById(id);
    	if (userInfo!=null) {
			return userInfo;
		}
    	return getUserInfoByCondition(userName, phoneNumber);
    }
    /**
     * ZHTODO v1.0根据用户的手机号或者是用户名得到用户信息
     *  之后可能会有邮箱或者是其他的查询条件 liuzhenhuan 20160603 
     * @param user 用户信息
     * @return
     */
    private User getUserInfoByCondition(String userName,long phoneNumber){
    	User userInfo =null;
    	userInfo=userMapper.getUserInfoByPhoneNumber(phoneNumber);
    	if (userInfo!=null) {
    		return userInfo;
    	}
    	if (StringUtils.isEmpty(userName)) {
    		return null;
    	}
    	userInfo=userMapper.getUserInfoByUserName(userName);
    	if (userInfo!=null) {
    		return userInfo;
    	}
    	return null;
    }

	

}
