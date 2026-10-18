import type { CapacitorConfig } from '@capacitor/cli';

const config: CapacitorConfig = {
  appId: 'com.gavioesfc.sorteio',
  appName: 'Gaviões FC',
  webDir: 'dist',
  plugins: {
    FirebaseMessaging: {
      // iOS: still show the notification while the app is open
      presentationOptions: ['badge', 'sound', 'alert'],
    },
  },
  experimental: {
    ios: {
      spm: {
        // Avoids a SwiftPM package identity collision with firebase-ios-sdk
        packageOptions: {
          '@capacitor-firebase/messaging': { symlink: true },
        },
      },
    },
  },
};

export default config;
