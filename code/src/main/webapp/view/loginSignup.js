function passwordRequest() {
	LoginSignup.passwordRequest(generateFormData("#newAccountScreen"))
	.then(()=>{
		alert("Kijk op je e-mail.");
		$("#loginOptions").hide();
	}).catch(error=>{
		console.log(error);
		alert(error)
	});
}
async function renderCaptcha() {
	grecaptcha.render('recaptcha', {
		sitekey: await Utils.sendGet("recaptchaKey")
	})
}
function load() {
	return LoginSignup.getLoggedinUser().catch(message=>{
		$('.afterLogin').hide();
		if(message==403 || message=="niet ingelogd") {
			document.write('<script src="https://www.google.com/recaptcha/api.js?onload=renderCaptcha&render=explicit" type="text/javascript"></script>');
			$('#notLoggedIn').show();
		}
		else {
			console.log(message);
			alert(message);
		}
		return Promise.reject(403);
	}).then(user=> {
		$('.afterLogin').show()
		$('#userMenuName').text(user.name);
		$('#userMenuPicture').attr('src', '/rest/media/' + user.profilePicture);
		setTimeout(()=>{
			WS.connect().then(showChats);
			onLogin(user);
		},1000);
		return user
	})

}
function savePassword() {
	LoginSignup.savePassword(generateFormData("#newPasswordScreen"))
		.then(()=> {
			$('#newPasswordScreen').hide();
			$('#loginOptions').show();
			alert("Wachtwoord ingesteld.");
			autoChanged();
			window.location.hash = "";
		},error=>{
			if(error.error) error=error.error;
			console.log(error);
			alert(error);
			return Promise.reject(error);
		})
		.then().then(load)
}
function login() {
	LoginSignup.login(generateFormData("#loginScreen"))
	.then(()=> {
		location.reload();//bescherming tegen google tracking.
	},error=>{
		if(error==401) alert("email/wachtwoord verkeerd");
		else throw error;
	}).catch(error=>{
		console.log(error);
		alert(error);
		return Promise.reject(error);
	})
}
function logout() {
	return unsubscribe()
		.then(LoginSignup.logout)
		.then(()=>location.reload());//veiligste manier, haalt alle html elementen ook leeg.
}