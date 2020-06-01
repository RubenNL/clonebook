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
function load() {
	LoginSignup.getLoggedinUser().catch(message=>{
		$('#notLoggedIn').show();
		return Promise.reject(403);
	}).then(User.getUser).catch(message=>{
		if(message===403) $('#notLoggedIn').show();
		else {
			console.log(message);
			alert(message);
		}
		return Promise.reject(403);
	}).then(user=>{
		$('#userMenuName').text(user.name);
		return user.getPage();
	}).then(showPage).catch(()=>{});//silent catch, ik weet niet wat ik hier anders van moet maken.
}
function savePassword() {
	LoginSignup.savePassword(generateFormData("#newPasswordScreen"))
	.then(()=>{
		$('#newPasswordScreen').hide();
		$('#loginOptions').show();
		alert("Wachtwoord ingesteld.");
	}).catch(error=>{
		console.log(error);
		alert(error)
	});
}
function login() {
	LoginSignup.login(generateFormData("#loginScreen"))
	.catch(error=>{
		console.log(error);
		alert(error);
		return Promise.reject(error);
	}).then(()=> {
		$('#notLoggedIn').hide();
		load();
	})
}
function logout() {
	LoginSignup.logout();
	$('#userMenuName').text("");
	logoutPost();
	load();
}