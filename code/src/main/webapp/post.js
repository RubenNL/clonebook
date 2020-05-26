var picker=new EmojiButton();
var lastSelectedField;
var currentPage;
function addPost(repliedTo,id,imgLocation,user,text,mediaList) {
	let node = $('#postTemplate').contents("article").clone();
	node.attr("id",id);
	node.find('.profilePicture').attr('src',user.profilepicture);
	node.find('.name').text(user.name);
	node.find('.name').attr('user',user.id);
	node.find('.text').text(text);
	mediaList.forEach(media=>{
		node.find('.media').append('<a href="/rest/file/'+media.id+'"><img class="mediaImage" src="/rest/file/'+media.id+'"></a>');
	});
	$(repliedTo?'#'+repliedTo+' > .subReplies':'#posts').append(node);
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
	sendPost('/rest/post/'+$(event.target).parent().parent().attr('id')+'/vote',{vote:$(event.target).attr('action')});
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
		return sendPost('/rest/post',event.target);
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
function loadPage(id) {
	sendGet("/rest/page/"+id).then(page=>{
		$('#pageHeader > h1').text(page.name);
		$('#posts').html('');
		page.last10Posts.forEach(post=>addPost(null,post.id,null,post.user,post.text,post.media));
		currentPage=page.id;
	}).catch(page=>{
		$('#pageHeader > h1').text(page.name);
		$('#posts').html('Geen toegang tot deze pagina.');
		currentPage=page.id;
	})
}
addPost(null,"1","icon.png",{name:"Voornaam Achternaam",profilepicture:"icon.png",id:"1234"},"Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.")
addPost("1","2","icon.png",{name:"Voornaam Achternaam",profilepicture:"icon.png",id:"1234"},"Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.")
addPost("2","3","icon.png",{name:"Voornaam Achternaam",profilepicture:"icon.png",id:"1234"},"Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.")
addPost("3","4","icon.png",{name:"Voornaam Achternaam",profilepicture:"icon.png",id:"1234"},"Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.")
addPost("4","5","icon.png",{name:"Voornaam Achternaam",profilepicture:"icon.png",id:"1234"},"Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.")
addPost("5","6","icon.png",{name:"Voornaam Achternaam",profilepicture:"icon.png",id:"1234"},"Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.")