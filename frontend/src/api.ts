// Client for the case-viewer REST API. Each UI (customer inbox, agent console) keeps its
// own JWT under its own storage key, so both can be signed in from one browser.

export type CaseStatus = 'OPEN' | 'IN_PROGRESS' | 'RESOLVED' | 'CLOSED'
export type Direction = 'INBOUND' | 'OUTBOUND'
export type Role = 'USER' | 'AGENT'

export const CASE_STATUSES: CaseStatus[] = ['OPEN', 'IN_PROGRESS', 'RESOLVED', 'CLOSED']

export interface Attachment {
  attachmentId: number
  emailId: number
  activityId: number
  fileName: string
  contentType: string | null
  sizeBytes: number
}

export interface Message {
  emailId: number
  activityId: number
  direction: Direction
  from: string
  to: string
  subject: string
  body: string
  sentAt: string
  attachments: Attachment[]
}

export interface ConversationSummary {
  caseId: number
  subject: string
  status: CaseStatus
  customer: string
  createdAt: string
  lastMessageAt: string
  lastDirection: Direction | null
  lastPreview: string | null
  awaitingReply: boolean
}

export interface Conversation {
  caseId: number
  subject: string
  status: CaseStatus
  customer: string
  createdAt: string
  updatedAt: string
  messages: Message[]
}

export interface Session {
  token: string
  username: string
  expiresAt: number
}

export class ApiError extends Error {
  status: number
  constructor(status: number, message: string) {
    super(message)
    this.status = status
  }
}

const API = `${import.meta.env.BASE_URL}api`

function decodeSession(token: string): Session | null {
  try {
    const payload = JSON.parse(atob(token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/')))
    return { token, username: payload.sub, expiresAt: payload.exp * 1000 }
  } catch {
    return null
  }
}

async function readError(res: Response): Promise<string> {
  const text = await res.text()
  try {
    const body = JSON.parse(text)
    if (body && typeof body.message === 'string') return body.message
  } catch {
    /* not JSON */
  }
  return text || `Request failed (${res.status})`
}

export function createClient(tokenKey: string) {
  let onUnauthorized: () => void = () => {}

  function loadSession(): Session | null {
    let token: string | null = null
    try {
      token = localStorage.getItem(tokenKey)
    } catch {
      return null
    }
    const session = token ? decodeSession(token) : null
    if (!session || session.expiresAt <= Date.now()) {
      clearSession()
      return null
    }
    return session
  }

  function clearSession() {
    try {
      localStorage.removeItem(tokenKey)
    } catch {
      /* storage unavailable */
    }
  }

  async function send(path: string, init: RequestInit = {}, auth = true): Promise<Response> {
    const headers: Record<string, string> = {}
    // Let the browser set multipart boundaries for FormData bodies
    if (init.body && !(init.body instanceof FormData)) headers['Content-Type'] = 'application/json'
    if (auth) {
      const session = loadSession()
      if (!session) {
        onUnauthorized()
        throw new ApiError(401, 'Your session has expired. Please sign in again.')
      }
      headers.Authorization = `Bearer ${session.token}`
    }

    let res: Response
    try {
      res = await fetch(`${API}${path}`, { ...init, headers })
    } catch {
      throw new ApiError(0, 'Cannot reach the server. Is the backend running?')
    }

    if (auth && (res.status === 401 || res.status === 403)) {
      clearSession()
      onUnauthorized()
      throw new ApiError(res.status, 'Your session has expired. Please sign in again.')
    }
    if (!res.ok) throw new ApiError(res.status, await readError(res))
    return res
  }

  async function json<T>(path: string, init: RequestInit = {}, auth = true): Promise<T> {
    const res = await send(path, init, auth)
    return (await res.json()) as T
  }

  function messageForm(fields: Record<string, string>, files: File[]) {
    const form = new FormData()
    for (const [k, v] of Object.entries(fields)) form.append(k, v)
    for (const f of files) form.append('files', f)
    return form
  }

  function query(params: Record<string, string | undefined>) {
    const qs = new URLSearchParams()
    for (const [k, v] of Object.entries(params)) if (v) qs.set(k, v)
    const s = qs.toString()
    return s ? `?${s}` : ''
  }

  async function download(path: string, fileName: string) {
    const res = await send(path)
    const url = URL.createObjectURL(await res.blob())
    const a = document.createElement('a')
    a.href = url
    a.download = fileName
    document.body.appendChild(a)
    a.click()
    a.remove()
    setTimeout(() => URL.revokeObjectURL(url), 1000)
  }

  return {
    loadSession,
    clearSession,
    setUnauthorizedHandler(handler: () => void) {
      onUnauthorized = handler
    },

    async login(username: string, password: string): Promise<{ session: Session; role: Role }> {
      const { token } = await json<{ token: string }>(
        '/auth/login',
        { method: 'POST', body: JSON.stringify({ username, password }) },
        false,
      )
      const session = decodeSession(token)
      if (!session) throw new ApiError(500, 'Received an invalid token')
      const me = await fetch(`${API}/me`, { headers: { Authorization: `Bearer ${token}` } })
      if (!me.ok) throw new ApiError(me.status, await readError(me))
      const { role } = (await me.json()) as { role: Role }
      try {
        localStorage.setItem(tokenKey, token)
      } catch {
        /* storage unavailable: session lasts until reload */
      }
      return { session, role }
    },

    register(username: string, password: string) {
      return json<{ id: number; username: string }>(
        '/auth/register',
        { method: 'POST', body: JSON.stringify({ username, password }) },
        false,
      )
    },

    me() {
      return json<{ username: string; role: Role }>('/me')
    },

    // Customer secure inbox
    inbox: {
      list: (q?: string, status?: string) =>
        json<ConversationSummary[]>(`/inbox/conversations${query({ q, status })}`),
      get: (caseId: number) => json<Conversation>(`/inbox/conversations/${caseId}`),
      send: (subject: string, body: string, files: File[]) =>
        json<Conversation>('/inbox/conversations', {
          method: 'POST',
          body: messageForm({ subject, body }, files),
        }),
      reply: (caseId: number, body: string, files: File[]) =>
        json<Conversation>(`/inbox/conversations/${caseId}/messages`, {
          method: 'POST',
          body: messageForm({ body }, files),
        }),
      download: (a: Attachment) => download(`/inbox/attachments/${a.attachmentId}`, a.fileName),
    },

    // Agent console
    agent: {
      list: (q?: string, status?: string) =>
        json<ConversationSummary[]>(`/agent/conversations${query({ q, status })}`),
      get: (caseId: number) => json<Conversation>(`/agent/conversations/${caseId}`),
      reply: (caseId: number, body: string, files: File[]) =>
        json<Conversation>(`/agent/conversations/${caseId}/messages`, {
          method: 'POST',
          body: messageForm({ body }, files),
        }),
      setStatus: (caseId: number, status: CaseStatus) =>
        json<Conversation>(`/agent/conversations/${caseId}/status`, {
          method: 'PATCH',
          body: JSON.stringify({ status }),
        }),
      download: (a: Attachment) => download(`/agent/attachments/${a.attachmentId}`, a.fileName),
    },
  }
}

export type Client = ReturnType<typeof createClient>

export function formatDate(iso: string | null | undefined) {
  if (!iso) return '—'
  return new Date(iso).toLocaleString(undefined, { dateStyle: 'medium', timeStyle: 'short' })
}

export function formatRelative(iso: string) {
  const d = new Date(iso)
  const now = new Date()
  if (d.toDateString() === now.toDateString()) {
    return d.toLocaleTimeString(undefined, { hour: 'numeric', minute: '2-digit' })
  }
  return d.toLocaleDateString(undefined, { day: 'numeric', month: 'short' })
}

export function formatSize(bytes: number) {
  if (bytes < 1024) return `${bytes} B`
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`
  return `${(bytes / 1024 / 1024).toFixed(1)} MB`
}

export function statusLabel(status: string) {
  return status.replace('_', ' ').toLowerCase().replace(/^\w/, (c) => c.toUpperCase())
}
