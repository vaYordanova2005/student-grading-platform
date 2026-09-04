import { useCallback, useEffect, useState } from 'react';
import apiClient, { extractErrorMessage } from '../api/client';
import type { ResourceCache, ResourceState } from '../api/resourceCache';

const IDLE: ResourceState<never> = { data: null, error: null, loaded: false };

export interface ApiResource<T> {
  data: T | null;
  error: string | null;
  loading: boolean;
  /** Drops what is loaded (and the cache, if any) and fetches again. */
  reload: () => void;
}

/**
 * One GET, one piece of state. `loading` is derived rather than stored, which
 * keeps the effect free of the synchronous `setState` calls that make React
 * re-render twice on mount.
 */
export function useApiResource<T>(path: string, enabled = true, cache?: ResourceCache<T>): ApiResource<T> {
  const [state, setState] = useState<ResourceState<T>>(() => cache?.snapshot() ?? IDLE);

  useEffect(() => {
    if (!enabled || state.loaded) return;

    // A response that arrives after this effect has been torn down — fast
    // navigation, a reload, a change of `enabled` — must not overwrite the
    // state that replaced it, including overwriting fresh data with a stale
    // error. `ignore` rather than an AbortController: with a shared cache the
    // request may be one another component is still waiting on, so it is this
    // subscription that has to be cancelled, not the request.
    let ignore = false;

    const request = cache
      ? cache.load(path)
      : apiClient.get<T>(path).then(
          (response): ResourceState<T> => ({ data: response.data, error: null, loaded: true }),
          (err): ResourceState<T> => ({ data: null, error: extractErrorMessage(err), loaded: true })
        );

    request.then((next) => {
      if (!ignore) setState(next);
    });

    return () => {
      ignore = true;
    };
  }, [path, enabled, cache, state.loaded]);

  const reload = useCallback(() => {
    cache?.clear();
    setState(IDLE);
  }, [cache]);

  return { data: state.data, error: state.error, loading: enabled && !state.loaded, reload };
}
