/* Service worker for web push (PWA). Firebase config arrives in the query
 * string set by src/lib/webPush.ts, so this file needs no build step.
 * Keep the SDK version in sync with the "firebase" package. */

// Registered before the Firebase SDK so it handles clicks first:
// open (or focus) the birthdays page.
self.addEventListener('notificationclick', (event) => {
  event.notification.close();
  event.stopImmediatePropagation();
  const target = new URL('/aniversariantes', self.location.origin).href;
  event.waitUntil(
    self.clients.matchAll({ type: 'window', includeUncontrolled: true }).then((windows) => {
      const open = windows.find((w) => w.url.startsWith(self.location.origin));
      if (open) {
        return open.focus().then((w) => w && 'navigate' in w ? w.navigate(target) : w);
      }
      return self.clients.openWindow(target);
    })
  );
});

importScripts('https://www.gstatic.com/firebasejs/12.19.0/firebase-app-compat.js');
importScripts('https://www.gstatic.com/firebasejs/12.19.0/firebase-messaging-compat.js');

const params = new URL(self.location.href).searchParams;
firebase.initializeApp({
  apiKey: params.get('apiKey'),
  projectId: params.get('projectId'),
  messagingSenderId: params.get('messagingSenderId'),
  appId: params.get('appId'),
});

// Messages with a "notification" payload are shown automatically while the
// app is in the background, which iOS requires for every push.
firebase.messaging();
