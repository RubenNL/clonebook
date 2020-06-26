let ws;
let chats={};
let chatUsers={};
function showChat(chat) {
	chats[chat.id]=chat;
	const chatId=chat.id;
	let button=$('#chatHeaders > button[chatId="'+chatId+'"]');
	if(button.length>0) {
		button.trigger('click');
		return;
	}
	const name=chat.otherUser.name;
	chatUsers[chat.otherUser.id]=chatId;
	let node = $('#chatTemplate').contents("div").clone();
	node.attr("chatId",chatId);
	node.attr("name",name);
	chat.onMessage(message=>{
		message=JSON.parse(message);
		const chatDiv=$('.chat[chatId="'+chatId+'"]');
		const scrollBack=chatDiv[0].scrollTopMax==chatDiv[0].scrollTop;
		chatDiv.find('.chatMessages').append('<li class="chatMessage '+(message.from?"him":"me")+'">'+message.message+'</li>');
		if(scrollBack) chatDiv[0].scrollTop=chatDiv[0].scrollTopMax;
	})
	$('#chats').append(node);
	const chatDiv=$('.chat[chatId="'+chatId+'"]');
	chatDiv.on('scroll',event=>{
		console.log('scroll!');
		if(event.currentTarget.scrollTop==0) displayMoreChats($(event.currentTarget).attr('chatId'));
	});
	chat.messages.reverse().forEach(message=>{
		chatDiv.find('.chatMessages').append('<li class="chatMessage '+(message.userId==getLoggedInId()?"me":"him")+'">'+message.message+'</li>');
	})
	$('#chatHeaders').prepend('<button class="chatTab" chatId="'+chatId+'">'+name.substring(0,10)+'</button>');
	chatDiv[0].scrollTop=chatDiv[0].scrollTopMax;
	if(chat.allLoaded) $('#getMoreChats').hide();
}
function showChats() {
	Chat.getAll().then(chatsReceived=>{
		chatsReceived.forEach(showChat);
	});
	WS.onClose(()=>{
		$('#chatPopup').css('background-color','red');
		$('#chatPopup').text("Verbinding met chatserver verbroken.\nVervers de pagina.");
	})
}
function sendChat(event) {
	const box=$(event.currentTarget).parent().find('input');
	let val=box.val();
	val=val.trim();
	if(val=="") return;
	box.val("");
	const chatId=box.parent().parent().attr('chatId');
	chats[chatId].send(val);
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
	const chatId=target.attr('chatId');
	$('#chats > [chatId="'+chatId+'"]').show();
	if(!chats[chatId].firstLoaded) displayMoreChats(chatId);
});
function toggleChat() {
	$("#chatPopup").toggle();
	$('#showChat').toggle();
}
$('#closeChat').on('click',toggleChat);
$('#showChat').on('click',toggleChat);
function displayMoreChats(chatId) {
	console.log('moreChats!');
	const chatDiv=$('.chat[chatId="'+chatId+'"]');
	const chat=chats[chatId];
	if(chat.allLoaded) return;
	const scrollBottom=chatDiv[0].scrollTopMax-chatDiv[0].scrollTop;
	chat.before(chat.messages[0]?chat.messages[0].date:253402128000000).then(messages=>{
		messages.forEach(message=> {
			chatDiv.find('.chatMessages').prepend('<li class="chatMessage '+(message.userId==getLoggedInId()?"me":"him")+'">'+message.message+'</li>');
		})
	}).then(()=>chatDiv[0].scrollTop=chatDiv[0].scrollTopMax-scrollBottom)
		.then(()=>{
			if(chat.allLoaded) $('#getMoreChats').hide();
		});
}
$(document).on('click','.getMoreChats',(event)=>{
	const chatId=$(event.currentTarget).parent().parent().attr('chatId');
	displayMoreChats(chatId);
});

$(document).on('click','.lid:not(.loggedInUser)',(event)=>{
	const userId=$(event.currentTarget).attr('userId');
	const chatId=chatUsers[userId];
	if(chatId) showChat(chats[chatId]);
	else if(!confirm("weet u zeker dat u een chat wilt starten met "+$(event.currentTarget).find('.name').text()+'?')) return;
	else {
		Chat.create(userId).then(showChat).then(()=>{
			$('#chatHeaders > button[chatId="'+chatId+'"]').trigger('click');
		});
	}
});
$(()=>{
	$('#chatHeaders').width($('#noscroll').width());
	$('#noscroll').remove();
})