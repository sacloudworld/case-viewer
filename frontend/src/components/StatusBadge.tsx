import { statusLabel } from '../api'

export default function StatusBadge({ status }: { status: string }) {
  return <span className={`badge badge-${status.toLowerCase()}`}>{statusLabel(status)}</span>
}
