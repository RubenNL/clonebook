var picker=new EmojiButton();
var lastSelectedField;
var currentPage;
function addPost(post) {
	let node = $('#postTemplate').contents("article").clone();
	node.attr("id",post.id);
	node.find('.profilePicture').attr('src',post.user.profilepicture);
	node.find('.name').text(post.user.name);
	node.find('.name').attr('user',post.user.id);
	node.find('.text').text(post.text);
	post.media.forEach(media=>{
		node.find('.media').append('<a href="/rest/media/'+media.id+'"><img class="mediaImage" src="/rest/media/'+media.id+'"></a>');
	});
	$(post.repliedTo?'#'+post.repliedTo+' > .subReplies':'#posts').append(node);
}
picker.on('emoji',emoji=>lastSelectedField.value+=emoji);
$(document).on('click','.messageEmoticon',(event)=>{
	picker.togglePicker(event.target);
	lastSelectedField=$(event.target).parent().find('.messageBox')[0];
});
$(document).on('click','.name',event=>{
	event.preventDefault();
	console.log('user:',$(event.target).attr('user'));
});
$(document).on('click','.vote',event=>{
	Utils.sendPost('/rest/post/'+$(event.target).parent().parent().attr('id')+'/vote',{vote:$(event.target).attr('action')});
});
$(document).on('click','.replyButton',(event)=>{
	if($(event.target).parent().parent().find('.messageForm').length>0) $(event.target).parent().parent().find('.messageForm').toggle();
	else {
		clone=$('#messageBoxTemplate').contents("div").clone();
		clone.find('[name="repliedTo"]').val($(event.target).parent().parent().attr('id'));
		clone.find('[name="pageId"]').val(currentPage);
		$(event.target).parent().after(clone);
	}
});
$(document).on('click','.newFile',event=>{
	$(event.target).parent().find('.files').append('<input type="file" class="fileUpload">');
});
$(document).on('submit','.messageForm',event=>{
	event.preventDefault();
	Promise.all($(event.target).parent().parent().find('.files').children().map((id,file)=>{
		return sendFile(file.files[0])
	})).then(fileList=>{
		fileList.forEach(obj=>$(event.target).parent().parent().find('.messageForm').append('<input type="hidden" name="file" value="' + obj.id + '">'))
	}).then(()=>{
		$(event.target).find('[name="pageId"]').val(currentPage);
		return Utils.sendPost('/rest/post',event.target);
	}).then(post=>{
		$(event.target).parent().remove();
		alert('bericht gepost!');
		loadPage(currentPage);
	});
});
$('#newPost').on('click', () =>{
	if($('#page').children('.messageDiv').length) $('#page').children('.messageDiv').toggle();
	else {
		clone=$('#messageBoxTemplate').contents("div").clone();
		clone.insertBefore('#posts');
	}
});
function showPage(page) {
	$('#pageHeader > h1').text(page.name);
	$('#posts').html('');
	page.last10Posts.forEach(post=>addPost({repliedTo:null,id:post.id,user:post.user,text:post.text,media:post.media}));
	currentPage=page.id;
}