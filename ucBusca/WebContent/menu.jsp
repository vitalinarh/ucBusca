<%@ taglib prefix="s" uri="/struts-tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
	<title>ucBusca</title>

	<link rel="stylesheet" type="text/css" href="css/style.css">

	<script type="text/javascript">

		var websocket = null;

		window.onload = function() { // URI = ws://10.16.0.165:8080/WebSocket/ws
			connect('ws://' + window.location.host + '/ucBusca/ws');
		}

		function connect(host) { // connect to the host websocket
			if ('WebSocket' in window)
				websocket = new WebSocket(host);
			else if ('MozWebSocket' in window)
				websocket = new MozWebSocket(host);
			else {
				writeToHistory('Get a real browser which supports WebSocket.');
				return;
			}

			websocket.onopen    = onOpen; // set the 4 event listeners below
			websocket.onclose   = onClose;
			websocket.onmessage = onMessage;
			websocket.onerror   = onError;
		}

		function onOpen(event) {
            websocket.send('${session.userId}');
            //writeToNotificationBoard('Connected to ' + window.location.host + '.');
		}

		function onClose(event) {
			//writeToNotificationBoard('WebSocket closed (code ' + event.code + ').');
		}

		function onMessage(message) { // print the received message
            writeToNotificationBoard(message.data);
		}

		function onError(event) {
            writeToNotificationBoard('WebSocket error.');
		}

		function writeToNotificationBoard(text) {
			var notificationBoard = document.getElementById('notificationBoard');
			var line = document.createElement('p');
			line.style.wordWrap = 'break-word';
			line.innerHTML = text;
            notificationBoard.appendChild(line);
            notificationBoard.scrollTop = notificationBoard.scrollHeight;
		}

	</script>

</head>

<body>

	<c:choose>
		<c:when test="${session.isLogged == true}">

			<c:choose>
				<c:when test="${session.isAdmin == true}">

					<div class="topnav">
						<a class="active" href="<s:url action="menu" />">Home</a>
						<a href="<s:url action="search" />">Search words</a>
						<a href="<s:url action="searchPages"/>">Search pages by ref</a>
						<a href="<s:url action="searchHistory"/>">Search history</a>
						<a href="<s:url action="indexURL"/>">ADMIN: index a url</a>
						<a href="<s:url action="giveAdminPrivilege"/>">ADMIN: Give admin privileges</a>
						<a href="<s:url action="adminPage"/>">ADMIN: Admin Page</a>
						<a href="<s:url action="logout"/>">Logout</a>
					</div>

				</c:when>
				<c:otherwise>

					<div class="topnav">
						<a class="active" href="<s:url action="menu" />">Home</a>
						<a href="<s:url action="searchPages"/>">Search pages by ref</a>
						<a href="<s:url action="searchHistory"/>">Search history</a>
						<a href="<s:url action="logout"/>">Logout</a>
					</div>

				</c:otherwise>
			</c:choose>

			<div id="container"><div id="notificationBoard"></div></div>

			<h1 style="text-align: center">Welcome, ${session.username}. </h1>

		</c:when>
		<c:otherwise>

			<div class="topnav">
				<a class="active" href="<s:url action="menu" />">Home</a>
				<a href="<s:url action="search"/>">Search words</a>
				<a href="<s:url action="register"/>">Register</a>
				<a href="<s:url action="login"/>">Login</a>
                <a href="<s:url action="face_auth" />">Authenticate with Facebook</a>
			</div>

			<h1 style="text-align: center">Welcome, anonymous user.</h1>

		</c:otherwise>
	</c:choose>


    <div class="search">
        <h1>Search</h1>
        <s:form action="search" method="post">
            <s:textfield name="search" />
            <s:submit />
        </s:form>
    </div>

</body>
</html>