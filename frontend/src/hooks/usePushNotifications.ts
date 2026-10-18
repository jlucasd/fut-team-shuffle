import { useCallback, useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { getPushPermission, isNativeApp, registerPush, type PushPermission } from '../lib/push';

/**
 * App-wide: once logged in, (re)registers the phone for push so the backend
 * always has a fresh FCM token. Tapping a notification opens the birthdays page.
 */
export function usePushRegistration() {
  const navigate = useNavigate();

  useEffect(() => {
    if (!isNativeApp) return;
    registerPush(() => navigate('/aniversariantes')).catch(() => {
      // Push is optional; the rest of the app keeps working
    });
  }, [navigate]);
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
