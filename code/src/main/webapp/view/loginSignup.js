function passwordRequest() {
	LoginSignup.passwordRequest(generateFormData("#newAccountScreen"))
	.then(()=>{
		alert("Kijk op je e-mail.");
		$("#loginOptions").hide();
	}).catch(error=>{
		console.log(error);
		if(error.error) alert(error.error);
		else alert(error);
	});
}
async function renderCaptcha() {
	grecaptcha.render('recaptcha', {
		sitekey: await Utils.sendGet("recaptchaKey")
	})
}
function load() {
	return LoginSignup.getLoggedinUser().catch(message=>{
		if(message==403 || message=="niet ingelogd" || message==500) { //500 wanneer JWT raar is. ik weet niet hoe het komt, iets in de backend.
			let script=document.createElement('script');
			script.src='https://www.google.com/recaptcha/api.js?onload=renderCaptcha&render=explicit';
			document.querySelector('body').appendChild(script);
			$('#notLoggedIn').show();
		}
		else {
			console.log(message);
			alert(message);
		}
		return Promise.reject(403);
	}).then(user=> {
		$('.afterLogin').removeClass('afterLogin');
		$('#userMenuName').text(user.name);
		if(user.profilePicture) $('#userMenuPicture').attr('src', Media.fromId(user.profilePicture).getUrl());
		else $('#userMenuName').show();
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
	unsubscribe();//fix als er nog oude tokens opgeslagen zijn.
	LoginSignup.login(generateFormData("#loginScreen"))
	.then(()=> {
		if(navigator.serviceWorker && window.localStorage.getItem("long")=="true" && confirm("Wilt u meldingen ontvangen? Dit is uit te schakelen in de instellingen.")) return Settings.subscribe().catch(err=>{
			alert(err);
		}).then(()=>location.reload());
		else location.reload();//bescherming tegen google tracking.
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
	autoChanged();
	window.location.hash='';
	return unsubscribe()
		.then(LoginSignup.logout)
		.then(()=>location.reload());//veiligste manier, haalt alle html elementen ook leeg.
}