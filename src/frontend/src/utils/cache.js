const cache = new Map()

export function requestCache(key, data, ttl = 30000) {
  cache.set(key, { data, expire: Date.now() + ttl })
}

export function getCache(key) {
  const item = cache.get(key)
  if (!item) return null
  if (Date.now() > item.expire) {
    cache.delete(key)
    return null
  }
  return item.data
}

export function clearCache(key) {
  if (key) {
    cache.delete(key)
  } else {
    cache.clear()
  }
}

export function generateCacheKey(url, params) {
  return params ? `${url}?${JSON.stringify(params)}` : url
}