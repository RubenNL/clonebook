let picker=new EmojiButton();
function showPageHeader(pageId) {
	return Page.getPage(pageId).then(page=>{
		$('#leden').html('');
		$('#forbidden').hide();
		$('#notFound').hide();
		setTimeout(showLeden);
		return page;
	},message=>{
		$('#leden').html('');
		if(message==404) {
			$('#pageHeader').hide();
			$('#notFound').show();
		}
		if(message.id) {
			$('#forbidden').show();
			$('#forbiddenBlocked').hide();
			$('#askPermission').hide();
			$('#forbiddenAlreadyAsked').hide();
			if(message.blocked) $('#forbiddenBlocked').show();
			else if(message.request) $('#forbiddenAlreadyAsked').show();
			else $('#askPermission').show();
			message.last10Posts=[];
			return Page.fromRaw(message);
		} else throw message;
	}).then(page=>{
		currentPage=page;
		if(page.isAdmin()) {
			$('#viewBanned').show();
			$('#pageSettings').show();
			$('#leden').addClass("admin");
			showBannedUsers();
		} else {
			$('#viewBanned').hide();
			$('#pageSettings').hide();
			$('#leden').removeClass("admin");
		}
		$('#pageHeader > span').show();
		$('#pageHeader').show();
		$('#notFound').hide();
		$('#pageImageUpload').hide();
		$('#pageHeader > span').text(page.name);
		if(page.logo==null) url='icon.svg';
		else url='/rest/media/'+page.logo;
		$('#pageHeader > img').attr('src',url);
		$('#page').show();
		return page;
	})
}
$('#viewBanned').on('click',()=>{
	$('#blockedUserDialog')[0].showModal();
})
$('#pageSettings').on('click',()=>{
	if(!currentPage.isAdmin()) return;
	$('#pageHeader > span').toggle();
	$('#pageName').val(currentPage.name);
	$('#pageImageUpload').toggle();
	$('#pageIcon').toggle();
})
function showBannedUser(lid) {
	let node = $('#lidTemplate').contents("div").clone();
	node.attr("userId",lid.id);
	console.log(lid);
	if(lid.profilePicture) node.find('.lidProfilePicture').attr('src','/rest/media/'+lid.profilePicture);
	node.find('.name').text(lid.name);
	$('#bannedList').append(node);
}

function showBannedUsers() {
	$('#bannedList').html('');
	currentPage.getBanned().then(users=>{
		users.forEach(showBannedUser);
	})
}
function newPage() {
	const name=prompt("welke naam?");
	if(name=="") return;
	if(name==null) return;
	Page.new(name).then(showPage);
}
/*$(document).on('keydown','#pageName',event=>{
	if(event.originalEvent.key=="Enter") currentPage.setName($('#pageName').val());
});*/
function showPage(pageId) {
	showPageHeader(pageId).then(page=>{
		autoChanged();
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
	$('#leden').html('');
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
	currentPage.setName($('#pageName').val());
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
function showMorePosts() {
	currentPage.before($('#posts').children().last().find('.dateHoverEvent').attr('date')).then(posts=>{
		posts.forEach(post=>{
			addPost(post,false);
		})
	}).catch(()=>{});
}
$('#nextPosts').on('click',showMorePosts);
$(document).on('scroll',event=>{
	console.log('scroll!');
	const element=$('body')[0];
	if(element.scrollTop==element.scrollTopMax) showMorePosts();
});
$(document).on('click','.kick',event=>{
	User.getUser($(event.currentTarget).parent().parent().attr('userid')).then(user=>{
		currentPage.kick(user).then(()=>{
			$(event.currentTarget).remove();
		})
	})
})
$(document).on('click','.ban',event=>{
	User.getUser($(event.currentTarget).parent().parent().attr('userid')).then(user=>{
		currentPage.ban(user).then(()=>{
			$(event.currentTarget).remove();
			showBannedUsers();
		})
	})
})
$(document).on('click','.unbanAdd',(event)=>{
	User.getUser($(event.currentTarget).parent().parent().attr('userid')).then(user=>{
		currentPage.acceptLid(user).then(()=>{
			$(event.currentTarget).remove();
		}).then(showLeden);
	})
});