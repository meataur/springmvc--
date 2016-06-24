package com.gome.im.api.global;

import com.gome.im.api.utils.PropertiesUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

/**
 * 存放文件配置信息或者全局变量配置信息
 */
public class Global {
	static Logger log = LoggerFactory.getLogger(Global.class);

	public static String APP_ID = "TEST_APP_ID";

	public static String APP_KEY = "TEST_APP_KEY";

	public static String CENTER_IM_API_URL = "http:localhost:8080/center-im-api/";

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
	public static String MONGODB_DBNAME="db_im";

	//二维码配置
	public static int QRCODE_HEIGHT = 200; //默认
	public static int QRCODE_WIDTH = 200; //默认
	public static String QRCODE_URL_1 = "";
	public static String QRCODE_URL_2 = "";

	// 聊天消息分库模值
	public static int MSG_DB_MODULO;
	// 聊天消息分表模值
	public static int MSG_TABLE_MODULO;

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

			//二维码配置
			QRCODE_HEIGHT = Integer.parseInt(properties.getProperty("qrcode_image_height"));
			QRCODE_WIDTH = Integer.parseInt(properties.getProperty("qrcode_image_weight"));
			QRCODE_URL_1 = properties.getProperty("qrcode_url1");
			QRCODE_URL_2 = properties.getProperty("qrcode_url2");

			MSG_DB_MODULO = Integer.parseInt(properties.getProperty("msg.db.modulo"));
			MSG_TABLE_MODULO = Integer.parseInt(properties.getProperty("msg.table.modulo"));
		}catch (Exception e){
			e.printStackTrace();
			log.error(e.getMessage());
		}
	}

	public static void main(String[] args) {
		log.info("MQ_HOST:[{}]", Global.MQ_HOST);
	}
}
