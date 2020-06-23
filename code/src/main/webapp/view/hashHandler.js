let autoChangedBool=false;
let autoChangedTimeout;
function autoChanged() {
	autoChangedBool=true;
	clearTimeout(autoChangedTimeout);
	autoChangedTimeout=setTimeout(()=>autoChangedBool=false,5);
}
$(window).on('hashchange', function() {
	if(!autoChangedBool) handleHash()
})
function handleHash() {
	let hash=window.location.hash;
	if(hash) hash=hash.split('#')[1];
	if(!hash) {
		LoginSignup.getLoggedinUser().then(user=>showPage(user.privatePageId)).catch(()=>{});
		return;
	}
	const parts = hash.split('=');
	if (parts[1]) {
		if (parts[0] === "newAccount") {
			$("#newPasswordCode").val(parts[1]);
			$('#newPasswordScreen').show();
			$('#loginOptions').hide();
			$('#notLoggedIn').show();
		} else if (parts[0] == "post") showSinglePost(parts[1]);
		else if (parts[0] == "page") showPage(parts[1]);
		else if (parts[0] == "user") User.getUser(parts[1]).then(user => showPage(user.privatePageId));
	} else if(parts[0]=="notifications") $('#meldingen').show();
	else LoginSignup.getLoggedinUser().then(user => showPage(user.privatePageId)).catch(() => {})
}
$('.homeButton').on('click',()=>location.hash="");
handleHash();
load();