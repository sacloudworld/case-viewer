import { useEffect, useState } from 'react'

/** Selected conversation in the URL hash (#/1000), so it survives reloads and can be linked. */
export function useHashCase(): [number | null, (caseId: number | null) => void] {
  const parse = () => {
    const m = window.location.hash.match(/^#\/(\d+)$/)
    return m ? Number(m[1]) : null
  }
  const [caseId, setCaseId] = useState<number | null>(parse)
  useEffect(() => {
    const onHash = () => setCaseId(parse())
    window.addEventListener('hashchange', onHash)
    return () => window.removeEventListener('hashchange', onHash)
  }, [])
  const select = (id: number | null) => {
    window.location.hash = id === null ? '/' : `/${id}`
  }
  return [caseId, select]
}
