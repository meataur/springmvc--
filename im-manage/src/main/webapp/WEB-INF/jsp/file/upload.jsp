<%@ page language="java" contentType="text/html; charset=utf-8"
    pageEncoding="utf-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>文件上传</title>
<script src="../js/jquery-1.4.4.min.js" type="text/javascript"></script>
<script src="../js/jquery.md5.js" type="text/javascript" ></script>
<script src="../js/md5.js" type="text/javascript" ></script> 
</head>
<script type="text/javascript">
	function getNewData() {		
		var appId = document.getElementById("appId").value;
		var uId = document.getElementById("uid").value;
		var nowTime = (new Date()).valueOf();
		var currentTime = document.getElementById("currentTime");
		currentTime.value = nowTime;
		var keywords = appId+uId+nowTime;
		var ms = $.md5(keywords); 
		var md5s = md5(keywords);
        var key = document.getElementById("key");
        key.value = md5s;
        //修改图片表单中的数据
        var cti = document.getElementById("currentTimeImg");
        var keyi = document.getElementById("keyImg");
        cti.value = nowTime;
        keyi.value = md5s;
        //修改视频表单中的数据
        var ctv = document.getElementById("currentTimeVedio");
        var keyv = document.getElementById("keyVedio");
        ctv.value = nowTime;
        keyv.value = md5s;
        //头像上传
        var ctau = document.getElementById("currentTimeAvatarU");
        var keyau = document.getElementById("keyAvatarU");
        ctau.value = nowTime;
        keyau.value = md5s;
        //头像下载
        var ctad = document.getElementById("currentTimeAvatarD");
        var keyad = document.getElementById("keyAvatarD");
        ctad.value = nowTime;
        keyad.value = md5s;
	}
</script>
<body>
	<input type="button" value="点击修改数据" onclick="getNewData()" />
	<fieldset>
       <legend>头像上传 </legend>
       <form action="http://10.125.2.152:8080/im-upload/AvatarUploadServlet.do"
           method="post" enctype="multipart/form-data">
			currentTime:<input type="text" name="currentTime" id = "currentTimeAvatarU">
			traceId:<input type="text" name="traceId" id = "traceId" value="ttttttttt">
			appId:<input type="text" name="appId" id = "appId" value="appIdappIdappId">
			key:<input type="text" name="key" id = "keyAvatarU" value="">
			uid:<input type="text" name="uid" id = "uid" value="11111">
           <hr/>
          	 上传文件：	<input type="file" name="file">
           <br>
           <input type="submit" value="上传">
       </form>
     </fieldset>
     <br>
     <hr/>
     <br>
     <fieldset>
       <legend>头像下载 </legend>
       <!-- 
       	测试地址：http://10.69.211.16:8080 
       	开发地址：http://10.125.3.61:8080
       	预生产地址：http://10.125.2.152:8080
       	-->
       <form action="http://10.125.2.152:8080/im-upload/AvatarDownloadServlet.do" method="get" >
			currentTime:<input type="text" name="currentTime" id = "currentTimeAvatarD">
			traceId:<input type="text" name="traceId" id = "traceId" value="ttttttttt">
			appId:<input type="text" name="appId" id = "appId" value="appIdappIdappId">
			key:<input type="text" name="key" id = "keyAvatarD" value="">
			uid:<input type="text" name="uid" id = "uid" value="11111">
           <br>
           <input type="submit" value="下载">
       </form>
     </fieldset>
     <br>
     <hr/>
     <br>
     <fieldset>
       <legend>上传单个图片文件 </legend>
       <%-- <form action="${pageContext.request.contextPath}/ImageUploadServlet.do" --%>
       <form action="http://10.125.2.152:8080/im-upload/ImageUploadServlet.do"
           method="post" enctype="multipart/form-data">
			currentTime:<input type="text" name="currentTime" id = "currentTimeImg">
			traceId:<input type="text" name="traceId" id = "traceId" value="ttttttttt">
			appId:<input type="text" name="appId" id = "appId" value="appIdappIdappId">
			key:<input type="text" name="key" id = "keyImg" value="">
			uid:<input type="text" name="uid" id = "uid" value="11111">
           <hr/>
          	 上传文件：	<input type="file" name="file">
           <br>
           <input type="submit" value="上传">
       </form>
     </fieldset>
     <br>
     <hr/>
     <br>
	<fieldset>
	   	<legend>上传单个音频文件 </legend>
	   	<form action="http://10.125.2.152:8080/im-upload/VoiceUploadServlet.do"
			method="post" enctype="multipart/form-data">
			currentTime:<input type="text" name="currentTime" id = "currentTime">
			traceId:<input type="text" name="traceId" id = "traceId" value="ttttttttt">
			appId:<input type="text" name="appId" id = "appId" value="appIdappIdappId">
			key:<input type="text" name="key" id = "key" value="">
			uid:<input type="text" name="uid" id = "uid" value="11111">
			<hr/>
			上传文件：	<input type="file" name="file">
		    <br>
		    <input type="submit" value="上传">
		</form>
    </fieldset>
    <br>
    <hr/>
    <br>
     <fieldset>
       <legend>上传单个视频文件 </legend>
       <form action="http://10.125.2.152:8080/im-upload/VedioUploadServlet.do"
           method="post" enctype="multipart/form-data">
			currentTime:<input type="text" name="currentTime" id = "currentTimeVedio">
			traceId:<input type="text" name="traceId" id = "traceId" value="ttttttttt">
			appId:<input type="text" name="appId" id = "appId" value="appIdappIdappId">
			key:<input type="text" name="key" id = "keyVedio" value="">
			uid:<input type="text" name="uid" id = "uid" value="11111">
           <hr/>
           	上传文件：	<input type="file" name="file">
           <br>
           <input type="submit" value="上传">
       </form>
    </fieldset>
</body>
</html>