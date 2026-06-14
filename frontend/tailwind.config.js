/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        gavioes: {
          black: '#0A0A0A',
          yellow: '#F5C518',
          white: '#FFFFFF',
          'dark-gray': '#1A1A1A',
        },
      },
      fontFamily: {
        title: ['"Bebas Neue"', 'sans-serif'],
        body: ['Inter', 'sans-serif'],
      },
    },
  },
  plugins: [],
}
