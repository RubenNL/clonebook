var loginListeners=[]

function passwordRequest() {
	sendPost('/rest/user/new',{
		email:$('#newAccountEmail').val()
	}).then(response=>{
		console.log(response);
		alert('Account aangevraagd. Kijk op je e-mail');
		$('#newAccountEmail').val('');
		$('#notLoggedIn').hide();
	}).catch(exception=>alert(exception));
}
function load() {
	sendGet('/rest/user')
	.then(response=>Promise.all(loginListeners))
	.catch(message=>{
		if(message==401) $('#notLoggedIn').show()
		else {
			console.log(message);
			alert(message);
		}
	})
}
function savePassword() {
	sendPost('/rest/user/newPassword',{
		code:window.location.hash.split('=')[1],
		password:$('#newPasswordBox').val()
	})
	.then(response=>load())
	.catch(exception=>alert(exception));
}
function login() {
	sendPost('/rest/user/login',{
		email:$('#loginEmail').val(),
		password:$('#loginPassword').val()
	}).then(response=>{
		$('#notLoggedIn').hide();
		console.log(response);
		load();
	}).catch(exception=>{
		alert(exception);
	})
}
if(window.location.hash && window.location.hash.split('=')[1]) {
	parts=window.location.hash.split('=');
	if(parts[1]) {
		parts[0]=parts[0].split('#')[1];
		if(parts[0]==="newAccount") {
			$('#newPasswordScreen').show();
			$('#loginOptions').hide();
			$('#notLoggedIn').show();
		}
	}
} else load();