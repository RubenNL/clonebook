self.addEventListener('install', (event) => {
	console.log('service worker installed!');
	self.skipWaiting();
});

self.addEventListener('activate', (event) => {
	console.log('service worker activated!');
	return self.clients.claim();
});

self.addEventListener('fetch', function(event) {
	if(event.request.mode=="navigate") {
		if (navigator.onLine) event.respondWith(fetch(event.request));
		else {
			let headers = new Headers();
			headers.append("Content-Type", "text/html");
			event.respondWith(new Response("<h1>Browser is offline.</h1>", {headers: headers}));
		}
	} else if(event.request.destination!=="") {
		event.respondWith(
			caches.open('clonebook').then(function(cache) {
				return cache.match(event.request).then(function (response) {
					return response || fetch(event.request).then(function(response) {
						cache.put(event.request, response.clone());
						return response;
					});
				});
			})
		);
	}
});
self.addEventListener("notificationclick",event=>{
	const data=event.notification.data;
	if(data.length==0) return;
	if(data.split("#").length==1) return;
	const url=data;
	event.waitUntil(clients.matchAll({
		type: "window"
	}).then(function(clientList) {
		for (var i = 0; i < clientList.length; i++) {
			var client = clientList[i];
			if (client.url == url && 'focus' in client)
				return client.focus();
		}
		if (clients.openWindow)
			return clients.openWindow(url);
	}));
})
self.addEventListener('push',event=>{
	let data=event.data.json();
	data.action=data.data;
	if(data.action=="CHAT") {
		const parsed=JSON.parse(data.options.body)
		data={
			options:{
				body:parsed.message,
				tag:parsed.chatId,
				data:"/#chat="+parsed.chatId
			},
			title:"chat van "+parsed.title
		}
	}
	event.waitUntil(self.registration.showNotification(data.title, data.options));
	if(data.action=="LOGOUT") self.registration.unregister();
});
self.addEventListener('notificationclick', function(event) {
	console.log('[Service Worker] Notification click Received.');
});

