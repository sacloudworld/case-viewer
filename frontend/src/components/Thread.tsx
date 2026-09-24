import { useState } from 'react'
import { formatDate, formatSize, type Attachment, type Message } from '../api'

/**
 * The messages of one case, oldest first. `viewer` decides which side is "mine":
 * customers see their INBOUND messages on the right, agents their OUTBOUND ones.
 */
export default function Thread({
  messages,
  viewer,
  onDownload,
}: {
  messages: Message[]
  viewer: 'customer' | 'agent'
  onDownload: (a: Attachment) => Promise<void>
}) {
  return (
    <ol className="thread">
      {messages.map((m) => {
        const mine = viewer === 'customer' ? m.direction === 'INBOUND' : m.direction === 'OUTBOUND'
        const sender =
          m.direction === 'OUTBOUND'
            ? viewer === 'agent'
              ? `${m.from} (agent)`
              : 'Support team'
            : viewer === 'customer'
              ? 'You'
              : m.from
        return (
          <li key={m.emailId} className={`msg ${mine ? 'msg-mine' : 'msg-theirs'}`}>
            <div className="msg-meta">
              <strong>{sender}</strong>
              <span className={`dir dir-${m.direction.toLowerCase()}`}>{m.direction}</span>
              <span className="muted">{formatDate(m.sentAt)}</span>
            </div>
            <div className="msg-bubble">
              <p className="msg-body">{m.body}</p>
              {m.attachments.length > 0 && (
                <ul className="attachments">
                  {m.attachments.map((a) => (
                    <AttachmentChip key={a.attachmentId} attachment={a} onDownload={onDownload} />
                  ))}
                </ul>
              )}
            </div>
            <div className="msg-ids muted">
              Activity #{m.activityId} · Email #{m.emailId}
            </div>
          </li>
        )
      })}
    </ol>
  )
}

function AttachmentChip({
  attachment,
  onDownload,
}: {
  attachment: Attachment
  onDownload: (a: Attachment) => Promise<void>
}) {
  const [busy, setBusy] = useState(false)
  const [failed, setFailed] = useState(false)
  return (
    <li>
      <button
        type="button"
        className="chip"
        disabled={busy}
        title={`Download ${attachment.fileName}`}
        onClick={async () => {
          setBusy(true)
          setFailed(false)
          try {
            await onDownload(attachment)
          } catch {
            setFailed(true)
          } finally {
            setBusy(false)
          }
        }}
      >
        <PaperclipIcon />
        <span className="chip-name">{attachment.fileName}</span>
        <span className="muted">{busy ? 'Downloading…' : failed ? 'Failed – retry' : formatSize(attachment.sizeBytes)}</span>
      </button>
    </li>
  )
}

export function PaperclipIcon() {
  return (
    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
      <path d="m21.4 11.1-9.2 9.2a6 6 0 0 1-8.5-8.5l9.2-9.2a4 4 0 0 1 5.7 5.7l-9.2 9.2a2 2 0 0 1-2.8-2.8l8.5-8.5" />
    </svg>
  )
}
