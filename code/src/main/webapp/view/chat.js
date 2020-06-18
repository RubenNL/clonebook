let ws;
let chats={};
let userChats={};
async function connectWs() {
	$('#page').hide();
	$('body').attr('style','display:unset');
	ws = (new WS(getLoggedInId()));
	await ws.connect();
	ws.onMessage(onMessage);
}
function showChat(chat) {
	const userid=chat.otherUser.id;
	let button=$('#chatHeaders > button[userid="'+userid+'"]');
	if(button.length>0) {
		button.trigger('click');
		return;
	}
	const name=chat.otherUser.name;
	let node = $('#chatTemplate').contents("div").clone();
	node.attr("userid",userid);
	node.attr("name",name);
	chat.onMessage(message=>{
		message=JSON.parse(message);
		const userid=message.from||message.dest;
		const chatDiv=$('.chat[userid="'+userid+'"]');
		chatDiv.children('.chatMessages').append('<div>'+(message.from?"van":"naar")+': '+message.message+'</div>');
	})
	node.find('.getMoreChats').on('click',()=>{
		const chatDiv=$('.chat[userid="'+userid+'"]');
		const chat=chats[userChats[userid]];
		chat.before(chat.messages[0].date).then(messages=>{
			messages.forEach(message=> {
				chatDiv.children('.chatMessages').prepend('<div>' + (message.userId == getLoggedInId() ? "naar" : "van") + ': ' + message.message + '</div>');
			})
		});
	})
	$('#chats').append(node);
	const chatDiv=$('.chat[userid="'+userid+'"]');
	chat.messages.reverse().forEach(message=>{
		chatDiv.children('.chatMessages').append('<div>'+(message.userId==getLoggedInId()?"naar":"van")+': '+message.message+'</div>');
	})
	$('#chatHeaders').append('<button class="chatTab" userid="'+userid+'">'+name+'</button>');
	$('#chatHeaders > button[userid="'+userid+'"]').trigger('click');
}
function showChats() {
	Chat.getAll().then(chatsReceived=>{
		chatsReceived.forEach(chat=>{
			chats[chat.id]=chat;
			userChats[chat.otherUser.id]=chat.id;
			console.log(chat);
			showChat(chat);
		})
	})
}
function chat(userId,message) {
	chats[userChats[userId]].send(message);
}
$(document).on('click','.sendMessage',event=>{
	chat($(event.currentTarget).parent().attr('userid'),$(event.currentTarget).parent().find('input').val());
});
$(document).on('click','.lid',event=>{

});
$(document).on('click','.chatTab',event=>{
	const target=$(event.currentTarget);
	$('.chatTab').removeClass("active");
	target.addClass('active');
	$('#chats > *').hide();
	$('#chats > [userid="'+target.attr('userid')+'"]').show();
});