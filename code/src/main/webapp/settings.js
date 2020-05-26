function settingsSave() {
	Utils.sendPost("/rest/user/settings","#settings")
	.then(()=>{
		alert("Instellingen zijn opgeslagen");
		$('#settings').hide();
		load();
	}).catch(error=>{
		console.log(error);
		alert(error);
	});
};

function openSettings() {
	$('#settingsName').val(profile.name);
	$('#settingsEmail').val(profile.email);
	$('#settings').show();

}