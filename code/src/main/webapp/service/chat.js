class Chat {
	constructor(id,otherUser,messages) {
		this.id = id;
		this.otherUser = otherUser;
		this.messages= messages;
		this.messageListeners = [];
		WS.onMessage(message=>(this.messageReceived(message)));
	}
	static create(otherUser) {
		return Utils.sendPost('chat',generateFormData({user:otherUser.id})).then(response=>{
			return new Chat(response.id,otherUser,[]);
		})
	}
	static getChat(id) {
		return Utils.sendGet("chat/"+id).then(Chat.fromRaw);
	}
	static fromRaw(raw) {
		raw.users=raw.users.filter(user=>user.id!==getLoggedInId());
		return new Chat(raw.id,User.fromRaw(raw.users[0]),raw.messages);
	}
	static getAll() {
		return Utils.sendGet("chat").then(chats=>{
			return chats.map(Chat.fromRaw);
		})
	}
	send(message) {
		return Utils.sendPost('chat/'+this.id,generateFormData({message:message}))
	}
	onMessage(func) {
		this.messageListeners.push(func);
	}
	messageReceived(message) {
		this.messageListeners.forEach(func=>func(message));
	}
	before(date) {
		return Utils.sendGet('chat/'+this.id+'/'+date)
			.then(chats=>{
				chats.forEach(chat=>{
					this.messages.unshift(chat)
				});
				return chats;
			});
	}
}