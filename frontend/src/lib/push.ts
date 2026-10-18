import { Capacitor } from '@capacitor/core';
import { FirebaseMessaging } from '@capacitor-firebase/messaging';
import { registerDevice } from '../services/notificacaoService';

/** True inside the Android/iOS app (Capacitor), false in the browser. */
export const isNativeApp = Capacitor.isNativePlatform();

/** Must match the channel id the backend sends to (PushNotificationService). */
const CHANNEL_ID = 'aniversarios';

export type PushPermission = 'granted' | 'denied' | 'prompt' | 'unsupported';

let listenersReady = false;

function sendToken(token: string) {
  registerDevice(token, Capacitor.getPlatform()).catch(() => {
    // Retried on the next app start (registerPush runs again)
  });
}

function setupListeners(onOpen: () => void) {
  if (listenersReady) return;
  listenersReady = true;

  // FCM may rotate the token at any time
  FirebaseMessaging.addListener('tokenReceived', ({ token }) => sendToken(token));
  FirebaseMessaging.addListener('notificationActionPerformed', () => onOpen());
}

export async function getPushPermission(): Promise<PushPermission> {
  if (!isNativeApp) return 'unsupported';
  const { receive } = await FirebaseMessaging.checkPermissions();
  return receive === 'granted' ? 'granted' : receive === 'denied' ? 'denied' : 'prompt';
}

/**
 * Registers this phone with FCM (Android and iOS) and sends its token to the
 * backend. Asks for permission only when {@code askPermission} is true (i.e.
 * after a user tap).
 */
export async function registerPush(onOpen: () => void, askPermission = false): Promise<PushPermission> {
  if (!isNativeApp) return 'unsupported';

  let permission = await getPushPermission();
  if (permission === 'prompt' && askPermission) {
    const { receive } = await FirebaseMessaging.requestPermissions();
    permission = receive === 'granted' ? 'granted' : 'denied';
  }
  if (permission !== 'granted') return permission;

  setupListeners(onOpen);
  if (Capacitor.getPlatform() === 'android') {
    await FirebaseMessaging.createChannel({
      id: CHANNEL_ID,
      name: 'Aniversários',
      description: 'Avisos de aniversário dos jogadores',
      importance: 4,
    });
  }
  const { token } = await FirebaseMessaging.getToken();
  sendToken(token);
  return permission;
}
