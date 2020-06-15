let ws;
function connectWs() {
	ws=(new WS(getLoggedInId())).connect();
	ws.onMessage(message)
}