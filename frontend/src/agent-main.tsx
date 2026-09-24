import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import './index.css'
import AgentApp from './agent/AgentApp'

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <AgentApp />
  </StrictMode>,
)
