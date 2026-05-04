import { defineStore } from 'pinia'
import { organizationApi } from '@/api/organization'

export const useOrganizationStore = defineStore('organization', {
  state: () => ({
    list: [],
    positionTree: [],
    activeType: 'position',
    loading: false
  }),

  getters: {
    filteredItems: (state) => {
      const typeMap = { position: '岗位', dept: '部门', grade: '职级' }
      return state.list.filter(o => o.type === typeMap[state.activeType])
    },
    deptOptions: (state) => state.list.filter(o => o.type === '部门'),
    gradeOptions: (state) => state.list.filter(o => o.type === '职级').sort((a, b) => (a.level || 0) - (b.level || 0))
  },

  actions: {
    async init() {
      this.loading = true
      try {
        const [list, positionTree] = await Promise.all([
          organizationApi.list(),
          organizationApi.positionTree()
        ])
        this.list = list
        this.positionTree = positionTree
      } finally {
        this.loading = false
      }
    },

    async create(data) {
      await organizationApi.create(data)
      await this.init()
    },

    async delete(id) {
      await organizationApi.delete(id)
      await this.init()
    },

    setActiveType(type) {
      this.activeType = type
    }
  }
})
