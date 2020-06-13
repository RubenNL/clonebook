let settings;
function openSettings() {
	Settings.getSettings().then(_settings=>{
		settings=_settings;
		$('#settingsName').val(settings.name);
		$('#settingsEmail').val(settings.email);
		$('#settings').show();
		showSwStatus();
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
$('#logoutAll').on('click', ()=>{
	LoginSignup.getLoggedinUser().then(user=>user.deleteSessions()).then(logout).then(()=>{
		alert('alle sessies uitgelogd. log opnieuw in.');
	})
});
function showSwStatus() {
	Settings.getSubscription().then(subscription=>{
		if (subscription) $('#subscribeNotif').text('meldingen uitzetten');
		else $('#subscribeNotif').text('meldingen aanzetten');
	});
}
function unsubscribe() {
	return Settings.unsubscribe()
}
function subscribe() {
	Settings.subscribe().then(()=>alert('meldingen staan aan!'))
	.catch(function(err) {
		if(err=="long") err="Log opnieuw in met \"ingelogd blijven\" aan."
		console.log('Failed to subscribe the user: ', err);
		alert(err);
	});
}
$('#subscribeNotif').on('click',()=>{
	Settings.getSubscription().then(status=>{
		if(status) return unsubscribe();
		else return subscribe();
	}).then(showSwStatus);
})