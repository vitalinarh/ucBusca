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

    <div class="row">

        <div class="column">
            <s:form action="search" method="post">
                <s:text name="Search:" />
                <s:textfield name="search" />
                <s:submit />
            </s:form>
        </div>

        <div class="column">
            <p><a href="<s:url action="menu" />">Menu</a></p>
        </div>

    </div>

    <c:choose>
        <c:when test="${empty session.searchResults}">
            <p>No Results</p>
        </c:when>
        <c:otherwise>
            <p>Search Results</p>
            <p>Number of results: ${session.numResults}</p><br>
            <c:forEach items="${session.searchResults}" var="line">
                <p style="font-weight: bolder; font-family: Arial; font-size: 22px" >${line.title}.</p>
                <a href = ${line.url}>${line.url}</a>
                <p>Referenced ${line.count} times.</p>
                <p>${line.citation}.</p><br>
            </c:forEach>
        </c:otherwise>
    </c:choose>
</body>
</html>
