import { useCallback, useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { getPushPermission, isNativeApp, registerPush, type PushPermission } from '../lib/push';
import {
  enableWebPush,
  isIos,
  isStandalone,
  isWebPushSupported,
  refreshWebPush,
  webPushEnabled,
} from '../lib/webPush';

/**
 * App-wide: once logged in, (re)registers the phone/browser for push so the
 * backend always has a fresh FCM token. Tapping a notification opens the
 * birthdays page.
 */
export function usePushRegistration() {
  const navigate = useNavigate();

  useEffect(() => {
    // Push is optional; failures never block the rest of the app
    if (isNativeApp) {
      registerPush(() => navigate('/aniversariantes')).catch(() => {});
    } else {
      refreshWebPush().catch(() => {});
    }
  }, [navigate]);
}

export type WebPushState =
  | 'checking'
  | 'unsupported'
  | 'ios-needs-home-screen'
  | 'prompt'
  | 'denied'
  | 'enabled';

function currentWebPushState(supported: boolean): WebPushState {
  if (!supported) return isIos() && !isStandalone() ? 'ios-needs-home-screen' : 'unsupported';
  if (webPushEnabled()) return 'enabled';
  return Notification.permission === 'denied' ? 'denied' : 'prompt';
}

/** Settings hook for web push (browser / PWA on the Home Screen). */
export function useWebPush() {
  const [state, setState] = useState<WebPushState>('checking');

  useEffect(() => {
    if (isIos() && !isStandalone()) {
      // Safari only offers push to sites opened from the Home Screen
      setState('ios-needs-home-screen');
      return;
    }
    isWebPushSupported()
      .then((supported) => setState(currentWebPushState(supported)))
      .catch(() => setState('unsupported'));
  }, []);

  const enable = useCallback(async () => {
    const permission = await enableWebPush();
    setState(permission === 'granted' ? 'enabled' : permission === 'denied' ? 'denied' : 'prompt');
    return permission === 'granted';
  }, []);

  return { state, enable };
}

/** Settings hook for the birthdays page (native app only). */
export function usePushNotifications() {
  const navigate = useNavigate();
  const [permission, setPermission] = useState<PushPermission>(isNativeApp ? 'prompt' : 'unsupported');

  useEffect(() => {
    getPushPermission().then(setPermission).catch(() => setPermission('unsupported'));
  }, []);

  const enable = useCallback(async () => {
    const result = await registerPush(() => navigate('/aniversariantes'), true);
    setPermission(result);
    return result === 'granted';
  }, [navigate]);

  return { isNativeApp, permission, enable };
}
