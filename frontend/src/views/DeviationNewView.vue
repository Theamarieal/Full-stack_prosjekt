<template>
  <div class="deviation-new">
    <h1>Report Deviation</h1>

    <form class="deviation-form" @submit.prevent="handleSubmit">
      <div class="form-group">
        <label for="title">Title *</label>
        <input
            id="title"
            v-model="form.title"
            type="text"
            placeholder="Short description of the deviation"
        />
        <span v-if="errors.title" class="error">{{ errors.title }}</span>
      </div>

      <div class="form-group">
        <label for="module">Module *</label>
        <select id="module" v-model="form.module">
          <option value="">Select module</option>
          <option value="IK_MAT">IK-Mat</option>
          <option value="IK_ALKOHOL">IK-Alkohol</option>
        </select>
        <span v-if="errors.module" class="error">{{ errors.module }}</span>
      </div>

      <div class="form-group">
        <label for="description">Description</label>
        <textarea
            id="description"
            v-model="form.description"
            rows="4"
            placeholder="Describe the deviation in detail"
        />
      </div>

      <div v-if="submitError" class="error-banner">{{ submitError }}</div>

      <LoadingSpinner v-if="loading" message="Submitting..." />

      <div v-else class="form-actions">
        <button type="button" class="btn-secondary" @click="router.push('/deviations')">Cancel</button>
        <button type="submit" class="btn-primary">Report deviation</button>
      </div>
    </form>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import deviationApi from '@/api/deviation'
import LoadingSpinner from '@/components/LoadingSpinner.vue'

const router = useRouter()

const form = ref({
  title: '',
  module: '',
  description: '',
})

const errors = ref({})
const submitError = ref('')
const loading = ref(false)

function validate() {
  errors.value = {}
  if (!form.value.title.trim()) {
    errors.value.title = 'Title is required'
  }
  if (!form.value.module) {
    errors.value.module = 'Module is required'
  }
  return Object.keys(errors.value).length === 0
}

async function handleSubmit() {
  if (!validate()) return

  loading.value = true
  submitError.value = ''

  try {
    await deviationApi.create(form.value)
    router.push('/deviations')
  } catch (e) {
    submitError.value = 'Failed to submit deviation. Please try again.'
    console.error(e)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.deviation-new {
  max-width: 600px;
  margin: 40px auto;
  padding: 0 20px;
}

h1 {
  margin-bottom: 24px;
  color: #2c3e50;
}

.deviation-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

label {
  font-weight: 600;
  font-size: 0.95rem;
  color: #2c3e50;
}

input,
select,
textarea {
  padding: 10px 12px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 1rem;
  font-family: inherit;
  background: white;
}

input:focus,
select:focus,
textarea:focus {
  outline: none;
  border-color: #2563eb;
}

.error {
  color: #dc2626;
  font-size: 0.85rem;
}

.error-banner {
  background: #fee2e2;
  border: 1px solid #fecaca;
  color: #dc2626;
  padding: 12px;
  border-radius: 6px;
  font-weight: 600;
}

.form-actions {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
}

.btn-primary {
  background: #2563eb;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 1rem;
}

.btn-secondary {
  background: white;
  color: #2c3e50;
  border: 1px solid #ddd;
  padding: 10px 20px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 1rem;
}

.btn-secondary:hover {
  border-color: #2563eb;
}
@media (max-width: 768px) {
  .deviation-new {
    margin: 16px auto;
    padding: 0 16px;
  }

  input,
  select,
  textarea {
    min-height: 44px;
    font-size: 1rem;
  }

  .form-actions {
    flex-direction: column-reverse;
  }

  .btn-primary,
  .btn-secondary {
    min-height: 44px;
    width: 100%;
    text-align: center;
  }
}
</style>

