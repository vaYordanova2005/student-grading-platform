import { useCallback, useEffect, useState, useSyncExternalStore } from 'react';
import { createLocalResource, type ResourceCache } from '../api/resourceCache';

export interface ApiResource<T> {
  data: T | null;
  error: string | null;
  loading: boolean;
  /** Forces a refetch now, bypassing the cache's TTL. */
  reload: () => void;
}

/**
 * One GET, read through a {@link ResourceCache} via `useSyncExternalStore` —
 * so a background revalidation (see resourceCache.ts) that resolves while
 * this component is mounted re-renders it with the new data, and multiple
 * components sharing a cache stay in sync with each other.
 *
 * A caller that passes no `cache` gets a private, unregistered one created
 * once per hook instance: same staleness mechanics, but nothing else will
 * ever read from it, so it doesn't need sharing or focus-revalidation.
 */
export function useApiResource<T>(path: string, enabled = true, cache?: ResourceCache<T>): ApiResource<T> {
  // Lazy useState initializer rather than a ref: reading a ref's value during
  // render is itself a lint error (react-hooks/refs), since a ref update
  // doesn't schedule a re-render the way state does.
  const [localCache] = useState<ResourceCache<T> | null>(() => (cache ? null : createLocalResource<T>()));
  const activeCache = cache ?? localCache!;

  const snapshot = useSyncExternalStore(activeCache.subscribe, activeCache.getSnapshot);

  useEffect(() => {
    if (enabled) activeCache.ensureFresh(path);
  }, [activeCache, path, enabled]);

  const reload = useCallback(() => {
    activeCache.refresh(path);
  }, [activeCache, path]);

  return {
    data: snapshot.data,
    error: snapshot.error,
    loading: enabled && !snapshot.loaded,
    reload,
  };
}
