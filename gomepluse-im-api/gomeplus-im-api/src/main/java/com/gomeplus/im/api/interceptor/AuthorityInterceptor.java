package com.gomeplus.im.api.interceptor;

import com.gomeplus.im.api.global.Global;
import com.gomeplus.im.api.utils.JedisClusterClient;
import com.gomeplus.im.api.utils.Md5Util;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import redis.clients.jedis.JedisCluster;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by wangshikai on 2016/6/13.
 */
public class AuthorityInterceptor implements HandlerInterceptor {
    private Logger logger = LoggerFactory.getLogger(AuthorityInterceptor.class);
    private final int loginErrorCode = 402;
    private final int errorCode = 403;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object o) throws Exception {
        String uri = request.getRequestURI().replace(request.getContextPath(), "");
        if (uri.contains("registerUser") || uri.contains("login") || uri.contains("applyApp")) {
            return true;
        }
        String appId = request.getParameter("appId");
        String userIdParam = request.getParameter("userId");
        String signature = request.getParameter("signature");
        String timestamp = request.getParameter("timestamp");
        logger.info("appId={},userId={},signature={},timestamp={}", appId, userIdParam, signature, timestamp);
//        HttpSession session = request.getSession();
//        User user = (User) session.getAttribute("user");
//        if (user == null) {
//            response.setStatus(loginErrorCode);
//            logger.error("登录错误,session不存在,重新登录");
//            return false;
//        }
        if (StringUtils.isNotEmpty(appId) && StringUtils.isNotEmpty(signature) && StringUtils.isNotEmpty(timestamp) && StringUtils.isNotEmpty(userIdParam)) {
            long userId = 0;
            try {
                userId = Long.parseLong(userIdParam);
            } catch (NumberFormatException e) {
                logger.error("userId error:{},userId={}", e, userIdParam);
                response.setStatus(errorCode);
                return false;
            }
            JedisCluster jedis = JedisClusterClient.INSTANCE.getJedisCluster();
            String key = appId + "_" + userId + Global.TOKEN_SUFFIX;
            String token = jedis.get(key);
            // md5 签名方式(appId+"|"+token+"|"+userId+"|"+timestamp)
            String md5Str = Md5Util.md5Encode(appId + "|" + token + "|" + userId + "|" + timestamp);
            if (md5Str.equals(signature)) {
                return true;
            } else {
                logger.error("签名错误,appId={},userId={},signature={},timestamp={},正确签名:{},token={}", appId, userIdParam, signature, timestamp, md5Str, token);
                response.setStatus(errorCode);
                return false;
            }
        } else {
            response.setStatus(errorCode);
            logger.error("参数错误!");
            return false;
        }
    }

    @Override
    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {

    }
}
