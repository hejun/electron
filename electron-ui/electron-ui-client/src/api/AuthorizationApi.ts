import useAuthStore from '@/store/AuthStore.ts'
import { enc, SHA256 } from 'crypto-js'

const BASE_URL = import.meta.env.VITE_AUTHENTICATION_BASE_URL ?? ''
const TENANT = import.meta.env.VITE_TENANT ?? ''
const CLIENT_ID = import.meta.env.VITE_CLIENT_ID ?? ''
const REDIRECT_URI = import.meta.env.VITE_REDIRECT_URI ?? ''
const SCOPE = import.meta.env.VITE_SCOPE ?? ''

const REQUEST_PREFIX = TENANT ? `${BASE_URL}/${TENANT}` : `${BASE_URL}`

interface Userinfo {
  iss: string
  aud: string
  aud_name: string
  sub: string
}

export function signIn(callbackUrl?: string) {
  const authStore = useAuthStore()
  authStore.initAuthentication(callbackUrl ? enc.Base64.stringify(enc.Utf8.parse(callbackUrl as string)) : undefined)

  const urlParams = new URLSearchParams({
    response_type: 'code',
    client_id: CLIENT_ID,
    redirect_uri: REDIRECT_URI,
    scope: SCOPE,
    code_challenge_method: 'S256',
    code_challenge: enc.Base64url.stringify(SHA256(authStore.authenticator!.codeVerify)),
    state: authStore.authenticator!.state,
    nonce: authStore.authenticator!.nonce
  })

  window.location.href = `${REQUEST_PREFIX}/oauth2/authorize?${urlParams}`
}

export async function obtainToken(code: string, codeVerify: string) {
  const CLIENT_ID = import.meta.env.VITE_CLIENT_ID ?? ''
  const REDIRECT_URI = import.meta.env.VITE_REDIRECT_URI ?? ''

  const authStore = useAuthStore()

  const params = {
    grant_type: 'authorization_code',
    code: code,
    client_id: CLIENT_ID,
    redirect_uri: REDIRECT_URI,
    code_verifier: codeVerify
  }

  return fetch(`${REQUEST_PREFIX}/oauth2/token`, { method: 'POST', body: new URLSearchParams(Object.entries(params)) })
    .then(res => res.json())
    .then(res => {
      const expireAt = new Date()
      expireAt.setSeconds(expireAt.getSeconds() + res.expires_in)
      return {
        accessToken: res.access_token,
        expiresAt: expireAt.getTime(),
        idToken: res.id_token,
        scope: res.scope,
        tokenType: res.token_type
      }
    })
    .finally(() => authStore.destroyAuthenticator())
}

export function signOut() {
  const authStore = useAuthStore()

  authStore.destroyAuthentication()
  const url = `${BASE_URL}/logout`
  window.location.replace(url)
}

export async function findUserinfo() {
  const authStore = useAuthStore()

  return fetch(`${REQUEST_PREFIX}/userinfo`, {
    method: 'GET',
    headers: { Authorization: `${authStore.authentication?.tokenType} ${authStore.authentication?.accessToken}` }
  }).then<Userinfo>(res => res.json())
}
