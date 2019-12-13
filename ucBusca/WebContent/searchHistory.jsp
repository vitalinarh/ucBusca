<%--
  Created by IntelliJ IDEA.
  User: vital
  Date: 11/12/2019
  Time: 19:13
  To change this template use File | Settings | File Templates.
--%>
<%@ taglib prefix="s" uri="/struts-tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
         pageEncoding="ISO-8859-1"%>
<html>
<head>
    <title>ucBusca</title>
</head>
<body>
    <p><a href="<s:url action="menu" />">Menu</a></p>
    <h1>Your Search History</h1>
    <c:choose>
        <c:when test="${empty session.searchHistory}">
            <p>No searches have been made</p>
        </c:when>
        <c:otherwise>
            <c:forEach items="${session.searchHistory}" var="line">
                <p>${line}</p>
            </c:forEach>
        </c:otherwise>
    </c:choose>
</body>
</html>
