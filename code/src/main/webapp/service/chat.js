class Chat {
	constructor(id,otherUser) {
		this.id = id;
		this.otherUser = otherUser;
		this.messages= [];
		this.messageListeners = [];
		this.allLoaded=false;
		this.firstLoaded=false;
		WS.onMessage(message=>(this.messageReceived(message)));
	}
	static create(otherUser) {
		return Utils.sendPost('chat',generateFormData({user:otherUser})).then(response=>{
			return Chat.getChat(response.id);
		})
	}
	static getChat(id) {
		return Utils.sendGet("chat/"+id).then(Chat.fromRaw);
	}
	static fromRaw(raw) {
		raw.users=raw.users.filter(user=>user.id!==getLoggedInId());
		return new Chat(raw.id,User.fromRaw(raw.users[0]));
	}
	static getAll() {
		return Utils.sendGet("chat").then(chats=>{
			return chats.map(Chat.fromRaw);
		})
	}
	send(message) {
		//return Utils.sendPost('chat/'+this.id,generateFormData({message:message}))
		WS.send({id:this.id,message:message,type:"chat"});
	}
	onMessage(func) {
		this.messageListeners.push(func);
	}
	messageReceived(message) {
		this.messageListeners.forEach(func=>func(message));
	}
	before(date) {
		this.firstLoaded=true;
		return Utils.sendGet('chat/'+this.id+'/'+date)
			.then(chats=>{
				this.allLoaded=chats.length<10;
				chats.forEach(chat=>{
					this.messages.unshift(chat)
				});
				return chats;
			});
	}
}