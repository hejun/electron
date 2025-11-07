import useAuthStore from '@/store/AuthStore.ts'

export default class HttpClient {
  private readonly baseUrl: string
  private readonly authStore

  constructor(baseUrl?: string) {
    this.baseUrl = baseUrl || window.location.href
    this.authStore = useAuthStore()
  }

  private async request(url: string | URL, init?: RequestInit) {
    const authorizationHeader: HeadersInit = this.authStore.isAuthorized
      ? [['Authorization', `${this.authStore.authentication?.tokenType} ${this.authStore.authentication?.accessToken}`]]
      : []

    const headers = new Headers([
      ...new Headers(init?.headers).entries(),
      ...new Headers(authorizationHeader).entries()
    ])

    let response
    try {
      response = await fetch(url, { ...init, headers })
    } catch (e) {
      throw e
    }

    const contentType = response.headers.get('Content-Type')
    if (contentType && contentType.indexOf('application/json') !== -1) {
      if (!response.ok) {
        throw await response.json()
      } else {
        return await response.json()
      }
    } else {
      if (!response.ok) {
        throw await response.text()
      } else {
        return await response.text()
      }
    }
  }

  private stringifyQueryParam(query: Record<string, unknown>) {
    const searchParams = new URLSearchParams()

    for (const [key, value] of Object.entries(query)) {
      if (value === undefined || value === null) {
        continue
      }
      if (typeof value === 'string' && value.length === 0) {
        continue
      }
      if (Array.isArray(value) && value.length === 0) {
        continue
      }
      if (Array.isArray(value)) {
        value.forEach(item => searchParams.append(key, item.toString()))
      } else {
        searchParams.append(key, value.toString())
      }
    }

    return searchParams.toString()
  }

  private buildUrl(url: string, query?: Record<string, unknown>) {
    const targetUrl = new URL(url, this.baseUrl)
    if (query) {
      targetUrl.search = this.stringifyQueryParam(query)
    }
    return targetUrl
  }

  private buildHeader(body?: object | BodyInit) {
    const headers = new Headers()
    if (body && body instanceof FormData) {
      headers.set('Content-Type', 'multipart/form-data')
    } else {
      if (Object.prototype.toString.call(body) === '[object Object]') {
        headers.set('Content-Type', 'application/json')
      } else {
        headers.set('Content-Type', 'application/x-www-form-urlencoded')
      }
    }
    return headers
  }

  private buildBody(body?: object | BodyInit): BodyInit | null {
    let targetBody: BodyInit | null = null
    if (body !== undefined && body !== null) {
      if (Object.prototype.toString.call(body) === '[object Object]') {
        targetBody = JSON.stringify(body)
      } else {
        targetBody = body as BodyInit
      }
    }
    return targetBody
  }

  get<T>(url: string, query?: Record<string, unknown>): Promise<T> {
    const targetUrl = this.buildUrl(url, query)
    return this.request(targetUrl, { method: 'GET' })
  }

  post<T>(url: string, body?: object | BodyInit, query?: Record<string, unknown>): Promise<T> {
    const targetUrl = this.buildUrl(url, query)
    const headers = this.buildHeader(body)
    const targetBody = this.buildBody(body)
    return this.request(targetUrl, { method: 'POST', body: targetBody ?? '', headers })
  }

  put<T>(url: string, body?: object | BodyInit, query?: Record<string, unknown>): Promise<T> {
    const targetUrl = this.buildUrl(url, query)
    const headers = this.buildHeader(body)
    const targetBody = this.buildBody(body)
    return this.request(targetUrl, { method: 'PUT', body: targetBody ?? '', headers })
  }

  del<T>(url: string, body?: object | BodyInit, query?: Record<string, unknown>): Promise<T> {
    const targetUrl = this.buildUrl(url, query)
    const headers = this.buildHeader(body)
    const targetBody = this.buildBody(body)
    return this.request(targetUrl, { method: 'DELETE', body: targetBody ?? '', headers })
  }
}
