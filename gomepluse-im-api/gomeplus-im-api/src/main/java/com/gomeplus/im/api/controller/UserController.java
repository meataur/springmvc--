package com.gomeplus.im.api.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSON;
import com.gomeplus.im.api.model.User;
import com.gomeplus.im.api.request.ReqFindUser;
import com.gomeplus.im.api.request.response.ResultModel;
import com.gomeplus.im.api.service.UserService;

import java.util.HashMap;

/**
 * Created by wangshikai on 2016/2/29.
 */
@Controller
@RequestMapping("user")
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    @Autowired
    private UserService userService;

    /**
     * 注册用户
     * @param user
     */
    @RequestMapping(value = "registerUser", method = RequestMethod.POST)
    @ResponseBody
    public ResultModel<Object> registerUser(@RequestBody  User user,@RequestParam String appId) {
        logger.info("registerUser , parms:"+ JSON.toJSONString(user));
        return userService.saveUser(user,appId);
    }

    /**
     * 用户更新用户信息
     * @param user
     */
    @RequestMapping(value = "updateUserInfo", method = RequestMethod.POST)
    @ResponseBody
    public ResultModel<Object> updateUserInfo(@RequestBody User user,@RequestParam String appId,@RequestParam long userId) {
        logger.info("updateToken , parms:"+ JSON.toJSONString(user));
        return userService.updateUserInfo(user,appId,userId);
    }

    /**
     * 获取用户信息
     * @param user
     */
    @RequestMapping(value = "getUserInfo", method = RequestMethod.POST)
    @ResponseBody
    public ResultModel<Object> getUserInfo(@RequestBody User user,@RequestParam String appId) {
        logger.info("getUserInfo , parms:"+ JSON.toJSONString(user));
        return userService.getUserInfo(appId,user);
    }

    /**
     * 用户登录，得到token
     * @param user
     */
    @RequestMapping(value = "login", method = RequestMethod.POST)
    @ResponseBody
    public ResultModel<Object> login(@RequestBody User user,HttpServletRequest request,@RequestParam String appId) {
    	long timeStamp = System.currentTimeMillis();
        logger.info("timeStamp:{},login parms:{}",timeStamp,JSON.toJSONString(user));
        ResultModel<Object> resultModel = userService.login(user,appId);
        if (resultModel.getCode()==ResultModel.RESULT_OK) { 
        	request.getSession().setAttribute("user", resultModel.getData());
		}
        return resultModel;
    }

    /**
     * 用户退出
     */
    @RequestMapping(value = "logout", method = RequestMethod.GET)
    @ResponseBody
    public ResultModel<Object> logout(HttpServletRequest request) {
        HttpSession session = request.getSession();
        session.removeAttribute("user");
        return new ResultModel<Object>(ResultModel.RESULT_OK, "退出成功",new HashMap<>());
    }
    
    /**
     * 获取用户信息模糊匹配
     * @param reqFriend
     */
    @RequestMapping(value = "findUser", method = RequestMethod.POST)
    @ResponseBody
    public ResultModel<Object> findUser(@RequestBody ReqFindUser reqFriend,@RequestParam String appId) {
    	String keyWord = reqFriend.getKeyWord();
    	logger.info("finUserInfo , parms:"+ JSON.toJSONString(keyWord));
        return userService.findUser(keyWord,appId);
    }
    
    /**
     * 修改密码
     * @param reqFriend
     */
    @RequestMapping(value = "modifyPwd", method = RequestMethod.POST)
    @ResponseBody
    public ResultModel<Object> modifyPwd(@RequestBody ReqFindUser reqFriend,@RequestParam long userId,@RequestParam String appId) {
    	String keyWord = reqFriend.getKeyWord();
    	logger.info("finUserInfo , parms:"+ JSON.toJSONString(keyWord));
    	return userService.modifyPwd(reqFriend,appId,userId);
    }
    
    
    
}
