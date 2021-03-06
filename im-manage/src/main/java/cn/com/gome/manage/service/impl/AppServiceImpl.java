package cn.com.gome.manage.service.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;

import cn.com.gome.manage.mongodb.dao.AppDao;
import cn.com.gome.manage.mongodb.dao.AppSysAccountDao;
import cn.com.gome.manage.mongodb.model.AppAccount;
import cn.com.gome.manage.mongodb.model.AppSearchModel;
import cn.com.gome.manage.mongodb.model.AppSysAccount;
import cn.com.gome.manage.pageSupport.PageInfo;
import cn.com.gome.manage.pojo.AppInfo;
import cn.com.gome.manage.service.AppService;
import cn.com.gome.manage.utils.StringUtil;
@Service
public class AppServiceImpl implements AppService {
	
	private static Logger log = LoggerFactory.getLogger(AppServiceImpl.class);
	private final static AppDao appDao = new AppDao();
	private final static AppSysAccountDao  asaDao = new AppSysAccountDao();

	@Override
	public void saveAppInfo(AppInfo app) {
		long createTime = System.currentTimeMillis();
		app.setCreateTime(Long.toString(createTime));
		app.setAppKey(StringUtil.getUuid());
		log.info("AppInfo："+JSON.toJSONString(app));
		appDao.saveApp(app);
	}

	@Override
	public void updateAppInfo(AppInfo app) {
		app.setUpdateTime(Long.toString(System.currentTimeMillis()));
		appDao.updateAppInfo(app);
	}

	@Override
	public List<AppInfo> getAppInfo(PageInfo pageInfo,AppSearchModel appSearchModel) {
		List<AppInfo> app = appDao.getAppInfo(pageInfo, appSearchModel);
		return app;
	}
	
	@Override
	public List<AppInfo> getAllAppInfo(AppSearchModel appSearchModel) {
		List<AppInfo> app = appDao.getAllAppInfo(appSearchModel);
		return app;
	}
	
	public int getAPPIDCount(String appId){
		int count = appDao.getAPPIDCount(appId);
		return count;
	}

	@Override
	public void delAppInfo(String uId) {
		// TODO Auto-generated method stub

	}

	@Override
	public void saveAppSysAccount(AppSysAccount asa) {		
		asaDao.saveAppSysAccount(asa);
		
	}
	public List<AppSysAccount> getAppSysAccountByAppId(String appId, String uId){
		List<AppSysAccount> asaList = asaDao.getAppSysAccountByAppId(appId,uId);
		return asaList;
	}

	@Override
	public List<AppSysAccount> displayAppSysAccountByAppId(PageInfo pageInfo, String appId, String uId, String uName) {
		AppAccount appAccount = new AppAccount();
		appAccount.setAppId(appId);
		appAccount.setuId(uId);
		appAccount.setuName(uName);
		List<AppSysAccount> asaList = asaDao.displayAppSysAccountByAppId(pageInfo, appAccount);
		return asaList;
	}
	
	@Override
	public List<AppSysAccount> displayAllAppSysAccountByAppId(String appId, String uId, String uName) {
		AppAccount appAccount = new AppAccount();
		appAccount.setAppId(appId);
		appAccount.setuId(uId);
		appAccount.setuName(uName);
		List<AppSysAccount> asaList = asaDao.displayAllAppSysAccountByAppId(appAccount);
		return asaList;
	}

	@Override
	public List<String> getAllUidFromAppId(String appId) {
		List<String> asaList = asaDao.getAllUidFromAppId(appId);
		return asaList;
	}
	
}
