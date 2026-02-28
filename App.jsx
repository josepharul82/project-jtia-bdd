/**
 * src/App.jsx  –  Example showing BFF integration with native fetch
 *
 * The only changes from your existing App.jsx are:
 *   1. Wrap the root with <AuthGuard>
 *   2. Replace fetch/axios calls with bffFetch from bffClient.js
 *   3. Place <UserBadge /> wherever you want the user name + logout button
 *
 * Everything else (routing, pages, components, state) stays the same.
 */

import React, { useEffect, useState } from 'react';
import { AuthGuard, UserBadge } from './components/AuthGuard';
import { bffFetch, bffPost, bffDelete } from './api/bffClient';

// ─── Example page ─────────────────────────────────────────────────────────────

function ProjetGroupsPage() {
  const [groups,  setGroups]  = useState([]);
  const [loading, setLoading] = useState(true);
  const [error,   setError]   = useState(null);

  useEffect(() => {
    // GET — returns parsed JSON directly
    bffFetch('/api/v1/projetGroups')
      .then((data) => setGroups(data))
      .catch((err) => {
        // 401 is handled by bffFetch (auto-redirect to IdP).
        // Only real errors (500, network failure) land here.
        setError(err.message);
      })
      .finally(() => setLoading(false));
  }, []);

  // Example: create a new group
  async function handleCreate() {
    try {
      const created = await bffPost('/api/v1/projetGroups', { name: 'Nouveau groupe' });
      setGroups((prev) => [...prev, created]);
    } catch (err) {
      console.error('Create failed:', err.message);
    }
  }

  // Example: delete a group
  async function handleDelete(id) {
    try {
      await bffDelete(`/api/v1/projetGroups/${id}`);
      setGroups((prev) => prev.filter((g) => g.id !== id));
    } catch (err) {
      console.error('Delete failed:', err.message);
    }
  }

  if (loading) return <p>Loading…</p>;
  if (error)   return <p style={{ color: 'red' }}>Error: {error}</p>;

  return (
    <>
      <button onClick={handleCreate}>+ Nouveau groupe</button>
      <ul>
        {groups.map((g) => (
          <li key={g.id}>
            {g.name}
            <button onClick={() => handleDelete(g.id)} style={{ marginLeft: 8 }}>
              Supprimer
            </button>
          </li>
        ))}
      </ul>
    </>
  );
}

// ─── App root ─────────────────────────────────────────────────────────────────

export default function App() {
  return (
    <AuthGuard>
      <header style={{ display: 'flex', justifyContent: 'space-between', padding: '16px', borderBottom: '1px solid #e2e8f0' }}>
        <h1 style={{ margin: 0 }}>My App</h1>
        <UserBadge />
      </header>

      <main style={{ padding: '24px' }}>
        <h2>Projet Groups</h2>
        <ProjetGroupsPage />
      </main>
    </AuthGuard>
  );
}

/*
 ┌─────────────────────────────────────────────────────────────────────────────┐
 │  MIGRATION SUMMARY                                                          │
 │                                                                             │
 │  Files to CREATE:                                                           │
 │    src/api/bffClient.js            ← replaces axios entirely               │
 │    src/hooks/useAuth.js            ← session check + logout                │
 │    src/components/AuthGuard.jsx    ← protects the app                      │
 │                                                                             │
 │  Files to MODIFY:                                                           │
 │    src/App.jsx                                                              │
 │      → wrap root with <AuthGuard>                                          │
 │      → add <UserBadge /> in header                                         │
 │                                                                             │
 │    Every file that calls fetch or axios:                                    │
 │      → replace: fetch('http://localhost:8080/api/...')                     │
 │      → with:    bffFetch('/api/...')                                       │
 │                                                                             │
 │  No npm install needed — bffClient.js uses native fetch only.              │
 └─────────────────────────────────────────────────────────────────────────────┘
*/
