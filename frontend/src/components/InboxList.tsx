import { useEffect, useState, type ReactNode } from 'react'
import { CASE_STATUSES, formatRelative, statusLabel, type ConversationSummary } from '../api'
import StatusBadge from './StatusBadge'
import { useDebounced } from './useDebounced'

/** Searchable, filterable list of conversations, most recent activity first. */
export default function InboxList({
  load,
  onSelect,
  selectedId,
  viewer,
  emptyAction,
  refreshKey,
  compact,
}: {
  load: (q?: string, status?: string) => Promise<ConversationSummary[]>
  onSelect: (caseId: number) => void
  selectedId: number | null
  viewer: 'customer' | 'agent'
  emptyAction?: ReactNode
  refreshKey?: number
  compact?: boolean
}) {
  const [q, setQ] = useState('')
  const [status, setStatus] = useState('')
  const [awaitingOnly, setAwaitingOnly] = useState(false)
  const [rows, setRows] = useState<ConversationSummary[] | null>(null)
  const [error, setError] = useState<string | null>(null)
  const debouncedQ = useDebounced(q, 300)

  useEffect(() => {
    let cancelled = false
    setError(null)
    load(debouncedQ, status)
      .then((r) => !cancelled && setRows(r))
      .catch((err) => !cancelled && setError(err.message))
    return () => {
      cancelled = true
    }
  }, [load, debouncedQ, status, refreshKey])

  const visible = rows && awaitingOnly ? rows.filter((r) => r.awaitingReply) : rows
  const filtered = Boolean(debouncedQ || status || awaitingOnly)

  return (
    <div className={compact ? 'inbox inbox-compact' : 'inbox'}>
      <div className="toolbar">
        <input
          type="search"
          className="search"
          placeholder={viewer === 'agent' ? 'Search #, subject, customer' : 'Search case # or subject'}
          value={q}
          onChange={(e) => setQ(e.target.value)}
          aria-label="Search conversations"
        />
        <select value={status} onChange={(e) => setStatus(e.target.value)} aria-label="Filter by status">
          <option value="">All statuses</option>
          {CASE_STATUSES.map((s) => (
            <option key={s} value={s}>
              {statusLabel(s)}
            </option>
          ))}
        </select>
      </div>
      {viewer === 'agent' && (
        <label className="check">
          <input type="checkbox" checked={awaitingOnly} onChange={(e) => setAwaitingOnly(e.target.checked)} />
          Awaiting reply only
        </label>
      )}

      {error && <div className="alert alert-error">{error}</div>}

      {visible === null && !error ? (
        <div className="card empty muted">Loading…</div>
      ) : visible && visible.length === 0 ? (
        <div className="card empty">
          {filtered ? (
            <p className="muted">No conversations match.</p>
          ) : (
            <>
              <p>{viewer === 'agent' ? 'No customer messages yet.' : 'Your inbox is empty.'}</p>
              {emptyAction}
            </>
          )}
        </div>
      ) : (
        visible && (
          <ul className="card inbox-list">
            {visible.map((c) => (
              <li key={c.caseId}>
                <button
                  className={`inbox-row${c.caseId === selectedId ? ' selected' : ''}${c.awaitingReply && viewer === 'agent' ? ' unread' : ''}`}
                  onClick={() => onSelect(c.caseId)}
                  aria-current={c.caseId === selectedId ? 'true' : undefined}
                >
                  <div className="inbox-row-top">
                    <span className="inbox-subject">
                      {c.awaitingReply && viewer === 'agent' && <span className="dot" aria-label="Awaiting reply" />}
                      {c.subject}
                    </span>
                    <span className="muted small nowrap">{formatRelative(c.lastMessageAt)}</span>
                  </div>
                  <div className="inbox-row-mid muted small">
                    <span className="mono">#{c.caseId}</span>
                    {viewer === 'agent' && <span>· {c.customer}</span>}
                    <StatusBadge status={c.status} />
                  </div>
                  {c.lastPreview && (
                    <div className="inbox-preview muted small">
                      {c.lastDirection === 'OUTBOUND'
                        ? viewer === 'agent'
                          ? 'Agent: '
                          : 'Support: '
                        : viewer === 'customer'
                          ? 'You: '
                          : ''}
                      {c.lastPreview}
                    </div>
                  )}
                </button>
              </li>
            ))}
          </ul>
        )
      )}
    </div>
  )
}
