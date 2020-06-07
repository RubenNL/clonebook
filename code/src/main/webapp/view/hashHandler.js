if(window.location.hash && window.location.hash.split('=')[1]) {
	const parts = window.location.hash.split('=');
	if(parts[1]) {
		parts[0]=parts[0].split('#')[1];
		if(parts[0]==="newAccount") {
			$("#newPasswordCode").val(parts[1]);
			$('#newPasswordScreen').show();
			$('#loginOptions').hide();
			$('#notLoggedIn').show();
		} else if(parts[0]=="post") {
			showSinglePost(parts[1]);
		} else if(parts[0]=="page") {
			Page.getPage(parts[1]).then(showPage).catch(message=>{
				if(message.id) {
					$('#pageHeader > h1').text(message.name);
					$('#posts').html('Geen toegang.');
					$('#page').show();
				}
			});
		} else if(parts[0]=="user") {
			User.getUser(parts[1]).then(user=>Page.getPage(user.privatePageId)).then(showPage,message=>{
				if(message.id) {
					$('#pageHeader > h1').text(message.name);
					$('#posts').html('Geen toegang.');
					$('#page').show();
				} else throw message;
			})
		}
	}
} else load();