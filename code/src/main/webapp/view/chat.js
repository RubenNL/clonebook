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
		const scrollBack=chatDiv[0].scrollTopMax==chatDiv[0].scrollTop;
		chatDiv.find('.chatMessages').append('<li class="chatMessage '+(message.from?"him":"me")+'">'+message.message+'</li>');
		if(scrollBack) chatDiv[0].scrollTop=chatDiv[0].scrollTopMax;
	})
	node.find('.getMoreChats').on('click',()=>{
		const chatDiv=$('.chat[userid="'+userid+'"]');
		const chat=chats[userChats[userid]];
		chat.before(chat.messages[0].date).then(messages=>{
			messages.forEach(message=> {
				chatDiv.find('.chatMessages').prepend('<li class="chatMessage '+(message.userId==getLoggedInId()?"me":"him")+'">'+message.message+'</li>');
			})
		});
	})
	$('#chats').append(node);
	const chatDiv=$('.chat[userid="'+userid+'"]');
	chat.messages.reverse().forEach(message=>{
		chatDiv.find('.chatMessages').append('<li class="chatMessage '+(message.userId==getLoggedInId()?"me":"him")+'">'+message.message+'</li>');
	})
	$('#chatHeaders').append('<button class="chatTab" userid="'+userid+'">'+name+'</button>');
	$('#chatHeaders > button[userid="'+userid+'"]').trigger('click');
	chatDiv[0].scrollTop=chatDiv[0].scrollTopMax;
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
function sendChat(event) {
	const box=$(event.currentTarget).parent().find('input');
	let val=box.val();
	val=val.trim();
	if(val=="") return;
	const userId=box.parent().parent().attr('userid');
	chats[userChats[userId]].send(val);
}
$(document).on('click','.sendMessage',sendChat);
$(document).on('keydown','.chatBox',event=>{
	if(event.originalEvent.key=="Enter") sendChat(event);
});
$(document).on('click','.chatTab',event=>{
	const target=$(event.currentTarget);
	$('.chatTab').removeClass("active");
	target.addClass('active');
	$('#chats > *').hide();
	$('#chats > [userid="'+target.attr('userid')+'"]').show();
});