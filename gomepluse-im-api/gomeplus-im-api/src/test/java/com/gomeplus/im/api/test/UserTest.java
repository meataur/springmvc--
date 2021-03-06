package com.gomeplus.im.api.test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.print.DocFlavor.STRING;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang.time.DateUtils;
import org.codehaus.janino.Java.NewAnonymousClassInstance;
import org.junit.Test;
import org.springframework.util.Base64Utils;

import com.alibaba.fastjson.util.Base64;
import com.gomeplus.im.api.utils.Md5Util;

public class UserTest extends ApiTester {

	public static final String USER = "/user";
	public static final String TEST = "/test";

	@Test
	public void registerTest() throws ParseException {
		Map<String, Object> params = new HashMap<String, Object>();
//		params.put("userName", "liuzhenhuan10");
		params.put("password", "123456");
		params.put("phoneNumber", "12345678907");
//		params.put("nickName", "刘贞环");
		params.put("avatar", "http://www.baidu.com/123.jpg");
		params.put("gender", "1");
		params.put("region", "山东");
		Date birthday = DateUtils.parseDate("1992-01-22", new String[]{"yyyy-MM-dd"});
		params.put("birthday", birthday.getTime());
		params.put("autograph", "贞环签名");
		params.put("hardwareIndentifier", "iPhone");
		params.put("osIdentifier", "iOS|9.3.2");
		params.put("deviceId", "123232");
		String result = post(GOMEPLUS_IM_API + USER + "/registerUser.json"+APP_ID_URL,
				params);
		outputResultInfo(GOMEPLUS_IM_API + USER + "/registerUser.json"+APP_ID_URL, result);
	}

	@Test
	public void registerTest2() {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("userName", "liuzhenhuan6");
		params.put("phoneNumber", 12345678906L);
		params.put("password", "123456");

		String result = get(GOMEPLUS_IM_API + USER + "/registerUser.json",
				params);
		outputResultInfo(GOMEPLUS_IM_API + USER + "/registerUser.json", result);
	}

	@Test
	public void loginTest() {
		Map<String, Object> params = new HashMap<String, Object>();
//		params.put("userName", "liuzhenhuan10");
		params.put("userName", "gome_im_12345678905");
		params.put("password", "123456");

		String result = post(GOMEPLUS_IM_API + USER + "/login.json"+APP_ID_URL, params);
		outputResultInfo(GOMEPLUS_IM_API + USER + "/login.json"+APP_ID, result);
	}
	
	@Test
	public void getUserInfoTest() throws Exception {
//		调用【http://10.69.16.23:8087/gomeplus-im-api/user/login.jsongomeplus_im_dev】，
//			结果为【{"message":"登录成功","code":0,"data":{"gender":1,"signature":"贞环签名","nickName":"刘贞环",
//		"mobile":12345678902,"tokenValidity":1466496098283,"avatar":"http://www.baidu.com/123.jpg",
//			"userName":"liuzhenhuan02","region":"山东","userId":3,"token":"64c10b3c553f40529e1d870e6663e7c7"}}】
		String token="64c10b3c553f40529e1d870e6663e7c7";
		long userId=3;
		String urlParams = getURLParams(token, userId);
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("id", 3);
		String result = post(GOMEPLUS_IM_API + USER + "/getUserInfo.json"+urlParams,
				params);
		outputResultInfo(GOMEPLUS_IM_API + USER + "/getUserInfo.json"+urlParams, result);
	}
	
	@Test
	public void DateTest(){
		SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd");
		System.out.println(format.format(new Date(1463132933359L)));
	}

	@Test
	public void updateUserInfoTest() {
		Map<String, Object> params = new HashMap<String, Object>();
		String token="64c10b3c553f40529e1d870e6663e7c7";
		long userId=25;
		String urlParams = getURLParams(token, userId);
		params.put("nickName", "刘贞环测试12121");
		String result = post(GOMEPLUS_IM_API + USER + "/updateUserInfo.json"+urlParams,
				params);
		outputResultInfo(GOMEPLUS_IM_API + USER + "/updateUserInfo.json"+urlParams, result);
	}
	@Test
	public void findUserInfoTest() {
		Map<String, Object> params = new HashMap<String, Object>();
		String token="64c10b3c553f40529e1d870e6663e7c7";
		long userId=1;
		String urlParams = getURLParams(token, userId);
//		params.put("nickName", "刘贞环贞环昵称2");
		params.put("keyWord", "liu");
		String result = post(GOMEPLUS_IM_API + USER + "/findUser.json"+urlParams,
				params);
		outputResultInfo(GOMEPLUS_IM_API + USER + "/findUser.json"+urlParams, result);
	}
	
	@Test
	public void modifyPwdTest() {
		Map<String, Object> params = new HashMap<String, Object>();
		String token="64c10b3c553f40529e1d870e6663e7c7";
		long userId=33;
		String urlParams = getURLParams(token, userId);
		params.put("oldPwd", Base64Utils.encodeToString("liuzhenhuan".getBytes()));
		params.put("newPwd",Base64Utils.encodeToString("liuzhenhuan2".getBytes()));
		String result = post(GOMEPLUS_IM_API + USER + "/modifyPwd.json"+urlParams,
				params);
		outputResultInfo(GOMEPLUS_IM_API + USER + "/modifyPwd.json"+urlParams, result);
	}
	
	@Test
	public void hashCodeReGenerateTest() {
		long count = Long.MAX_VALUE;
		Map<String, String> hashCodeMap = new HashMap<String, String>();
		for (int i = 0; i < count; i++) {
			String uuid = UUID.randomUUID().toString();
			hashCodeMap.put(uuid, uuid.hashCode() + "");
		}

		for (String uuid : hashCodeMap.keySet()) {
			for (String uuid2 : hashCodeMap.keySet()) {
				if (StringUtils.equals(uuid, uuid2)) {
					continue;
				}
				if (StringUtils.equals(hashCodeMap.get(uuid),
						hashCodeMap.get(uuid2))) {
					System.out.println("uuid:" + uuid + ",uuid2:" + uuid2);

				}

			}
		}

	}
	
	@Test
	public void findUserTest() {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("keyWord", " ss ");

		String result = get(GOMEPLUS_IM_API + USER + "/findUser.json", params);
		outputResultInfo(GOMEPLUS_IM_API + USER + "/findUser.json", result);

	}
	
	
	@Test
	public void base64Test() {
		String encodePwd = Base64Utils.encodeToString("dsdsdsdwsdsdaaa".getBytes());
		System.out.println(encodePwd);
	}
	
	

}
