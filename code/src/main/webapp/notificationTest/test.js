const applicationServerPublicKey = 'BJqEt0KJ2PaOYrOZC1MQatRgm4ddt0Bc4O6hdriShTNFim1VvtDwpxA3YKkorzmljcwqObOnz2qs0n93H3fk1d8';
const applicationServerKey = urlB64ToUint8Array(applicationServerPublicKey);
alert('test')
navigator.serviceWorker.register('sw.js')
.then(swRegistration=>{
	alert("test2");
	return swRegistration.pushManager.subscribe({
		userVisibleOnly: true,
		applicationServerKey: applicationServerKey
	})
})
.then(subscription=>JSON.parse(JSON.stringify(subscription)))
.then(subscription=>{
	alert(subscription.endpoint);
	return fetch("/rest/notification",{
		method:"POST",
		body: new URLSearchParams(generateFormData({endpoint:subscription.endpoint,auth:subscription.keys.auth,key:subscription.keys.p256dh}))
	})
}).then(response=>{
	console.log(response)
	alert(response);
})
.catch(function(err) {
	console.log('Failed to subscribe the user: ', err);
	alert(err);
});
function generateFormData(form) {
	if(typeof form=="string") return new FormData(document.querySelector(form)).entries();
	if(form instanceof HTMLElement) return new FormData(form);
	else {
		let formData = new FormData();
		for ( const key in form ) {
			formData.append(key, form[key]);
		}
		return formData;
	}
}
function urlB64ToUint8Array(base64String) {
  const padding = '='.repeat((4 - base64String.length % 4) % 4);
  const base64 = (base64String + padding)
    .replace(/\-/g, '+')
    .replace(/_/g, '/');

  const rawData = window.atob(base64);
  const outputArray = new Uint8Array(rawData.length);

  for (let i = 0; i < rawData.length; ++i) {
    outputArray[i] = rawData.charCodeAt(i);
  }
  return outputArray;
}
