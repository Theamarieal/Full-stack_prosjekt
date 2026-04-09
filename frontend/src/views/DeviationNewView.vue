<template>
  <div class="deviation-page">
    <header class="page-header-section">
      <div class="header-main">
        <h1>Report Deviation</h1>
        <p class="subtitle">Document and manage non-conformities</p>
      </div>
      <button type="button" class="back-btn-minimal" @click="router.push('/deviations')">← Back</button>
    </header>

    <section class="stat-card">
      <div class="card-header">
        <h3>Deviation Details</h3>
      </div>

      <form class="deviation-form" @submit.prevent="handleSubmit">
        <div class="form-group">
          <label for="title">Title *</label>
          <input
            id="title"
            v-model="form.title"
            type="text"
            placeholder="E.g. Refrigerator failure or expired ID check"
            :class="{ 'input-error': errors.title }"
            :aria-invalid="!!errors.title"
            :aria-describedby="errors.title ? 'title-error' : undefined"
          />
          <span
            v-if="errors.title"
            id="title-error"
            class="field-error"
            role="alert"
          >
            <span aria-hidden="true">⚠ </span>{{ errors.title }}
          </span>
        </div>

        <div class="form-group">
          <label for="module">Module *</label>
          <select
            id="module"
            v-model="form.module"
            :class="{ 'input-error': errors.module }"
            :aria-invalid="!!errors.module"
            :aria-describedby="errors.module ? 'module-error' : undefined"
          >
            <option value="">Select category</option>
            <option value="IK_MAT">IK-Mat (Food Safety)</option>
            <option value="IK_ALKOHOL">IK-Alkohol (Alcohol Control)</option>
          </select>
          <span
            v-if="errors.module"
            id="module-error"
            class="field-error"
            role="alert"
          >
            <span aria-hidden="true">⚠ </span>{{ errors.module }}
          </span>
        </div>

        <div class="form-group">
          <label for="description">Detailed Description</label>
          <textarea
            id="description"
            v-model="form.description"
            rows="5"
            placeholder="Explain what happened and what immediate actions were taken..."
          ></textarea>
        </div>

        <div v-if="submitError" class="error-banner" role="alert">
          <span aria-hidden="true">⚠ </span>{{ submitError }}
        </div>

        <div class="form-actions">
          <LoadingSpinner v-if="loading" message="Submitting..." />
          <template v-else>
            <button type="button" class="cancel-btn" @click="router.push('/deviations')">Cancel</button>
            <button type="submit" class="save-btn">Report Deviation</button>
          </template>
        </div>
      </form>
    </section>
  </div>
</template>

<script setup>
/**
 * DeviationNewView
 *
 * Form view for registering a new deviation report.
 * Allows all authenticated users to submit a deviation with a title,
 * description, and module (IK-Mat or IK-Alkohol).
 * Redirects to the deviation list on successful submission.
 */
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
.deviation-page {
  max-width: 800px;
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

.header-main h1 {
  font-size: 2rem;
  font-weight: 800;
  color: #3C3489;
  margin-bottom: 4px;
}

.subtitle {
  color: #5a529f;
  font-weight: 600;
  font-size: 0.95rem;
}

.back-btn-minimal {
  background: transparent;
  color: #534AB7;
  border: 1.5px solid #e0dfd8;
  padding: 10px 16px;
  border-radius: 10px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.2s;
}

.stat-card {
  background: white;
  border: 1px solid #e0dfd8;
  border-left: 6px solid #534AB7;
  border-radius: 14px;
  padding: 32px;
  box-shadow: 0 4px 12px rgba(60, 52, 137, 0.04);
}

.card-header h3 {
  color: #3C3489;
  font-weight: 800;
  font-size: 1.25rem;
  margin-bottom: 24px;
}

.deviation-form {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.form-group {
  display: flex;
  flex-direction: column;
}

.form-group label {
  font-weight: 700;
  margin-bottom: 8px;
  color: #3C3489;
  font-size: 0.9rem;
}

input, select, textarea {
  padding: 12px 14px;
  border: 1.5px solid #e0dfd8;
  border-radius: 10px;
  font-size: 1rem;
  background-color: #fafaf8;
  color: #2c2c2a;
  width: 100%;
  font-family: inherit;
  transition: all 0.2s;
}

button:focus-visible,
input:focus-visible,
select:focus-visible,
textarea:focus-visible {
  outline: 3px solid #1d4ed8;
  outline-offset: 2px;
}

input:focus, select:focus, textarea:focus {
  outline: none;
  border-color: #534AB7;
  background: white;
  box-shadow: 0 0 0 3px rgba(83, 74, 183, 0.1);
}

.input-error {
  border-color: #dc2626;
  background-color: #fffafa;
}

.field-error {
  color: #b91c1c;
  font-size: 0.85rem;
  font-weight: 700;
  margin-top: 6px;
}

.error-banner {
  background: #fff5f5;
  border: 1px solid #fecaca;
  color: #991b1b;
  padding: 16px;
  border-radius: 10px;
  font-weight: 700;
}

.form-actions {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
  margin-top: 12px;
}

.save-btn {
  background: #534AB7;
  color: white;
  border: none;
  padding: 12px 24px;
  border-radius: 10px;
  font-weight: 700;
  cursor: pointer;
  transition: background 0.2s;
}

.save-btn:hover {
  background: #3C3489;
}

.cancel-btn {
  background: white;
  color: #4b5563;
  border: 1.5px solid #e0dfd8;
  padding: 12px 24px;
  border-radius: 10px;
  font-weight: 700;
  cursor: pointer;
}

.cancel-btn:hover {
  background: #f8fafc;
  color: #3C3489;
  border-color: #3C3489;
}

@media (max-width: 786px) {
  .page-header-section {
    flex-direction: column;
    gap: 16px;
  }

  .back-btn-minimal {
    width: 100%;
    text-align: center;
  }

  .form-actions {
    flex-direction: column-reverse;
  }

  .save-btn, .cancel-btn {
    width: 100%;
    padding: 14px;
  }
}
</style>
