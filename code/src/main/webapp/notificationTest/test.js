function getSw() {
	return navigator.serviceWorker.register('sw.js')
}
function getSubscription() {
	return getSw().then(sw=>sw.pushManager.getSubscription());
}
function getAuth() {
	return getSubscription().then(subscription=>JSON.parse(JSON.stringify(subscription)).keys.auth)
}
function showSwStatus() {
	getSubscription().then(subscription=>{
		if (subscription) $('#subscribe').text('unsubscribe');
		else $('#subscribe').text('subscribe');
	});
}
function unsubscribe() {
	return getSubscription().then(subscription=>{
		return Utils.sendDelete('notification/'+JSON.parse(JSON.stringify(subscription)).keys.auth)
			.catch(message=> {
				if (message == 404) return
				else throw message
			}).then(()=>subscription.unsubscribe());
	})
}
function subscribe() {
	return Promise.all([Utils.sendGet('notification'),getSw()]).then(list=>{
		return list[1].pushManager.subscribe({
			userVisibleOnly: true,
			applicationServerKey: urlB64ToUint8Array(list[0].key)
		}).then(subscription=>JSON.parse(JSON.stringify(subscription)))
			.then(subscription=>{
				return Utils.sendPost('notification',generateFormData({
					endpoint:subscription.endpoint,
					auth:subscription.keys.auth,
					key:subscription.keys.p256dh
				}))
			}).then(response=>{
				console.log(response);
				alert(response);
			})
	}).catch(function(err) {
		console.log('Failed to subscribe the user: ', err);
		alert(err);
	});
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
$('#subscribe').on('click',()=>{
	getSubscription().then(status=>{
		if(status) return unsubscribe();
		else return subscribe();
	}).then(showSwStatus);
})
showSwStatus();
$('#sendToBrowser').on('click',()=>{
	return getAuth().then(auth=>{
		return Utils.sendPost('notification/'+auth,generateFormData({}));
	})
})
$('#sendToUser').on('click',()=>{
	return Utils.sendPost('user/'+getLoggedInId()+'/notif',generateFormData({}));
})