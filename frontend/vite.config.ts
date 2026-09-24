import react from '@vitejs/plugin-react'
import { resolve } from 'node:path'
import { defineConfig } from 'vite'

// Two pages: the customer secure inbox (index.html) and the agent console (agent/index.html).
// The UI is served by Spring Boot under its context path (/case-viewer),
// so `npm run build` writes straight into the backend's static resources.
// In `npm run dev`, API calls are proxied to the Spring Boot app on :8081.
export default defineConfig({
  base: '/case-viewer/',
  plugins: [react()],
  build: {
    outDir: '../src/main/resources/static',
    emptyOutDir: true,
    rollupOptions: {
      input: {
        customer: resolve(import.meta.dirname, 'index.html'),
        agent: resolve(import.meta.dirname, 'agent/index.html'),
      },
    },
  },
  server: {
    port: 5173,
    proxy: {
      '/case-viewer/api': 'http://localhost:8081',
    },
  },
})
