export default function TopBar({
  title,
  username,
  badge,
  onSignOut,
}: {
  title: string
  username: string
  badge?: string
  onSignOut: () => void
}) {
  return (
    <header className="topbar">
      <a className="brand" href="#/">
        <img src={`${import.meta.env.BASE_URL}favicon.svg`} alt="" width={24} height={24} />
        {title}
        {badge && <span className="brand-badge">{badge}</span>}
      </a>
      <div className="topbar-user">
        <span className="muted">
          Signed in as <strong>{username}</strong>
        </span>
        <button className="btn btn-ghost" onClick={onSignOut}>
          Sign out
        </button>
      </div>
    </header>
  )
}
