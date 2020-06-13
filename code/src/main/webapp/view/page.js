let picker=new EmojiButton();
function showPageHeader(pageId) {
	return Page.getPage(pageId).then(page=>{
		$('#leden').html('');
		$('#forbidden').hide();
		$('#notFound').hide();
		setTimeout(showLeden,1000);
		return page;
	},message=>{
		$('#leden').html('');
		if(message==404) {
			$('#pageHeader').hide();
			$('#notFound').show();
		}
		if(message.id) {
			$('#forbidden').show();
			message.last10Posts=[];
			return Page.fromRaw(message);
		} else throw message;
	}).then(page=>{
		$('#pageHeader').show();
		$('#notFound').hide();
		$('#pageImageUpload').hide();
		currentPage=page;
		$('#pageHeader > h1').text(page.name);
		$('#pageHeader > img').attr('src','/rest/media/'+page.logo);
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
	console.log(lid);
	if(lid.profilePicture) node.find('.lidProfilePicture').attr('src','/rest/media/'+lid.profilePicture);
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
});

$(document).on('click','#pageHeader > img',event=>{
	if(currentPage.isAdmin()) $('#pageImageUpload').toggle();
});
$(document).on('submit','#pageImageUpload',event=>{
	event.preventDefault();
	const file=$(event.target).find('input[type="file"]')[0];
	if(!file.files[0]) return;
	Media.create(file.files[0]).catch(message=>{
		if(message===415) alert("bestandstype niet toegestaan.");
		else if(message==413) alert('bestand te groot');
		throw new Error("FileUploadException");
	}).then(icon=>{
		return currentPage.setIcon(icon);
	}).then(()=>{
		alert('afbeelding geupload!');
	});
});
