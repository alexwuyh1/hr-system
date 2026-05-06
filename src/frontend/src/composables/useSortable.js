import { ref, computed } from 'vue'

function getVal(obj, key) {
  if (key.includes('.')) {
    return key.split('.').reduce((o, k) => (o ? o[k] : null), obj)
  }
  return obj[key]
}

function parseDate(v) {
  if (!v) return null
  if (v instanceof Date) return v.getTime()
  const s = String(v)
  if (/^\d{4}-\d{2}-\d{2}/.test(s)) {
    const t = new Date(v).getTime()
    return isNaN(t) ? null : t
  }
  return null
}

function compare(a, b, key, dir) {
  let va = getVal(a, key)
  let vb = getVal(b, key)

  if (va === null || va === undefined) va = ''
  if (vb === null || vb === undefined) vb = ''

  if (typeof va === 'number' && typeof vb === 'number') {
    return dir === 'asc' ? va - vb : vb - va
  }

  const ta = parseDate(va)
  const tb = parseDate(vb)
  if (ta !== null && tb !== null) {
    return dir === 'asc' ? ta - tb : tb - ta
  }

  if (typeof va === 'string' && typeof vb === 'string') {
    return dir === 'asc'
      ? va.localeCompare(vb, 'zh-CN')
      : vb.localeCompare(va, 'zh-CN')
  }

  return 0
}

export function useSortable(data, key, direction = 'none') {
  const sortKey = ref(key)
  const sortDir = ref(direction)

  const sorted = computed(() => {
    if (!data || !Array.isArray(data.value || data)) return []
    const arr = [...(data.value || data)]

    if (sortDir.value === 'none' || !sortKey.value) return arr

    return arr.sort((a, b) => {
      const c = compare(a, b, sortKey.value, sortDir.value)
      if (c !== 0) return c

      if (sortKey.value === 'workDate') {
        const c2 = compare(a, b, 'checkIn', sortDir.value)
        if (c2 !== 0) return c2
        return compare(a, b, 'checkOut', sortDir.value)
      }

      return 0
    })
  })

  function toggle(key) {
    if (sortKey.value !== key) {
      sortKey.value = key
      sortDir.value = 'asc'
    } else {
      const dirs = ['asc', 'desc', 'none']
      const idx = dirs.indexOf(sortDir.value)
      sortDir.value = dirs[(idx + 1) % dirs.length]
    }
  }

  function getClass(key) {
    if (sortKey.value !== key) return 'sort-none'
    return sortDir.value === 'asc' ? 'sort-asc' : sortDir.value === 'desc' ? 'sort-desc' : 'sort-none'
  }

  return { sorted, sortKey, sortDir, toggle, getClass }
}
