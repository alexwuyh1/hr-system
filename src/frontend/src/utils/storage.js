function btoaUnicode(str) {
  return btoa(unescape(encodeURIComponent(str)))
}

function atobUnicode(encoded) {
  return decodeURIComponent(escape(atob(encoded)))
}

export const secureStorage = {
  setItem(key, value) {
    const encoded = btoaUnicode(value)
    localStorage.setItem(`hr_${key}`, encoded)
  },

  getItem(key) {
    const encoded = localStorage.getItem(`hr_${key}`)
    if (!encoded) return null
    try {
      return atobUnicode(encoded)
    } catch {
      return null
    }
  },

  removeItem(key) {
    localStorage.removeItem(`hr_${key}`)
  }
}
