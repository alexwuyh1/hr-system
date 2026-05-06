import { ref, computed, onMounted, onUnmounted } from 'vue'

export function useVirtualScroll(items, itemHeight, containerHeight) {
  const scrollTop = ref(0)
  const containerRef = ref(null)

  const startIndex = computed(() => Math.floor(scrollTop.value / itemHeight))
  const visibleCount = computed(() => Math.ceil(containerHeight / itemHeight) + 2)
  const endIndex = computed(() => Math.min(startIndex.value + visibleCount.value, items.value.length))

  const visibleItems = computed(() => {
    const start = Math.max(0, startIndex.value - 1)
    const end = Math.min(items.value.length, endIndex.value + 1)
    return items.value.slice(start, end).map((item, index) => ({
      item,
      index: start + index,
      style: { position: 'absolute', top: `${(start + index) * itemHeight}px`, height: `${itemHeight}px`, left: 0, right: 0 }
    }))
  })

  const totalHeight = computed(() => items.value.length * itemHeight)

  function onScroll(e) {
    scrollTop.value = e.target.scrollTop
  }

  onMounted(() => {
    if (containerRef.value) {
      containerRef.value.addEventListener('scroll', onScroll)
    }
  })

  onUnmounted(() => {
    if (containerRef.value) {
      containerRef.value.removeEventListener('scroll', onScroll)
    }
  })

  return { containerRef, visibleItems, totalHeight, startIndex }
}