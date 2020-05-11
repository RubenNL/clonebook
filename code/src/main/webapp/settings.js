$('#settingsSave').on('click',()=>{
	sendPost('/rest/user/settings',{
		name:$('#settingsName').val(),
		email:$('#settingsEmail').val()
	}).then(response=>console.log(response))
})
loginListeners.push(sendGet("/rest/user/settings").then(settings=>{
	$('#settingsName').val(settings.name)
	$('#settingsEmail').val(settings.email)
	openSettings();
}))
function openSettings() {
	$('#settings').show();
}