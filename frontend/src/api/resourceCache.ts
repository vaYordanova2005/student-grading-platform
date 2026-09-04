import apiClient, { extractErrorMessage } from './client';

export interface ResourceSnapshot<T> {
  data: T | null;
  error: string | null;
  loaded: boolean;
}

export interface ResourceCache<T> {
  getSnapshot(): ResourceSnapshot<T>;
  subscribe(listener: () => void): () => void;
  /**
   * Fetches if nothing has ever loaded, or if what's loaded is older than the
   * cache's TTL. Whatever was already loaded keeps being served — including
   * if the revalidation itself fails — so calling it on every mount never
   * blanks the screen or replaces good data with a transient network error.
   * A failure is remembered for the TTL just like a success, so a backend
   * that's down isn't hammered on every mount/focus in the meantime; the
   * exception is a cache with no TTL (see `createLocalResource`), where a
   * failure is effectively retried on the very next call.
   */
  ensureFresh(path: string): void;
  /**
   * Always starts a fresh request, bypassing both the TTL and any request
   * already in flight — a caller reaching for this wants the current server
   * state (e.g. right after its own POST/DELETE), not whatever a
   * revalidation that started earlier happens to return.
   */
  refresh(path: string): void;
  clear(): void;
}

interface Store<T> extends ResourceCache<T> {
  /** Used by the window-focus handler; not meaningful to call before ensureFresh/refresh has recorded a path. */
  revalidateIfStale(): void;
}

const DEFAULT_TTL_MS = 30_000;

function createStore<T>(ttlMs: number): Store<T> {
  let snapshot: ResourceSnapshot<T> = { data: null, error: null, loaded: false };
  let fetchedAt = 0;
  let inflight: Promise<void> | null = null;
  let generation = 0;
  let lastPath: string | null = null;
  const listeners = new Set<() => void>();

  function notify() {
    for (const listener of listeners) listener();
  }

  function isStale() {
    return !snapshot.loaded || Date.now() - fetchedAt > ttlMs;
  }

  function fetchNow(path: string, force: boolean): Promise<void> {
    lastPath = path;
    if (inflight && !force) return inflight;
    // Forcing bumps the generation so a request already in flight becomes a
    // no-op when it resolves — this one is meant to supersede it, not race it.
    if (force) generation += 1;
    const startedAt = generation;

    const request: Promise<void> = apiClient
      .get<T>(path)
      .then(
        (response): void => {
          if (startedAt !== generation) return;
          snapshot = { data: response.data, error: null, loaded: true };
          fetchedAt = Date.now();
          notify();
        },
        (err): void => {
          if (startedAt !== generation) return;
          // A background revalidation failing must not blank out data
          // that's already on screen — only surface the error when there's
          // nothing to fall back to.
          snapshot =
            snapshot.loaded && snapshot.data !== null
              ? { data: snapshot.data, error: null, loaded: true }
              : { data: null, error: extractErrorMessage(err), loaded: true };
          fetchedAt = Date.now();
          notify();
        }
      )
      .finally(() => {
        // A stale request's own `.finally` must not clear a newer `inflight`
        // that a later `fetchNow` call (e.g. a forced refresh) has since set.
        if (inflight === request) inflight = null;
      });

    inflight = request;
    return request;
  }

  return {
    getSnapshot: () => snapshot,
    subscribe(listener) {
      listeners.add(listener);
      return () => listeners.delete(listener);
    },
    ensureFresh(path) {
      lastPath = path;
      if (isStale()) fetchNow(path, false);
    },
    refresh(path) {
      fetchNow(path, true);
    },
    clear() {
      generation += 1;
      snapshot = { data: null, error: null, loaded: false };
      fetchedAt = 0;
      inflight = null;
      notify();
    },
    revalidateIfStale() {
      if (lastPath && isStale()) fetchNow(lastPath, false);
    },
  };
}

const registry: Store<unknown>[] = [];

/**
 * Session-scoped cache for a GET that several pages read, so navigating
 * between them (e.g. Начало ↔ Дневник) renders from what's already fetched
 * instead of refiring the same request.
 *
 * A cache that never expired would show a student's grades as they were at
 * login for the rest of the session, even after a teacher enters a new one
 * mid-visit. To avoid that without giving up the sharing, entries go stale
 * after `ttlMs` and get revalidated in the background — on the next mount
 * that reads them, and whenever the tab regains focus — while continuing to
 * serve what's already cached, including across a revalidation that fails.
 */
export function createResourceCache<T>(ttlMs = DEFAULT_TTL_MS): ResourceCache<T> {
  const store = createStore<T>(ttlMs);
  registry.push(store as Store<unknown>);
  return store;
}

/**
 * Same store/fetch mechanics as {@link createResourceCache}, but with no TTL
 * (so every call to `ensureFresh` past the first one refetches) and private
 * to a single hook instance: not registered for window-focus revalidation,
 * since nothing else will ever be waiting on it once its one subscriber
 * unmounts. Used by {@link useApiResource} when no shared cache is passed in.
 */
export function createLocalResource<T>(): ResourceCache<T> {
  return createStore<T>(0);
}

/** Cached data belongs to one account, so login and logout both drop it. */
export function clearAllResourceCaches(): void {
  for (const cache of registry) cache.clear();
}

if (typeof window !== 'undefined') {
  window.addEventListener('focus', () => {
    for (const cache of registry) cache.revalidateIfStale();
  });
}
