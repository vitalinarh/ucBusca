<%@ taglib prefix="s" uri="/struts-tags"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
	<title>ucBusca</title>

	<script type="text/javascript">

		var websocket = null;

		window.onload = function() { // URI = ws://10.16.0.165:8080/WebSocket/ws
			connect('wss://' + window.location.host + '/ucBuscaWAR/wss');
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

			if(message[0] == "s" && message[1] == "t" && message[2] == "a" && message[3] == "i"){
				message = message.substring(11);
				var messageArray = message.split("!");

				for(var i = 0 ; i<messageArray.length ; i++)
					{
						console.log(messageArray[i]);
						writeToAdminStatistics(messageArray[i].data);}

			}
			else
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

		function writeToAdminStatistics(text) {
			var adminStatistics = document.getElementById('adminStatistics');
			adminStatistics.innerText = "";

			var line = document.createElement('p');
			line.style.wordWrap = 'break-word';
			line.innerHTML = text;
			adminStatistics.appendChild(line);
			adminStatistics.scrollTop = adminStatistics.scrollHeight;
		}

	</script>


</head>
<body>

	<p><a href="<s:url action="menuFromAdminPage" />">Menu</a></p>
	<h1>Admin Page</h1>
	<div id="container"><div id="notificationBoard"></div></div>
	<div id="container2"><div id="adminStatistics"></div></div>




</body>
</html>