self.addEventListener('install', (event) => {
  console.log('service worker installed!');
  self.skipWaiting();
});

self.addEventListener('activate', (event) => {
  console.log('service worker activated!');
  return self.clients.claim();
});

self.addEventListener('fetch', function(event) {
  if(navigator.onLine) event.respondWith(fetch(event.request));
  else {
    let headers=new Headers();
    headers.append("Content-Type","text/html");
    event.respondWith(new Response("<h1>Browser is offline.</h1>",{headers:headers}));
  }
});