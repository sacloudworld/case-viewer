import { useState } from 'react'
import Modal from '../components/Modal'
import Composer from '../components/Composer'

export default function NewMessageModal({
  onClose,
  onSend,
}: {
  onClose: () => void
  onSend: (subject: string, body: string, files: File[]) => Promise<void>
}) {
  const [subject, setSubject] = useState('')

  return (
    <Modal title="New message to support" onClose={onClose}>
      <label className="field">
        <span>Subject</span>
        <input
          autoFocus
          value={subject}
          onChange={(e) => setSubject(e.target.value)}
          maxLength={200}
          placeholder="What is this about?"
        />
      </label>
      <Composer
        placeholder="Describe your question or issue…"
        submitLabel="Send message"
        onSend={async (body, files) => {
          // Composer shows the error message
          if (!subject.trim()) throw new Error('Please add a subject.')
          await onSend(subject.trim(), body, files)
        }}
      />
    </Modal>
  )
}
