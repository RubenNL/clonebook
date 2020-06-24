class WS {
	static ws;
	static messageListeners=[];
	static closeListeners=[];
	static async connect() {
		WS.ws=new WebSocket("wss://clonebook.rubend.nl/ws/" + (await Utils.sendPost("user/" + getLoggedInId() + '/ws')).code);
		WS.ws.onmessage=(message)=>{
			WS.messageListeners.forEach(func=>func(message.data));
		}
		WS.ws.onclose=()=>{
			WS.closeListeners.forEach(func=>func());
		}
	}
	static send(message) {
		WS.ws.send(JSON.stringify(message));
	}
	static close() {
		WS.ws.close();
	}
	static onMessage(f) {
		WS.messageListeners.push(f);
	}
	static onClose(f) {
		WS.closeListeners.push(f);
	}
}
