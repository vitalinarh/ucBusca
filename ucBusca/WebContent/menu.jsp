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
			<h1>Welcome, ${session.username}. </h1>

			<p><a href="<s:url action="search" />">Search words</a></p>
			<p><a href="<s:url action="searchPages" />">Search pages connected to specified page</a></p>
			<p><a href="<s:url action="searchHistory" />">Search history</a></p>

			<c:choose>
				<c:when test="${session.isAdmin == true}">
					<p><a href="<s:url action="indexURL" />">Index URL</a></p>
					<p><a href="<s:url action="giveAdminPrivilege" />">Give a user admin privileges</a></p>
					<p><a href="<s:url action="adminPage" />">Administration page</a></p>
				</c:when>
			</c:choose>

			<p><a href="<s:url action="logout" />">Logout</a></p>

		</c:when>
		<c:otherwise>
			<h1>Welcome, anonymous user.</h1>

			<p><a href="<s:url action="search" />">Search words</a></p>
			<p><a href="<s:url action="register" />">Register</a></p>
			<p><a href="<s:url action="login" />">Login</a></p>
            <p><a href="<s:url action="face_auth" />">Authenticate with Facebook</a></p>
		</c:otherwise>
	</c:choose>

</body>
</html>