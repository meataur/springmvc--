package com.gome.im.api.model.request;

import java.io.Serializable;

/**
 * 朋友圈
 */
public class ReqFriendGroup extends BaseRequest implements Serializable {
	private long id;
	private long uid;
	private String nickName;
	private String content;
	private String extra;
	private int contentType; //1:txt 2:image 3:video
	private long beforeDay;  //查看前几天发布的内容
	private long endTime;    //查看结束时间

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getUid() {
		return uid;
	}

	public void setUid(long uid) {
		this.uid = uid;
	}

	public String getNickName() {
		return nickName;
	}

	public void setNickName(String nickName) {
		this.nickName = nickName;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getExtra() {
		return extra;
	}

	public void setExtra(String extra) {
		this.extra = extra;
	}

	public int getContentType() {
		return contentType;
	}

	public void setContentType(int contentType) {
		this.contentType = contentType;
	}


	public long getEndTime() {
		return endTime;
	}

	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}

	public long getBeforeDay() {
		return beforeDay;
	}

	public void setBeforeDay(long beforeDay) {
		this.beforeDay = beforeDay;
	}
}
