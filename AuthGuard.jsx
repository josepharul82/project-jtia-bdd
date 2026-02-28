/**
 * src/components/AuthGuard.jsx
 *
 * ─────────────────────────────────────────────────────────────────────────────
 *  CREATE this file. Wrap your entire app (or any protected subtree) with it.
 * ─────────────────────────────────────────────────────────────────────────────
 *
 *  What it does:
 *    1. Calls GET /bff/user on mount (via useAuth)
 *    2. Shows a loading spinner while the check is in flight
 *    3. If unauthenticated → axiosClient redirects to IdP automatically
 *    4. If authenticated → renders children and provides { user, logout }
 *       via UserContext so any component can access them with useUser()
 *
 *  Usage:
 *    // In your main App.jsx or router root:
 *    <AuthGuard>
 *      <YourApp />
 *    </AuthGuard>
 *
 *    // In any child component:
 *    const { user, logout } = useUser();
 */

import React from 'react';
import { useAuth } from '../hooks/useAuth';

// ─── Context ──────────────────────────────────────────────────────────────────

export const UserContext = React.createContext(null);

/** Access the authenticated user and logout function from any child component. */
export function useUser() {
  const ctx = React.useContext(UserContext);
  if (!ctx) throw new Error('useUser must be used inside <AuthGuard>');
  return ctx;
}

// ─── AuthGuard ────────────────────────────────────────────────────────────────

export function AuthGuard({ children }) {
  const { user, loading, logout } = useAuth();

  // Session check is in flight
  if (loading) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', marginTop: '20vh' }}>
        <p style={{ color: '#64748b', fontFamily: 'monospace' }}>Checking session…</p>
      </div>
    );
  }

  // user === null means 401 was returned, and axiosClient has already called
  // window.location.href = loginUrl. Render nothing to avoid a flash.
  if (!user) return null;

  return (
    <UserContext.Provider value={{ user, logout }}>
      {children}
    </UserContext.Provider>
  );
}

// ─── UserBadge ────────────────────────────────────────────────────────────────

/**
 * Optional ready-made component: displays the user's name and a logout button.
 * Place it in your header/navbar.
 *
 * Usage:
 *   import { UserBadge } from '@/components/AuthGuard';
 *   <UserBadge />
 */
export function UserBadge() {
  const { user, logout } = useUser();

  return (
    <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
      <span style={{ fontSize: 14 }}>
        👤 {user.given_name || user.name || user.email}
      </span>
      <button
        onClick={logout}
        style={{
          padding: '4px 12px',
          cursor: 'pointer',
          borderRadius: 4,
          border: '1px solid #cbd5e1',
          background: 'transparent',
          fontSize: 13,
        }}
      >
        Logout
      </button>
    </div>
  );
}
