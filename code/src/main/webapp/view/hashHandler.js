function handleHash() {
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
				showPage(parts[1]);
			} else if(parts[0]=="user") {
				User.getUser(parts[1]).then(user=>showPage(user.privatePageId));
			}
		}
	} else {
		LoginSignup.getLoggedinUser().then(user=>showPage(user.privatePageId)).catch(()=>{});//silent catch, ik weet niet wat ik hier anders van moet maken.
	}
}
handleHash();
load();