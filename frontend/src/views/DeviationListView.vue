<template>
  <div class="deviation-page">
    <header class="page-header-section">
      <div class="header-main">
        <h1>Deviations</h1>
        <p class="subtitle">Monitor and resolve quality gaps</p>
      </div>
    
      <div class="header-actions">
        <button type="button" class="add-btn" @click="router.push('/deviations/new')">
          <span class="plus-icon" aria-hidden="true">+</span> Report deviation
        </button>
        <button type="button" class="back-btn-minimal" @click="router.push('/')">
          ← Dashboard
        </button>
      </div>
    </header>

    <div class="filter-wrapper">
      <div class="filter-grid">
        <div class="filter-group">
          <label for="filter-status">Status</label>
          <select id="filter-status" v-model="filterStatus">
            <option value="">All statuses</option>
            <option value="OPEN">Open / Pending</option>
            <option value="IN_PROGRESS">In Progress</option>
            <option value="RESOLVED">Resolved</option>
          </select>
        </div>

        <div class="filter-group">
          <label for="filter-module">Department/Module</label>
          <select id="filter-module" v-model="filterModule">
            <option value="">All modules</option>
            <option value="IK_MAT">IK-Mat (Food Safety)</option>
            <option value="IK_ALKOHOL">IK-Alkohol (Alcohol)</option>
          </select>
        </div>
      </div>
    </div>

    <LoadingSpinner v-if="loading" message="Loading deviations..." />

    <div v-else-if="error" class="error-banner" role="alert">
      <span aria-hidden="true">⚠ </span>{{ error }}
    </div>

    <div v-else-if="filtered.length === 0" class="empty-state" role="status">
      <div class="empty-icon" aria-hidden="true">🫸🫷</div>
      <p>No deviations found. Everything is looking good!</p>
    </div>

    <div v-else class="deviation-cards">
      <section
        v-for="deviation in filtered"
        :key="deviation.id"
        class="deviation-card"
        :class="statusClass(deviation.status)"
        :aria-label="`Deviation: ${deviation.title}`"
      >
        <div class="card-top">
          <div class="card-title-area">
            <span class="module-pill">{{ formatModule(deviation.module) }}</span>
            <h3>{{ deviation.title }}</h3>
          </div>
          <span class="status-badge" :class="statusClass(deviation.status)">
            {{ formatStatus(deviation.status) }}
          </span>
        </div>

        <p v-if="deviation.description" class="description">{{ deviation.description }}</p>

        <div class="card-meta">
          <div class="meta-item">
            <span class="meta-label">Reported:</span>
            <span>{{ formatDate(deviation.createdAt) }}</span>
          </div>
          <div class="meta-item" v-if="deviation.reportedBy">
            <span class="meta-label">By:</span>
            <span>{{ deviation.reportedBy.email.split('@')[0] }}</span>
          </div>
        </div>

        <div v-if="isManagerOrAdmin" class="manager-actions">
          <div v-if="updateError === deviation.id" class="error-inline" role="alert">
            <span aria-hidden="true">⚠ </span>Failed to update status.
          </div>
          <div class="update-controls">
            <label :for="`status-update-${deviation.id}`">Update status:</label>
            <select
              :id="`status-update-${deviation.id}`"
              :value="deviation.status"
              @change="updateStatus(deviation.id, $event.target.value)"
              class="status-select-small"
            >
              <option value="OPEN">Open</option>
              <option value="IN_PROGRESS">In Progress</option>
              <option value="RESOLVED">Resolved</option>
            </select>
          </div>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import deviationApi from '@/api/deviation'
import LoadingSpinner from '@/components/LoadingSpinner.vue'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const deviations = ref([])
const loading = ref(true)
const error = ref('')
const updateError = ref(null)
const filterStatus = ref('')
const filterModule = ref(route.query.module || '')

const isManagerOrAdmin = computed(() => {
  const role = authStore.user?.role
  return role === 'MANAGER' || role === 'ADMIN'
})

const filtered = computed(() => {
  return deviations.value.filter((d) => {
    const matchStatus = filterStatus.value ? d.status === filterStatus.value : true
    const matchModule = filterModule.value ? d.module === filterModule.value : true
    return matchStatus && matchModule
  })
})

watch(() => route.query.module, (val) => {
  filterModule.value = val || ''
})

onMounted(async () => {
  try {
    const res = await deviationApi.getAll()
    deviations.value = Array.isArray(res.data) ? res.data : []
  } catch (e) {
    error.value = 'Failed to load deviations.'
    console.error(e)
  } finally {
    loading.value = false
  }
})

async function updateStatus(id, newStatus) {
  updateError.value = null
  try {
    const res = await deviationApi.updateStatus(id, newStatus)
    const index = deviations.value.findIndex((d) => d.id === id)
    if (index !== -1) deviations.value[index] = res.data
  } catch (e) {
    updateError.value = id
    console.error('Failed to update status', e)
  }
}

function statusClass(status) {
  if (status === 'OPEN') return 'status-open'
  if (status === 'IN_PROGRESS') return 'status-in-progress'
  if (status === 'RESOLVED') return 'status-resolved'
  return ''
}

function formatStatus(status) {
  if (status === 'OPEN') return 'Open'
  if (status === 'IN_PROGRESS') return 'In progress'
  if (status === 'RESOLVED') return 'Resolved'
  return status
}

function formatModule(module) {
  if (module === 'IK_MAT') return 'IK-Mat'
  if (module === 'IK_ALKOHOL') return 'IK-Alkohol'
  return module || ''
}

function formatDate(dateStr) {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleDateString('no-NO', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
  })
}
</script>

<style scoped>
.deviation-page {
  max-width: 900px;
  margin: 0 auto;
  padding: 32px 20px;
  min-height: 100vh;
  background-color: #f7f6f2;
}

.page-header-section {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  margin-bottom: 32px;
}

.header-actions {
  display: flex;
  gap: 12px;
  align-items: center;
  flex-shrink: 0;
}

.header-main h1 { font-size: 2rem; font-weight: 800; color: #3C3489; margin-bottom: 4px; }
.subtitle { color:#5a529f; font-weight: 600; font-size: 0.95rem; }

.add-btn {
  background: #534AB7;
  color: white;
  border: none;
  padding: 12px 20px;
  border-radius: 10px;
  font-weight: 700;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
  transition: transform 0.2s, background 0.2s;
}
.add-btn:hover { transform: translateY(-2px); background: #3C3489; }

.back-btn-minimal {
  background: transparent;
  color: #534AB7;
  border: 1.5px solid #e0dfd8;
  padding: 10px 16px;
  border-radius: 10px;
  font-weight: 700;
  cursor: pointer;
}

.back-btn-minimal:hover {
  background: #fafaf8;
}

button:focus-visible,
select:focus-visible {
  outline: 3px solid #1d4ed8;
  outline-offset: 2px;
}

.filter-wrapper {
  margin-bottom: 32px;
  background: white;
  padding: 20px;
  border-radius: 14px;
  border: 1px solid #e0dfd8;
}

.filter-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
}

.filter-group label {
  display: block;
  font-weight: 700;
  color: #3C3489;
  font-size: 0.85rem;
  margin-bottom: 8px;
}

select {
  width: 100%;
  padding: 10px;
  border: 1.5px solid #e0dfd8;
  border-radius: 10px;
  background: #fafaf8;
  font-weight: 600;
  color: #1f2937;
}

.deviation-cards { display: flex; flex-direction: column; gap: 20px; }

.deviation-card {
  background: white;
  border: 1px solid #e0dfd8;
  border-left: 6px solid #ddd;
  border-radius: 14px;
  padding: 24px;
  box-shadow: 0 4px 12px rgba(60, 52, 137, 0.04);
}

.deviation-card.status-open {
  border-left-color: #ef4444;
}

.deviation-card.status-in-progress {
  border-left-color: #f59e0b;
}

.deviation-card.status-resolved {
  border-left-color: #10b981;
}

.card-top { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 16px; }

.module-pill {
  font-size: 0.7rem;
  font-weight: 800;
  text-transform: uppercase;
  color: #4338ca;
  background: #f5f4ff;
  padding: 2px 8px;
  border-radius: 4px;
  display: inline-block;
  margin-bottom: 4px;
  border: 1px solid #c7d2fe;
}

.error-banner {
  background: #fff5f5;
  border: 1px solid #fecaca;
  color: #991b1b;
  padding: 16px;
  border-radius: 12px;
  font-weight: 700;
}

.error-inline {
  margin-bottom: 12px;
  color: #991b1b;
  font-weight: 700;
}

.card-title-area h3 { color: #3C3489; font-weight: 800; margin: 0; }

.status-badge {
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 0.75rem;
  font-weight: 800;
  border: 1px solid transparent;
}

.status-badge.status-open {
  background: #fff1f2;
  color: #991b1b;
  border-color: #fecdd3;
}

.status-badge.status-in-progress {
  background: #fffbeb;
  color: #92400e;
  border-color: #fde68a;
}

.status-badge.status-resolved {
  background: #f0fdf4;
  color: #166534;
  border-color: #bbf7d0;
}

.description { color: #374151; margin-bottom: 16px; line-height: 1.5; }
.card-meta { display: flex; gap: 20px; font-size: 0.9rem; color: #4b5563; }
.meta-label { font-weight: 700; margin-right: 4px; }

.manager-actions {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #f0f0f0;
}

.update-controls { display: flex; align-items: center; gap: 12px; }
.update-controls label { font-size: 0.85rem; font-weight: 700; color: #3C3489; }

.status-select-small {
  padding: 6px 12px;
  border-radius: 8px;
  font-size: 0.85rem;
  width: auto;
}

.empty-state {
  text-align: center;
  padding: 60px;
  background: white;
  border-radius: 14px;
  border: 2px dashed #d1d5db;
  color: #374151;
}
.empty-icon { font-size: 3rem; margin-bottom: 12px; }

@media (max-width: 786px) {
  .page-header-section { flex-direction: column; gap: 16px; align-items: stretch;}
  .header-actions {flex-direction: column; width: 100%;}
  .add-btn { width: 100%; justify-content: center; }
  .card-top { flex-direction: column; gap: 12px; }
  .back-btn-minimal {width: 100%; justify-content: center; text-align: center;}
}
</style>