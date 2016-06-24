package com.gomeplus.im.api.request;


import com.gomeplus.im.api.request.response.Member;

import java.io.Serializable;
import java.util.List;

/**
 * 群业务
 */
public class ReqGroup implements Serializable {
	private String groupId; // 群组id
	private String nickName;//群组创建者昵称
	private List<ReqGroupMember> members;// 成员列表
	private String groupName; // 群组名称
	private String desc; // 群组描述
	private String avatar; // 群组头像url
	private String qRCode; // 群组二维码url
	private int capacity; // 群组容量
	private int isAudit; // 申请加入是否需要审核;0:否,1:是
	private int auditResult;//审核结果
	private String extra; // 最后一条消息扩展信息
	private int isDele; // 是否被删除;0:否,1:是
	private long markedUserId;//被备注者id
	private String mark;//备注内容
	private String content;//申请加入群输入的内容
	private int joinType;//加入类型，1:邀请加入群，2:扫二维码加入群，3:通过群名/号加入群
	private int status;//成员在群中的状态;0:未通过,1:通过,2:拒绝
	private int isTop; // 置顶;0:否,1:是
	private int isShield;// 是否屏蔽群消息;0:否,1:是
	
	

	private int page; //页 从1开始
	private int pageSize; //每页数量

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}


	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public String getNickName() {
		return nickName;
	}

	public void setNickName(String nickName) {
		this.nickName = nickName;
	}


	public List<ReqGroupMember> getMembers() {
		return members;
	}

	public void setMembers(List<ReqGroupMember> members) {
		this.members = members;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public String getAvatar() {
		return avatar;
	}

	public void setAvatar(String avatar) {
		this.avatar = avatar;
	}

	public String getqRCode() {
		return qRCode;
	}

	public void setqRCode(String qRCode) {
		this.qRCode = qRCode;
	}

	public int getCapacity() {
		return capacity;
	}

	public void setCapacity(int capacity) {
		this.capacity = capacity;
	}

	public int getIsAudit() {
		return isAudit;
	}

	public void setIsAudit(int isAudit) {
		this.isAudit = isAudit;
	}

	public int getAuditResult() {
		return auditResult;
	}

	public void setAuditResult(int auditResult) {
		this.auditResult = auditResult;
	}

	public String getExtra() {
		return extra;
	}

	public void setExtra(String extra) {
		this.extra = extra;
	}

	public int getIsDele() {
		return isDele;
	}

	public void setIsDele(int isDele) {
		this.isDele = isDele;
	}


	public String getMark() {
		return mark;
	}

	public void setMark(String mark) {
		this.mark = mark;
	}


	public long getMarkedUserId() {
		return markedUserId;
	}

	public void setMarkedUserId(long markedUserId) {
		this.markedUserId = markedUserId;
	}

	public int getJoinType() {
		return joinType;
	}

	public void setJoinType(int joinType) {
		this.joinType = joinType;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public int getIsTop() {
		return isTop;
	}

	public void setIsTop(int isTop) {
		this.isTop = isTop;
	}

	public int getIsShield() {
		return isShield;
	}

	public void setIsShield(int isShield) {
		this.isShield = isShield;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public int getPage() {
		return page;
	}

	public void setPage(int page) {
		this.page = page;
	}
}
