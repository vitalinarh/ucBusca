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
    <title>Search Results</title>
</head>
<body>
    <s:form action="search" method="post">
        <s:text name="Search:" />
        <s:textfield name="search" /><br>
        <s:submit />
    </s:form>
    <c:choose>
        <c:when test="${empty session.searchResults}">
            <p>No Results</p>
        </c:when>
        <c:otherwise>
            <p>Search Results</p><br>
            <c:forEach items="${session.searchResults}" var="line">
                <p>${line}.</p>
            </c:forEach>
        </c:otherwise>
    </c:choose>
</body>
</html>
