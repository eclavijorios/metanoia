import { useState, useEffect, useCallback } from 'react';
import useStore from '../store/useStore';

function DevotionalPanel() {
  const {
    activeVerse, saved, addSaved, loadSavedItem,
    devTitle, devBody, setDevTitle, setDevBody,
  } = useStore();

  const [saveStatus, setSaveStatus] = useState('');

  const wordCount = devBody.trim() ? devBody.trim().split(/\s+/).length : 0;

  const autoSave = useCallback(() => {
    const d = { title: devTitle, body: devBody, verse: activeVerse, updated: Date.now() };
    localStorage.setItem('metanoiaLastDevotional', JSON.stringify(d));
    setSaveStatus('✓ Auto-guardado');
    setTimeout(() => setSaveStatus(''), 2000);
  }, [devTitle, devBody, activeVerse]);

  useEffect(() => {
    const last = localStorage.getItem('metanoiaLastDevotional');
    if (last) {
      try {
        const d = JSON.parse(last);
        if (!devTitle && !devBody) {
          setDevTitle(d.title || '');
          setDevBody(d.body || '');
        }
      } catch { /* ignore */ }
    }
  }, []);

  useEffect(() => {
    if (!devTitle && !devBody) return;
    const timer = setTimeout(autoSave, 800);
    return () => clearTimeout(timer);
  }, [devTitle, devBody, autoSave]);

  function handleSave() {
    const t = devTitle.trim();
    if (!t) {
      setSaveStatus('✗ Escribe un título');
      setTimeout(() => setSaveStatus(''), 2000);
      return;
    }
    const d = {
      id: Date.now().toString(36),
      title: devTitle,
      body: devBody,
      verse: activeVerse,
      verseRef: activeVerse?.ref || '',
      created: Date.now(),
      updated: Date.now(),
    };
    addSaved(d);
    localStorage.setItem('metanoiaLastDevotional', JSON.stringify(d));
    setSaveStatus(`✓ «${t}» guardado`);
    setTimeout(() => setSaveStatus(''), 2500);
  }

  function handleLoad(id) {
    loadSavedItem(id);
    setSaveStatus('✓ Cargado');
    setTimeout(() => setSaveStatus(''), 2000);
  }

  function handleNew() {
    if (devTitle || devBody) {
      if (!confirm('¿Descartar el devocional actual?')) return;
    }
    setDevTitle('');
    setDevBody('');
    setSaveStatus('');
  }

  return (
    <>
      <div className="panel-header flex items-center justify-between">
        <h2 className="text-xs font-semibold uppercase tracking-[.12em] text-text-muted">Devocional</h2>
        <div className="flex items-center gap-2">
          {saveStatus && (
            <span className="text-[10px] text-text-muted">{saveStatus}</span>
          )}
          <button
            onClick={handleSave}
            className="flex items-center gap-1.5 text-xs font-medium bg-accent text-[#0c0a09] px-3 py-1.5 rounded-md hover:bg-accent-light transition-colors"
          >
            <svg className="w-3.5 h-3.5" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24"><path d="M5 13l4 4L19 7" /></svg>
            Guardar
          </button>
        </div>
      </div>

      <div className="p-5 flex flex-col h-full">
        <input
          type="text"
          placeholder="Título del devocional…"
          value={devTitle}
          onChange={(e) => setDevTitle(e.target.value)}
          className="editor-input text-2xl font-display font-semibold mb-1 py-2"
        />

        <div className="flex items-center gap-2 mb-4 text-[11px] text-text-muted">
          <span className="italic">
            {activeVerse?.ref ? (
              <span className="text-accent">{activeVerse.ref}</span>
            ) : (
              'Sin versículo seleccionado'
            )}
          </span>
          <span className="ml-auto">{wordCount} palabras</span>
        </div>

        <textarea
          placeholder="Escribe aquí tu devocional…"
          value={devBody}
          onChange={(e) => setDevBody(e.target.value)}
          className="editor-input flex-1 text-[15px] leading-relaxed py-2"
          style={{ color: '#d6d3d1', minHeight: 200, lineHeight: 1.8 }}
        />

        <div className="mt-4 border-t border-border pt-3">
          <h3 className="text-[10px] uppercase tracking-wider text-text-muted mb-2 font-semibold">Guardados</h3>
          {saved.length === 0 ? (
            <div className="text-xs text-text-muted py-2 text-center">Aún no hay devocionales guardados</div>
          ) : (
            <div className="space-y-0">
              {saved.slice(0, 5).map((d) => (
                <div key={d.id} className="saved-item" onClick={() => handleLoad(d.id)}>
                  <div className="flex items-center justify-between">
                    <span className="text-sm font-medium text-text">{d.title}</span>
                    <span className="saved-date">{fmt(d.created)}</span>
                  </div>
                  {d.verseRef && (
                    <div className="text-[10px] text-accent mt-0.5">{d.verseRef}</div>
                  )}
                  <div className="saved-preview mt-0.5">{trunc(d.body || '', 80)}</div>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </>
  );
}

function fmt(ts) {
  const d = new Date(ts), n = new Date(), diff = n - d;
  if (diff < 60000) return 'ahora';
  if (diff < 3600000) return `${Math.floor(diff / 60000)}m`;
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}h`;
  return d.toLocaleDateString('es', { day: 'numeric', month: 'short' });
}

function trunc(t, m) {
  return t.length <= m ? t : t.slice(0, m) + '…';
}

export default DevotionalPanel;
