function settingsSave() {
	sendPost("/rest/user","#settings")
	.then(()=>{
		alert("Instellingen zijn opgeslagen");
	}).catch(error=>{
		console.log(error);
		alert(error);
	});
};
loginListeners.push(function () {
	$('#settingsName').val(profile.name);
	$('#settingsEmail').val(profile.email);
	openSettings();
});
function openSettings() {
	$('#settings').show();
}