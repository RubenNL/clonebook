function addPost(repliedTo,id,imgLocation,name,text) {
	let node = $('#postTemplate').contents("article").clone();
	node.attr("id",id);
	node.find('.profilePicture').attr('src',imgLocation);
	node.find('.name').text(name);
	node.find('.text').text(text);
	$(repliedTo?'#'+repliedTo+' > .subReplies':'#posts').append(node);
}
var picker=new EmojiButton();
var lastSelectedField;
picker.on('emoji',emoji=>lastSelectedField.value+=emoji);
$(document).on('click','.messageEmoticon',(event)=>{
	picker.togglePicker(event.target);
	lastSelectedField=$(event.target).parent().find('.messageBox')[0];
});
$(document).on('click','.vote',event=>{
	sendPost('/rest/post/'+$(event.target).parent().parent().attr('id')+'/vote',{vote:$(event.target).attr('action')});
});
$(document).on('click','.replyButton',(event)=>{
	if($(event.target).parent().parent().find('.messageForm').length>0) $(event.target).parent().parent().find('.messageForm').toggle();
	else {
		clone=$('#messageBoxTemplate').contents("div").clone();
		clone.find('[type="hidden"]').val($(event.target).parent().parent().attr('id'));
		$(event.target).parent().after(clone);
	}
});
$(document).on('submit','.messageForm',event=>{
	event.preventDefault();
	console.log(event);
	sendPost('/rest/post',event.target);
});
addPost(null,"1","icon.png","Voornaam Achternaam","Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.")
addPost("1","2","icon.png","Voornaam Achternaam","Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.")
addPost("2","3","icon.png","Voornaam Achternaam","Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.")
addPost("3","4","icon.png","Voornaam Achternaam","Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.")
addPost("4","5","icon.png","Voornaam Achternaam","Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.")
addPost("5","6","icon.png","Voornaam Achternaam","Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.")