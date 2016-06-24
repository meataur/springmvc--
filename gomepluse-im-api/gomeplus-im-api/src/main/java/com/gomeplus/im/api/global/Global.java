package com.gomeplus.im.api.global;

import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gomeplus.im.api.utils.PropertiesUtils;

/**
 * 存放文件配置信息或者全局变量配置信息
 */
public class Global {
	static Logger log = LoggerFactory.getLogger(Global.class);

	public static long TOKEN_EXPIRE_TIME = 7*24*60*60*1000;
	
	public static String REDIS_LOCK_KEY = "lock"; 
	
	public static String ALL_APP_KEY = "all_appId"; 
	
	public static final String TOKEN_SUFFIX = "_token";

	public static final int TOKEN_EXPIRES_REDIS = 7*24*60*60;

	public static final String GROUP_INITSEQ_INCR_SUFFIX = "_inc";

	public static final String GROUP_MEMBERS_SUFFIX = "_members";

	public static final String GROUP_MEMBER_INITSEQ_SUFFIX = "_initSeq";
	
	/**
	 * redis中的临时有修改的昵称用户IDs的集合的Key
	 */
	public static final String TEMP_CHANGE_NIKENAME_IDS="temp_change_nickname_ids";
	
	/**
	 * redis中的临时有修改的昵称（用户ID-昵称）key值后缀
	 */
	public static final String TEMP_CHANGE_USERID_NICKNAME_SUFFIX="_change_nikename";

	public static final String DATABASE_PREFIX = "gomeplus_im_";

	// 消息队列host
	public static String MQ_HOST;
	// 消息队列port
	public static int MQ_PORT;
	// 消息队列virtualHost
	public static String MQ_VIRTUALHOST;
	// 消息队列账号
	public static String MQ_USERNAME;
	// 消息队列密码
	public static String MQ_PASSWORD;
	// 消息队列名称
	public static String MQ_QUEUENAME;
	//mongondb库名
	public static String MONGODB_DBNAME="db_gomeplus_im";

	public static String MONGODB_HOST="10.125.3.11";
	public static int MONGODB_PORT;
	public static int MONGODB_CONNECTIONS;
	public static int MONGODB_CONNECTION_MULTIPLIER;



	public static String GLOBAL_CONF_FILE_NAME = "/conf/config.properties";

	static {
		try{
			Properties properties = PropertiesUtils.LoadProperties(GLOBAL_CONF_FILE_NAME);
			MQ_HOST = properties.getProperty("mq.host");
			MQ_PORT = Integer.parseInt(properties.getProperty("mq.port"));
			MQ_VIRTUALHOST = properties.getProperty("mq.virtualHost");
			MQ_USERNAME = properties.getProperty("mq.username");
			MQ_PASSWORD = properties.getProperty("mq.password");
			MQ_QUEUENAME = properties.getProperty("mq.queueName");

			MONGODB_DBNAME = properties.getProperty("mongodb.dbName");
			MONGODB_HOST = properties.getProperty("host");
			MONGODB_PORT = Integer.parseInt(properties.getProperty("port"));
			MONGODB_CONNECTIONS = Integer.parseInt(properties.getProperty("connectionsPerHost"));
			MONGODB_CONNECTION_MULTIPLIER = Integer.parseInt(properties.getProperty("connectionMultiplier"));

		}catch (Exception e){
			e.printStackTrace();
			log.error(e.getMessage());
		}
	}

	public static void main(String[] args) {
		log.info("MQ_HOST:[{}]", Global.MQ_HOST);
	}
}
