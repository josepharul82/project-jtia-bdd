/**
 * src/hooks/useAuth.js
 *
 * ─────────────────────────────────────────────────────────────────────────────
 *  CREATE this file. It is the single source of truth for authentication state.
 * ─────────────────────────────────────────────────────────────────────────────
 *
 *  On mount, calls GET /bff/user to verify the current session.
 *
 *  Possible outcomes:
 *    • 200  → user is authenticated, returns the user profile
 *    • 401  → bffFetch redirects to the IdP automatically
 *             (this hook returns { user: null, loading: false })
 *    • 5xx  → BFF is down, returns { user: null, loading: false }
 *
 *  Usage:
 *    const { user, loading, logout } = useAuth();
 *    if (loading) return <Spinner />;
 *    // user is guaranteed to be non-null here (401 triggered a redirect)
 */

import { useState, useEffect, useCallback } from 'react';
import { bffFetch, bffPost } from '../api/bffClient';

export function useAuth() {
  const [user,    setUser]    = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    bffFetch('/bff/user')
      .then((data) => setUser(data))
      .catch((err) => {
        // 401 is handled by bffFetch (triggers redirect to IdP).
        // We only land here for unexpected errors (BFF down, network error, etc.).
        if (err.status !== 401) {
          console.error('[useAuth] unexpected error:', err.message);
        }
        setUser(null);
      })
      .finally(() => setLoading(false));
  }, []);

  /**
   * Logout: POST /logout with the CSRF token (attached automatically by bffPost),
   * then redirect to the app root.
   *
   * Spring Security will:
   *  1. Clear the BFF_TOKEN cookie (Max-Age=0)
   *  2. Redirect to Keycloak's end_session_endpoint (RP-Initiated Logout)
   *  3. Keycloak invalidates the SSO session and redirects back to /
   *
   * IMPORTANT: Use this function for logout, never a plain <a href="/logout">.
   * Spring Security only accepts POST on /logout (CSRF protection).
   */
  const logout = useCallback(() => {
    bffPost('/logout', null)
      .catch(() => { /* ignore errors during logout */ })
      .finally(() => {
        window.location.href = '/';
      });
  }, []);

  return { user, loading, logout };
}
