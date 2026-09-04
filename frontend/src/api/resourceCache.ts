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
   * while this revalidation is in flight — so calling it on every mount never
   * causes a flash back to a loading state.
   */
  ensureFresh(path: string): void;
  /** Always refetches, bypassing the TTL. */
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

  function fetchNow(path: string): Promise<void> {
    lastPath = path;
    if (inflight) return inflight;
    const startedAt = generation;
    inflight = apiClient
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
          snapshot = { data: null, error: extractErrorMessage(err), loaded: true };
          fetchedAt = Date.now();
          notify();
        }
      )
      .finally(() => {
        inflight = null;
      });
    return inflight;
  }

  return {
    getSnapshot: () => snapshot,
    subscribe(listener) {
      listeners.add(listener);
      return () => listeners.delete(listener);
    },
    ensureFresh(path) {
      lastPath = path;
      if (isStale()) fetchNow(path);
    },
    refresh(path) {
      fetchNow(path);
    },
    clear() {
      generation += 1;
      snapshot = { data: null, error: null, loaded: false };
      fetchedAt = 0;
      inflight = null;
      notify();
    },
    revalidateIfStale() {
      if (lastPath && isStale()) fetchNow(lastPath);
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
 * serve what's already cached until the revalidation resolves.
 */
export function createResourceCache<T>(ttlMs = DEFAULT_TTL_MS): ResourceCache<T> {
  const store = createStore<T>(ttlMs);
  registry.push(store as Store<unknown>);
  return store;
}

/**
 * Same staleness/fetch mechanics as {@link createResourceCache}, but private
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
