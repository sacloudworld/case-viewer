import { useState, type FormEvent } from 'react'
import { ApiError, type Client, type Role, type Session } from '../api'

export default function AuthPage({
  client,
  title,
  subtitle,
  requiredRole,
  allowRegister,
  onSignedIn,
}: {
  client: Client
  title: string
  subtitle: string
  requiredRole: Role
  allowRegister: boolean
  onSignedIn: (s: Session) => void
}) {
  const [mode, setMode] = useState<'login' | 'register'>('login')
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [notice, setNotice] = useState<string | null>(null)
  const [busy, setBusy] = useState(false)

  async function submit(e: FormEvent) {
    e.preventDefault()
    setError(null)
    setNotice(null)
    setBusy(true)
    try {
      if (mode === 'register') {
        await client.register(username.trim(), password)
        setNotice('Account created. You can sign in now.')
        setMode('login')
        setPassword('')
      } else {
        const { session, role } = await client.login(username.trim(), password)
        if (role !== requiredRole) {
          client.clearSession()
          setError(
            requiredRole === 'AGENT'
              ? 'This account is not an agent account. Customers sign in to the secure inbox.'
              : 'Agent accounts sign in to the agent console, not the customer inbox.',
          )
        } else {
          onSignedIn(session)
        }
      }
    } catch (err) {
      if (mode === 'login' && err instanceof ApiError && err.status >= 400 && err.status < 500) {
        setError('Invalid username or password.')
      } else {
        setError(err instanceof Error ? err.message : 'Something went wrong')
      }
    } finally {
      setBusy(false)
    }
  }

  function switchMode(next: 'login' | 'register') {
    setMode(next)
    setError(null)
    setNotice(null)
  }

  return (
    <div className="auth">
      <form className="card auth-card" onSubmit={submit}>
        <div className="auth-brand">
          <img src={`${import.meta.env.BASE_URL}favicon.svg`} alt="" width={36} height={36} />
          <div>
            <h1>{title}</h1>
            <p className="muted small">{subtitle}</p>
          </div>
        </div>

        {allowRegister && (
          <div className="tabs" role="tablist">
            <button
              type="button"
              role="tab"
              aria-selected={mode === 'login'}
              className={mode === 'login' ? 'tab active' : 'tab'}
              onClick={() => switchMode('login')}
            >
              Sign in
            </button>
            <button
              type="button"
              role="tab"
              aria-selected={mode === 'register'}
              className={mode === 'register' ? 'tab active' : 'tab'}
              onClick={() => switchMode('register')}
            >
              Create account
            </button>
          </div>
        )}

        {notice && <div className="alert alert-success">{notice}</div>}
        {error && <div className="alert alert-error">{error}</div>}

        <label className="field">
          <span>Username</span>
          <input
            autoFocus
            autoComplete="username"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            minLength={mode === 'register' ? 3 : undefined}
            required
          />
        </label>
        <label className="field">
          <span>Password</span>
          <input
            type="password"
            autoComplete={mode === 'login' ? 'current-password' : 'new-password'}
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            minLength={mode === 'register' ? 8 : undefined}
            required
          />
          {mode === 'register' && <small className="muted">At least 8 characters.</small>}
        </label>

        <button className="btn btn-primary btn-block" disabled={busy}>
          {busy ? 'Please wait…' : mode === 'login' ? 'Sign in' : 'Create account'}
        </button>
      </form>
    </div>
  )
}
