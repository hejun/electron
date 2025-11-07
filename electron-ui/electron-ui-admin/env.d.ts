/// <reference types="vite/client" />
/// <reference types="unplugin-icons/types/vue" />

interface ImportMetaEnv {
  VITE_REQUEST_BASE_URL: string
  VITE_AUTHENTICATION_BASE_URL: string
  VITE_TENANT: string
  VITE_CLIENT_ID: string
  VITE_REDIRECT_URI: string
  VITE_SCOPE: string
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}
