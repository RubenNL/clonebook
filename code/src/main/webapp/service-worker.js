self.addEventListener('install', (event) => {
	console.log('service worker installed!');
	self.skipWaiting();
});

self.addEventListener('activate', (event) => {
	console.log('service worker activated!');
	return self.clients.claim();
});

self.addEventListener('fetch', function(event) {
	if(event.request.mode!=="navigate") return
	if(navigator.onLine) event.respondWith(fetch(event.request));
	else {
		let headers=new Headers();
		headers.append("Content-Type","text/html");
		event.respondWith(new Response("<h1>Browser is offline.</h1>",{headers:headers}));
	}
});
self.addEventListener('push', function(event) {
	console.log('[Service Worker] Push Received.');
	console.log(`[Service Worker] Push had this data: "${event.data.text()}"`);

	const title = 'Push Codelab';
	const options = {
		body: event.data.text()
	};

	event.waitUntil(self.registration.showNotification(title, options));
});
self.addEventListener('notificationclick', function(event) {
	console.log('[Service Worker] Notification click Received.');
});

