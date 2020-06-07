let picker=new EmojiButton();
let lastSelectedField;
let currentPage;
function addPost(post,prepend) {
	let node = $('#postTemplate').contents("article").clone();
	node.attr("id",post.id);
	node.find('.profilePicture').attr('src',post.user.profilepicture);
	node.find('.name').text(post.user.name);
	node.find('.name').attr('user',post.user.id);
	node.find('.text').text(post.text);
	post.media.forEach(media=>{
		node.find('.media').append('<a href="'+media.getUrl()+'"><img class="mediaImage" alt="'+post.user.name+" media object"+'" src="'+media.getUrl()+'"></a>');
	});
	let field=$(post.repliedTo?'#'+post.repliedTo+' > .subReplies':'#posts');
	if(prepend) field.prepend(node);
	else field.append(node);
	post.children.forEach(child=>{
		addPost(child,prepend);
	})
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
	Utils.sendPost('post/'+$(event.target).parent().parent().attr('id')+'/vote',generateFormData({vote:$(event.target).attr('action')}));
});
$(document).on('click','.replyButton',(event)=>{
	if($(event.target).parent().parent().find('.messageForm').length>0) $(event.target).parent().parent().find('.messageForm').toggle();
	else {
		clone=$('#messageBoxTemplate').contents("div").clone();
		clone.find('[name="repliedTo"]').val($(event.target).parent().parent().attr('id'));
		clone.find('[name="pageId"]').val(currentPage.id);
		$(event.target).parent().after(clone);
	}
});
$(document).on('click','.newFile',event=>{
	$(event.target).parent().find('.files').append('<input type="file" accept="image/*" class="fileUpload">');
});
$(document).on('submit','.messageForm',event=>{
	event.preventDefault();
	Promise.all($(event.target).find('.files').find('[type="file"]').map((id,file)=>{
		if(!file.files[0]) return Promise.resolve();
		return Media.create(file.files[0]).then(obj=>{
			$(file).attr('type','hidden');
			$(file).attr("name","file");
			$(file).val(obj.id);
		});
	})).catch(message=>{
		if(message===415) alert("bestandstype niet toegestaan.");
		throw new Error("FileUploadException");
	}).then(()=>{
		$(event.target).find('[name="pageId"]').val(currentPage.id);
		return Utils.sendPost('post',generateFormData(event.target));
	}).then(post=>{
		$(event.target).parent().remove();
		alert('bericht gepost!');
		addPost(post,true);
	});
});
$('#newPost').on('click', () =>{
	if($('#page').children('.messageDiv').length) $('#page').children('.messageDiv').toggle();
	else {
		clone=$('#messageBoxTemplate').contents("div").clone();
		clone.insertBefore('#posts');
	}
});
function showSinglePost(postId) {
	Post.getPost(postId).then(post=>{
		$('#posts').html('');
		addPost(post);
		window.location.hash='#post='+post.id;
		return post.getPage();
	},message=>{
		if(message.pageId) {
			$('#posts').html("Geen toegang.");
			return Page.getPage(message.pageId);
		} else throw message;
	}).catch(message=>{
			if(message.id) return message; //Geen toegang exception
			else throw message;
		})
		.then(page=>{
			currentPage=page;
			$('#pageHeader > h1').text(page.name);
			$('#page').show();
		})
}
function showPage(page) {
	$('#pageHeader > h1').text(page.name);
	$('#posts').html('');
	page.last10Posts.forEach(post=>addPost(post));
	currentPage=page;
	$("#page").show();
	window.location.hash='#page='+page.id;
}
function logoutPost() {
	$('#posts').html('');
	$('#pageHeader > h1').text("");
	$("#page").hide();
}