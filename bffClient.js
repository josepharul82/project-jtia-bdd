/**
 * src/api/bffClient.js
 *
 * ─────────────────────────────────────────────────────────────────────────────
 *  Thin fetch wrapper for all BFF calls. No dependencies — native fetch only.
 *  Replace every axios / fetch call in your project with this client.
 * ─────────────────────────────────────────────────────────────────────────────
 *
 *  BEFORE (calling backend directly):
 *    const res = await fetch('http://localhost:8080/api/v1/projets');
 *
 *  AFTER (calling through the BFF):
 *    import { bffFetch } from '@/api/bffClient';
 *    const data = await bffFetch('/api/v1/projets');
 *
 *  Your /api/v1/... paths do NOT change.
 *
 *  What this file handles automatically:
 *
 *  1. BASE URL
 *     All requests go to the BFF (:9090), which proxies /api/** to the backend.
 *
 *  2. SESSION COOKIE  (credentials: 'include')
 *     The BFF_TOKEN is an HttpOnly cookie — JS cannot read it, but the browser
 *     sends it on every request when credentials: 'include' is set.
 *
 *  3. CSRF TOKEN  (X-XSRF-TOKEN header)
 *     The BFF writes a readable XSRF-TOKEN cookie. Spring Security requires
 *     it back as the X-XSRF-TOKEN header on POST / PUT / PATCH / DELETE.
 *     bffFetch reads the cookie and adds the header automatically.
 *
 *  4. SESSION EXPIRY  (401 → full-page redirect to IdP)
 *     When BFF_TOKEN expires, BFF returns 401 + { loginUrl }.
 *     bffFetch catches this, redirects the browser to the IdP login page,
 *     and returns a never-resolving promise so no component error state fires.
 *     After login the BFF redirects back to the page the user was on.
 *
 *  5. JSON parsing
 *     If the response Content-Type is application/json, bffFetch parses and
 *     returns the body directly. For other content types it returns the raw
 *     Response object so you can call .blob(), .text(), etc. yourself.
 */

// ─── Config ───────────────────────────────────────────────────────────────────

const BFF_BASE_URL = 'http://localhost:9090'; // change to your production BFF URL
const TIMEOUT_MS   = 30_000;

// ─── Utility ──────────────────────────────────────────────────────────────────

/** Reads a browser cookie by name. Returns null if absent. */
function getCookie(name) {
  const match = document.cookie.match(
    new RegExp('(?:^|; )' + name.replace(/([.*+?^=!:${}()|[\]/\\])/g, '\\$1') + '=([^;]*)')
  );
  return match ? decodeURIComponent(match[1]) : null;
}

/** Returns true for HTTP methods that mutate state and require a CSRF token. */
function requiresCsrf(method = 'GET') {
  return ['POST', 'PUT', 'PATCH', 'DELETE'].includes(method.toUpperCase());
}

// ─── Core function ────────────────────────────────────────────────────────────

/**
 * Drop-in fetch wrapper for all BFF API calls.
 *
 * @param {string} path        - Path relative to the BFF, e.g. '/api/v1/projets'
 * @param {RequestInit} options - Standard fetch options (method, body, headers, …)
 * @returns {Promise<any>}      - Parsed JSON body, or raw Response for non-JSON
 *
 * @example
 *   // GET
 *   const projets = await bffFetch('/api/v1/projets');
 *
 *   // POST
 *   const created = await bffFetch('/api/v1/projets', {
 *     method: 'POST',
 *     body: JSON.stringify({ name: 'Nouveau projet' }),
 *   });
 *
 *   // PUT
 *   await bffFetch(`/api/v1/projets/${id}`, {
 *     method: 'PUT',
 *     body: JSON.stringify(updatedProjet),
 *   });
 *
 *   // DELETE
 *   await bffFetch(`/api/v1/projets/${id}`, { method: 'DELETE' });
 */
export async function bffFetch(path, options = {}) {
  const { method = 'GET', headers = {}, body, signal, ...rest } = options;

  // ── Build headers ──────────────────────────────────────────────────────────
  const mergedHeaders = {
    'Content-Type': 'application/json',
    'Accept':       'application/json',
    ...headers,
  };

  // Attach CSRF token on mutating requests
  if (requiresCsrf(method)) {
    const csrfToken = getCookie('XSRF-TOKEN');
    if (csrfToken) {
      mergedHeaders['X-XSRF-TOKEN'] = csrfToken;
    }
  }

  // ── Timeout via AbortController ────────────────────────────────────────────
  const controller  = new AbortController();
  const timeoutId   = setTimeout(() => controller.abort(), TIMEOUT_MS);
  // Merge caller's signal with our timeout signal
  const mergedSignal = signal
    ? anySignal([signal, controller.signal])
    : controller.signal;

  // ── Execute fetch ──────────────────────────────────────────────────────────
  let response;
  try {
    response = await fetch(`${BFF_BASE_URL}${path}`, {
      method,
      credentials: 'include',   // REQUIRED: sends BFF_TOKEN + XSRF-TOKEN cookies
      headers:     mergedHeaders,
      body,
      signal:      mergedSignal,
      ...rest,
    });
  } catch (err) {
    clearTimeout(timeoutId);
    if (err.name === 'AbortError') {
      throw new BffError('Request timed out', 408);
    }
    throw new BffError(`Network error: ${err.message}`, 0);
  } finally {
    clearTimeout(timeoutId);
  }

  // ── Handle 401: session expired → redirect to IdP ─────────────────────────
  if (response.status === 401) {
    let loginUrl = '/oauth2/authorization/keycloak';
    try {
      const body = await response.json();
      if (body?.loginUrl) loginUrl = body.loginUrl;
    } catch {
      // ignore JSON parse error, use default loginUrl
    }

    // Full-page redirect triggers the OAuth2 flow.
    // The BFF saves the current URL in BFF_SAVED_REQUEST cookie and will
    // redirect back here after the user logs in.
    window.location.href = loginUrl;

    // Return a never-resolving promise so no downstream .catch() or component
    // error state fires while the page is redirecting.
    return new Promise(() => {});
  }

  // ── Handle non-2xx errors ──────────────────────────────────────────────────
  if (!response.ok) {
    let message = `HTTP ${response.status} ${response.statusText}`;
    try {
      const errorBody = await response.json();
      if (errorBody?.message) message = errorBody.message;
      else if (errorBody?.error) message = errorBody.error;
    } catch {
      // response body is not JSON, keep the default message
    }
    throw new BffError(message, response.status);
  }

  // ── Parse response ─────────────────────────────────────────────────────────
  // Return parsed JSON for JSON responses, raw Response otherwise
  // (e.g. file downloads, blobs, plain text).
  const contentType = response.headers.get('Content-Type') ?? '';
  if (contentType.includes('application/json')) {
    return response.json();
  }
  return response; // caller can call .blob(), .text(), etc.
}

// ─── Convenience helpers ──────────────────────────────────────────────────────
// These mirror the axios shorthand API so migration is a one-word change.

export const bffGet    = (path, options = {}) =>
  bffFetch(path, { ...options, method: 'GET' });

export const bffPost   = (path, body, options = {}) =>
  bffFetch(path, { ...options, method: 'POST',  body: JSON.stringify(body) });

export const bffPut    = (path, body, options = {}) =>
  bffFetch(path, { ...options, method: 'PUT',   body: JSON.stringify(body) });

export const bffPatch  = (path, body, options = {}) =>
  bffFetch(path, { ...options, method: 'PATCH', body: JSON.stringify(body) });

export const bffDelete = (path, options = {}) =>
  bffFetch(path, { ...options, method: 'DELETE' });

// ─── Custom error class ───────────────────────────────────────────────────────

export class BffError extends Error {
  constructor(message, status) {
    super(message);
    this.name   = 'BffError';
    this.status = status;
  }
}

// ─── Internal: merge multiple AbortSignals ────────────────────────────────────

function anySignal(signals) {
  const controller = new AbortController();
  for (const signal of signals) {
    if (signal.aborted) {
      controller.abort();
      break;
    }
    signal.addEventListener('abort', () => controller.abort(), { once: true });
  }
  return controller.signal;
}
