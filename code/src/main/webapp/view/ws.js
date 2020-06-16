let ws;
async function connectWs() {
	$('#page').hide();
	$('#chatId').text(getLoggedInId());
	ws = (new WS(getLoggedInId()));
	await ws.connect();
	ws.onMessage(onMessage)
}
function chat(userId,message) {
	ws.send({dest:userId,message:message,type:"chat"})
}
function onMessage(message) {
	message=JSON.parse(message);
	if(message.type!=="chat") return;
	receivedFromUser(message.from,message.message);

}
function receivedFromUser(userId,message) {
	alert('received from '+userId+':'+message);
}
function sendMessage() {
	chat($('#chatDest').val(),$('#chatMessage').val());
}