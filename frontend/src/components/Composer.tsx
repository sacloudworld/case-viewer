import { useRef, useState, type FormEvent } from 'react'
import { formatSize } from '../api'
import { PaperclipIcon } from './Thread'

const MAX_FILES = 5
const MAX_FILE_BYTES = 10 * 1024 * 1024

/** Message box with attachments. Clears itself after a successful send. */
export default function Composer({
  placeholder,
  submitLabel,
  onSend,
  autoFocus,
}: {
  placeholder: string
  submitLabel: string
  onSend: (body: string, files: File[]) => Promise<void>
  autoFocus?: boolean
}) {
  const [body, setBody] = useState('')
  const [files, setFiles] = useState<File[]>([])
  const [error, setError] = useState<string | null>(null)
  const [busy, setBusy] = useState(false)
  const fileInput = useRef<HTMLInputElement>(null)

  function addFiles(list: FileList | null) {
    if (!list) return
    const next = [...files]
    for (const f of Array.from(list)) {
      if (f.size > MAX_FILE_BYTES) {
        setError(`${f.name} is larger than 10 MB.`)
        continue
      }
      if (next.length >= MAX_FILES) {
        setError(`You can attach up to ${MAX_FILES} files.`)
        break
      }
      next.push(f)
    }
    setFiles(next)
    if (fileInput.current) fileInput.current.value = ''
  }

  async function submit(e: FormEvent) {
    e.preventDefault()
    if (!body.trim()) return
    setBusy(true)
    setError(null)
    try {
      await onSend(body.trim(), files)
      setBody('')
      setFiles([])
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Could not send')
    } finally {
      setBusy(false)
    }
  }

  return (
    <form className="composer" onSubmit={submit}>
      {error && <div className="alert alert-error">{error}</div>}
      <textarea
        rows={4}
        placeholder={placeholder}
        value={body}
        onChange={(e) => setBody(e.target.value)}
        maxLength={20000}
        autoFocus={autoFocus}
        aria-label="Message"
        onKeyDown={(e) => {
          if (e.key === 'Enter' && (e.metaKey || e.ctrlKey)) submit(e)
        }}
      />
      {files.length > 0 && (
        <ul className="attachments">
          {files.map((f, i) => (
            <li key={`${f.name}-${i}`} className="chip chip-static">
              <PaperclipIcon />
              <span className="chip-name">{f.name}</span>
              <span className="muted">{formatSize(f.size)}</span>
              <button
                type="button"
                className="chip-remove"
                aria-label={`Remove ${f.name}`}
                onClick={() => setFiles(files.filter((_, j) => j !== i))}
              >
                ×
              </button>
            </li>
          ))}
        </ul>
      )}
      <div className="composer-actions">
        <label className="btn btn-ghost">
          <PaperclipIcon /> Attach
          <input
            ref={fileInput}
            type="file"
            multiple
            hidden
            onChange={(e) => addFiles(e.target.files)}
          />
        </label>
        <span className="muted small hide-sm">Ctrl/⌘ + Enter to send</span>
        <button className="btn btn-primary" disabled={busy || !body.trim()}>
          {busy ? 'Sending…' : submitLabel}
        </button>
      </div>
    </form>
  )
}
