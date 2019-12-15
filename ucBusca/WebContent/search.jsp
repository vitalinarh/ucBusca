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
    <div id="fb-root"></div>
    <script async defer crossorigin="anonymous" src="https://connect.facebook.net/en_US/sdk.js#xfbml=1&version=v5.0&appId=2592229557523032&autoLogAppEvents=1"></script>

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

        <div class="fb-share-button" data-href="https://developers.facebook.com/docs/plugins/" data-layout="button_count" data-size="small"><a target="_blank" href="https://www.facebook.com/sharer/sharer.php?u=https%3A%2F%2Fdevelopers.facebook.com%2Fdocs%2Fplugins%2F&amp;src=sdkpreparse" class="fb-xfbml-parse-ignore">Share</a></div>

</div>

    <div id = "results">
        <c:choose>
            <c:when test="${empty session.searchResults}">
                <p>No Results</p>
            </c:when>
            <c:otherwise>
                <p>Search Results</p>
                <c:choose>
                    <c:when test="${session.translate == 'false'}">
                        <p><a href="<s:url action="translate" />">Translate Page To Portuguese</a></p>
                    </c:when>
                    <c:otherwise>
                        <div class="column">
                            <p><a href="<s:url action="translate" />">Show Original</a></p>
                        </div>
                    </c:otherwise>
                </c:choose>

                <p>Number of results: ${session.numResults}</p><br>
                <c:forEach items="${session.searchResults}" var="line">
                    <c:choose>
                        <c:when test="${session.translate == 'false'}">
                            <p style="font-weight: bolder; font-family: Arial; font-size: 22px" >${line.title}.</p>
                            <p>Original Language ${line.language}.</p>
                            <a href = ${line.url}>${line.url}</a>
                            <p>${line.citation}.</p><br>
                        </c:when>
                        <c:otherwise>
                            <p style="font-weight: bolder; font-family: Arial; font-size: 22px" >${line.title_translation}.</p>
                            <p>Original Language ${line.language}.</p>
                            <a href = ${line.url}>${line.url}</a>
                            <p>Translation To Portuguese: ${line.translation}.</p>
                        </c:otherwise>
                    </c:choose>
                    <p>Referenced ${line.count} times.</p>

                </c:forEach>
            </c:otherwise>
        </c:choose>
    </div>
</body>
</html>
