import apiClient, { extractErrorMessage } from './client';

export interface ResourceState<T> {
  data: T | null;
  error: string | null;
  loaded: boolean;
}

export interface ResourceCache<T> {
  /** What is already known, or `null` if nothing has been loaded yet. */
  snapshot(): ResourceState<T> | null;
  load(path: string): Promise<ResourceState<T>>;
  clear(): void;
}

const registry: { clear(): void }[] = [];

/**
 * Session-lifetime cache for a GET that several pages read. Without it, every
 * navigation between pages backed by the same endpoint fires the same request
 * again; with it, the second page renders from what the first already has.
 *
 * Only successful responses are kept — an error is retried on the next visit
 * rather than remembered for the rest of the session.
 */
export function createResourceCache<T>(): ResourceCache<T> {
  let value: ResourceState<T> | null = null;
  let inflight: Promise<ResourceState<T>> | null = null;
  // Bumped by clear() so a request started for the previous account can never
  // land in the cache after a logout.
  let generation = 0;

  const cache: ResourceCache<T> = {
    snapshot: () => value,

    load(path) {
      if (value) return Promise.resolve(value);
      if (!inflight) {
        const startedAt = generation;
        inflight = apiClient
          .get<T>(path)
          .then(
            (response): ResourceState<T> => ({ data: response.data, error: null, loaded: true }),
            (err): ResourceState<T> => ({ data: null, error: extractErrorMessage(err), loaded: true })
          )
          .then((next) => {
            if (startedAt === generation) {
              if (next.error === null) value = next;
              inflight = null;
            }
            return next;
          });
      }
      return inflight;
    },

    clear() {
      generation += 1;
      value = null;
      inflight = null;
    },
  };

  registry.push(cache);
  return cache;
}

/** Cached data belongs to one account, so login and logout both drop it. */
export function clearAllResourceCaches(): void {
  for (const cache of registry) cache.clear();
}
