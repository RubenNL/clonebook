let picker=new EmojiButton();
function showPageHeader(pageId) {
	return Page.getPage(pageId).then(page=>{
		$('#forbidden').hide();
		setTimeout(showLeden,1000);
		return page;
	},message=>{
		if(message.id) {
			$('#forbidden').show();
			message.last10Posts=[];
			return Page.fromRaw(message);
		} else throw message;
	}).then(page=>{
		currentPage=page;
		$('#pageHeader > h1').text(page.name);
		$('#pageHeader > img').attr('src',page.logo);
		$('#page').show();
		return page;
	})
}
function showPage(pageId) {
	showPageHeader(pageId).then(page=>{
		window.location.hash='#page='+page.id;
		$('#posts').html('');
		page.last10Posts.forEach(post=>addPost(post));
	})
}
function showLid(lid) {
	let node = $('#lidTemplate').contents("div").clone();
	node.attr("userId",lid.id);
	node.find('.lidProfilePicture').attr('src',lid.profilepicture);
	node.find('.name').text(lid.name);
	$('#leden').append(node);
}
function showLeden() {
	currentPage.getLeden().then(leden=>{
		leden.forEach(showLid);
	})
}
$('#newPost').on('click', () =>{
	if($('#page').children('.messageDiv').length) $('#page').children('.messageDiv').toggle();
	else {
		clone=$('#messageBoxTemplate').contents("div").clone();
		clone.insertBefore('#posts');
	}
});
picker.on('emoji',emoji=>lastSelectedField.value+=emoji);
$(document).on('click','.messageEmoticon',(event)=>{
	picker.togglePicker(event.target);
	lastSelectedField=$(event.target).parent().find('.messageBox')[0];
});
$('#askPermission').on('click',()=>{
	currentPage.askPermissions().then(()=>{
		alert('toegang gevraagd.');
	})
})