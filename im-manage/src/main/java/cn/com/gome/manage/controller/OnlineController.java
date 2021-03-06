package cn.com.gome.manage.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import cn.com.gome.manage.mongodb.model.AppSearchModel;
import cn.com.gome.manage.mongodb.model.TUserInfo;
import cn.com.gome.manage.mongodb.model.UserSearchModel;
import cn.com.gome.manage.pageSupport.PageInfo;
import cn.com.gome.manage.pageSupport.PageInfoFactory;
import cn.com.gome.manage.pojo.AppInfo;
import cn.com.gome.manage.pojo.OnlineStatistics;
import cn.com.gome.manage.pojo.ServerEcharts;
import cn.com.gome.manage.pojo.ServerSeries;
import cn.com.gome.manage.pojo.User;
import cn.com.gome.manage.pojo.UserInfo;
import cn.com.gome.manage.redis.RedisHashInfo;
import cn.com.gome.manage.redis.OnlineUserInfo;
import cn.com.gome.manage.redis.UserRedisService;
import cn.com.gome.manage.service.OnlineStatisticsService;
import cn.com.gome.manage.service.UserInfoService;
import cn.com.gome.manage.service.impl.AppServiceImpl;
import cn.com.gome.manage.utils.Constant;
import cn.com.gome.manage.utils.HttpUtil;
import cn.com.gome.manage.utils.IPUtils;
import cn.com.gome.manage.utils.ParamUtil;
import cn.com.gome.manage.utils.StringUtil;

/**
 * Created by wangshikai on 2015/12/22.
 */
@Controller
@RequestMapping("/online")
public class OnlineController {
    private Logger log = LoggerFactory.getLogger(OnlineController.class);
    
	@Autowired
	private UserInfoService userInfoService;
	@Autowired
	private OnlineStatisticsService onlineStatisticsService;
	@Autowired
	private AppServiceImpl appService;
	

    private UserRedisService userRedisService = new UserRedisService();

    @RequestMapping(value="online", method = RequestMethod.GET)
    public ModelAndView online(HttpServletRequest request, HttpServletResponse response) {
    	//增加appID列表
    	PageInfo pageInfoFroApp = PageInfoFactory.getPageInfo(10, 1);
  		AppSearchModel appSearchModel = new AppSearchModel();
  		User user =(User)request.getSession().getAttribute("loginInfo");
  		appSearchModel.setUserId(Long.toString(user.getId()));
  		appSearchModel.setType(user.getType());
      	List<AppInfo> appInfo = appService.getAppInfo(pageInfoFroApp,appSearchModel);
      	ModelAndView modelAndView = new ModelAndView("online/userList");
      	modelAndView.addObject("appIdList", appInfo);  
      	modelAndView.addObject("type",user.getType());
    	return modelAndView;
    }

    @RequestMapping(value="getOnlineUserList", method = RequestMethod.GET)
    public void getOnlineUserList(HttpServletRequest request, HttpServletResponse response) {
        long userId = ParamUtil.getLongParams(request, "userId", 0);
        List<OnlineUserInfo> list = new ArrayList<>();
        try{
            Map<String, String> userMap = userRedisService.listUserRsp(userId);
            Set<Map.Entry<String,String>> set = userMap.entrySet();
            for(Map.Entry<String,String> entry : set){
                OnlineUserInfo user = new OnlineUserInfo();
                user.setUserId(String.valueOf(userId));
                user.setClientId(entry.getKey());
                String ip_port = entry.getValue();
                String[] str = ip_port.split(":");
                String ip = str[0];
                ip = IPUtils.longToIP(Long.parseLong(ip));
                user.setIpAndPort( ip +":" + str[1]);
                list.add(user);
            }
        }catch (Exception e){
            log.error("getOnlineUserList error:"+ e.getMessage());
        }
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("Rows", list);
        HttpUtil.writeResult(response, map);
    }
    
    /**
     * 分页获取im用户信息
     * @param request
     * @param response
     */
    @RequestMapping(value="listUser", method = RequestMethod.GET)
    public ModelAndView listUser(HttpServletRequest request, HttpServletResponse response) {
    	ModelAndView modelAndView = new ModelAndView("online/userList");
    	User user =(User)request.getSession().getAttribute("loginInfo");
    	modelAndView.addObject("type",user.getType());
    	String appId = ParamUtil.getParams(request, "appId", "");
    	if(user.getType().equals(Constant.USER_TYPE.E_USER_TYPE_IM.value)){    		
    		if("".equals(appId)){
    			return new ModelAndView("online/userList");
    		}
    	}else{
    		PageInfo pageInfoFroApp = PageInfoFactory.getPageInfo(10, 1);
    		AppSearchModel appSearchModel = new AppSearchModel();  		
	  		appSearchModel.setUserId(Long.toString(user.getId()));
	  		appSearchModel.setType(user.getType());
	      	List<AppInfo> appInfo = appService.getAppInfo(pageInfoFroApp,appSearchModel);
	      	modelAndView.addObject("appIdList", appInfo);
    	}
    	UserSearchModel userSearchModel = new UserSearchModel();
    	userSearchModel.setAppId(appId);
    	userSearchModel.setuId(ParamUtil.getParams(request, "uId", ""));
    	userSearchModel.setStartTime(ParamUtil.getParams(request, "startTime", ""));
    	userSearchModel.setEndTime(ParamUtil.getParams(request, "endTime", ""));
    	int pageNo =  ParamUtil.getIntParams(request, "page", 1);
		int pageSize =  ParamUtil.getIntParams(request, "pagesize", 10);
		PageInfo pageInfo = PageInfoFactory.getPageInfo(pageSize, pageNo);		
        //List<UserInfo> list = userInfoService.listUserInfo(pageInfo);
		List<TUserInfo> list = userInfoService.listTUserInfo(userSearchModel,pageInfo);
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("Rows", list);
        map.put("Total",pageInfo.getTotalResult()); //数据总数
        HttpUtil.writeResult(response, map);
        return modelAndView;
    }
    /**
     * 根据昵称分页查询im用户信息
     * @param request
     * @param response
     */
    @RequestMapping(value="searchUser", method = RequestMethod.GET)
    public void searchUser(HttpServletRequest request, HttpServletResponse response) {
    	String nickName = ParamUtil.getParams(request, "nickName", "");
    	int pageNo =  ParamUtil.getIntParams(request, "page", 1);
		int pageSize =  ParamUtil.getIntParams(request, "pagesize", 10);
		PageInfo pageInfo = PageInfoFactory.getPageInfo(pageSize, pageNo);
        List<UserInfo> list = userInfoService.searchUserInfo(nickName, pageInfo);
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("Rows", list);
        map.put("Total",pageInfo.getTotalResult()); //数据总数
        HttpUtil.writeResult(response, map);
    }
    
    /**
     * 根据groupId,uId查询用户推送信息
     * @param request
     * @param response
     */
    @RequestMapping(value="getApnsInfo", method = RequestMethod.GET)
    public ModelAndView getApnsInfo(HttpServletRequest request, HttpServletResponse response) {
    	ModelAndView modelAndView = new ModelAndView("online/apnsInfo");
    	int pageNo = ParamUtil.getIntParams(request, "page", 1);
		int pageSize = ParamUtil.getIntParams(request, "pagesize", 10);
		PageInfo pageInfo = PageInfoFactory.getPageInfo(pageSize, pageNo);
    	User user = (User) request.getSession().getAttribute("loginInfo");
		modelAndView.addObject("type",user.getType());
    	String appId = ParamUtil.getParams(request, "appId", "");
    	String userId = ParamUtil.getParams(request, "uId", "");
    	if(appId.equals("")){
    		return modelAndView;
    	}
        List<RedisHashInfo> list = new ArrayList<>();
        Map<String, String> userMap = null;
         try{
        	 if(StringUtils.equals(userId, "")){
        		 list = userRedisService.getAllApnsInfo(appId,pageInfo);
        		 Map<String, Object> map = new HashMap<String, Object>();
                 map.put("Rows", list);
                 map.put("Total", pageInfo.getTotalResult());
                 HttpUtil.writeResult(response, map);
                 return modelAndView;
        	 }else{
        		 userMap = userRedisService.getApnsInfo(appId,userId);
        	 }
        	 if(userMap == null){
        		 return modelAndView;
        	 }
             Set<Map.Entry<String,String>> set = userMap.entrySet();
             for(Map.Entry<String,String> entry : set){
            	 RedisHashInfo apnsInfo = new RedisHashInfo();
            	 apnsInfo.setuId(userId);
                 apnsInfo.setKey(entry.getKey());
                 apnsInfo.setValue(entry.getValue());
                 list.add(apnsInfo);
             }
         }catch (Exception e){
             log.error("getApnsInfo error:"+ e.getMessage());
         }
         Map<String, Object> map = new HashMap<String, Object>();
         map.put("Rows", list);
         HttpUtil.writeResult(response, map);
         return modelAndView;
    }
    
    /**
     * 根据groupId,uId查询用户登录信息
     * @param request
     * @param response
     */
    @RequestMapping(value="getLoginInfo", method = RequestMethod.GET)
    public ModelAndView getLoginInfo(HttpServletRequest request, HttpServletResponse response) {
    	ModelAndView modelAndView = new ModelAndView("online/loginInfo"); 
    	User user = (User) request.getSession().getAttribute("loginInfo");
		modelAndView.addObject("type",user.getType());
		int pageNo = ParamUtil.getIntParams(request, "page", 1);
		int pageSize = ParamUtil.getIntParams(request, "pagesize", 10);
		PageInfo pageInfo = PageInfoFactory.getPageInfo(pageSize, pageNo);
		String userId = ParamUtil.getParams(request, "uId", "");
    	String appId = ParamUtil.getParams(request, "appId", "");
    	if(appId.equals("")){
    		return modelAndView;
    	}
         List<RedisHashInfo> list = new ArrayList<>();
         Map<String, String> userMap = null;
         try{
        	 if(StringUtils.equals(userId, "")){
        		 list = userRedisService.getAllLogonInfo(appId,pageInfo);
        		 Map<String, Object> map = new HashMap<String, Object>();
                 map.put("Rows", list);
                 map.put("Total", pageInfo.getTotalResult());
                 HttpUtil.writeResult(response, map);
                 return modelAndView;
        	 }
             userMap = userRedisService.getLoginInfo(appId,userId);
             if(userMap == null){
        		 return modelAndView;
        	 }
             Set<Map.Entry<String,String>> set = userMap.entrySet();
             for(Map.Entry<String,String> entry : set){
            	 RedisHashInfo loginInfo = new RedisHashInfo();
            	 loginInfo.setuId(String.valueOf(userId));
            	 loginInfo.setKey(entry.getKey());
            	 loginInfo.setValue(entry.getValue());
                 list.add(loginInfo);
             }
         }catch (Exception e){
             log.error("getLoginInfo error:"+ e.getMessage());
         }
         Map<String, Object> map = new HashMap<String, Object>();
         map.put("Rows", list);
         HttpUtil.writeResult(response, map);
         return modelAndView;
    }
    
    @RequestMapping(value = "preOnlineStatistics", method = RequestMethod.GET)
	public String preOnlineStatistics(HttpServletRequest request, Model model) {
		String appId = ParamUtil.getParams(request, "appId", "0");
		String time = StringUtil.long2DateString2(System.currentTimeMillis());
		model.addAttribute("appId", appId);
		model.addAttribute("nowTime", time);
		
		return "online/onlineStatistics";
	}
    
    @RequestMapping(value = "onlineStatistics", method = RequestMethod.POST)
	public void serverStatistics(HttpServletRequest request, HttpServletResponse response) {
		String time = ParamUtil.getParams(request, "time", "0");
		String appId = ParamUtil.getParams(request, "appId", "0");
		OnlineStatistics statistics = new OnlineStatistics();
		statistics.setAppId(appId);
		long statisticsTime = StringUtil.dateString2Long(time);
		statistics.setStatisticsTime(statisticsTime);
		List<OnlineStatistics> listStatistics = onlineStatisticsService.listOnlineStatistics(statistics);
		int size = listStatistics.size();
		List<String> axis = new ArrayList<String>(size);
//		List<Number> avgMemRateList = new ArrayList<Number>(size);
//		List<Number> maxMemRateList = new ArrayList<Number>(size);
//		List<Number> minMemRateList = new ArrayList<Number>(size);
//		List<Number> avgCpuRateList = new ArrayList<Number>(size);
//		List<Number> maxCpuRateList = new ArrayList<Number>(size);
//		List<Number> minCpuRateList = new ArrayList<Number>(size);
		List<Number> avgConnList = new ArrayList<Number>(size);
		List<Number> maxConnList = new ArrayList<Number>(size);
		List<Number> minConnList = new ArrayList<Number>(size);
		for(OnlineStatistics statis : listStatistics) {
			String hour = StringUtil.long2DateString3(statis.getStatisticsTime());
//			double avgMemRate = statis.getAvgMemRate();
//			double maxMemRate = statis.getMaxMemRate();
//			double minMemRate = statis.getMinMemRate();
//			double avgCpuRate = statis.getAvgCpuRate();
//			double maxCpuRate = statis.getMaxCpuRate();
//			double minCpuRate = statis.getMinCpuRate();
			long avgConn = statis.getAvgOnlineCount();
			long maxConn = statis.getMaxOnlineCount();
			long minConn = statis.getMinOnlineCount();
			axis.add(hour);
//			avgMemRateList.add(avgMemRate);
//			maxMemRateList.add(maxMemRate);
//			minMemRateList.add(minMemRate);
//			avgCpuRateList.add(avgCpuRate);
//			maxCpuRateList.add(maxCpuRate);
//			minCpuRateList.add(minCpuRate);
			avgConnList.add(avgConn);
			maxConnList.add(maxConn);
			minConnList.add(minConn);
		}

		List<ServerSeries> series = new ArrayList<ServerSeries>();
		List<String> legend = new ArrayList<String>(Arrays.asList(new String[]{"平均连接数", "最大连接数", "最小连接数"}));
//		series.add(new ServerSeries("平均内存", "line", avgMemRateList));
//		series.add(new ServerSeries("最大内存", "line", maxMemRateList));
//		series.add(new ServerSeries("最小内存", "line", minMemRateList));
//		series.add(new ServerSeries("平均CPU", "line", avgCpuRateList));
//		series.add(new ServerSeries("最大CPU", "line", maxCpuRateList));
//		series.add(new ServerSeries("最小CPU", "line", minCpuRateList));
		series.add(new ServerSeries("平均连接数", "line", avgConnList));
		series.add(new ServerSeries("最大连接数", "line", maxConnList));
		series.add(new ServerSeries("最小连接数", "line", minConnList));
		ServerEcharts echarts = new ServerEcharts(legend, axis, series);
		HttpUtil.writeResult(response, echarts);
		
	}
}
