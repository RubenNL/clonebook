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
	return LoginSignup.getLoggedinUser().catch(message=>{
		if(message===403 || message=="niet ingelogd") $('#notLoggedIn').show();
		else {
			console.log(message);
			alert(message);
		}
		return Promise.reject(403);
	}).then(user=> {
		$('#userMenu').show();
		$('#userMenuName').text(user.name);
		return user
	})
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
		load().then(handleHash);
	})
}
function logout() {
	return LoginSignup.logout().then(()=>{
		$('#userMenuName').text("");
		$('#userMenu').hide();
		logoutPost();
	}).then(load);
}
function getLoggedInId() {
	const token=sessionStorage.getItem("jwt");
	if(!token || token.length==0) return null;
	const base64Url = token.split('.')[1];
	const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
	const jsonPayload = decodeURIComponent(atob(base64).split('').map(function(c) {
		return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
	}).join(''));
	return JSON.parse(jsonPayload).sub;
}