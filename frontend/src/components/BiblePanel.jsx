import { useEffect, useCallback, useState, useRef } from 'react';
import useStore from '../store/useStore';
import { getPassage, searchBible } from '../lib/api';

const BOOKS = [
  { id: 'Gen', n: 'Génesis', ch: 50, t: 'AT' },
  { id: 'Exod', n: 'Éxodo', ch: 40, t: 'AT' },
  { id: 'Lev', n: 'Levítico', ch: 27, t: 'AT' },
  { id: 'Num', n: 'Números', ch: 36, t: 'AT' },
  { id: 'Deut', n: 'Deuteronomio', ch: 34, t: 'AT' },
  { id: 'Josh', n: 'Josué', ch: 24, t: 'AT' },
  { id: 'Judg', n: 'Jueces', ch: 21, t: 'AT' },
  { id: 'Ruth', n: 'Rut', ch: 4, t: 'AT' },
  { id: '1Sam', n: '1 Samuel', ch: 31, t: 'AT' },
  { id: '2Sam', n: '2 Samuel', ch: 24, t: 'AT' },
  { id: '1Kgs', n: '1 Reyes', ch: 22, t: 'AT' },
  { id: '2Kgs', n: '2 Reyes', ch: 25, t: 'AT' },
  { id: '1Chr', n: '1 Crónicas', ch: 29, t: 'AT' },
  { id: '2Chr', n: '2 Crónicas', ch: 36, t: 'AT' },
  { id: 'Ezra', n: 'Esdras', ch: 10, t: 'AT' },
  { id: 'Neh', n: 'Nehemías', ch: 13, t: 'AT' },
  { id: 'Esth', n: 'Ester', ch: 10, t: 'AT' },
  { id: 'Job', n: 'Job', ch: 42, t: 'AT' },
  { id: 'Ps', n: 'Salmos', ch: 150, t: 'AT' },
  { id: 'Prov', n: 'Proverbios', ch: 31, t: 'AT' },
  { id: 'Eccl', n: 'Eclesiastés', ch: 12, t: 'AT' },
  { id: 'Song', n: 'Cantares', ch: 8, t: 'AT' },
  { id: 'Isa', n: 'Isaías', ch: 66, t: 'AT' },
  { id: 'Jer', n: 'Jeremías', ch: 52, t: 'AT' },
  { id: 'Lam', n: 'Lamentaciones', ch: 5, t: 'AT' },
  { id: 'Ezek', n: 'Ezequiel', ch: 48, t: 'AT' },
  { id: 'Dan', n: 'Daniel', ch: 12, t: 'AT' },
  { id: 'Hos', n: 'Oseas', ch: 14, t: 'AT' },
  { id: 'Joel', n: 'Joel', ch: 3, t: 'AT' },
  { id: 'Amos', n: 'Amós', ch: 9, t: 'AT' },
  { id: 'Obad', n: 'Abdías', ch: 1, t: 'AT' },
  { id: 'Jonah', n: 'Jonás', ch: 4, t: 'AT' },
  { id: 'Mic', n: 'Miqueas', ch: 7, t: 'AT' },
  { id: 'Nah', n: 'Nahúm', ch: 3, t: 'AT' },
  { id: 'Hab', n: 'Habacuc', ch: 3, t: 'AT' },
  { id: 'Zeph', n: 'Sofonías', ch: 3, t: 'AT' },
  { id: 'Hag', n: 'Hageo', ch: 2, t: 'AT' },
  { id: 'Zech', n: 'Zacarías', ch: 14, t: 'AT' },
  { id: 'Mal', n: 'Malaquías', ch: 4, t: 'AT' },
  { id: 'Matt', n: 'Mateo', ch: 28, t: 'NT' },
  { id: 'Mark', n: 'Marcos', ch: 16, t: 'NT' },
  { id: 'Luke', n: 'Lucas', ch: 24, t: 'NT' },
  { id: 'John', n: 'Juan', ch: 21, t: 'NT' },
  { id: 'Acts', n: 'Hechos', ch: 28, t: 'NT' },
  { id: 'Rom', n: 'Romanos', ch: 16, t: 'NT' },
  { id: '1Cor', n: '1 Corintios', ch: 16, t: 'NT' },
  { id: '2Cor', n: '2 Corintios', ch: 13, t: 'NT' },
  { id: 'Gal', n: 'Gálatas', ch: 6, t: 'NT' },
  { id: 'Eph', n: 'Efesios', ch: 6, t: 'NT' },
  { id: 'Phil', n: 'Filipenses', ch: 4, t: 'NT' },
  { id: 'Col', n: 'Colosenses', ch: 4, t: 'NT' },
  { id: '1Thess', n: '1 Tesalonicenses', ch: 5, t: 'NT' },
  { id: '2Thess', n: '2 Tesalonicenses', ch: 3, t: 'NT' },
  { id: '1Tim', n: '1 Timoteo', ch: 6, t: 'NT' },
  { id: '2Tim', n: '2 Timoteo', ch: 4, t: 'NT' },
  { id: 'Titus', n: 'Tito', ch: 3, t: 'NT' },
  { id: 'Phlm', n: 'Filemón', ch: 1, t: 'NT' },
  { id: 'Heb', n: 'Hebreos', ch: 13, t: 'NT' },
  { id: 'Jas', n: 'Santiago', ch: 5, t: 'NT' },
  { id: '1Pet', n: '1 Pedro', ch: 5, t: 'NT' },
  { id: '2Pet', n: '2 Pedro', ch: 3, t: 'NT' },
  { id: '1John', n: '1 Juan', ch: 5, t: 'NT' },
  { id: '2John', n: '2 Juan', ch: 1, t: 'NT' },
  { id: '3John', n: '3 Juan', ch: 1, t: 'NT' },
  { id: 'Jude', n: 'Judas', ch: 1, t: 'NT' },
  { id: 'Rev', n: 'Apocalipsis', ch: 22, t: 'NT' },
];

const BMAP = Object.fromEntries(BOOKS.map((b) => [b.id, b]));

function BiblePanel() {
  const {
    bibleVersion, setBibleVersion,
    bibleMode, setBibleMode,
    selBook, setSelBook,
    selCh, setSelCh,
    search, setSearch,
    chapterVerses, setChapterVerses,
    setActiveVerse, activeVerse,
    setContextMenu,
    togglePanel,
    comments,
  } = useStore();

  const [verseResults, setVerseResults] = useState([]);
  const searchTimer = useRef(null);
  const searchRef = useRef(null);
  const firstMatchRef = useRef(null);

  const isLocalSearch = selCh && search.trim().length > 0;

  useEffect(() => {
    if (selBook && selCh) {
      getPassage(selBook, selCh, { version: bibleVersion })
        .then((verses) => {
          setChapterVerses(verses || []);
          if (verses?.length > 0) {
            const v = verses[0];
            const bookName = BMAP[selBook]?.n || selBook;
            setActiveVerse({
              ref: `${bookName} ${selCh}:${v.verse}`,
              text: v.text,
              xrefs: [],
            });
          } else {
            setActiveVerse(null);
          }
        })
        .catch(() => {
          setChapterVerses([]);
          setActiveVerse(null);
        });
    }
  }, [selBook, selCh, bibleVersion, setChapterVerses, setActiveVerse]);

  useEffect(() => {
    if (!search.trim() || isLocalSearch) { setVerseResults([]); return; }
    if (searchTimer.current) clearTimeout(searchTimer.current);
    searchTimer.current = setTimeout(() => {
      searchBible(search, bibleVersion)
        .then(setVerseResults)
        .catch(() => setVerseResults([]));
    }, 300);
    return () => { if (searchTimer.current) clearTimeout(searchTimer.current); };
  }, [search, bibleVersion, isLocalSearch]);

  useEffect(() => {
    if (isLocalSearch && firstMatchRef.current) {
      firstMatchRef.current.scrollIntoView({ block: 'center', behavior: 'smooth' });
    }
  }, [search, chapterVerses, isLocalSearch]);

  function selectBook(id) {
    setSelBook(id);
    setSelCh('');
    setBibleMode('chapters');
    setChapterVerses([]);
    setActiveVerse(null);
  }

  function selectCh(ch) {
    setSelCh(ch);
  }

  function selectVerse(v) {
    const bookName = BMAP[selBook]?.n || selBook;
    setActiveVerse({
      ref: `${bookName} ${selCh}:${v.verse}`,
      text: v.text,
      xrefs: [],
    });
  }

  function goToVerse(v) {
    const book = BOOKS.find((b) => b.n === v.bookName);
    if (!book) return;
    setSearch('');
    setVerseResults([]);
    setSelBook(book.id);
    setSelCh(v.chapter);
    setBibleMode('chapters');
    setActiveVerse({
      ref: `${v.bookName} ${v.chapter}:${v.verse}`,
      text: v.text,
      xrefs: [],
    });
  }

  function clearSearch() {
    setSearch('');
    setVerseResults([]);
    searchRef.current?.focus();
  }

  const handleContextMenu = useCallback((e, v) => {
    e.preventDefault();
    const bookName = BMAP[selBook]?.n || selBook;
    const ref = `${bookName} ${selCh}:${v.verse}`;
    setContextMenu({
      x: e.clientX,
      y: e.clientY,
      items: [
        {
          label: 'Copiar referencia',
          icon: <svg fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24"><path d="M8 16H6a2 2 0 01-2-2V6a2 2 0 012-2h8a2 2 0 012 2v2m-6 12h8a2 2 0 002-2v-8a2 2 0 00-2-2h-8a2 2 0 00-2 2v8a2 2 0 002 2z" /></svg>,
          shortcut: '⌘C',
          action: () => navigator.clipboard.writeText(ref),
        },
        { divider: true },
        {
          label: 'Añadir comentario',
          icon: <svg fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24"><path d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z" /></svg>,
          action: () => {
            togglePanel('notas');
            setTimeout(() => {
              const input = document.getElementById('commentInput');
              if (input) input.focus();
            }, 100);
          },
        },
        {
          label: 'Agregar a devocional',
          icon: <svg fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24"><path d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" /></svg>,
          action: () => selectVerse(v),
        },
        { divider: true },
        {
          label: 'Comparar versiones',
          icon: <svg fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24"><path d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" /></svg>,
          action: () => {
            bibleVersion === 'fbv' ? setBibleVersion('rv1909') : setBibleVersion('fbv');
          },
        },
      ],
    });
  }, [selBook, selCh, setContextMenu, bibleVersion, setBibleVersion, togglePanel]);

  const verseComments = (v) => {
    const bookName = BMAP[selBook]?.n || selBook;
    const ref = `${bookName} ${selCh}:${v.verse}`;
    return comments.filter((c) => c.ref === ref);
  };

  const showChapters = bibleMode === 'chapters' && selBook;
  const showVerses = selBook && selCh && chapterVerses.length > 0;
  const isGlobalSearch = !isLocalSearch && search.trim().length > 0;

  const filteredVerses = isLocalSearch
    ? chapterVerses.filter((v) => v.text.toLowerCase().includes(search.toLowerCase()))
    : chapterVerses;

  const searchPlaceholder = isLocalSearch
    ? 'Buscar en este capítulo…'
    : 'Buscar en toda la Biblia…';

  return (
    <>
      <div className="panel-header" style={{ flexDirection: 'column', alignItems: 'stretch', gap: 8 }}>
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-1 text-xs min-w-0">
            <button
              onClick={() => { setSelBook(''); setSelCh(''); setBibleMode('books'); setChapterVerses([]); setActiveVerse(null); }}
              className="font-semibold uppercase tracking-[.12em] text-text-muted shrink-0 hover:text-accent transition-colors"
            >Biblia</button>
            {selBook && (
              <>
                <span className="text-text-muted shrink-0">›</span>
                <button
                  onClick={() => { setSelCh(''); setBibleMode('chapters'); setChapterVerses([]); setActiveVerse(null); }}
                  className={`truncate hover:text-accent transition-colors ${!selCh ? 'text-accent font-medium' : 'text-text-secondary'}`}
                >
                  {BMAP[selBook]?.n || selBook}
                </button>
              </>
            )}
            {selCh && (
              <>
                <span className="text-text-muted shrink-0">›</span>
                <button
                  onClick={() => { setSelCh(''); setChapterVerses([]); setActiveVerse(null); }}
                  className={`shrink-0 hover:text-accent transition-colors ${!activeVerse ? 'text-accent font-medium' : 'text-text-secondary'}`}
                >
                  {selCh}
                </button>
              </>
            )}
            {activeVerse && (
              <>
                <span className="text-text-muted shrink-0">›</span>
                <span className="text-accent font-medium shrink-0">
                  v.{activeVerse.ref.split(':').pop()}
                </span>
              </>
            )}
          </div>
          <div className="version-toggle">
            <button className={bibleVersion === 'fbv' ? 'active' : ''} onClick={() => setBibleVersion('fbv')}>FBV</button>
            <button className={bibleVersion === 'rv1909' ? 'active' : ''} onClick={() => setBibleVersion('rv1909')}>RV09</button>
          </div>
        </div>
        <div className="flex items-center gap-2 bg-surface rounded-lg border border-border px-3 py-1.5 focus-within:border-accent/40 focus-within:ring-1 focus-within:ring-accent/20 transition-all">
          <svg className="w-3.5 h-3.5 text-text-muted shrink-0" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24"><path d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" /></svg>
          <input
            ref={searchRef}
            type="text"
            placeholder={searchPlaceholder}
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            className="flex-1 bg-transparent text-sm text-text outline-none placeholder-text-muted"
          />
          {search && (
            <button onClick={clearSearch} className="text-text-muted hover:text-text text-xs px-1">
              ✕
            </button>
          )}
        </div>
      </div>

      <div className="pb-4">
        {isGlobalSearch ? (
          <SearchResults
            query={search.toLowerCase()}
            selBook={selBook}
            verseResults={verseResults}
            onSelectBook={(id) => { clearSearch(); selectBook(id); }}
            onSelectVerse={goToVerse}
          />
        ) : showVerses || (isLocalSearch && chapterVerses.length > 0) ? (
          <ChapterVerses
            verses={filteredVerses}
            allVerses={chapterVerses}
            activeVerse={activeVerse}
            searchQuery={isLocalSearch ? search.toLowerCase() : ''}
            onSelectVerse={selectVerse}
            onContextMenu={handleContextMenu}
            getComments={verseComments}
            firstMatchRef={firstMatchRef}
          />
        ) : showChapters && !selCh ? (
          <ChapterGrid bookId={selBook} selCh={selCh} onSelectCh={selectCh} />
        ) : (
          <BookList selBook={selBook} onSelectBook={(id) => { clearSearch(); selectBook(id); }} />
        )}
        {isLocalSearch && filteredVerses.length === 0 && (
          <div className="px-4 py-8 text-center text-text-muted text-sm">
            No hay versículos con «{search}» en este capítulo
          </div>
        )}
        {showChapters && selCh && chapterVerses.length === 0 && !isLocalSearch && (
          <div className="px-4 py-8 text-center text-text-muted text-sm">Cargando versículos…</div>
        )}
      </div>
    </>
  );
}

function BookList({ selBook, onSelectBook }) {
  const sections = [
    { title: 'Antiguo Testamento', books: BOOKS.filter((b) => b.t === 'AT') },
    { title: 'Nuevo Testamento', books: BOOKS.filter((b) => b.t === 'NT') },
  ];
  return sections.map((s) => (
    <div key={s.title}>
      <div className="px-4 pt-4 pb-1 text-[10px] uppercase tracking-[.12em] text-text-muted font-semibold">{s.title}</div>
      <div className="px-3 pb-2">
        {s.books.map((b) => (
          <div
            key={b.id}
            className={`book-item flex items-center justify-between px-3 py-1.5 text-sm ${b.id === selBook ? 'selected' : 'text-text-secondary'}`}
            onClick={() => onSelectBook(b.id)}
          >
            <span>{b.n}</span>
            <span className="text-[10px] text-text-muted">{b.ch} cap.</span>
          </div>
        ))}
      </div>
    </div>
  ));
}

function ChapterGrid({ bookId, selCh, onSelectCh }) {
  const b = BMAP[bookId];
  if (!b) return null;
  return (
    <>
      <div className="px-4 pt-2 pb-1 flex items-center gap-2">
        <span className="text-sm font-medium text-text">{b.n}</span>
        <span className="text-[10px] text-text-muted ml-auto">{b.ch} capítulos</span>
      </div>
      <div className="chapter-grid">
        {Array.from({ length: b.ch }, (_, i) => i + 1).map((ch) => (
          <button
            key={ch}
            className={`chapter-btn ${String(selCh) === String(ch) ? 'active' : ''}`}
            onClick={() => onSelectCh(ch)}
          >
            {ch}
          </button>
        ))}
      </div>
    </>
  );
}

function ChapterVerses({ verses, allVerses, activeVerse, searchQuery, onSelectVerse, onContextMenu, getComments, firstMatchRef }) {
  const isSearching = searchQuery.length > 0;
  const total = allVerses?.length || verses.length;

  return (
    <div className="px-4 space-y-1">
      {isSearching && (
        <div className="text-[11px] text-text-muted py-1.5 border-b border-border mb-2">
          {verses.length} de {total} versículos
        </div>
      )}
      {verses.map((v) => {
        const refEnds = `:${v.verse}`;
        const isActive = activeVerse?.ref?.endsWith(refEnds);
        const vComments = getComments(v);
        const isDimmed = isSearching && !v.text.toLowerCase().includes(searchQuery);
        return (
          <div
            key={v.id}
            ref={isSearching && !isDimmed && firstMatchRef ? firstMatchRef : undefined}
            className={`verse-row ${isActive ? 'active' : ''} ${isDimmed ? 'dimmed' : ''}`}
            onClick={() => onSelectVerse(v)}
            onContextMenu={(e) => onContextMenu(e, v)}
          >
            <span className="verse-num">{v.verse}</span>
            <span className="verse-text">
              {isSearching && !isDimmed ? highlight(v.text, searchQuery) : v.text}
            </span>
            {vComments.length > 0 && <span className="comment-dot" />}
            <div className="verse-actions">
              <button title="Comentar" onClick={(e) => { e.stopPropagation(); onSelectVerse(v); }}>💬</button>
              <button title="Copiar referencia" onClick={(e) => { e.stopPropagation(); navigator.clipboard.writeText(activeVerse?.ref || ''); }}>📋</button>
              <button title="Más opciones" onClick={(e) => { e.stopPropagation(); onContextMenu(e, v); }}>⋯</button>
            </div>
          </div>
        );
      })}
    </div>
  );
}

function SearchResults({ query, selBook, verseResults, onSelectBook, onSelectVerse }) {
  const matchedB = BOOKS.filter((b) => b.n.toLowerCase().includes(query));
  const hasBooks = matchedB.length > 0;
  const hasVerses = verseResults.length > 0;

  if (!hasBooks && !hasVerses) {
    return (
      <div className="empty-state" style={{ paddingTop: 60 }}>
        <p className="text-sm">Sin resultados para<br />«{query}»</p>
      </div>
    );
  }

  return (
    <div>
      {hasBooks && (
        <>
          <div className="px-4 pt-3 pb-1 text-[10px] uppercase tracking-[.12em] text-text-muted font-semibold">Libros</div>
          {matchedB.map((b) => (
            <div
              key={b.id}
              className={`book-item flex items-center justify-between px-3 py-1.5 text-sm ${b.id === selBook ? 'selected' : 'text-text-secondary'}`}
              onClick={() => onSelectBook(b.id)}
            >
              <span>{highlight(b.n, query)}</span>
              <span className="text-[10px] text-text-muted">{b.ch} cap.</span>
            </div>
          ))}
        </>
      )}
      {hasVerses && (
        <>
          <div className="px-4 pt-3 pb-1 text-[10px] uppercase tracking-[.12em] text-text-muted font-semibold">Versículos</div>
          {verseResults.map((v, i) => (
            <div
              key={v.id || i}
              className="verse-item px-4 py-2"
              onClick={() => onSelectVerse(v)}
            >
              <div className="verse-ref text-sm font-display font-semibold">{highlight(`${v.bookName} ${v.chapter}:${v.verse}`, query)}</div>
              <div className="text-xs text-text-secondary mt-0.5 leading-relaxed">{highlight(trunc(v.text, 120), query)}</div>
            </div>
          ))}
        </>
      )}
    </div>
  );
}

function highlight(text, query) {
  if (!query || !text) return text;
  const r = new RegExp(`(${query.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')})`, 'gi');
  const parts = text.split(r);
  return parts.map((part, i) =>
    r.test(part) ? <mark key={i}>{part}</mark> : part
  );
}

function trunc(t, m) {
  if (!t) return '';
  return t.length <= m ? t : t.slice(0, m) + '…';
}

export default BiblePanel;
