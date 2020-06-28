function addMelding(melding) {
	let node = $('#meldingTemplate').contents("div").clone();
	if(melding.picture) node.find('.meldingImage').attr('src',Media.fromId(melding.picture).getUrl());
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
	Promise.all([Page.getPage(pageId),User.getUser(userId)]).then(vars=>{
		if(action=="accept") {
			return vars[0].acceptLid(vars[1]).then(showLeden);
		}
		else if(action=="deny") return vars[0].kick(vars[1]);
		else if(action=="block") return vars[0].ban(vars[1]);
	}).then(()=>{
		$(event.currentTarget).parent().remove();
	})
});
function getNotifications(user) {
	user.getLidAanvragenOpPaginas().then(aanvragen=>{
		aanvragen.forEach(addMelding);
	})
}
setTimeout(()=>{
	LoginSignup.getLoggedinUser().then(getNotifications);
},1000)