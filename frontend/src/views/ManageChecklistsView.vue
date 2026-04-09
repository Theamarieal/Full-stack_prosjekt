<template>
  <div class="admin-page">
    <header class="page-header-section">
      <div class="header-main">
        <h1>Administer Checklists</h1>
        <p class="subtitle">Create and manage operational routines</p>
      </div>
      <button type="button" class="back-btn-minimal" @click="router.push('/')">← Dashboard</button>
    </header>

    <div v-if="formMessage" class="success-banner" role="status">
      <span aria-hidden="true">✓ </span>{{ formMessage }}
    </div>

    <div v-if="formError" class="error-banner" role="alert">
      <span aria-hidden="true">⚠ </span>{{ formError }}
    </div>

    <section class="stat-card create-section">
      <div class="card-header">
        <h3>Create New Checklist</h3>
      </div>

      <form @submit.prevent="handleCreate" class="creation-form">
        <div class="form-grid-main">
          <div class="form-group">
            <label for="cl-title">Checklist Title</label>
            <input
              id="cl-title"
              v-model="form.title"
              placeholder="e.g. Morning Routine Kitchen"
              required
              :aria-invalid="!!fieldErrors.title"
              :aria-describedby="fieldErrors.title ? 'cl-title-error' : undefined"
            />
            <span v-if="fieldErrors.title" id="cl-title-error" class="field-error" role="alert">
              <span aria-hidden="true">⚠ </span>{{ fieldErrors.title }}
            </span>
          </div>

          <div class="form-group">
            <label for="cl-frequency">Frequency</label>
            <select id="cl-frequency" v-model="form.frequency">
              <option value="DAILY">Daily</option>
              <option value="WEEKLY">Weekly</option>
              <option value="MONTHLY">Monthly</option>
            </select>
          </div>

          <div class="form-group">
            <label for="cl-module">Department</label>
            <select id="cl-module" v-model="form.module">
              <option value="KITCHEN">Kitchen</option>
              <option value="BAR">Bar</option>
            </select>
          </div>
        </div>

        <div class="form-group">
          <label for="cl-description">Description (optional)</label>
          <textarea
            id="cl-description"
            v-model="form.description"
            placeholder="Short explanation of the checklist purposes..."
          ></textarea>
        </div>

        <fieldset class="items-management">
          <legend>Checklist Items</legend>

          <div v-for="(item, index) in form.items" :key="index" class="item-row">
            <div class="item-number" aria-hidden="true">{{ index + 1 }}</div>

            <div class="item-input-group">
              <label :for="`cl-item-${index}`" class="sr-only">
                Checklist item {{ index + 1 }}
              </label>
              <input
                :id="`cl-item-${index}`"
                v-model="form.items[index]"
                placeholder="What needs to be done?"
                required
                :aria-invalid="!!fieldErrors.items?.[index]"
                :aria-describedby="fieldErrors.items?.[index] ? `cl-item-${index}-error` : undefined"
              />
              <span
                v-if="fieldErrors.items?.[index]"
                :id="`cl-item-${index}-error`"
                class="field-error"
                role="alert"
              >
                <span aria-hidden="true">⚠ </span>{{ fieldErrors.items[index] }}
              </span>
            </div>

            <button
              type="button"
              @click="removeItem(index)"
              class="remove-item-btn"
              :aria-label="`Remove checklist item ${index + 1}`"
            >
              ✖
            </button>
          </div>

          <button type="button" @click="addItem" class="add-point-btn">
            + Add another point
          </button>
        </fieldset>

        <div class="form-actions">
          <button type="submit" class="save-btn" :disabled="loading">
            {{ loading ? 'Saving...' : 'Create Checklist' }}
          </button>
        </div>
      </form>
    </section>

    <section class="active-lists-section">
      <div class="section-header">
        <h2>Active Checklists</h2>
        <span class="count-badge">{{ totalElements }} total</span>
      </div>

      <div v-if="checklists.length === 0" class="empty-state" role="status">
        <div class="empty-icon" aria-hidden="true">📋</div>
        <p>No checklists created yet. Start by filling out the form above.</p>
      </div>

      <div class="checklists-grid">
        <div v-for="list in checklists" :key="list.id" class="stat-card list-item-card">
          <div class="list-info">
            <div class="list-meta">
              <span class="tag tag-purple">{{ list.frequency }}</span>
              <span class="tag tag-outline">{{ list.module }}</span>
            </div>
            <h3>{{ list.title }}</h3>
            <p v-if="list.description" class="list-desc">{{ list.description }}</p>
          </div>

          <div class="list-actions">
            <button
              type="button"
              @click="handleDelete(list.id)"
              class="delete-btn-minimal"
              :aria-label="`Delete checklist ${list.title}`"
            >
              Delete
            </button>
          </div>
        </div>
      </div>

      <!-- Pagination -->
      <nav v-if="totalPages > 1" class="pagination" aria-label="Pagination">
        <button
          class="page-btn"
          @click="prevPage"
          :disabled="currentPage === 0"
          aria-label="Previous page"
        >
          ← Previous
        </button>

        <span class="page-info" aria-live="polite">
          Page {{ currentPage + 1 }} of {{ totalPages }}
        </span>

        <button
          class="page-btn"
          @click="nextPage"
          :disabled="currentPage >= totalPages - 1"
          aria-label="Next page"
        >
          Next →
        </button>
      </nav>
    </section>
  </div>
</template>

<script setup>
/**
 * ManageChecklistsView
 *
 * Checklist management view accessible to users with the MANAGER or ADMIN role.
 * Allows managers to create, edit, and delete checklists and their items,
 * and to configure module type and frequency for each checklist.
 */
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import api from '@/api/axios'

const router = useRouter()

const checklists = ref([])
const currentPage = ref(0)
const pageSize = 6
const totalPages = ref(0)
const totalElements = ref(0)
const loading = ref(false)
const formError = ref('')
const formMessage = ref('')
const fieldErrors = ref({})

const form = ref({
  title: '',
  description: '',
  frequency: 'DAILY',
  module: 'KITCHEN',
  items: [''],
})

const fetchChecklists = async () => {
  try {
    const response = await api.get('/checklists', {
      params: { page: currentPage.value, size: pageSize }
    })
    checklists.value = response.data.content ?? []
    totalPages.value = response.data.totalPages ?? 0
    totalElements.value = response.data.totalElements ?? 0
  } catch (err) {
    formError.value = 'Could not fetch checklists.'
    console.error('Could not fetch lists')
  }
}

const prevPage = () => {
  if (currentPage.value > 0) {
    currentPage.value--
    fetchChecklists()
  }
}

const nextPage = () => {
  if (currentPage.value < totalPages.value - 1) {
    currentPage.value++
    fetchChecklists()
  }
}

const addItem = () => {
  form.value.items.push('')
}

const removeItem = (index) => {
  form.value.items.splice(index, 1)
  if (form.value.items.length === 0) {
    form.value.items.push('')
  }
}

function validateForm() {
  fieldErrors.value = { items: [] }
  formError.value = ''
  formMessage.value = ''

  if (!form.value.title.trim()) {
    fieldErrors.value.title = 'Checklist title is required.'
  }

  form.value.items.forEach((item, index) => {
    if (!item.trim()) {
      fieldErrors.value.items[index] = 'This checklist item is required.'
    }
  })

  return !fieldErrors.value.title && !fieldErrors.value.items.some(Boolean)
}

const handleCreate = async () => {
  if (!validateForm()) {
    formError.value = 'Please correct the errors in the form.'
    return
  }

  loading.value = true
  try {
    await api.post('/checklists', form.value)
    form.value = {
      title: '',
      description: '',
      frequency: 'DAILY',
      module: 'KITCHEN',
      items: [''],
    }
    fieldErrors.value = {}
    formMessage.value = 'Checklist created successfully.'
    currentPage.value = 0
    await fetchChecklists()
  } catch (err) {
    formError.value = 'Error while saving. Check that all required fields are filled out.'
  } finally {
    loading.value = false
  }
}

const handleDelete = async (id) => {
  formError.value = ''
  formMessage.value = ''

  try {
    await api.delete(`/checklists/${id}`)
    formMessage.value = 'Checklist deleted successfully.'
    if (checklists.value.length === 1 && currentPage.value > 0) {
      currentPage.value--
    }
    await fetchChecklists()
  } catch (err) {
    console.error(err)
    formError.value = 'Could not delete checklist. Do you have rights for this?'
  }
}

onMounted(fetchChecklists)
</script>

<style scoped>
.admin-page {
  max-width: 1000px;
  margin: 0 auto;
  padding: 32px 20px;
  min-height: 100vh;
  background-color: #f7f6f2;
}

.page-header-section {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 32px;
}

.header-main h1 { font-size: 2rem; font-weight: 800; color: #3C3489; margin-bottom: 4px; }
.subtitle { color: #5a529f; font-weight: 600; font-size: 0.95rem; }

.back-btn-minimal {
  background: transparent; color: #534AB7; border: 1.5px solid #e0dfd8;
  padding: 10px 16px; border-radius: 10px; font-weight: 700; cursor: pointer;
}

.stat-card {
  background: white; border: 1px solid #e0dfd8; border-left: 6px solid #534AB7;
  border-radius: 14px; padding: 24px; box-shadow: 0 4px 12px rgba(60, 52, 137, 0.04);
  margin-bottom: 32px;
}

.field-error {
  color: #991b1b;
  font-size: 0.85rem;
  font-weight: 700;
  margin-top: 6px;
}

.error-banner {
  background: #fff5f5;
  border: 1px solid #fecaca;
  color: #991b1b;
  padding: 16px;
  border-radius: 12px;
  font-weight: 700;
  margin-bottom: 20px;
}

.success-banner {
  background: #ecfdf5;
  border: 1px solid #bbf7d0;
  color: #166534;
  padding: 16px;
  border-radius: 12px;
  font-weight: 700;
  margin-bottom: 20px;
}

.empty-state {
  text-align: center;
  padding: 48px;
  background: white;
  border-radius: 14px;
  border: 2px dashed #d1d5db;
  color: #374151;
}

.sr-only {
  position: absolute;
  width: 1px;
  height: 1px;
  padding: 0;
  margin: -1px;
  overflow: hidden;
  clip: rect(0, 0, 0, 0);
  white-space: nowrap;
  border: 0;
}

.card-header h3 { color: #3C3489; font-weight: 800; margin-bottom: 20px; font-size: 1.2rem; }

.form-grid-main {
  display: grid;
  grid-template-columns: 2fr 1fr 1fr;
  gap: 16px;
  margin-bottom: 20px;
}

.form-group { display: flex; flex-direction: column; margin-bottom: 16px; }
.form-group label { font-weight: 700; color: #3C3489; font-size: 0.85rem; margin-bottom: 8px; }

input, select, textarea {
  padding: 12px; border: 1.5px solid #e0dfd8; border-radius: 10px;
  background: #fafaf8; font-size: 1rem; width: 100%;
}

button:focus-visible,
input:focus-visible,
select:focus-visible,
textarea:focus-visible {
  outline: 3px solid #1d4ed8;
  outline-offset: 2px;
}

.items-management {
  background: #fcfcfb; border: 1.5px solid #f0f0ee;
  padding: 20px; border-radius: 12px; margin-bottom: 24px;
}

.items-management legend {
  font-weight: 700;
  color: #3C3489;
  margin-bottom: 12px;
  padding: 0 4px;
}

.item-input-group { flex: 1; }

.item-row { display: flex; align-items: center; gap: 12px; margin-bottom: 10px; }
.item-number {
  width: 28px; height: 28px; background: #534AB7; color: white;
  display: flex; align-items: center; justify-content: center;
  border-radius: 50%; font-size: 0.75rem; font-weight: 800; flex-shrink: 0;
}

.remove-item-btn {
  background: #fef2f2; color: #dc2626; border: none;
  width: 32px; height: 32px; border-radius: 8px; cursor: pointer; font-size: 0.8rem; flex-shrink: 0;
}

.add-point-btn {
  background: transparent; color: #534AB7; border: 1.5px dashed #534AB7;
  padding: 10px; border-radius: 10px; cursor: pointer; width: 100%; font-weight: 700; margin-top: 8px;
}

.section-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.section-header h2 { color: #3C3489; font-size: 1.4rem; font-weight: 800; }
.count-badge { background: #e0dfd8; color: #4b5563; padding: 4px 12px; border-radius: 20px; font-size: 0.8rem; font-weight: 700; }

.checklists-grid { display: grid; grid-template-columns: 1fr; gap: 16px; }

.list-item-card {
  display: flex; justify-content: space-between; align-items: center;
  padding: 20px; margin-bottom: 0; border-left-width: 4px;
}

.list-info h3 { color: #3C3489; margin-bottom: 4px; font-weight: 800; font-size: 1.1rem; }
.list-desc { font-size: 0.9rem; color: #4b5563; margin-bottom: 12px; }

.list-meta { display: flex; gap: 8px; margin-bottom: 8px; }
.tag { font-size: 0.65rem; font-weight: 800; padding: 3px 8px; border-radius: 6px; text-transform: uppercase; }
.tag-purple { background: #f5f4ff; color: #4338ca; }
.tag-outline { border: 1px solid #d1d5db; color: #4b5563; }

.delete-btn-minimal {
  background: white; color: #dc2626; border: 1.5px solid #fecaca;
  padding: 8px 16px; border-radius: 8px; font-weight: 700; cursor: pointer; transition: all 0.2s;
}
.delete-btn-minimal:hover { background: #dc2626; color: white; border-color: #dc2626; }

.save-btn {
  background: #534AB7; color: white; border: none; padding: 14px 32px;
  border-radius: 10px; font-weight: 800; cursor: pointer; width: 100%;
}

.empty-icon { font-size: 2.5rem; margin-bottom: 12px; }

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 16px;
  margin-top: 24px;
}

.page-btn {
  padding: 10px 20px;
  border: 1.5px solid #e0dfd8;
  border-radius: 10px;
  background: white;
  color: #534AB7;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.2s;
}

.page-btn:hover:not(:disabled) {
  background: #534AB7;
  color: white;
  border-color: #534AB7;
}

.page-btn:disabled { color: #c7c7c7; cursor: not-allowed; }
.page-info { font-weight: 700; color: #3C3489; font-size: 0.95rem; }

@media (max-width: 786px) {
  .form-grid-main { grid-template-columns: 1fr; }
  .list-item-card { flex-direction: column; align-items: flex-start; gap: 16px; }
  .delete-btn-minimal { width: 100%; text-align: center; }
  .page-header-section { flex-direction: column; gap: 16px; }
  .back-btn-minimal { width: 100%; }
}
</style>
