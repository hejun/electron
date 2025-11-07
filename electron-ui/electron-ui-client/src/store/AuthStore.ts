import { defineStore } from 'pinia'

export interface Authenticator {
  codeVerify: string
  state: string
  nonce: string
}

export interface Authentication {
  accessToken: string
  expiresAt: number
  idToken: string
  scope: string
  tokenType: string
}

export interface AuthState {
  authenticator: Authenticator | null
  authentication: Authentication | null
}

const useAuthStore = defineStore('auth', {
  state: (): AuthState => ({
    authenticator: null,
    authentication: null
  }),
  getters: {
    isAuthorized: (state: AuthState): boolean =>
      state.authentication !== null && state.authentication.expiresAt > new Date().getTime()
  },
  actions: {
    initAuthentication(state?: string) {
      this.authenticator = {
        codeVerify: Math.random().toString(36).slice(6),
        state: state ?? '',
        nonce: Math.random().toString(36).slice(6)
      }
    },
    destroyAuthenticator() {
      this.authenticator = null
    },
    destroyAuthentication() {
      this.authentication = null
    },
    updateToken(authentication: Authentication) {
      this.authentication = authentication
    }
  },
  persist: {
    storage: sessionStorage
  }
})

export default useAuthStore
