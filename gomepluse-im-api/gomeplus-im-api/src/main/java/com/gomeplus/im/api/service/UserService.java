package com.gomeplus.im.api.service;


import com.gomeplus.im.api.model.User;
import com.gomeplus.im.api.request.ReqFindUser;
import com.gomeplus.im.api.request.response.ResultModel;

public interface UserService {
	
	public ResultModel<Object> saveUser(User user,String appId);
	
	public ResultModel<Object> updateUserInfo(User user,String appId,long userId);
	
	public ResultModel<Object> getUserInfo(String appId,User user);

	public ResultModel<Object> login(User user,String appId);
	
	public ResultModel<Object> findUser(String keyWord,String appId);
	
	public ResultModel<Object> modifyPwd(ReqFindUser reqFindUser,String appId,long userId);
}
