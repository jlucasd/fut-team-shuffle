import type { Messaging } from 'firebase/messaging';
import { registerDevice } from '../services/notificacaoService';

/**
 * Web push for the browser / installed PWA (incl. iPhone from iOS 16.4, once
 * added to the Home Screen). The Firebase SDK is loaded on demand.
 */

const firebaseConfig = {
  apiKey: import.meta.env.VITE_FIREBASE_API_KEY,
  projectId: import.meta.env.VITE_FIREBASE_PROJECT_ID,
  messagingSenderId: import.meta.env.VITE_FIREBASE_MESSAGING_SENDER_ID,
  appId: import.meta.env.VITE_FIREBASE_APP_ID,
};
const vapidKey = import.meta.env.VITE_FIREBASE_VAPID_KEY;

const ENABLED_KEY = 'gv_web_push_enabled';
const SW_URL = '/firebase-messaging-sw.js';

/** True when the Firebase web config was provided at build time. */
export const webPushConfigured = Object.values(firebaseConfig).every(Boolean) && !!vapidKey;

export function isIos(): boolean {
  const ua = navigator.userAgent;
  // iPadOS reports itself as a Mac with touch support
  return /iPhone|iPad|iPod/.test(ua) || (ua.includes('Macintosh') && navigator.maxTouchPoints > 1);
}

/** Opened from the Home Screen icon (required for push on iPhone). */
export function isStandalone(): boolean {
  return (
    window.matchMedia('(display-mode: standalone)').matches ||
    (navigator as Navigator & { standalone?: boolean }).standalone === true
  );
}

function readEnabled(): boolean {
  try {
    return localStorage.getItem(ENABLED_KEY) === '1';
  } catch {
    return false;
  }
}

function writeEnabled(enabled: boolean) {
  try {
    localStorage.setItem(ENABLED_KEY, enabled ? '1' : '0');
  } catch {
    // Only affects the token refresh on the next start
  }
}

let messagingPromise: Promise<Messaging | null> | null = null;

async function getMessagingInstance(): Promise<Messaging | null> {
  messagingPromise ??= (async () => {
    const [{ initializeApp, getApps }, messaging] = await Promise.all([
      import('firebase/app'),
      import('firebase/messaging'),
    ]);
    if (!(await messaging.isSupported())) return null;
    const app = getApps()[0] ?? initializeApp(firebaseConfig);
    const instance = messaging.getMessaging(app);

    // While the app is open the SDK doesn't display pushes: show them ourselves.
    messaging.onMessage(instance, async (payload) => {
      const registration = await navigator.serviceWorker.getRegistration(SW_URL);
      registration?.showNotification(payload.notification?.title ?? 'Gaviões FC', {
        body: payload.notification?.body,
        icon: '/icons/icon-192.png',
      });
    });
    return instance;
  })();
  return messagingPromise;
}

export async function isWebPushSupported(): Promise<boolean> {
  if (!webPushConfigured || !('serviceWorker' in navigator) || !('Notification' in window)) return false;
  return (await getMessagingInstance()) !== null;
}

async function registerToken(): Promise<void> {
  const messaging = await getMessagingInstance();
  if (!messaging) throw new Error('Web push não suportado');

  const swUrl = `${SW_URL}?${new URLSearchParams(firebaseConfig as Record<string, string>)}`;
  const registration = await navigator.serviceWorker.register(swUrl);
  await navigator.serviceWorker.ready;

  const { getToken } = await import('firebase/messaging');
  const token = await getToken(messaging, { vapidKey, serviceWorkerRegistration: registration });
  await registerDevice(token, 'web');
}

/** Asks for permission (must run from a tap) and registers this browser. */
export async function enableWebPush(): Promise<NotificationPermission> {
  // Request first, while still inside the user gesture (Safari requires it).
  const permission = await Notification.requestPermission();
  if (permission !== 'granted') return permission;
  await registerToken();
  writeEnabled(true);
  return permission;
}

export function webPushEnabled(): boolean {
  return webPushConfigured && readEnabled() && 'Notification' in window && Notification.permission === 'granted';
}

/** On app start: re-send the (possibly rotated) token to the backend. */
export async function refreshWebPush(): Promise<void> {
  if (webPushEnabled()) await registerToken();
}
