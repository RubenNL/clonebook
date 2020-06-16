let ws;
async function connectWs() {
	$('#page').hide();
	$('body').attr('style','display:unset');
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
	const userid=message.from||message.dest;
	const chatDiv=$('.chat[userid="'+userid+'"]');
	chatDiv.children('.chatMessages').append('<div>'+(message.from?"van":"naar")+': '+message.message+'</div>');

}
function receivedFromUser(userId,message) {

}
function receivedToUser(userId,message) {
	const chatDiv=$('.chat[userid="'+userId+'"]');
	chatDiv.children('.chatMessages').append('<div>naar: '+message+'</div>');
}
$(document).on('click','.sendMessage',event=>{
	chat($(event.currentTarget).parent().attr('userid'),$(event.currentTarget).parent().find('input').val());
});
$(document).on('click','.lid',event=>{
	const userid=$(event.currentTarget).attr("userid");
	let button=$('#chatHeaders > button[userid="'+userid+'"]');
	if(button.length>0) {
		button.trigger('click');
		return;
	}
	const name=$(event.currentTarget).find('.name').text();
	let node = $('#chatTemplate').contents("div").clone();
	node.attr("userid",userid);
	node.attr("name",name);
	$('#chats').append(node);
	$('#chatHeaders').append('<button class="chatTab" userid="'+userid+'">'+name+'</button>');
	$('#chatHeaders > button[userid="'+userid+'"]').trigger('click');
});
$(document).on('click','.chatTab',event=>{
	const target=$(event.currentTarget);
	$('.chatTab').removeClass("active");
	target.addClass('active');
	$('#chats > *').hide();
	$('#chats > [userid="'+target.attr('userid')+'"]').show();
});