var profile;
function passwordRequest() {
	sendPost("/rest/user/new","#newAccountScreen")
	.then(()=>{
		alert("Kijk op je e-mail.");
		$("#loginOptions").hide();
	}).catch(error=>{
		console.log(error)
		alert(error)
	});
}
function load() {
	sendGet('/rest/user')
	.then(response=>{
		profile=response;
		loadPage(profile.privatePageId);
		$('#userMenuName').text(profile.name);
	}).catch(message=>{
		if(message==403) $('#notLoggedIn').show();
		else {
			console.log(message);
			alert(message);
		}
	})
}
function savePassword() {
	sendPost("/rest/user/newPassword","#newPasswordScreen")
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
	sendPost("/rest/login","#loginScreen")
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