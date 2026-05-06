const SECRET_KEY = 'hr_sys_secure_2024'

function btoaUnicode(str) {
  return btoa(unescape(encodeURIComponent(str)))
}

function atobUnicode(encoded) {
  return decodeURIComponent(escape(atob(encoded)))
}

function xorEncrypt(str, key) {
  const encrypted = btoaUnicode(str)
  return btoaUnicode(String.fromCharCode(...[...encrypted].map((c, i) => c.charCodeAt(0) ^ key.charCodeAt(i % key.length))))
}

function xorDecrypt(encrypted, key) {
  const decoded = atobUnicode(encrypted)
  return String.fromCharCode(...[...decoded].map((c, i) => c.charCodeAt(0) ^ key.charCodeAt(i % key.length)))
}

export const secureStorage = {
  setItem(key, value) {
    const encrypted = xorEncrypt(value, SECRET_KEY)
    localStorage.setItem(`hr_${key}`, encrypted)
  },

  getItem(key) {
    const encrypted = localStorage.getItem(`hr_${key}`)
    if (!encrypted) return null
    try {
      return xorDecrypt(encrypted, SECRET_KEY)
    } catch {
      return null
    }
  },

  removeItem(key) {
    localStorage.removeItem(`hr_${key}`)
  }
}