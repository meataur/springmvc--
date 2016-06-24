package com.gomeplus.im.api.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import com.gomeplus.im.api.dao.FriendGroupMapper;
import com.gomeplus.im.api.dao.FriendMapper;
import com.gomeplus.im.api.global.Constant;
import com.gomeplus.im.api.model.FriendGroup;
import com.gomeplus.im.api.request.ReqFriendGroup;
import com.gomeplus.im.api.request.response.ResultModel;
import com.gomeplus.im.api.request.response.RspFriendGroup;
import com.gomeplus.im.api.service.FriendGroupService;

@Service
public class FriendGroupServiceImpl implements FriendGroupService {
    private static final Logger logger = LoggerFactory.getLogger(FriendGroupServiceImpl.class);

    @Autowired
    private FriendGroupMapper friendGroupMapper;
    
    @Autowired
    private FriendMapper friendMapper;

    @Override
    public ResultModel<Object> addFriendGroup(ReqFriendGroup reqFriendGroup,String appId,long userId) {
    	if (reqFriendGroup==null) {
			logger.error("参数为空");
			return  new ResultModel<Object>(ResultModel.RESULT_FAILURE, "参数为空", new HashMap<>());
    	}
    	String groupName = reqFriendGroup.getGroupName();
    	if(StringUtils.isBlank(groupName)){
    		logger.error("groupName为空");
    		return new ResultModel<Object>(ResultModel.RESULT_FAILURE, "groupName为空", new HashMap<>());
    	}
    	
    	FriendGroup friendGroup=new FriendGroup();
    	friendGroup.setUserId(userId);
    	friendGroup.setGroupName(groupName);
    	long nowTime=System.currentTimeMillis();
		friendGroup.setCreateTime(nowTime);
		friendGroup.setUpdateTime(nowTime);
		try {
			int saveRowCount = friendGroupMapper.saveFriendGroup(friendGroup);
			logger.info("保存好友分组的行数为：{}",saveRowCount);
			if (saveRowCount==0) {
				return new ResultModel<Object>(ResultModel.RESULT_FAILURE, "添加好友分组失败", new HashMap<>());
			}
    		logger.info("添加好友分组成功");
    		return new ResultModel<Object>(ResultModel.RESULT_OK, "添加好友分组成功",new HashMap<>());

		} catch (Exception e) {
			logger.error("添加好友分组失败，出现异常",e);
    		return new ResultModel<Object>(ResultModel.RESULT_FAILURE, "添加好友分组失败", new HashMap<>());
		}
    } 
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public ResultModel<Object> deleteFriendGroup(ReqFriendGroup reqFriendGroup,String appId) {
    	if (reqFriendGroup==null) {
			logger.error("参数为空");
			return new ResultModel<Object>(ResultModel.RESULT_FAILURE, "参数为空", new HashMap<>());
    	}
    	long id = reqFriendGroup.getId();
    	if (id<=0) {
    		logger.error("Id不正确");
    		return new ResultModel<Object>(ResultModel.RESULT_FAILURE, "Id不正确", new HashMap<>());
		}
    	
		//更新分组下的所有好友的信息
		HashMap<String, Object> parm=new HashMap<String, Object>();
		
		parm.put("newFriendGroupId", Constant.DEFAULT_FRIEND_GROUP_ID);
		parm.put("oldFriendGroupId", id);
		parm.put("updateTime", System.currentTimeMillis());
		
		int updateFriendRowCount = friendMapper.updateFriendByGroupId(parm);
		logger.info("更新好友记录数,rowCount:{}",updateFriendRowCount);

		int deleteFriendGroupRowCount = friendGroupMapper.deleteFriendGroupById(id);
		logger.info("删除好友分组记录数,rowCount:{}",deleteFriendGroupRowCount);
		if (deleteFriendGroupRowCount==0) {//删除的记录数为0,表示没有该分组的好友
			return new ResultModel<Object>(ResultModel.RESULT_FAILURE, "删除好友分组失败，可能没有id="+id+"的好友分组", new HashMap<>());
		}
		logger.info("删除好友分组成功");
		return new ResultModel<Object>(ResultModel.RESULT_OK, "删除好友分组成功", new HashMap<>());
    }
    @Override
    public ResultModel<Object> getFriendGroup(String appId,long userId) {
    	try {
			List<FriendGroup> friendGroupList = friendGroupMapper.getFriendGroupByUserId(userId);
			if (CollectionUtils.isEmpty(friendGroupList)) {
				logger.error("此人目前没有创建任何好友分组，userId:{}",userId);
				return new ResultModel<Object>(ResultModel.RESULT_FAILURE, "此人目前没有创建任何好友分组", new HashMap<>());
			}
    		logger.info("查找好友分组成功");
    		return new ResultModel<Object>(ResultModel.RESULT_OK, "查找好友分组成功",convertRspFriendGroupList(friendGroupList));
		} catch (Exception e) {
			logger.error("查找好友分组失败，出现异常",e);
			return new ResultModel<Object>(ResultModel.RESULT_FAILURE, "查找好友分组失败", new HashMap<>());
		}
    }
    @Override
    public ResultModel<Object> updateFriendGroup(ReqFriendGroup reqFriendGroup,String appId) {
    	if (reqFriendGroup==null) {
			logger.error("参数为空");
			return new ResultModel<Object>(ResultModel.RESULT_FAILURE, "参数为空", new HashMap<>());
    	}
    	long id = reqFriendGroup.getId();
    	if (id<=0) {
    		logger.error("Id不正确");
    		return new ResultModel<Object>(ResultModel.RESULT_FAILURE, "Id不正确", new HashMap<>());
		}
    	String groupName = reqFriendGroup.getGroupName();
    	if (StringUtils.isBlank(groupName)) {
    		logger.error("groupName为空");
    		return new ResultModel<Object>(ResultModel.RESULT_FAILURE, "groupName为空", new HashMap<>());
		}
    	try {
    		//ZHTODO 在update前先查一遍数据库，update时会再查找一边数据库，这样会多一遍查询，
    		//由于判断id的记录是否存在，需要给出id不存在的提示，需要先查数据库，暂时先这么做 ，如果影响到性能，
    		//会牺牲这个提示，即直接更新数据库，之前不在查询。update的返回值是跟新记录数，使用这个可以判断是否更新成功，
    		//但是不能肯定是id不存在的情况，相比之下，暂时先这么做。                     liuzhenhuan 20160607
    		FriendGroup friendGroup = friendGroupMapper.getFriendGroupById(id);
    		if(friendGroup==null){
    			logger.error("id不存在，id: {}",id);
    			return new ResultModel<Object>(ResultModel.RESULT_FAILURE, "id不存在", new HashMap<>());
    		}
			friendGroup.setGroupName(reqFriendGroup.getGroupName());
			friendGroup.setUpdateTime(System.currentTimeMillis());
			
			int updateRowCount = friendGroupMapper.updateFriendGroupById(friendGroup);
			logger.info("更新好友分组记录数：{}",updateRowCount);
			if (updateRowCount==0) {
				return new ResultModel<Object>(ResultModel.RESULT_FAILURE, "修改好友分组失败，id="+id+"用户不存在", new HashMap<>());
			}
    		logger.info("修改好友分组成功");
    		return new ResultModel<Object>(ResultModel.RESULT_OK, "修改好友分组成功",  new HashMap<>());

		} catch (Exception e) {
			logger.error("修改好友分组失败，出现异常",e);
    		return new ResultModel<Object>(ResultModel.RESULT_FAILURE, "修改好友分组失败", new HashMap<>());
		}
    }
    /**
     * 转化为返回的好友分组信息 
     * @param friendGroup 数据库中的好友分组信息
     * @return
     */
    private RspFriendGroup convertRspFriendGroup(FriendGroup friendGroup){
    	RspFriendGroup rspFriendGroup=new RspFriendGroup();
    	rspFriendGroup.setFriendGroupId(friendGroup.getId());
    	rspFriendGroup.setGroupName(friendGroup.getGroupName());
    	return rspFriendGroup;
    	
    }
    /**
     * 转化为返回的好友分组集合信息
     * @param friendGroupList 数据库中的好友分组信息集合
     * @return
     */
    private List<RspFriendGroup> convertRspFriendGroupList(List<FriendGroup> friendGroupList){
    	List<RspFriendGroup> rspFriendGroupList=new ArrayList<RspFriendGroup>();
    	for (FriendGroup friendGroup : friendGroupList) {
			rspFriendGroupList.add(convertRspFriendGroup(friendGroup));
		}
    	return rspFriendGroupList;
    }

}
