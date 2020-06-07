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
 
