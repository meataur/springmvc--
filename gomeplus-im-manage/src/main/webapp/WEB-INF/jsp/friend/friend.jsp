<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/tags" prefix="date"%>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>好友信息</title>
    <script type="text/javascript" src="../js/jquery-1.2.6.pack.js"></script>
    <link href="../css/ligerui-all.css" rel="stylesheet" type="text/css" />
    <!-- script src="js/jquery-1.4.4.min.js" type="text/javascript"></script -->
    <script src="../js/ligerUI/base.js" type="text/javascript"></script>
    <script src="../js/ligerUI/ligerGrid.js" type="text/javascript"></script>
    <script src="../js/ligerUI/ligerDrag.js" type="text/javascript"></script>
    <script src="../js/ligerUI/ligerResizable.js" type="text/javascript"></script>
    <style type="text/css">
        body{margin:0; padding:0; font-size:12px; font-family:"宋体"; color:#000000; background-color:#f4f4f4;}
        a {
            text-decoration: none;
        }
        table {
            border: 1px;
            border-style: solid;
            border-color: gray;

            #font-family: "微软雅黑";
            text-align: center;

            margin: 0px;
            padding: 0px;
            border-collapse: collapse;
            padding: 0px;
        }
        tr:first-child {
            background-color: #AEC7E1;
        }
        tr {
            cursor: default;
        }
        td {
            #width: 100px;
            height:25px;
            border: 1px;
            border-style: solid;
            border-color: gray;
        }

    </style>
    <script type="text/javascript">
        function searchFriends(){
            var url;
            var userName = $("#userName").val();
            if(userName == ''){
                alert("用户名不能为空！");
                return false;
            }
            url = "getFriend.do?userName="+userName+"&random=" + (new Date()).valueOf();
            if(url == null){
                return false;
            }

            $("#maingrid").ligerGrid({
                columns : [
                    {
                        display : '用户ID',
                        name : 'userId',
                        width : 150,
                        align : 'left'
                    },
                    {
                        display : '好友ID',
                        name : 'friendUserId',
                        width : 150,
                        align : 'left'
                    }, {
                        display : '好友昵称',
                        name : 'friendNickName',
                        width : 250,
                        align : 'left'
                    },{
                        display : '当前状态',
                        name : 'status',
                        width : 50,
                        type : 'int',
                        align : 'left',
                        render:function(rowdata, index, value){
                            switch(value)
								{
								case 0:
								  return "未通过";
								  break;
								case 1:
								 return  "通过";
								  break;
								case 2:
								  return "拒绝";
								  break;
								default:
								  return "未知";
								}

                            
                        }
                    },
                    {
                        display : '好友备注',
                        name : 'mark',
                        width : 250,
                        align : 'left'
                    },{
                        display : '创建时间',
                        name : 'createTime',
                        width : 160,
                        type : 'int',
                        align : 'left',
                        render:function(rowdata, index, value){
                            
                            if(value <= 0) return value;
                            return new Date(parseInt(value)).toLocaleString();
                            
                        }
                    } ,{
                        display : '修改时间',
                        name : 'updateTime',
                        width : 160,
                        type : 'date',
                        align : 'left',
                        render:function(rowdata, index, value){
                            
                            if(value <= 0) return value;
                            return new Date(parseInt(value)).toLocaleString();
                            
                        }
                    }],
                allowHideColumn : false,
                checkbox : false,
                usePager : true,
                rownumbers : false,
                alternatingRow : false,
                tree : {
                    columnName : 'name'
                },
                url : url,
                method : "get",
            });
        }

        $(document).ready(function() {
        });
    </script>
</head>
<body>
<h3>好友信息</h3>
<div>
    &nbsp;&nbsp;&nbsp;&nbsp;
    用户名：<input type="text" name="userName" id="userName"  value = "${param.userName}"/>
    &nbsp;&nbsp;
    <input type="button" onclick="searchFriends()" value="查询用户好友" />
</div>
<div id="maingrid"></div>
</body>
</html>