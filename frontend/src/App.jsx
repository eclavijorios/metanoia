import { useEffect } from 'react';
import BiblePanel from './components/BiblePanel';
import DevotionalPanel from './components/DevotionalPanel';
import ReferencePanel from './components/ReferencePanel';
import useStore from './store/useStore';

const NAV_ITEMS = [
  {
    id: 'biblia', label: 'Explorar Biblia', shortcut: 'B',
    icon: 'M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.747 0 3.332.477 4.5 1.253v13C19.832 18.477 18.247 18 16.5 18c-1.746 0-3.332.477-4.5 1.253',
  },
  {
    id: 'devocional', label: 'Devocional', shortcut: 'D',
    icon: 'M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z',
  },
  {
    id: 'notas', label: 'Notas / Referencias', shortcut: 'R',
    icon: 'M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10',
  },
];

function App() {
  const { activeTab, setActiveTab, visiblePanels, togglePanel, isMobile, checkMobile, contextMenu, setContextMenu } = useStore();

  useEffect(() => {
    checkMobile();
    window.addEventListener('resize', checkMobile);
    return () => window.removeEventListener('resize', checkMobile);
  }, [checkMobile]);

  useEffect(() => {
    function handleKey(e) {
      if (!e.metaKey && !e.ctrlKey) return;
      const k = e.key.toLowerCase();
      if (k === 'b') { e.preventDefault(); togglePanel('biblia'); }
      else if (k === 'd') { e.preventDefault(); togglePanel('devocional'); }
      else       if (k === 'r') { e.preventDefault(); togglePanel('notas'); }
      else if (k === 's') { e.preventDefault(); /* save devotional */ }
    }
    document.addEventListener('keydown', handleKey);
    return () => document.removeEventListener('keydown', handleKey);
  }, [togglePanel]);

  useEffect(() => {
    if (!contextMenu) return;
    function close() { setContextMenu(null); }
    document.addEventListener('scroll', close, true);
    return () => document.removeEventListener('scroll', close, true);
  }, [contextMenu, setContextMenu]);

  const panels = [
    { id: 'biblia', component: <BiblePanel /> },
    { id: 'devocional', component: <DevotionalPanel /> },
    { id: 'notas', component: <ReferencePanel /> },
  ];

  const visible = panels.filter((p) => visiblePanels.includes(p.id));
  const n = visible.length;
  let gridCols = '300px 1fr 300px';
  if (n === 1) gridCols = '1fr';
  else if (n === 2) gridCols = '1fr 1fr';
  else if (n === 3) gridCols = '300px 1fr 1fr';

  return (
    <div id="app">
      <aside id="sidebar">
        <div className="logo">🔥</div>
        {NAV_ITEMS.map((item) => (
          <button
            key={item.id}
            className={`nav-btn ${visiblePanels.includes(item.id) ? 'active' : ''}`}
            onClick={() => togglePanel(item.id)}
          >
            <svg fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24">
              <path d={item.icon} />
            </svg>
            <span className="tooltip">
              {visiblePanels.includes(item.id) ? `Cerrar ${item.label}` : `Abrir ${item.label}`}
              <span style={{ color: '#57534e' }}> ⌘{item.shortcut}</span>
            </span>
          </button>
        ))}
        <div className="spacer" />
        <button
          className="nav-btn"
          onClick={() => {}}
        >
          <svg fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24" style={{ width: 16, height: 16 }}>
            <path d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.066 2.573c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.573 1.066c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.066-2.573c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z" />
            <path d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
          </svg>
        </button>
      </aside>

      <div id="main">
        <header>
          <div className="brand">
            <h1>Metanoia</h1>
            <span className="hidden sm:inline text-[10px] uppercase tracking-[.15em] text-text-muted font-medium">Estudio Bíblico</span>
          </div>
        </header>

        <main className="panels" style={{ gridTemplateColumns: gridCols }}>
          {visible.map((p) => (
            <section key={p.id} className={`panel ${p.id === activeTab ? 'panel-active' : ''}`}>
              {p.component}
            </section>
          ))}
        </main>
      </div>

      {contextMenu && (
        <>
          <div className="context-menu-overlay" onClick={() => setContextMenu(null)} />
          <div className="context-menu" style={{ left: contextMenu.x, top: contextMenu.y }}>
            {contextMenu.items.map((item, i) =>
              item.divider ? (
                <div key={i} className="context-menu-divider" />
              ) : (
                <button
                  key={i}
                  className="context-menu-item"
                  onClick={() => { item.action?.(); setContextMenu(null); }}
                >
                  {item.icon}
                  <span>{item.label}</span>
                  {item.shortcut && <span className="shortcut">{item.shortcut}</span>}
                </button>
              )
            )}
          </div>
        </>
      )}
    </div>
  );
}

function fmt(ts) {
  const d = new Date(ts), n = new Date(), diff = n - d;
  if (diff < 60000) return 'ahora';
  if (diff < 3600000) return `${Math.floor(diff / 60000)}m`;
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}h`;
  return d.toLocaleDateString('es', { day: 'numeric', month: 'short' });
}

export default App;
