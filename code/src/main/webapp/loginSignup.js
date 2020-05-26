var profile;
function passwordRequest() {
	Utils.sendPost("/rest/user/new","#newAccountScreen")
	.then(()=>{
		alert("Kijk op je e-mail.");
		$("#loginOptions").hide();
	}).catch(error=>{
		console.log(error)
		alert(error)
	});
}
function load() {
	getLoggedinUser().catch(message=>{
		$('#notLoggedIn').show();
	}).then(User.getUser).catch(message=>{
		if(message===403) $('#notLoggedIn').show();
		else {
			console.log(message);
			alert(message);
		}
	}).then(user=>{
		$('#userMenuName').text(user.name);
		return user.getPage()
	}).then(showPage);
}
function savePassword() {
	Utils.sendPost("/rest/user/newPassword","#newPasswordScreen")
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
	Utils.sendPost("/rest/login","#loginScreen")
	.then(response=>{
		window.sessionStorage.setItem("jwt",response.JWT);
		$('#notLoggedIn').hide();
		load();
	})
	.catch(error=>{
		console.log(error);
		alert(error);
	});
}
function logout() {
	window.sessionStorage.removeItem("jwt");
	load();
}
if(window.location.hash && window.location.hash.split('=')[1]) {
	const parts = window.location.hash.split('=');
	if(parts[1]) {
		parts[0]=parts[0].split('#')[1];
		if(parts[0]==="newAccount") {
			$("#newPasswordCode").val(parts[1]);
			$('#newPasswordScreen').show();
			$('#loginOptions').hide();
			$('#notLoggedIn').show();
		}
	}
} else load();
function getLoggedinUser() {
	return new Promise((resolve,reject)=>{
		token=sessionStorage.getItem("jwt");
		if(token.length==0) reject("niet ingelogd");
		var base64Url = token.split('.')[1];
		var base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
		var jsonPayload = decodeURIComponent(atob(base64).split('').map(function(c) {
			return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
		}).join(''));
		resolve(JSON.parse(jsonPayload).sub);
	})
};