import HttpClient from '@/api/HttpClient.ts'

const http = new HttpClient(import.meta.env.VITE_AUTHENTICATION_BASE_URL)

export interface Tenant {
  id?: string
  name: string
  issuer: string
  publicKey?: string
  privateKey?: string
  termsOfServiceTitle?: string
  termsOfServiceDesc?: string
  termsOfServiceContent?: string
  privacyPolicyTitle?: string
  privacyPolicyDesc?: string
  privacyPolicyContent?: string
  copyright?: string
  enabled: boolean
  createDate?: string
  lastModifiedDate?: string
}

export async function findTenantByIssuer(issuer: string): Promise<Tenant> {
  return http.get(`/tenant/issuer`, { issuer })
}