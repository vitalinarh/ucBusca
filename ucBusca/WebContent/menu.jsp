<%@ taglib prefix="s" uri="/struts-tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>ucBusca</title>
</head>
<body>
	<c:choose>
		<c:when test="${session.isLogged == true}">
			<p>Welcome, ${session.username}. </p>

			<p><a href="<s:url action="goToSearch" />">Search words</a></p>
			<p><a href="<s:url action="goToSearchPageRef" />">Search pages connected to specified page</a></p>
			<p><a href="<s:url action="searchHistory" />">Search history</a></p>

			<c:when test="${session.isAdmin == true}">
				<p><a href="<s:url action="goToIndexURL" />">Index URL</a></p>
				<p><a href="<s:url action="goToGiveAdminPrivilege" />">Give a user admin privileges</a></p>
				<p><a href="<s:url action="adminPage" />">Administration page</a></p>
			</c:when>
		</c:when>
		<c:otherwise>
			<p>Welcome, anonymous user.</p>

			<p><a href="<s:url action="goToSearch" />">Search words</a></p>
			<p><a href="<s:url action="goToRegister" />">Register</a></p>
			<p><a href="<s:url action="goToLogin" />">Register</a></p>
		</c:otherwise>
	</c:choose>

	<p><a href="<s:url action="index" />">Start</a></p>
</body>
</html>