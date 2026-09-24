import { useCallback, useEffect, useState } from 'react'
import type { Client, Session } from '../api'

/** Tracks the signed-in session for a client; signs out on 401/403 and when the JWT expires. */
export function useSession(client: Client) {
  const [session, setSession] = useState<Session | null>(() => client.loadSession())

  const signOut = useCallback(() => {
    client.clearSession()
    setSession(null)
  }, [client])

  useEffect(() => client.setUnauthorizedHandler(signOut), [client, signOut])

  useEffect(() => {
    if (!session) return
    const timer = window.setTimeout(signOut, Math.max(0, session.expiresAt - Date.now()))
    return () => window.clearTimeout(timer)
  }, [session, signOut])

  return { session, setSession, signOut }
}
