$('#showNewAccount').on('click',()=>{
	$('#newAccountScreen').toggle()
	$('#loginScreen').toggle()
})
$('#createNewAccountButton').on("click",()=>{
	sendPost('/rest/user/new',{
		email:$('#newAccountEmail').val()
	}).then(response=>{
		console.log(response);
		alert('Account aangevraagd. Kijk op je e-mail');
		$('#newAccountEmail').val('');
	}).catch(exception=>{
		alert(exception);
	})
})
function load() {
	sendGet('/rest/user').then(response=> {
		if (response.needsPassword) {

		}
	})
}
if(window.location.hash) {
	parts=window.location.hash.split('=')
	if(parts[1]) {
		parts[0]=parts[0].split('#')[1];
		if(parts[0]==="newAccount") {
			$('#loginScreen').hide();
			$('#newPasswordScreen').show();
		}
	} else {
		load()
	}
}
$('#newPasswordButton').on('click',()=>{
	sendPost('/rest/user/newPassword',{
		code:window.location.hash.split('=')[1],
		password:$('#newPasswordBox').val()
	}).then(response=>{
		console.log(response);
	}).catch(exception=>{
		alert(exception);
	})
});
$('#loginButton').on('click',()=>{
	sendPost('/rest/user/login',{
		email:$('#loginEmail').val(),
		password:$('#loginPassword').val()
	}).then(response=>{
		console.log(response);
	}).catch(exception=>{
		alert(exception);
	})
});