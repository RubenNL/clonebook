class WS {
	constructor(userId) {
		this.userId=userId;
		this.messageListeners=[];
	}
	async connect() {
		this.ws = new WebSocket("wss://clonebook.rubend.nl/ws/" + (await Utils.sendPost("user/" + this.userId + '/ws')).code);
		this.ws.onmessage=(message)=>{
			this.messageListeners.forEach(func=>func(message.data));
		}
	}
	send(message) {
		this.ws.send(JSON.stringify(message));
	}
	close() {
		this.ws.close();
	}
	onMessage(f) {
		this.messageListeners.push(f);
	}
}
