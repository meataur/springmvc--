package com.gomeplus.im.api.test;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class FriendTest extends ApiTester {
	public static final String FRIEND = "/friend";
	
	@Test
	public void applayAddFriendTest() {
		
		String token="497dfeff5a4449d086dacc983ef9a431";
		long userId=33;
		String urlParams = getURLParams(token, userId);
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("friendUserId", "35");	
		params.put("userNickName", "刘贞环");
		String result = post(GOMEPLUS_IM_API + FRIEND
				+ "/applyAddFriend.json"+urlParams, params);
		outputResultInfo(GOMEPLUS_IM_API + FRIEND
				+ "/applyAddFriend.json"+urlParams, result);
	}
	@Test
	public void auditFriendTest() {
		String token="497dfeff5a4449d086dacc983ef9a431";
		long userId=33;
		String urlParams = getURLParams(token, userId);
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("friendUserId", "29");
		params.put("status", 1);
		
		String result = post(GOMEPLUS_IM_API + FRIEND
				+ "/auditFriend.json"+urlParams, params);
		outputResultInfo(GOMEPLUS_IM_API + FRIEND
				+ "/auditFriend.json"+urlParams, result);
	}
	@Test
	public void editMarkTest() {
		long userId=1;
		String token="4ccf2767c74d47829c74fb1dae815bf2";
		String urlParams = getURLParams(token, userId);
		
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("mark", "liuzhenhuan备注ss!");
		
		String result = post(GOMEPLUS_IM_API + FRIEND
				+ "/editMark.json"+urlParams, params);
		outputResultInfo(GOMEPLUS_IM_API + FRIEND
				+ "/editMark.json"+urlParams, result);
	}
	@Test
	public void updateFriendTest() {
		long userId=1;
		String token="4ccf2767c74d47829c74fb1dae815bf2";
		String urlParams = getURLParams(token, userId);
		
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("mark", "liuzhenhuan备注ss!");
		
		String result = post(GOMEPLUS_IM_API + FRIEND
				+ "/updateFriend.json"+urlParams, params);
		outputResultInfo(GOMEPLUS_IM_API + FRIEND
				+ "/updateFriend.json"+urlParams, result);
	}
	@Test
	public void deleteFriendTest() {
		long userId=33;
		String token="4ccf2767c74d47829c74fb1dae815bf2";
		String urlParams = getURLParams(token, userId);

		Map<String, Object> params = new HashMap<String, Object>();
		params.put("friendUserId", "35");
		params.put("mark", "liuzhenhuan备注!");
		
		String result = post(GOMEPLUS_IM_API + FRIEND
				+ "/deleteFriend.json"+urlParams, params);
		outputResultInfo(GOMEPLUS_IM_API + FRIEND
				+ "/deleteFriend.json"+urlParams, result);
	}
	@Test
	public void listFriendsTest() {
		Map<String, Object> params = new HashMap<String, Object>();
		long userId=33;
		String token="4ccf2767c74d47829c74fb1dae815bf2";
		String urlParams = getURLParams(token, userId);
		params.put("page", 1);
		params.put("pageSize", 10);
		String result = post(GOMEPLUS_IM_API + FRIEND
				+ "/listFriends.json"+urlParams, params);
		outputResultInfo(GOMEPLUS_IM_API + FRIEND
				+ "/listFriends.json"+urlParams, result);
	}
	@Test
	public void getGroupFriendsTest() {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("userId", "1");
		params.put("friendGroupId", "19");
		
		String result = post(GOMEPLUS_IM_API + FRIEND
				+ "/getGroupFriends.json", params);
		outputResultInfo(GOMEPLUS_IM_API + FRIEND
				+ "/getGroupFriends.json", result);
	}
	@Test
	public void getAllFriendsTest() {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("userId", "1");
		
		String result = post(GOMEPLUS_IM_API + FRIEND
				+ "/getAllFriends.json", params);
		outputResultInfo(GOMEPLUS_IM_API + FRIEND
				+ "/getAllFriends.json", result);
	}
	@Test
	public void updateFriendGroupTest() {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("userId", "1");
		params.put("friendGroupId", "-1");
		
		String result = post(GOMEPLUS_IM_API + FRIEND
				+ "/updateFriendGroup.json", params);
		outputResultInfo(GOMEPLUS_IM_API + FRIEND
				+ "/updateFriendGroup.json", result);
	}
	
	@Test
	public void redisGetNickName(){
		String userNickName="123";
		long userId=1;
		String nickName = JedisClusterClientTest.getNickNameByUserId(userId, userNickName);
		System.out.println(nickName);
	}
	
}
