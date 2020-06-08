function addMelding(melding) {
	let node = $('#meldingTemplate').contents("div").clone();
	node.find('.meldingImage').attr('src','/rest/media/'+melding.picture);
	node.find('.meldingUserName').text(melding.userName);
	node.find('.meldingUserName').attr('user',melding.userId);
	node.find('.meldingPageName').text(melding.pageName);
	node.find('.meldingPageName').attr('page',melding.pageId);
	$('#meldingen').append(node);
}
$(document).on('click','.meldingButton',event=>{
	const target=$(event.target);
	const userId=target.parent().find('.meldingUserName').attr('user');
	const pageId=target.parent().find('.meldingPageName').attr('page');
	const action=target.attr('action');
	console.log(action,userId,pageId);
	Promise.all([Page.getPage(pageId),User.getUser(userId)]).then(vars=>{
		if(action=="accept") return vars[0].acceptLid(vars[1]);
		else return vars[0].denyLid(vars[1]);
	}).then(output=>{
		console.log(output);
	})
});
function getNotifications(user) {
	user.getLidAanvragenOpPaginas().then(aanvragen=>{
		console.log(aanvragen);
		aanvragen.forEach(addMelding);
	})
}
setTimeout(()=>{
	LoginSignup.getLoggedinUser().then(getNotifications);
},1000)