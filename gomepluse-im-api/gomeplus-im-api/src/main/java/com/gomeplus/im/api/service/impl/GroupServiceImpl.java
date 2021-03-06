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
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import com.gomeplus.im.api.dao.UserMapper;
import com.gomeplus.im.api.global.Constant;
import com.gomeplus.im.api.message.ApplyJoinGroupMsg;
import com.gomeplus.im.api.message.DisbandGroupMsg;
import com.gomeplus.im.api.message.EditGroupMsg;
import com.gomeplus.im.api.message.InvitedJoinGroupMsg;
import com.gomeplus.im.api.message.NoticeApplicantMsg;
import com.gomeplus.im.api.message.NoticeManagerMsg;
import com.gomeplus.im.api.message.QuitGroupMsg;
import com.gomeplus.im.api.model.Group;
import com.gomeplus.im.api.model.GroupMember;
import com.gomeplus.im.api.model.GroupMemberMark;
import com.gomeplus.im.api.model.GroupQuitMember;
import com.gomeplus.im.api.model.User;
import com.gomeplus.im.api.mongo.GroupDao;
import com.gomeplus.im.api.mongo.GroupMemberDao;
import com.gomeplus.im.api.mongo.GroupMemberMarkDao;
import com.gomeplus.im.api.mongo.GroupQuitMemberDao;
import com.gomeplus.im.api.request.ReqGroup;
import com.gomeplus.im.api.request.ReqGroupMember;
import com.gomeplus.im.api.request.response.ResultModel;
import com.gomeplus.im.api.request.response.RspGroup;
import com.gomeplus.im.api.request.response.RspGroupMember;
import com.gomeplus.im.api.request.response.RspMemberMark;
import com.gomeplus.im.api.service.GroupService;
import com.gomeplus.im.api.threadPool.ThreadPool;
import com.gomeplus.im.api.utils.JedisClusterClient;

@Service
public class GroupServiceImpl implements GroupService {

    private static final Logger logger = LoggerFactory.getLogger(GroupServiceImpl.class);

    private GroupDao groupDao = new GroupDao();

    private GroupMemberDao groupMemberDao = new GroupMemberDao();

    private GroupMemberMarkDao groupMemberMarkDao = new GroupMemberMarkDao();

    private GroupQuitMemberDao groupQuitMemberDao = new GroupQuitMemberDao();

    @Autowired
    private UserMapper userMapper;

   
    /**
     * 创建群组
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public ResultModel<Object> addGroup(ReqGroup reqGroup,String appId,long userId) {
    	if (reqGroup==null) {
			logger.error("参数为空");
			return new ResultModel<Object>(ResultModel.RESULT_FAILURE, "参数为空", new HashMap<>());
		}
        String groupId = com.gomeplus.im.api.utils.StringUtils.getUuid();
        //1、生成群组信息和群成员信息
        Group group = generateGroup(reqGroup, groupId,userId);
        List<GroupMember> groupMemberList = generateGroupMemberList(reqGroup, groupId,userId);
        List<GroupMemberMark> groupMemberMarkList = generateGroupMemberMarkList(reqGroup, groupId, userId);
        
        //2、保存到mysql或者mongodb中
        //groupMapper.saveGroup(group);
        //groupMemberMapper.saveGroupMemberBatch(groupMemberList);
        groupDao.save(appId, group);
        if (!CollectionUtils.isEmpty(groupMemberList)) {
        	groupMemberDao.save(appId, groupMemberList);
        	if (!CollectionUtils.isEmpty(groupMemberMarkList)) {
        		groupMemberMarkDao.save(appId, groupMemberMarkList);
			}
		}
        Map<String, Object> resultMap=new HashMap<String, Object>();
        resultMap.put("groupId", groupId);
        resultMap.put("userId", userId);
        ResultModel<Object> resultModel = new ResultModel<Object>(ResultModel.RESULT_OK, "群组创建成功", resultMap);
        return resultModel;
    }
    
   
    /**
     * 根据参数请求生成群组成员List
     * @param reqGroup
     * @param groupId
     * @return
     */
    private List<GroupMember> generateGroupMemberList(ReqGroup reqGroup,String groupId,long userId){
    	GroupMember createMemeber = generateCreateMemeber(reqGroup, groupId,userId);//创建者即为群组中的一员
    	List<ReqGroupMember> memberList=reqGroup.getMembers();
    	List<GroupMember> groupMemberList=new ArrayList<GroupMember>();
    	groupMemberList.add(createMemeber);
    	if (CollectionUtils.isEmpty(memberList)) {
			return groupMemberList;
		}
    	//保存群成员
    	for (ReqGroupMember reqRember : memberList) {
    		GroupMember groupMember = new GroupMember();
    		long memberUserId = reqRember.getUserId();
    		groupMember.setUserId(memberUserId);
    		groupMember.setNickName(reqRember.getNickName());
    		groupMember.setGroupId(groupId);
    		if (StringUtils.isNotBlank(groupId)) {
    			groupMember.setGroupIdHash(groupId.hashCode());
    		}
    		groupMember.setIdentity(reqRember.getIdentity());
    		groupMember.setIsTop(Constant.GROUP_MEMBER_TOP.E_MEMBER_TOP_NO.value);//不置顶
    		groupMember.setIsShield(Constant.GROUP_MEMBER_SHIELD.E_MEMBER_SHIELD_NO.value); //不屏蔽群消息
    		groupMember.setStatus(Constant.GROUP_MEMBER_STATUS.E_MEMBER_STATUS_YES.value); //是审核通过的System.currentTimeMillis()
    		long nowTime = System.currentTimeMillis();
    		groupMember.setJoinTime(nowTime);
    		groupMember.setUpdateTime(nowTime);
    		//groupMember.setInitSeq(0);//初始化 0
    		//groupMember.setReadSeq(0);//初始化 0
    		groupMemberList.add(groupMember);
    	}
    	return groupMemberList;
    	
    }
    /**
     * 根据参数请求生成群组成员List
     * @param reqGroup
     * @param groupId
     * @return
     */
    private List<GroupMemberMark> generateGroupMemberMarkList(ReqGroup reqGroup,String groupId,long userId){
    	List<ReqGroupMember> reqGroupMemberList = reqGroup.getMembers();
    	if (CollectionUtils.isEmpty(reqGroupMemberList)) {
			return new ArrayList<GroupMemberMark>();
		}
    	List<GroupMemberMark> groupMemberList=new ArrayList<GroupMemberMark>();
    	for (ReqGroupMember reqGroupMember : reqGroupMemberList) {
    		GroupMemberMark groupMemberMark=new GroupMemberMark();
    		groupMemberMark.setUserId(userId);
    		groupMemberMark.setMarkedUserId(reqGroupMember.getUserId());
    		groupMemberMark.setMark(reqGroupMember.getMark());
    		groupMemberMark.setGroupId(groupId);
    		groupMemberMark.setGroupIdHash(groupId.hashCode());
    		groupMemberMark.setCreateTime(System.currentTimeMillis());
    		groupMemberList.add(groupMemberMark);
    	}
    	return groupMemberList;
    	
    }
    
    /** 根据请求参数生成group
     * @param reqGroup 
     * @param groupId
     * @return
     */
    private Group generateGroup(ReqGroup reqGroup,String groupId,long userId){
        String groupName = reqGroup.getGroupName();
        String desc = reqGroup.getDesc();
        String avatar = reqGroup.getAvatar();
        int capacity = reqGroup.getCapacity();
        if (capacity <= 0) {//暂时默认200
            capacity = Constant.GROUP_CAPACITY;
        }
        int isAudit = reqGroup.getIsAudit();
        List<ReqGroupMember> members = reqGroup.getMembers();
        if (CollectionUtils.isEmpty(members)) {
            members = new ArrayList<ReqGroupMember>();
        }
        List<GroupMember> groupMemberList = new ArrayList<GroupMember>();
        long time = System.currentTimeMillis();
    	//保存群组信息
        Group group = new Group();
        group.setGroupId(groupId);
        if (StringUtils.isNotBlank(groupId)) {
			group.setGroupIdHash(groupId.hashCode());
		}
        group.setUserId(userId);
        group.setCapacity(capacity);
        group.setIsAudit(isAudit);
        group.setIsDele(Constant.GROUP_DEL.E_GROUP_DEL_NOT.value);
        StringBuilder builder = new StringBuilder();
        if (StringUtils.isBlank(groupName)) {
            for (int i = 0; i < groupMemberList.size(); i++) {
                GroupMember member = groupMemberList.get(i);
                if (i != 1) {
                    String nickName = member.getNickName();
                    if (builder.length() > 0) {
                        builder.append("、");
                        builder.append(nickName);
                    } else {
                        builder.append(nickName);
                    }
                }
            }
            if (builder.length()<=60) {
            	groupName=builder.toString();
			}else {
				groupName = builder.substring(0, 60) + "......";
			}
        }
        group.setGroupDesc(desc);
        group.setType(Constant.CHAT_TYPE.E_CHAT_TYP_GROUP.value);
        group.setGroupName(groupName);
        group.setqRcode(reqGroup.getqRCode());
        group.setAvatar(avatar);
        group.setCreateTime(time);
        group.setUpdateTime(time);
        return group;
    }
    /**
     * 根据请求参数得到创建群的member
     * @param reqGroup 请求群组
     * @param groupId 
     * @return
     */
    private GroupMember generateCreateMemeber(ReqGroup reqGroup,String groupId,long userId){
		String nikeName = reqGroup.getNickName();//自己在群里的昵称
        List<ReqGroupMember> members = reqGroup.getMembers();
        if (CollectionUtils.isEmpty(members)) {
            members = new ArrayList<ReqGroupMember>();
        }
        long time = System.currentTimeMillis();
        GroupMember owner = new GroupMember();
		owner.setUserId(userId);
        owner.setNickName(nikeName);
        owner.setGroupId(groupId);
        if (StringUtils.isNotBlank(groupId)) {
        	owner.setGroupIdHash(groupId.hashCode());
		}
        owner.setIdentity(Constant.GROUP_MEMBER_IDENTITY.E_MEMBER_CREATOR.value);//创建者
        owner.setIsTop(Constant.GROUP_MEMBER_TOP.E_MEMBER_TOP_NO.value);//不置顶
        owner.setIsShield(Constant.GROUP_MEMBER_SHIELD.E_MEMBER_SHIELD_NO.value); //不屏蔽群消息
        owner.setStatus(Constant.GROUP_MEMBER_STATUS.E_MEMBER_STATUS_YES.value);
        owner.setJoinTime(time);
        owner.setUpdateTime(time);
        //owner.setInitSeq(0);   //初始化 0
        //owner.setReadSeq(0);   //初始化 0
    	return owner;
    }

    /**
     * 用户主动加入群
     */
    @Override
    public ResultModel<Object> joinGroup(ReqGroupMember reqGroupMember,String appId,long userId) {
        try {
            String groupId = reqGroupMember.getGroupId();
            long time = System.currentTimeMillis();
            Group group = groupDao.getGroup(appId, groupId);
            if (group == null) {
            	logger.error("groupId不正确，没有此群");
                return new ResultModel<Object>(ResultModel.RESULT_FAILURE, "groupId不正确,没有此群", new HashMap<>());
            }
            String nickName = reqGroupMember.getNickName();
            if (StringUtils.isBlank(nickName)) {
				nickName=JedisClusterClient.getNowNickName(userId, nickName);
			}
            if (StringUtils.isBlank(nickName)) {
            	User user = userMapper.getUserInfoById(userId);
            	nickName=user.getNickName();
			}
            if (StringUtils.isBlank(nickName)) {
				logger.error("昵称为空");
				return new ResultModel<Object>(ResultModel.RESULT_FAILURE,"昵称为空", new HashMap<>());
			}
            int isAudit = group.getIsAudit();
            int status = Constant.GROUP_STATUS.E_GROUP_STATUS_NOT.value;
            boolean isSave = false;
            GroupMember member = groupMemberDao.getGroupMemberByUid(appId, groupId,userId);
            if (member == null) {
                isSave = true;
                //不需要审核
                if (isAudit == Constant.GROUP_AUDIT.E_GROUP_AUDIT_NOT.value) {
                    status = Constant.GROUP_STATUS.E_GROUP_STATUS_OK.value;
                }
            } else {
                int oldStatus = member.getStatus();
                //等待审核或者已经是成员
                if (oldStatus == Constant.GROUP_STATUS.E_GROUP_STATUS_NOT.value) {
                    return new ResultModel<Object>(ResultModel.RESULT_OK, "等待管理员审核", new HashMap<>());
                } else if (oldStatus == Constant.GROUP_STATUS.E_GROUP_STATUS_OK.value) {
                    return new ResultModel<Object>(ResultModel.RESULT_OK, "已经加入该群", new HashMap<>());
                }
            }
            if (isSave) {
                member = new GroupMember();
                member.setGroupId(groupId);
                if (StringUtils.isNotBlank(groupId)) {
                	member.setGroupIdHash(groupId.hashCode());
				}
                member.setUserId(userId);
                member.setNickName(nickName);
                member.setStatus(status);
                member.setJoinTime(time);
                member.setUpdateTime(time);
                member.setIdentity(Constant.GROUP_MEMBER_IDENTITY.E_MEMBER_ORDINARY.value);//默认为普通成员
                member.setIsTop(Constant.GROUP_MEMBER_TOP.E_MEMBER_TOP_NO.value);//不置顶
                member.setIsShield(Constant.GROUP_MEMBER_SHIELD.E_MEMBER_SHIELD_NO.value);//不屏蔽群消息
                //member.setInitSeq(0);
                //member.setReadSeq(0);
                groupQuitMemberDao.delQuitMember(appId,groupId,userId);
                groupMemberDao.save(appId, member);
            }
            if (isAudit == Constant.GROUP_AUDIT.E_GROUP_AUDIT_NEED.value) {
                //需要审核;通知管理员审核;保存系统消息
                //  TODO    发送个人消息，通知群创建者
    			ThreadPool pool = ThreadPool.getInstance();
    			
    			NoticeManagerMsg msg = new NoticeManagerMsg();
    			
    			msg.setGroupId(groupId);
    			msg.setApplicantId(member.getUserId());
    			msg.setApplicantName(nickName);
    			msg.setContent(nickName +"申请加入群");
    			msg.setOptTime(System.currentTimeMillis());
    			
    			msg.setAppId(Integer.valueOf(appId));
    			msg.setExtra(Constant.STRING_NULL);
    			
    			pool.addTask(msg);
            	
                return new ResultModel<Object>(ResultModel.RESULT_OK, "等待管理员审核", new HashMap<>());
            } else {
                //不需要审核全群通知;存离线消息
                //  TODO    发送群组消息，通知全群
    			ThreadPool pool = ThreadPool.getInstance();
    			
    			ApplyJoinGroupMsg msg = new ApplyJoinGroupMsg();
    			
    			msg.setGroupId(groupId);
    			msg.setApplicantId(member.getUserId());
    			msg.setApplicantName(nickName);
    			msg.setContent(nickName +"加入群");
    			msg.setOptTime(System.currentTimeMillis());
    			
    			msg.setAppId(Integer.valueOf(appId));
    			msg.setExtra(Constant.STRING_NULL);
    			
    			pool.addTask(msg);
            	
            }
            return new ResultModel<Object>(ResultModel.RESULT_OK, "加入成功", new HashMap<>());
        } catch (Exception e) {
            logger.error("加入群失败，出现异常",e);
            return new ResultModel<Object>(ResultModel.RESULT_FAILURE, "加入群失败", new HashMap<>());
        }
    }

    /**
     * 邀请加入群
     */
    @Override
    public ResultModel<Object> inviteGroup(ReqGroup reqGroup,String appId,long userId) {
        try {
            List<ReqGroupMember> members = reqGroup.getMembers();
            if (CollectionUtils.isEmpty(members)) {
            	logger.error("邀请加入群人员为空");
                return new ResultModel<Object>(ResultModel.RESULT_FAILURE, "邀请加入群人员为空", new HashMap<>());
            }
            String groupId = reqGroup.getGroupId();
            Group group = groupDao.getGroup(appId,groupId);
            if (group == null) {
            	logger.error("该群不存在");
            	return new ResultModel<Object>(ResultModel.RESULT_FAILURE, "该群不存在", new HashMap<>());
            }
            GroupMember member = groupMemberDao.getGroupMemberByUid(appId, groupId, userId);
            if (member == null) {
            	logger.error("该用户不在此群中");
            	return new ResultModel<Object>(ResultModel.RESULT_FAILURE, "该用户不在此群中", new HashMap<>());
            }
            long time = System.currentTimeMillis();
            String userNickName=reqGroup.getNickName();
    		if (StringUtils.isBlank(userNickName)) {
				userNickName=JedisClusterClient.getNowNickName(userId, userNickName);
			}
    		if (StringUtils.isBlank(userNickName)) {
    			userNickName=member.getNickName();
    		}
    		if (StringUtils.isBlank(userNickName)) {
				userNickName=userMapper.getUserInfoById(userId).getNickName();
			}
    		
    		if (StringUtils.isBlank(userNickName)) {
				logger.error("昵称为空");
				return new ResultModel<Object>(ResultModel.RESULT_FAILURE,"昵称为空", new HashMap<>());
			}
            int isAudit = group.getIsAudit();
            List<GroupMember> groupMemberList = new ArrayList<GroupMember>();
            List<Long> memberUserIds = new ArrayList<Long>();
            
            List<Long> memberUserIds2=new ArrayList<Long>();
            for (ReqGroupMember member2 : members) {
				memberUserIds2.add(member2.getUserId());
			}
            List<User> dbUserList = userMapper.findUsersByIds(memberUserIds2);
            if (CollectionUtils.isEmpty(dbUserList)) {
            	logger.error("邀请的人都不存在");
            	return new ResultModel<Object>(ResultModel.RESULT_FAILURE,"邀请的人都不存在", new HashMap<>());
            }
            
            
            if (isAudit == Constant.GROUP_AUDIT.E_GROUP_AUDIT_NOT.value || group.getUserId() == userId) {

            	//不需要审核;管理员邀请加入
                int status = Constant.GROUP_STATUS.E_GROUP_STATUS_NOT.value;
                for (User user : dbUserList) {
                	GroupMember oldMember = groupMemberDao.getGroupMemberByUid(appId, groupId, user.getId());
                	if (oldMember!=null) {
						continue;
					}
                	GroupMember groupMember = new GroupMember();
                	long memberUserId = user.getId();
                	groupMember.setUserId(memberUserId);
                	memberUserIds.add(memberUserId);
                	groupMember.setNickName(user.getNickName());
                	groupMember.setGroupId(groupId);
                	groupMember.setGroupIdHash(groupId.hashCode());
                	groupMember.setIsTop(Constant.GROUP_MEMBER_TOP.E_MEMBER_TOP_NO.value);//不置顶
                	groupMember.setIsShield(Constant.GROUP_MEMBER_SHIELD.E_MEMBER_SHIELD_NO.value); //不屏蔽群消息
                	groupMember.setIdentity(Constant.GROUP_MEMBER_IDENTITY.E_MEMBER_ORDINARY.value);//默认为普通成员
                	groupMember.setStatus(status);
                	groupMember.setJoinTime(time);
                	groupMember.setUpdateTime(time);
                	groupMemberList.add(groupMember);
                }
                groupQuitMemberDao.delQuitMembers(appId, groupId, memberUserIds);
                groupMemberDao.save(appId,groupMemberList);
                ThreadPool pool = ThreadPool.getInstance();
                String name ="";
                for (GroupMember m : groupMemberList) {
                	name += JedisClusterClient.getNowNickName(m.getUserId(), m.getNickName())+"、";
                }
                
                name = name.substring(0, name.lastIndexOf("、"));
                NoticeApplicantMsg msg = new NoticeApplicantMsg();
                String message = userNickName + "邀请" + name + "加入群聊";
                msg.setGroupId(groupId);
                msg.setFromUid(userId);
                msg.setFromName(userNickName);
                msg.setContent(message);
                msg.setOptTime(System.currentTimeMillis());
                
                msg.setAppId(Integer.valueOf(appId));
                msg.setExtra(Constant.STRING_NULL);
                
                pool.addTask(msg);
                /* for (GroupMember m : groupMemberList) {
                    String name = JedisClusterClient.getNowNickName(m.getUserId(), m.getNickName());
                    String message = nickName + "邀请" + name + "加入群聊等待您的审核";
                    //  TODO    发送个人消息，通知群创建者
        			
        			NoticeManagerMsg msg = new NoticeManagerMsg();
        			
        			msg.setGroupId(groupId);
        			msg.setApplicantId(m.getUserId());
        			msg.setApplicantName(nickName);
        			msg.setContent(message);
        			msg.setOptTime(System.currentTimeMillis());
        			
        			msg.setAppId(Integer.valueOf(appId));
        			msg.setExtra(Constant.STRING_NULL);
        			
        			pool.addTask(msg);
                    
                }*/
                logger.info("邀请好友加入群聊成功");
                return new ResultModel<Object>(ResultModel.RESULT_OK, "邀请成功", new HashMap<>());
            }
            else {//需要审核
                int status = Constant.GROUP_STATUS.E_GROUP_STATUS_OK.value;
                for (User user : dbUserList) {
                	//TODO 循环中查询数据库，不是很好 ,liuzhenhuan 20160616
                	GroupMember oldMember = groupMemberDao.getGroupMemberByUid(appId, groupId,user.getId());
                	if (oldMember != null) {
                		continue;
                	}
                    GroupMember groupMember = new GroupMember();
                    long memberUserId = user.getId();
                    memberUserIds.add(memberUserId);
                    groupMember.setUserId(memberUserId);
                    groupMember.setNickName(user.getNickName());
                    groupMember.setGroupId(groupId);
                    groupMember.setGroupIdHash(groupId.hashCode());
                    groupMember.setIsTop(Constant.GROUP_MEMBER_TOP.E_MEMBER_TOP_NO.value);//不置顶
                    groupMember.setIsShield(Constant.GROUP_MEMBER_SHIELD.E_MEMBER_SHIELD_NO.value); //不屏蔽群消息
                    groupMember.setIdentity(Constant.GROUP_MEMBER_IDENTITY.E_MEMBER_ORDINARY.value);//默认为普通成员
                    groupMember.setStatus(status);
                    groupMember.setJoinTime(time);
                    groupMember.setUpdateTime(time);
                    groupMemberList.add(groupMember);
                }
                groupQuitMemberDao.delQuitMembers(appId, groupId, memberUserIds);
                groupMemberDao.save(appId,groupMemberList);
                logger.info("members size=[{}]", members.size());
                if (!CollectionUtils.isEmpty(groupMemberList)) {
                	//  TODO    发送群组消息，通知全群
                	List<Long> invitedUidList=new ArrayList<Long>();
                	List<String> invitedNameList=new ArrayList<String>();
                	
                	for (GroupMember member2 : groupMemberList) {
                		invitedUidList.add(member2.getUserId());
                		invitedNameList.add(JedisClusterClient.getNowNickName(member2.getUserId(), member2.getNickName()));
                	}
                	ThreadPool pool = ThreadPool.getInstance();
                	InvitedJoinGroupMsg msg=new InvitedJoinGroupMsg();
                	msg.setAppId(Integer.valueOf(appId));
                	msg.setExtra(Constant.STRING_NULL);
                	msg.setFromName(userNickName);
                	msg.setFromUid(userId);
                	msg.setGroupId(groupId);
                	
                	msg.setOptTime(System.currentTimeMillis());
					msg.setInvitedUids(invitedUidList);
					msg.setInvitedNames(invitedNameList);
					msg.setContent("加入"+group.getGroupName());
                	pool.addTask(msg);
				}
                return new ResultModel<Object>(ResultModel.RESULT_OK, "邀请成功", new HashMap<>());
            }
        } catch (Exception e) {
           logger.error("邀请好友加入群失败",e);
           return  new ResultModel<Object>(ResultModel.RESULT_FAILURE, "邀请失败", new HashMap<>());
        }
    }

    /**
     * 退出群
     */
    @Override
    public ResultModel<Object> quitGroup(ReqGroup reqGroup,String appId,long userId) {
//        ResultModel<Object> resultModel = new ResultModel<Object>(ResultModel.RESULT_FAILURE, "退出群失败", new HashMap<>());
        try {
            String groupId = reqGroup.getGroupId();
            Group group = groupDao.getGroup(appId,groupId);
            if (group == null) {
                return new ResultModel<Object>(ResultModel.RESULT_FAILURE, "参数错误", new HashMap<>());
            }
            if (userId == group.getUserId()) {
                //群主退出时，调用解散群接口
                return disbandGroup(reqGroup,appId,userId);
            }
            GroupMember member = groupMemberDao.getGroupMemberByUid(appId,groupId,userId);
            String nickName=reqGroup.getNickName();
            if (StringUtils.isBlank(nickName)) {
				nickName=JedisClusterClient.getNowNickName(userId, nickName);
			}
            if (StringUtils.isBlank(nickName)) {
            	nickName=member.getNickName();
            }
            
            if (StringUtils.isBlank(nickName)) {
				logger.error("昵称为空");
				return new ResultModel<Object>(ResultModel.RESULT_FAILURE,"昵称为空", new HashMap<>());
			}
            
            if (member == null) {
            	logger.error("此人不是群成员");
                return new ResultModel<Object>(ResultModel.RESULT_FAILURE, "此人不是群成员", new HashMap<>());
            }
            //删除成员
            groupMemberDao.delGroupMember(appId,groupId,userId);
            //删除对该群组成员的备注
            groupMemberMarkDao.delMemberMark(appId,groupId,userId);
            //保存退出的成员
            GroupQuitMember quitMember = new GroupQuitMember(userId, groupId);
            groupQuitMemberDao.save(appId,quitMember);

            //消息全群广播
            //  TODO    群组内广播通知群组成员退出
			ThreadPool pool = ThreadPool.getInstance();
			
			QuitGroupMsg msg = new QuitGroupMsg();
			List<Long> kickUidList=new ArrayList<Long>();
			kickUidList.add(userId);
			List<String> kickedNameList=new ArrayList<String>();
			kickedNameList.add(nickName);

			msg.setGroupId(groupId);
			msg.setOptTime(System.currentTimeMillis());
			msg.setAppId(Integer.valueOf(appId));
			msg.setExtra(Constant.STRING_NULL);
			msg.setFromName(nickName);
			
			msg.setContent(nickName+"退出群");
			msg.setFromUid(userId);
			msg.setQuitType(Constant.GROUP_MEMEBER_QUIR_TYPE.QUIT.value);
			msg.setKickedUids(kickUidList);
			msg.setKickedNames(kickedNameList);

			pool.addTask(msg);
            logger.info("退出群成功,userId:{}",userId);
            
            return new ResultModel<Object>(ResultModel.RESULT_OK, "退出群成功", new HashMap<>());
        } catch (Exception e) {
        	logger.error("退出群失败，出现异常",e);
        	return new ResultModel<Object>(ResultModel.RESULT_FAILURE, "退出群失败", new HashMap<>());
        }
    }

    /**
     * 踢人
     */
    @Override
    public ResultModel<Object> kickGroup(ReqGroup reqGroup,String appId,long userId) {
        try {
            String groupId = reqGroup.getGroupId();
            List<ReqGroupMember> members = reqGroup.getMembers();
            if (CollectionUtils.isEmpty(members)) {
            	logger.error("踢人为空");
                return new ResultModel<Object>(ResultModel.RESULT_FAILURE, "踢人为空", new HashMap<>());
            }
            Group group = groupDao.getGroup(appId,groupId);
            if (group == null) {
            	logger.error("此群不存在,groupId:{}",groupId);
                return new ResultModel<Object>(ResultModel.RESULT_FAILURE, "此群不存在", new HashMap<>());
            }
            GroupMember creater = groupMemberDao.getGroupMemberByUid(appId,groupId,userId);
            
            if (creater == null) {
            	logger.error("用户不是群成员,groupId:{},userId:{}",groupId,userId);
                return new ResultModel<Object>(ResultModel.RESULT_FAILURE, "用户不是群成员", new HashMap<>());
            }
            if (group.getUserId() != creater.getUserId()) {
            	logger.error("用户不是群主，无权踢人,groupId:{},userId:{}",groupId,userId);
                return new ResultModel<Object>(ResultModel.RESULT_FAILURE, "用户不是群主，无权踢人", new HashMap<>());
            }
            String nickName=reqGroup.getNickName();
            if (StringUtils.isBlank(nickName)) {
				nickName=JedisClusterClient.getNowNickName(userId, nickName);
			}
            if (StringUtils.isBlank(nickName)) {
				nickName=creater.getNickName();
			}
            
            if (StringUtils.isBlank(nickName)) {
            	nickName=userMapper.getUserInfoById(userId).getNickName();
            }
            
            if (StringUtils.isBlank(nickName)) {
				logger.error("昵称为空");
				return new ResultModel<Object>(ResultModel.RESULT_FAILURE,"昵称为空", new HashMap<>());
			}

            //删除群组成员,以及群组内对成员的备注
            List<GroupQuitMember> groupQuitMembers = new ArrayList<>();
            List<Long> groupMemberUserIds = new ArrayList<>();
            for (ReqGroupMember member : members) {
                long memberUserId = member.getUserId();
                if (memberUserId==userId) {
                	logger.info("群主不能踢自己，userId:{},groupId:{}",userId,groupId);
					continue;
				}
                
                groupMemberUserIds.add(memberUserId);
                GroupQuitMember quitMember = new GroupQuitMember(memberUserId, groupId);
                groupQuitMembers.add(quitMember);
                groupMemberMarkDao.delMemberMark(appId, groupId, memberUserId);
            }
          
            logger.info("members size=[{}]", members.size());
            if (!CollectionUtils.isEmpty(members)) {
                ThreadPool pool = ThreadPool.getInstance();
                //  TODO    发送群组消息通知
                List<Long> imUserIdList = new ArrayList<Long>();
                List<String> imUserNameList=new ArrayList<String>();
                String content = "";
                for (ReqGroupMember m : members) {
                    imUserIdList.add(m.getUserId());
                    String nickName2 = m.getNickName();
                    if (StringUtils.isBlank(nickName2)) {
                    	nickName2=JedisClusterClient.getNowNickName(m.getUserId(), nickName2);
        			}
                    if (StringUtils.isBlank(nickName2)) {
						GroupMember groupMember = groupMemberDao.getGroupMemberByUid(appId, groupId, m.getUserId());
						if (groupMember==null) {
							continue;
						}
						nickName2=groupMember.getNickName();
					}
                    if (StringUtils.isBlank(nickName2)) {
                    	User user = userMapper.getUserInfoById(m.getUserId());
                    	nickName2=user.getNickName();
                    }
                    imUserNameList.add(nickName2);
                    content += nickName2 + "、";
                }
    			content=content.substring(0,content.lastIndexOf("、"));
    			QuitGroupMsg msg = new QuitGroupMsg();
    			msg.setGroupId(groupId);
    			msg.setOptTime(System.currentTimeMillis());
    			msg.setAppId(Integer.valueOf(appId));
    			msg.setExtra(Constant.STRING_NULL);
    			msg.setFromName(nickName);
    			
    			msg.setContent(content + "被移除群");
    			msg.setFromUid(userId);
    			msg.setQuitType(Constant.GROUP_MEMEBER_QUIR_TYPE.KICK.value);
    			msg.setKickedUids(imUserIdList);
    			msg.setKickedNames(imUserNameList);
    			  //删除群组成员
                groupMemberDao.delGroupMembers(appId,groupId,groupMemberUserIds);
                //保存删除的群组成员
                groupQuitMemberDao.saveGroupQuitMembers(appId, groupQuitMembers);
    			
    			pool.addTask(msg);
    			
                
            }
            return new ResultModel<Object>(ResultModel.RESULT_OK, "踢人成功", new HashMap<>());
        } catch (Exception e) {
            logger.error("踢人失败，出现异常",e);
            return new ResultModel<Object>(ResultModel.RESULT_FAILURE, "踢人失败",  new HashMap<>());
        }
    }

    /**
     * 修改群信息
     */
    @Override
    public ResultModel<Object> editGroup(ReqGroup reqGroup,String appId,long userId) {
        try {
            String groupId = reqGroup.getGroupId();
            Group group = groupDao.getGroup(appId,groupId);
            if (group == null) {
            	logger.error("此群不存在,groupId:{}",groupId);
                return new ResultModel<Object>(ResultModel.RESULT_FAILURE, "此群不存在", new HashMap<>());
            }
            GroupMember user = groupMemberDao.getGroupMemberByUid(appId,groupId,userId);
            if (user == null) {
            	logger.error("用户不是群成员,userId:{},groupId:{}",userId,groupId);
                return new ResultModel<Object>(ResultModel.RESULT_FAILURE, "用户不是群成员", new HashMap<>());
            }
            if (userId != group.getUserId()) {
            	logger.error("不是群主，无权限修改群信息,userId:{},groupId:{}",userId,groupId);
                return new ResultModel<Object>(ResultModel.RESULT_FAILURE, "不是群主，无权限修改群信息", new HashMap<>());
            }
            String nickName=reqGroup.getNickName();
            if (StringUtils.isBlank(nickName)) {
				nickName=JedisClusterClient.getNowNickName(userId, nickName);
			}
            if (StringUtils.isBlank(nickName)) {
				nickName=user.getNickName();
			}
            if (StringUtils.isBlank(nickName)) {
				logger.error("昵称为空");
				return new ResultModel<Object>(ResultModel.RESULT_FAILURE,"昵称为空", new HashMap<>());
			}
            //修改群信息
            group.setGroupName(reqGroup.getGroupName());
            group.setGroupDesc(reqGroup.getDesc());
            group.setAvatar(reqGroup.getAvatar());
            group.setqRcode(reqGroup.getqRCode());
            group.setCapacity(reqGroup.getCapacity());
            group.setIsAudit(reqGroup.getIsAudit());
            groupDao.update(appId,group);
            
            //  TODO    发送群组通知，只有修改群名称的时候才发送信息
            if (StringUtils.isNotBlank(reqGroup.getGroupName())) {
            	ThreadPool pool = ThreadPool.getInstance();
            	
            	EditGroupMsg msg = new EditGroupMsg();
            	msg.setGroupId(groupId);
            	msg.setFromUid(userId);
            	msg.setFromName(nickName);
            	msg.setContent(nickName +"将群名称该为"+reqGroup.getGroupName());
            	msg.setOptTime(System.currentTimeMillis());
            	
            	msg.setAppId(Integer.valueOf(appId));
            	msg.setExtra(Constant.STRING_NULL);
            	
            	pool.addTask(msg);
			}
            
            return new ResultModel<Object>(ResultModel.RESULT_OK, "修改群信息成功", new HashMap<>());
        } catch (Exception e) {
            logger.error("修改群信息失败，出现异常",e);
            return new ResultModel<Object>(ResultModel.RESULT_FAILURE, "修改群信息失败", new HashMap<>());
            
        }
    }

    /**
     * 解散群
     */
    @Override
    public ResultModel<Object> disbandGroup(ReqGroup reqGroup,String appId,long userId) {
        try {
            String groupId = reqGroup.getGroupId();
            Group group = groupDao.getGroup(appId,groupId);
            if (group == null) {
            	logger.error("参数为空");
                return new ResultModel<Object>(ResultModel.RESULT_FAILURE, "参数为空", new HashMap<>());
            }
            GroupMember creater = groupMemberDao.getGroupMemberByUid(appId,groupId,userId);
            if (creater == null) {
            	logger.error("此人不是群成员,userId:{},groupId:{}",userId,groupId);
                return new ResultModel<Object>(ResultModel.RESULT_FAILURE, "此人不是群成员", new HashMap<>());
            }
            if (creater.getUserId() != group.getUserId()) {
            	logger.error("此人不是群主，无权解散群,userId:{},groupId:{}",userId,groupId);
                return new ResultModel<Object>(ResultModel.RESULT_FAILURE, "此人不是群主，无权解散群", new HashMap<>());
            }
            String nickName = reqGroup.getNickName();
            if (StringUtils.isBlank(nickName)) {
				nickName=JedisClusterClient.getNowNickName(userId, nickName);
			}
            if (StringUtils.isBlank(nickName)) {
				nickName=creater.getNickName();
			}
            if (StringUtils.isBlank(nickName)) {
				logger.error("昵称为空");
				return new ResultModel<Object>(ResultModel.RESULT_FAILURE,"昵称为空", new HashMap<>());
			}
            groupDao.setGroupIsDel(appId,groupId, Constant.GROUP_DEL.E_GROUP_DEL_OK.value);

            List<GroupMember> members = groupMemberDao.listGroupMembers(appId,groupId);
            List<GroupQuitMember> groupQuitMembers = new ArrayList<>();
            List<Long> disbandIdList=new ArrayList<Long>();
            for(GroupMember member: members) {
                GroupQuitMember quitMember = new GroupQuitMember(member.getUserId(), groupId);
                groupQuitMembers.add(quitMember);
                if (member.getUserId()==userId) {
					continue;
				}
                disbandIdList.add(member.getUserId());
            }
            //删除所有群组成员
            groupMemberDao.delGroupAllMember(appId, groupId);
            //删除所有群组相关备注
            groupMemberMarkDao.delAllMemberMark(appId, groupId);
            //保存删除的群组成员
            groupQuitMemberDao.saveGroupQuitMembers(appId, groupQuitMembers);

            ThreadPool pool = ThreadPool.getInstance();
            //  TODO    群组广播群组解散
			DisbandGroupMsg msg = new DisbandGroupMsg();
			
			msg.setGroupId(groupId);
			msg.setAppId(Integer.valueOf(appId));
			String idListStr="";
			for (Long long1 : disbandIdList) {
				idListStr+=long1+",";
			}
			idListStr = idListStr.substring(0, idListStr.lastIndexOf(","));
			msg.setExtra(idListStr);
			msg.setFromName(nickName);
			msg.setContent(nickName+"解散群");

			msg.setFromUid(userId);
			msg.setOptTime(System.currentTimeMillis());

			pool.addTask(msg);
            logger.info("群解散成功，groupId:{},userId:{}",groupId,userId);
            return  new ResultModel<Object>(ResultModel.RESULT_OK, "群组解散成功", new HashMap<>());
        } catch (Exception e) {
            logger.error("群解散失败，出现异常",e);
            return new ResultModel<Object>(ResultModel.RESULT_FAILURE, "解散群失败", new HashMap<>());
        }
    }

    /**
     * 审核群成员
     * 
     * @param reqGroup
     * @return
     */
    @Override
    public ResultModel<Object> auditMember(ReqGroup reqGroup,String appId,long userId) {
        try {
            String groupId = reqGroup.getGroupId();
            List<ReqGroupMember> members = reqGroup.getMembers();
            if (CollectionUtils.isEmpty(members)) {
            	logger.error("审核成员为空");
                return new ResultModel<Object>(ResultModel.RESULT_FAILURE, "审核成员为空", new HashMap<>());
            }
            Group group = groupDao.getGroup(appId,groupId);
            if (group == null) {
            	logger.error("此群不存在,userId:{},groupId:{}",userId,groupId);
                return new ResultModel<Object>(ResultModel.RESULT_FAILURE, "此群不存在", new HashMap<>());
            }
            GroupMember sender = groupMemberDao.getGroupMemberByUid(appId,groupId,userId);
            if (sender == null) {
            	logger.error("用户不在群中,userId:{},groupId:{}",userId,groupId);
                return new ResultModel<Object>(ResultModel.RESULT_FAILURE, "用户不在群中", new HashMap<>());
            }
            //TODO  审核群成员应该是A邀请B入群，而B的审核，并非一定是群主
            if (sender.getUserId() != group.getUserId()) {
            	logger.error("不是群主,无权审核群成员,userId:{},groupId:{}",userId,groupId);
                return new ResultModel<Object>(ResultModel.RESULT_FAILURE, "不是群主，无权审核群成员", new HashMap<>());
            }
            int status = reqGroup.getStatus();
            ThreadPool pool = ThreadPool.getInstance();
            if (status != Constant.GROUP_STATUS.E_GROUP_STATUS_OK.value) {
            	 logger.info("审核未通过,groupId:{},群主uid:{}", groupId, userId);
            	 
                 return new ResultModel<Object>(ResultModel.RESULT_FAILURE, "群成员审核未通过", new HashMap<>());
            }
            //如果审核通过，调用api加入将该通过的成员加入群组
            for (ReqGroupMember reqGroupMember : members) {
            	GroupMember groupMember = groupMemberDao.getGroupMemberByUid(appId,groupId,reqGroupMember.getUserId());
            	if (groupMember == null) {
            		continue;
            	}
            	if (Constant.GROUP_MEMBER_STATUS.E_MEMBER_STATUS_YES.value != groupMember.getStatus()) {
            		groupMember.setStatus(Constant.GROUP_MEMBER_STATUS.E_MEMBER_STATUS_YES.value);
            		groupMemberDao.updateStatus(appId, groupId, reqGroupMember.getUserId(),Constant.GROUP_MEMBER_STATUS.E_MEMBER_STATUS_YES.value);
            		
            		  //TODO 审核通过发送消息
        			
        			ApplyJoinGroupMsg msg = new ApplyJoinGroupMsg();
        			
        			msg.setGroupId(groupId);
        			msg.setApplicantId(reqGroupMember.getUserId());
        			String nickName=reqGroupMember.getNickName();
        			if (StringUtils.isBlank(nickName)) {
						nickName=JedisClusterClient.getNowNickName(reqGroupMember.getUserId(), nickName);
					}
        			if (StringUtils.isBlank(nickName)) {
						nickName=userMapper.getUserInfoById(reqGroupMember.getUserId()).getNickName();
					}
        			
					msg.setApplicantName(nickName);
        			msg.setContent(nickName +"加入群");
        			msg.setOptTime(System.currentTimeMillis());
        			
        			msg.setAppId(Integer.valueOf(appId));
        			msg.setExtra(Constant.STRING_NULL);
        			
        			pool.addTask(msg);
            		
            	}
            }
            
            return new ResultModel<Object>(ResultModel.RESULT_OK, "审核群成员成功,", new HashMap<>());
        } catch (Exception e) {
        	logger.error("审核群成员失败",e);
        	return new ResultModel<Object>(ResultModel.RESULT_FAILURE, "审核群成员失败", new HashMap<>());
        }
    }

    /**
     * 获取群组信息
     */
    @Override
    public ResultModel<Object> getGroupInfo(ReqGroup reqGroup,String appId,long userId) {
        try {
            String groupId = reqGroup.getGroupId();
            Group group = groupDao.getGroup(appId, groupId);
            if (group == null) {
            	logger.error("群组不存在：userId:{},groupId:{}",userId,groupId);
                return new ResultModel<Object>(ResultModel.RESULT_FAILURE, "群组不存在", new HashMap<>());
            }
            GroupMember member = groupMemberDao.getGroupMemberByUid(appId, groupId, userId);
            if (member == null) {
            	logger.error("用户不是群成员，userId:{},groupId:{}",userId,groupId);
                return new ResultModel<Object>(ResultModel.RESULT_FAILURE, "用户不是群成员", new HashMap<>());
            }
            RspGroup rspGroup = group2Rsp(group);
            //rspGroup.setInitSeq(member.getInitSeq());
            //rspGroup.setReadSeq(member.getReadSeq());
            rspGroup.setIsTop(member.getIsTop());
            rspGroup.setIsShield(member.getIsShield());
            return new ResultModel<Object>(ResultModel.RESULT_OK, "获取群组信息成功", rspGroup);
        } catch (Exception e) {
            logger.error("获取群组信息失败",e);
            return new ResultModel<Object>(ResultModel.RESULT_FAILURE, "获取群组信息失败", new HashMap<>());
        }
    }
    /**
     * 获取群组信息
     */
    @Override
    public ResultModel<Object> getGroupMembers(ReqGroup reqGroup,String appId,long userId) {
    	try {
    		String groupId = reqGroup.getGroupId();
    		Group group = groupDao.getGroup(appId, groupId);
    		if (group == null) {
    			logger.error("群组不存在：userId:{},groupId:{}",userId,groupId);
    			return new ResultModel<Object>(ResultModel.RESULT_FAILURE, "群组不存在", new HashMap<>());
    		}
    		GroupMember member = groupMemberDao.getGroupMemberByUid(appId, groupId, userId);
    		if (member == null) {
    			logger.error("用户不是群成员，userId:{},groupId:{}",userId,groupId);
    			return new ResultModel<Object>(ResultModel.RESULT_FAILURE, "用户不是群成员", new HashMap<>());
    		}
    		List<GroupMember> groupMemberList = groupMemberDao.listGroupMembers(appId,groupId,0,reqGroup.getStatus(),reqGroup.getPage(),reqGroup.getPageSize());
    		List<GroupMemberMark> memberMarkList = groupMemberMarkDao.getMemberMarks(appId,groupId,userId);
    		List<RspGroupMember> rspGroupMemberList = new ArrayList<>();
    		for (GroupMember groupMember : groupMemberList) {
    			RspGroupMember rspMember = member2Rsp(groupMember);
    			if (CollectionUtils.isEmpty(memberMarkList)) {
    				rspMember.setMark(StringUtils.EMPTY);
    			}else {
    				for (GroupMemberMark memberMark : memberMarkList) {
    					if (groupMember.getUserId() == memberMark.getMarkedUserId()) {
    						rspMember.setMark(memberMark.getMark());
    						break;
    					}
    				}
    			}
    			rspGroupMemberList.add(rspMember);
    		}
    		return new ResultModel<Object>(ResultModel.RESULT_OK, "获取群组信息成功", rspGroupMemberList);
    	} catch (Exception e) {
    		logger.error("获取群组信息失败",e);
    		return new ResultModel<Object>(ResultModel.RESULT_FAILURE, "获取群组信息失败", new HashMap<>());
    	}
    }

    /**
     * 获取群组列表信息
     */
    @Override
    public ResultModel<Object> listGroup(String appId,long userId) {
        try {
            List<RspGroup> rspGroupList = new ArrayList<RspGroup>();
            List<GroupMember> userGroups = groupMemberDao.listMemberGroups(appId,userId);//用户在所有群中的成员信息
            if (CollectionUtils.isEmpty(userGroups)) {
            	logger.error("用户没有加入任何群,userId:{}",userId);
            	return new ResultModel<Object>(ResultModel.RESULT_FAILURE, "用户没有加入任何群", new HashMap<>());
			}
           
            for (GroupMember member : userGroups) {
                String groupId = member.getGroupId();
                Group group = groupDao.getGroup(appId,groupId);
                RspGroup repGroup = group2Rsp(group);
                repGroup.setIsTop(member.getIsTop());
                repGroup.setIsShield(member.getIsShield());
                rspGroupList.add(repGroup);
            }
            logger.info("获取群组列表信息,userId:{},加入的群有：{}个",userId,rspGroupList.size());
            return new ResultModel<Object>(ResultModel.RESULT_OK, "获取群组列表信息成功", rspGroupList);
        } catch (Exception e) {
            logger.error("获取群组列表信息失败",e);
            return new ResultModel<Object>(ResultModel.RESULT_FAILURE, "获取群组列表信息失败", new HashMap<>());
        }
    }

    /**
     * 修改群成员备注
     */
    @Override
    public ResultModel<Object> editMemberMark(ReqGroup reqGroup,String appId,long userId) {
        try {
			String mark = reqGroup.getMark();
			if (StringUtils.isBlank(mark)) {
				logger.error("备注不能为空");
				return new ResultModel<Object>(ResultModel.RESULT_FAILURE,"备注不能为空", new HashMap<>());
			}
			String groupId = reqGroup.getGroupId();
			Group group = groupDao.getGroup(appId, groupId);
			if (group == null) {
				logger.error("群组不存在：userId:{},groupId:{}", userId, groupId);
				return new ResultModel<Object>(ResultModel.RESULT_FAILURE,"群组不存在", new HashMap<>());
			}
			GroupMember member = groupMemberDao.getGroupMemberByUid(appId,groupId, userId);
			if (member == null) {
				logger.error("用户不是群成员，userId:{},groupId:{}", userId, groupId);
				return new ResultModel<Object>(ResultModel.RESULT_FAILURE,"用户不是群成员", new HashMap<>());
			}
			long markedUserId = reqGroup.getMarkedUserId();
			GroupMember markedMember = groupMemberDao.getGroupMemberByUid(appId,groupId, markedUserId);
			if (markedMember == null) {
				logger.error("备注的用户不是群成员，userId:{},groupId:{},markUserId:{}", userId, groupId,markedUserId);
				return new ResultModel<Object>(ResultModel.RESULT_FAILURE,"备注的用户不是群成员", new HashMap<>());
			}
            GroupMemberMark memberMark = groupMemberMarkDao.getMemberMark(appId, groupId, userId, markedUserId);
            if (memberMark == null) {
                memberMark = new GroupMemberMark();
                memberMark.setGroupId(groupId);
                memberMark.setUserId(userId);
                memberMark.setMarkedUserId(markedUserId);
                memberMark.setMark(mark);
                long nowTime = System.currentTimeMillis();
                memberMark.setCreateTime(nowTime);
                memberMark.setUpdateTime(nowTime);
                groupMemberMarkDao.saveMemberMark(appId, memberMark);
            } else {
            	if (!StringUtils.equals(mark, memberMark.getMark())) {
            		memberMark.setMark(mark);
            		memberMark.setUpdateTime(System.currentTimeMillis());
            		groupMemberMarkDao.updateMemberMark(appId, groupId, userId, markedUserId, mark);
				}
            }
        	logger.info("修改备注成功，userId:{},groupId:{},markUserId:{}", userId, groupId,markedUserId);
            return new ResultModel<Object>(ResultModel.RESULT_OK, "修改或设置备注成功", new HashMap<>());
        } catch (Exception e) {
            logger.error("修改备注失败，出现异常",e);
            return new ResultModel<Object>(ResultModel.RESULT_FAILURE, "修改或设置备注失败", new HashMap<>());
        }
    }

    /**
     * TODO 与 getGroup 重复 我要删除你  liuzhenhuan 20160617
     * 获取群组成员 
     *
     * @param reqGroup
     * @return
     */
    @Deprecated
    public ResultModel<Object> listGroupMember(ReqGroup reqGroup,String appId,long userId) {
        ResultModel<Object> resultModel = new ResultModel<Object>(ResultModel.RESULT_FAILURE, "获取失败", new HashMap<>());
        try {
            List<RspGroupMember> rspMembers = new ArrayList<RspGroupMember>();
            List<RspMemberMark> rspMemberMarks = new ArrayList<>();
            String groupId = reqGroup.getGroupId();
            List<GroupMember> groupMemberList = groupMemberDao.listGroupMembers(appId,groupId);
            boolean isGroupMember = false;
            for (GroupMember member : groupMemberList) {
                if (member.getUserId() == userId) {
                    isGroupMember = true;
                }
                RspGroupMember rspMember = member2Rsp(member);
                rspMembers.add(rspMember);
            }
            List<GroupMemberMark> listMemberMark = groupMemberMarkDao.getMemberMarks(appId,groupId,userId);
            for (GroupMemberMark groupMemberMark : listMemberMark) {
                RspMemberMark rspMemberMark = new RspMemberMark();
                rspMemberMark.setMarkedUserId(groupMemberMark.getMarkedUserId());
                rspMemberMark.setMark(groupMemberMark.getMark());
                rspMemberMarks.add(rspMemberMark);
            }
            if (!isGroupMember) {
                return new ResultModel<Object>(ResultModel.RESULT_FAILURE, "无权限获取信息", new HashMap<>());
            }
            HashMap<String, Object> rsp = new HashMap<>();
            rsp.put("groupId", groupId);
            rsp.put("members", rspMembers);
            rsp.put("memberMarks", rspMemberMarks);
            resultModel = new ResultModel<Object>(ResultModel.RESULT_OK, "操作成功", rsp);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.getMessage());
        }
        return resultModel;
    }

    /**
     * 设置屏蔽群消息
     */
    @Override
    public ResultModel<Object> shieldGroup(ReqGroup reqGroup,String appId,long userId) {
        try {
        	String groupId = reqGroup.getGroupId();
			Group group = groupDao.getGroup(appId, groupId);
			if (group == null) {
				logger.error("群组不存在：userId:{},groupId:{}", userId, groupId);
				return new ResultModel<Object>(ResultModel.RESULT_FAILURE,"群组不存在", new HashMap<>());
			}
			GroupMember groupMember = groupMemberDao.getGroupMemberByUid(appId,groupId, userId);
			if (groupMember == null) {
				logger.error("用户不是群成员，userId:{},groupId:{}", userId, groupId);
				return new ResultModel<Object>(ResultModel.RESULT_FAILURE,"用户不是群成员", new HashMap<>());
			}
			int isShield = reqGroup.getIsShield();
			if (groupMember.getIsShield() != isShield) {
				groupMember.setIsShield(isShield);
				groupMemberDao.updateShield(appId,groupId,userId,isShield);
			}
			logger.info("设置群屏蔽消息成功,userId:{},groupId:{}", userId, groupId);
			return new ResultModel<Object>(ResultModel.RESULT_OK,"设置群屏蔽消息成功", new HashMap<>());
        } catch (Exception e) {
        	logger.info("设置群屏蔽消息失败,出现异常",e);
        	return new ResultModel<Object>(ResultModel.RESULT_OK,"设置群屏蔽消息失败", new HashMap<>());
        }
    }

    /**
     * 修改群置顶
     */
    @Override
    public ResultModel<Object> sickiesGroup(ReqGroup reqGroup,String appId,long userId) {
    	try {
        	String groupId = reqGroup.getGroupId();
			Group group = groupDao.getGroup(appId, groupId);
			if (group == null) {
				logger.error("群组不存在：userId:{},groupId:{}", userId, groupId);
				return new ResultModel<Object>(ResultModel.RESULT_FAILURE,"群组不存在", new HashMap<>());
			}
			GroupMember groupMember = groupMemberDao.getGroupMemberByUid(appId,groupId, userId);
			if (groupMember == null) {
				logger.error("用户不是群成员，userId:{},groupId:{}", userId, groupId);
				return new ResultModel<Object>(ResultModel.RESULT_FAILURE,"用户不是群成员", new HashMap<>());
			}
			int isTop = reqGroup.getIsTop();
			if (groupMember.getIsTop() != isTop) {
				groupMember.setIsTop(isTop);
				groupMemberDao.updateIsTop(appId,groupId,userId,isTop);
			}
			logger.info("修改群置顶成功,userId:{},groupId:{}", userId, groupId);
			return new ResultModel<Object>(ResultModel.RESULT_OK,"修改群置顶成功", new HashMap<>());
        } catch (Exception e) {
        	logger.info("修改群置顶失败,出现异常",e);
        	return new ResultModel<Object>(ResultModel.RESULT_OK,"修改群置顶失败", new HashMap<>());
        }
    }
    
    
    private RspGroupMember member2Rsp(GroupMember member) {
        RspGroupMember rspMember = new RspGroupMember();
        rspMember.setUserId(member.getUserId());
        rspMember.setJoinTime(member.getJoinTime());
        rspMember.setNickName(JedisClusterClient.getNowNickName(member.getUserId(), member.getNickName()));
        rspMember.setGroupId(member.getGroupId());
        rspMember.setIdentity(member.getIdentity());
        rspMember.setIsShield(member.getIsShield());
        rspMember.setStatus(member.getStatus());
        rspMember.setJoinTime(member.getJoinTime());
        
        return rspMember;
    }
    
    

    private  RspGroup group2Rsp(Group group) {
        RspGroup rspGroup = new RspGroup();
        rspGroup.setGroupId(group.getGroupId());
        rspGroup.setUserId(group.getUserId());
        rspGroup.setGroupName(group.getGroupName());
        rspGroup.setAvatar(group.getAvatar());
        rspGroup.setDesc(group.getGroupDesc());
        rspGroup.setCapacity(group.getCapacity());
        rspGroup.setIsAudit(group.getIsAudit());
        rspGroup.setqRCode(group.getqRcode());
        rspGroup.setType(group.getType());
        return rspGroup;
    }

}
