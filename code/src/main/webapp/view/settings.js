let settings;
function openSettings() {
	Settings.getSettings().then(_settings=>{
		settings=_settings;
		$('#settingsName').val(settings.name);
		$('#settingsEmail').val(settings.email);
		$('#settings').show();
	})
}
function settingsSave() {
	settings.email=$('#settingsEmail').val();
	settings.name=$('#settingsName').val();
	settings.save().then(()=>{
		alert("Instellingen zijn opgeslagen");
		$('#settings').hide();
		load();
	}).catch(error=>{
		console.log(error);
		alert(error);
	});
}