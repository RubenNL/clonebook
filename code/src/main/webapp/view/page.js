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
			if(message.request) {
				$('#forbiddenAlreadyAsked').show();
				$('#askPermission').hide();
			} else {
				$('#forbiddenAlreadyAsked').hide();
				$('#askPermission').show();
			}
			message.last10Posts=[];
			return Page.fromRaw(message);
		} else throw message;
	}).then(page=>{
		if(page.isAdmin()) $('#pageSettings').show();
		else $('#pageSettings').hide();
		$('#pageHeader > span').show();
		$('#pageName').hide();
		$('#pageHeader').show();
		$('#notFound').hide();
		$('#pageImageUpload').hide();
		currentPage=page;
		$('#pageHeader > span').text(page.name);
		if(page.logo==null) url='icon.svg';
		else url='/rest/media/'+page.logo;
		$('#pageHeader > img').attr('src',url);
		$('#page').show();
		return page;
	})
}
$('#pageSettings').on('click',()=>{
	if(!currentPage.isAdmin()) return;
	$('#pageHeader > span').toggle();
	$('#pageName').val(currentPage.name);
	$('#pageName').toggle();
	$('#pageImageUpload').toggle();
	$('#pageIcon').toggle();
})
function newPage() {
	const name=prompt("welke naam?");
	if(name=="") return;
	if(name==null) return;
	Page.new(name).then(showPage);
}
$(document).on('keydown','#pageName',event=>{
	if(event.originalEvent.key=="Enter") currentPage.setName($('#pageName').val());
});
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
	}).then(()=>$('.asideTab[tab="leden"]').trigger('click'))
}
$('#newPost').on('click', () =>{
	if($('#page').children('.messageDiv').length) $('#page').children('.messageDiv').toggle();
	else {
		$('#messageBoxTemplate').contents("div").clone().insertBefore('#posts');
	}
});
picker.on('emoji',emoji=>lastSelectedField.value+=emoji);
$(document).on('click','.messageEmoticon',(event)=>{
	picker.togglePicker(event.target);
	lastSelectedField=$(event.target).parent().find('.messageBox')[0];
});
$('#askPermission').on('click',()=>{
	currentPage.askPermissions().then(()=>showPageHeader(currentPage.id))
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
