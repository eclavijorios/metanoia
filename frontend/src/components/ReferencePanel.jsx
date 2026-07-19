import useStore from '../store/useStore';

function ReferencePanel() {
  const { activeVerse, comments, addComment } = useStore();

  const verseComments = activeVerse
    ? comments.filter((c) => c.ref === activeVerse.ref)
    : [];

  const allVerseComments = activeVerse
    ? comments.filter((c) => c.ref && c.ref.startsWith(activeVerse.ref.split(':').slice(0, -1).join(':')))
    : [];

  return (
    <>
      <div className="panel-header">
        <h2 className="text-xs font-semibold uppercase tracking-[.12em] text-text-muted">Notas</h2>
      </div>

      <div className="p-4 space-y-4">
        {activeVerse ? (
          <>
            <div className="verse-card animate-slide-up">
              <div className="verse-ref text-lg font-display font-semibold mb-1">{activeVerse.ref}</div>
              <p className="text-sm leading-relaxed mt-2" style={{ color: '#d6d3d1' }}>
                {activeVerse.text}
              </p>
            </div>

            {verseComments.length > 0 && (
              <div>
                <div className="text-[10px] uppercase tracking-wider text-text-muted mb-3 font-semibold flex items-center gap-1.5">
                  💬 Comentarios
                  <span className="comment-badge">{verseComments.length}</span>
                </div>
                {verseComments.map((c, i) => (
                  <div key={i} className="comment-bubble">
                    <div className="author">{c.author}</div>
                    <div className="text">{c.text}</div>
                    <div className="time">{fmt(c.time)}</div>
                  </div>
                ))}
              </div>
            )}

            <div>
              <div className="text-[10px] uppercase tracking-wider text-text-muted mb-2 font-semibold">
                {verseComments.length > 0 ? 'Añadir comentario' : 'Escribe un comentario'}
              </div>
              <div className="comment-input">
                <input
                  type="text"
                  placeholder="Escribe tu comentario…"
                  id="commentInput"
                  onKeyDown={(e) => {
                    if (e.key === 'Enter' && e.target.value.trim()) {
                      addComment({
                        author: 'Tú',
                        text: e.target.value.trim(),
                        ref: activeVerse.ref,
                        time: Date.now(),
                      });
                      e.target.value = '';
                    }
                  }}
                />
                <button onClick={() => {
                  const input = document.getElementById('commentInput');
                  if (input?.value.trim()) {
                    addComment({
                      author: 'Tú',
                      text: input.value.trim(),
                      ref: activeVerse.ref,
                      time: Date.now(),
                    });
                    input.value = '';
                  }
                }}>Enviar</button>
              </div>
            </div>
          </>
        ) : (
          <div className="empty-state">
            <svg fill="none" stroke="#57534e" strokeWidth="1.5" viewBox="0 0 24 24">
              <path d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.747 0 3.332.477 4.5 1.253v13C19.832 18.477 18.247 18 16.5 18c-1.746 0-3.332.477-4.5 1.253" />
            </svg>
            <p className="text-sm mt-2">Selecciona un versículo<br />para verlo y comentarlo</p>
          </div>
        )}
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

export default ReferencePanel;
