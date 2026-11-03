/** Shared loader so the login screen can prefetch the first page after login. */
export const loadHomePage = () => import('./HomePage').then((m) => ({ default: m.HomePage }));
