import { useEffect, useState } from 'react'
import { createClient, type Conversation } from '../api'
import AuthPage from '../components/AuthPage'
import { useSession } from '../components/useSession'
import { useHashCase } from '../components/useHashCase'
import TopBar from '../components/TopBar'
import InboxList from '../components/InboxList'
import Thread from '../components/Thread'
import Composer from '../components/Composer'
import StatusBadge from '../components/StatusBadge'
import NewMessageModal from './NewMessageModal'

const client = createClient('case-viewer.customer.token')

export default function CustomerApp() {
  const { session, setSession, signOut } = useSession(client)
  const [caseId, selectCase] = useHashCase()
  const [composing, setComposing] = useState(false)
  const [reload, setReload] = useState(0)

  if (!session) {
    return (
      <AuthPage
        client={client}
        title="Secure Inbox"
        subtitle="Message our support team securely"
        requiredRole="USER"
        allowRegister
        onSignedIn={setSession}
      />
    )
  }

  return (
    <div className="shell">
      <TopBar title="Secure Inbox" username={session.username} onSignOut={signOut} />
      <main className="content">
        {caseId === null ? (
          <>
            <div className="page-head">
              <div>
                <h1>Messages</h1>
                <p className="muted">Each new message opens a support case. Replies stay on the same case.</p>
              </div>
              <button className="btn btn-primary" onClick={() => setComposing(true)}>
                + New message
              </button>
            </div>
            <InboxList
              key={reload}
              load={client.inbox.list}
              onSelect={selectCase}
              selectedId={null}
              viewer="customer"
              emptyAction={
                <button className="btn btn-primary" onClick={() => setComposing(true)}>
                  Write your first message
                </button>
              }
            />
          </>
        ) : (
          <CustomerConversation key={caseId} caseId={caseId} onBack={() => selectCase(null)} />
        )}
      </main>

      {composing && (
        <NewMessageModal
          onClose={() => setComposing(false)}
          onSend={async (subject, body, files) => {
            const created = await client.inbox.send(subject, body, files)
            setComposing(false)
            setReload((n) => n + 1)
            selectCase(created.caseId)
          }}
        />
      )}
    </div>
  )
}

function CustomerConversation({ caseId, onBack }: { caseId: number; onBack: () => void }) {
  const [conversation, setConversation] = useState<Conversation | null>(null)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    client.inbox
      .get(caseId)
      .then(setConversation)
      .catch((err) => setError(err.message))
  }, [caseId])

  return (
    <>
      <button className="back link-btn" onClick={onBack}>
        ← All messages
      </button>
      {error ? (
        <div className="card empty">
          <p>{error.startsWith('Case not found') ? `Case ${caseId} was not found in your inbox.` : error}</p>
        </div>
      ) : !conversation ? (
        <div className="card empty muted">Loading conversation…</div>
      ) : (
        <section className="card conversation">
          <header className="conversation-head">
            <div>
              <div className="mono muted small">Case #{conversation.caseId}</div>
              <h1>{conversation.subject}</h1>
            </div>
            <StatusBadge status={conversation.status} />
          </header>
          <Thread messages={conversation.messages} viewer="customer" onDownload={client.inbox.download} />
          <div className="conversation-reply">
            <Composer
              placeholder="Write a reply to the support team…"
              submitLabel="Send reply"
              onSend={async (body, files) => setConversation(await client.inbox.reply(caseId, body, files))}
            />
          </div>
        </section>
      )}
    </>
  )
}
