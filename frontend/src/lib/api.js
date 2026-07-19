const BASE = '/api';

async function fetchJson(url) {
  const res = await fetch(url);
  if (!res.ok) throw new Error(`API error: ${res.status}`);
  return res.json();
}

export async function getBibleVersions() {
  return fetchJson(`${BASE}/bibles/versions`);
}

export async function getPassage(book, ch, { verse, version } = {}) {
  const params = new URLSearchParams({ book, ch });
  if (verse) params.set('v', verse);
  params.set('version', version || 'fbv');
  return fetchJson(`${BASE}/bibles/passage?${params}`);
}

export async function searchBible(q, version = 'fbv') {
  return fetchJson(`${BASE}/bibles/search?q=${encodeURIComponent(q)}&version=${version}`);
}

export async function getTodayDevotional() {
  return fetchJson(`${BASE}/devotionals/today`);
}

export async function getDevotionals(date) {
  const params = date ? `?date=${date}` : '';
  return fetchJson(`${BASE}/devotionals${params}`);
}

export async function createDevotional(date) {
  const res = await fetch(`${BASE}/devotionals`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: date ? JSON.stringify({ date }) : '{}',
  });
  return res.json();
}

export async function updateDevotional(id, { title, content }) {
  const res = await fetch(`${BASE}/devotionals/${id}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ title, content }),
  });
  return res.json();
}

export async function addVerse(devotionalId, { verseId, bibleVersionId, referenceText }) {
  const res = await fetch(`${BASE}/devotionals/${devotionalId}/verses`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ verseId, bibleVersionId, referenceText }),
  });
  return res.json();
}

export async function removeVerse(devotionalId, verseId) {
  await fetch(`${BASE}/devotionals/${devotionalId}/verses/${verseId}`, { method: 'DELETE' });
}
