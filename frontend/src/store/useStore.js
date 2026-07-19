import { create } from 'zustand';

const SAVED_KEY = 'metanoiaSaved';
const LAST_KEY = 'metanoiaLastDevotional';

function loadSaved() {
  try { return JSON.parse(localStorage.getItem(SAVED_KEY) || '[]'); } catch { return []; }
}

const useStore = create((set, get) => ({
  activeTab: 'devocional',
  setActiveTab: (tab) => set({ activeTab: tab }),

  visiblePanels: ['biblia', 'devocional', 'notas'],
  togglePanel: (id) => set((s) => {
    const was = s.visiblePanels;
    if (was.length <= 1 && was.includes(id)) return s;
    const next = was.includes(id) ? was.filter((p) => p !== id) : [...was, id];
    return { visiblePanels: next, activeTab: id };
  }),

  bibleVersion: 'fbv',
  setBibleVersion: (v) => set({ bibleVersion: v }),

  bibleMode: 'books',
  selBook: '',
  selCh: '',
  search: '',
  setBibleMode: (mode) => set({ bibleMode: mode }),
  setSelBook: (id) => set({ selBook: id }),
  setSelCh: (ch) => set({ selCh: ch }),
  setSearch: (q) => set({ search: q }),

  chapterVerses: [],
  setChapterVerses: (verses) => set({ chapterVerses: verses }),

  devTitle: '',
  devBody: '',
  setDevTitle: (t) => set({ devTitle: t }),
  setDevBody: (b) => set({ devBody: b }),

  activeVerse: null,
  setActiveVerse: (v) => set({ activeVerse: v }),

  saved: loadSaved(),
  addSaved: (d) => set((s) => {
    const saved = [d, ...s.saved].slice(0, 20);
    localStorage.setItem(SAVED_KEY, JSON.stringify(saved));
    return { saved };
  }),
  loadSavedItem: (id) => {
    const d = get().saved.find((x) => x.id === id);
    if (!d) return;
    set({ devTitle: d.title || '', devBody: d.body || '', activeVerse: d.verse || null });
  },

  contextMenu: null,
  setContextMenu: (menu) => set({ contextMenu: menu }),

  showComments: false,
  setShowComments: (v) => set({ showComments: v }),

  comments: [],
  addComment: (comment) => set((s) => ({ comments: [...s.comments, comment] })),

  isMobile: window.innerWidth <= 768,
  checkMobile: () => set({ isMobile: window.innerWidth <= 768 }),
}));

export default useStore;
