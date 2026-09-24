import { useEffect, useState } from 'react'
import { CASE_STATUSES, createClient, formatDate, statusLabel, type CaseStatus, type Conversation } from '../api'
import AuthPage from '../components/AuthPage'
import { useSession } from '../components/useSession'
import { useHashCase } from '../components/useHashCase'
import TopBar from '../components/TopBar'
import InboxList from '../components/InboxList'
import Thread from '../components/Thread'
import Composer from '../components/Composer'
import StatusBadge from '../components/StatusBadge'

const client = createClient('case-viewer.agent.token')

export default function AgentApp() {
  const { session, setSession, signOut } = useSession(client)
  const [caseId, selectCase] = useHashCase()
  const [refreshKey, setRefreshKey] = useState(0)

  if (!session) {
    return (
      <AuthPage
        client={client}
        title="Agent Console"
        subtitle="Reply to customer messages"
        requiredRole="AGENT"
        allowRegister={false}
        onSignedIn={setSession}
      />
    )
  }

  return (
    <div className="shell shell-agent">
      <TopBar title="Agent Console" badge="Agent" username={session.username} onSignOut={signOut} />
      <div className={`split${caseId !== null ? ' has-selection' : ''}`}>
        <aside className="split-list">
          <InboxList
            load={client.agent.list}
            onSelect={selectCase}
            selectedId={caseId}
            viewer="agent"
            refreshKey={refreshKey}
            compact
          />
        </aside>
        <main className="split-detail">
          {caseId === null ? (
            <div className="card empty muted">Select a conversation to read and reply.</div>
          ) : (
            <AgentConversation
              key={caseId}
              caseId={caseId}
              onBack={() => selectCase(null)}
              onChanged={() => setRefreshKey((n) => n + 1)}
            />
          )}
        </main>
      </div>
    </div>
  )
}

function AgentConversation({
  caseId,
  onBack,
  onChanged,
}: {
  caseId: number
  onBack: () => void
  onChanged: () => void
}) {
  const [conversation, setConversation] = useState<Conversation | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [statusError, setStatusError] = useState<string | null>(null)

  useEffect(() => {
    client.agent
      .get(caseId)
      .then(setConversation)
      .catch((err) => setError(err.message))
  }, [caseId])

  async function changeStatus(status: CaseStatus) {
    setStatusError(null)
    try {
      setConversation(await client.agent.setStatus(caseId, status))
      onChanged()
    } catch (err) {
      setStatusError(err instanceof Error ? err.message : 'Could not update status')
    }
  }

  if (error) {
    return (
      <div className="card empty">
        <p>{error}</p>
      </div>
    )
  }
  if (!conversation) return <div className="card empty muted">Loading conversation…</div>

  return (
    <section className="card conversation">
      <button className="back link-btn show-sm" onClick={onBack}>
        ← All conversations
      </button>
      <header className="conversation-head">
        <div>
          <div className="mono muted small">
            Case #{conversation.caseId} · Customer <strong>{conversation.customer}</strong> · Opened{' '}
            {formatDate(conversation.createdAt)}
          </div>
          <h1>{conversation.subject}</h1>
        </div>
        <div className="status-control">
          <StatusBadge status={conversation.status} />
          <select
            value={conversation.status}
            onChange={(e) => changeStatus(e.target.value as CaseStatus)}
            aria-label="Change case status"
          >
            {CASE_STATUSES.map((s) => (
              <option key={s} value={s}>
                {statusLabel(s)}
              </option>
            ))}
          </select>
        </div>
      </header>
      {statusError && <div className="alert alert-error">{statusError}</div>}
      <Thread messages={conversation.messages} viewer="agent" onDownload={client.agent.download} />
      <div className="conversation-reply">
        <Composer
          placeholder={`Reply to ${conversation.customer}…`}
          submitLabel="Send reply"
          onSend={async (body, files) => {
            setConversation(await client.agent.reply(caseId, body, files))
            onChanged()
          }}
        />
      </div>
    </section>
  )
}
