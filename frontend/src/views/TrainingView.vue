<template>
  <div class="training-page">
    <main class="training-content">
      <div class="welcome-card">
        <div class="welcome-header">
          <div class="welcome-text">
            <h2>Training and Document Storage</h2>
            <p>
              Access policies, complete training materials, and review training records for
              <strong>{{ authStore.user?.organization?.name || 'your organization' }}</strong>.
            </p>
          </div>

          <button type="button" class="back-btn-minimal" @click="router.push('/')">
            ← Dashboard
          </button>
        </div>
      </div>

      <p v-if="actionMessage" class="ok-text feedback-message" role="status" aria-live="polite">
        {{ actionMessage }}
      </p>
      <p v-if="actionError" class="warning-text feedback-message" role="alert">
        {{ actionError }}
      </p>

      <div v-if="canManage" class="manager-tools-section">
        <h2>Management</h2>

        <div class="upload-card">
          <h3>Add new document</h3>

          <div class="form-grid">
            <div class="form-group">
              <label for="document-title">Title</label>
              <input id="document-title" v-model="newDocument.title" type="text" placeholder="Title" />
            </div>

            <div class="form-group">
              <label for="document-type">Document type</label>
              <select id="document-type" v-model="newDocument.type">
                <option value="POLICY">Policy</option>
                <option value="TRAINING">Training Material</option>
              </select>
            </div>
          </div>

          <div class="form-grid">
            <div class="form-group">
              <label for="completion-type">Completion type</label>
              <select
                id="completion-type"
                v-model="newDocument.completionType"
                :disabled="newDocument.type === 'POLICY'"
              >
                <option v-if="newDocument.type === 'POLICY'" value="READ_ACKNOWLEDGE">
                  Read and acknowledge
                </option>

                <template v-else>
                  <option value="QUIZ">Quiz-based training</option>
                  <option value="PRACTICAL_SIGN_OFF">Practical sign-off</option>
                </template>
              </select>
            </div>

            <div></div>
          </div>

          <div class="form-group">
            <label for="document-description">Short description</label>
            <input
              id="document-description"
              v-model="newDocument.description"
              type="text"
              placeholder="Short description"
            />
          </div>

          <div class="form-group">
            <label for="document-content">Document content</label>
            <textarea
              id="document-content"
              v-model="newDocument.content"
              rows="8"
              placeholder="Write the document content here..."
            />
          </div>

          <div class="file-upload-row">
            <label class="file-upload-label" for="document-file">Upload PDF or document</label>

            <div class="file-upload-box">
              <input id="document-file" ref="fileInputRef" type="file" @change="handleFileUpload" />

              <div v-if="selectedFileName" class="selected-file-chip">
                <span>{{ selectedFileName }}</span>
                <button
                  type="button"
                  class="remove-file-button"
                  @click="clearSelectedFile"
                  aria-label="Remove selected file"
                >
                  ✕
                </button>
              </div>
            </div>
          </div>

          <button class="primary-button" @click="createDocument">Save document</button>

          <p v-if="managerMessage" class="ok-text feedback-message" role="status" aria-live="polite">
            {{ managerMessage }}
          </p>
          <p v-if="managerError" class="warning-text feedback-message" role="alert">
            {{ managerError }}
          </p>
        </div>
      </div>

      <div class="sections-grid">
        <section class="module-section">
          <h2>Policies</h2>

          <div class="stats-grid">
            <div v-for="policy in policies" :key="policy.id" class="stat-card">
              <div class="card-header">
                <div>
                  <h3>{{ policy.title }}</h3>
                  <span class="completion-badge">
                    {{ getCompletionTypeLabel(policy.completionType) }}
                  </span>
                </div>

                <span
                  v-if="authStore.user?.role === 'EMPLOYEE'"
                  class="status-badge"
                  :class="statusClass(getTrainingStatus(policy.id))"
                >
                  {{ formatStatus(getTrainingStatus(policy.id)) }}
                </span>
              </div>

              <p class="card-description">{{ policy.description }}</p>

              <div class="card-actions">
                <button class="secondary-button" @click="openDocument(policy)">View</button>

                <button
                  v-if="
                    policy.completionType === 'READ_ACKNOWLEDGE' &&
                    authStore.user?.role === 'EMPLOYEE' &&
                    getTrainingStatus(policy.id) !== 'ACKNOWLEDGED'
                  "
                  class="primary-button"
                  @click="acknowledgeDocument(policy.id)"
                >
                  Acknowledge
                </button>

                <span
                  v-else-if="
                    policy.completionType === 'READ_ACKNOWLEDGE' &&
                    authStore.user?.role === 'EMPLOYEE' &&
                    getTrainingStatus(policy.id) === 'ACKNOWLEDGED'
                  "
                  class="ok-text"
                >
                  Acknowledged
                </span>
              </div>
            </div>

            <div v-if="!policies.length" class="stat-card">
              <p>No policies available.</p>
            </div>
          </div>
        </section>

        <section class="module-section">
          <h2>Training Materials</h2>

          <div class="stats-grid">
            <div v-for="material in materials" :key="material.id" class="stat-card">
              <div class="card-header training-card-header">
                <div>
                  <h3>{{ material.title }}</h3>
                  <span class="completion-badge">
                    {{ getCompletionTypeLabel(material.completionType) }}
                  </span>
                </div>

                <span
                  v-if="canTakeQuiz"
                  class="status-badge"
                  :class="statusClass(getTrainingStatus(material.id))"
                >
                  {{ formatStatus(getTrainingStatus(material.id)) }}
                </span>
              </div>

              <p class="card-description">{{ material.description }}</p>

              <div class="card-actions">
                <button class="secondary-button" @click="openDocument(material)">View</button>

                <button
                  v-if="material.completionType === 'QUIZ' && canTakeQuiz"
                  class="primary-button"
                  @click="openQuiz(material)"
                >
                  Start Quiz
                </button>

                <button
                  v-if="material.completionType === 'QUIZ' && canManage"
                  class="secondary-button"
                  @click="openQuiz(material)"
                >
                  View Quiz
                </button>

                <button
                  v-if="material.completionType === 'PRACTICAL_SIGN_OFF' && canManage"
                  class="primary-button"
                  @click="openPracticalModal(material)"
                >
                  Approve Training
                </button>
              </div>

              <div
                v-if="
                  material.completionType === 'PRACTICAL_SIGN_OFF' &&
                  authStore.user?.role === 'EMPLOYEE'
                "
                class="info-box"
              >
                Awaiting manager approval after practical observation.
              </div>

              <div
                v-if="canManage && material.completionType === 'QUIZ'"
                class="manager-inline-tools"
              >
                <button class="text-button" @click="openQuestionManager(material)">
                  Add Quiz Question
                </button>
              </div>
            </div>

            <div v-if="!materials.length" class="stat-card">
              <p>No training materials available.</p>
            </div>
          </div>
        </section>

        <section v-if="authStore.user?.role === 'EMPLOYEE'" class="module-section">
          <h2>My Training Records</h2>

          <div class="stats-grid">
            <div v-for="cert in myCertifications" :key="cert.id" class="cert-card">
              <div class="cert-icon">✓</div>

              <div class="cert-body">
                <h3>{{ cert.title }}</h3>
                <p class="cert-subtitle">{{ cert.document?.title }}</p>
                <p class="ok-text">Completed</p>
                <p class="small-text">Issued {{ formatDate(cert.issuedAt) }}</p>
              </div>
            </div>

            <div v-if="!myCertifications.length" class="stat-card">
              <p>No training records available yet.</p>
            </div>
          </div>
        </section>

        <section v-if="canManage" class="module-section">
          <div class="section-topbar">
            <h2>Team Certifications</h2>

            <div class="filters">
              <label class="sr-only" for="employee-filter">Search by email</label>
              <input
                id="employee-filter"
                v-model="employeeFilter"
                type="text"
                class="filter-input"
                placeholder="Search by email"
              />

              <label class="sr-only" for="employee-select">Filter by employee</label>
              <select id="employee-select" v-model="selectedEmployee" class="filter-select">
                <option value="">All employees</option>
                <option v-for="email in employeesForFilter" :key="email" :value="email">
                  {{ email.split('@')[0] }}
                </option>
              </select>
            </div>
          </div>

          <div class="team-table-wrapper">
            <table v-if="filteredTeamCertifications.length" class="team-table">
              <thead>
                <tr>
                  <th scope="col">Employee</th>
                  <th scope="col">Certification</th>
                  <th scope="col">Training</th>
                  <th scope="col">Issued</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="cert in filteredTeamCertifications" :key="cert.id">
                  <td>{{ cert.user?.email }}</td>
                  <td>{{ cert.title }}</td>
                  <td>{{ cert.document?.title }}</td>
                  <td>{{ formatDate(cert.issuedAt) }}</td>
                </tr>
              </tbody>
            </table>

            <p v-else>No matching team certifications found.</p>
          </div>
        </section>

        <section v-if="canManage" class="module-section">
          <div class="section-topbar">
            <h2>Team Policy Acknowledgements</h2>

            <div class="filters">
              <label class="sr-only" for="policy-employee-filter">Search by email</label>
              <input
                id="policy-employee-filter"
                v-model="policyEmployeeFilter"
                type="text"
                class="filter-input"
                placeholder="Search by email"
              />

              <label class="sr-only" for="policy-employee-select">Filter by employee</label>
              <select
                id="policy-employee-select"
                v-model="selectedPolicyEmployee"
                class="filter-select"
              >
                <option value="">All employees</option>
                <option v-for="email in policyEmployees" :key="email" :value="email">
                  {{ email.split('@')[0] }}
                </option>
              </select>
            </div>
          </div>

          <div class="team-table-wrapper">
            <table v-if="filteredTeamAcknowledgements.length" class="team-table">
              <thead>
                <tr>
                  <th scope="col">Employee</th>
                  <th scope="col">Policy</th>
                  <th scope="col">Acknowledged</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="ack in filteredTeamAcknowledgements" :key="ack.id">
                  <td>{{ ack.user?.email }}</td>
                  <td>{{ ack.document?.title }}</td>
                  <td>{{ formatDate(ack.acknowledgedAt) }}</td>
                </tr>
              </tbody>
            </table>

            <p v-else>No matching team policy acknowledgements found.</p>
          </div>
        </section>
      </div>
    </main>

    <div v-if="selectedDocument" class="overlay">
      <div class="modal large-modal">
        <div class="card-header">
          <h3>{{ selectedDocument.title }}</h3>
          <button class="close-button" @click="closeDocument" aria-label="Close document">✕</button>
        </div>

        <p class="card-description">{{ selectedDocument.description }}</p>

        <div v-if="documentPreviewUrl && isPdfDocument(selectedDocument)" class="file-viewer">
          <iframe :src="documentPreviewUrl" width="100%" height="520" class="pdf-frame" />
        </div>

        <div v-else-if="selectedDocument.content" class="document-fallback">
          <div class="document-content">
            {{ selectedDocument.content }}
          </div>
        </div>

        <div v-else class="document-fallback">
          <p>No preview available for this file.</p>
        </div>
      </div>
    </div>

    <div v-if="selectedTraining" class="overlay">
      <div class="modal">
        <div class="card-header">
          <h3>{{ selectedTraining.title }} {{ canTakeQuiz ? 'Quiz' : 'Quiz Overview' }}</h3>
          <button class="close-button" @click="closeQuiz" aria-label="Close quiz">✕</button>
        </div>

        <div v-if="!quizQuestions.length" class="empty-state">
          <p>No quiz questions available for this training yet.</p>
        </div>

        <div v-for="question in quizQuestions" :key="question.id" class="quiz-question">
          <p>
            <strong>{{ question.question }}</strong>
          </p>

          <label class="quiz-option">
            <input
              v-if="canTakeQuiz"
              v-model="quizAnswers[question.id]"
              type="radio"
              :name="`q-${question.id}`"
              value="A"
            />
            <span :class="{ 'correct-answer': canManage && question.correctAnswer === 'A' }">
              {{ question.optionA }}
            </span>
          </label>

          <label class="quiz-option">
            <input
              v-if="canTakeQuiz"
              v-model="quizAnswers[question.id]"
              type="radio"
              :name="`q-${question.id}`"
              value="B"
            />
            <span :class="{ 'correct-answer': canManage && question.correctAnswer === 'B' }">
              {{ question.optionB }}
            </span>
          </label>

          <label class="quiz-option">
            <input
              v-if="canTakeQuiz"
              v-model="quizAnswers[question.id]"
              type="radio"
              :name="`q-${question.id}`"
              value="C"
            />
            <span :class="{ 'correct-answer': canManage && question.correctAnswer === 'C' }">
              {{ question.optionC }}
            </span>
          </label>

          <label class="quiz-option">
            <input
              v-if="canTakeQuiz"
              v-model="quizAnswers[question.id]"
              type="radio"
              :name="`q-${question.id}`"
              value="D"
            />
            <span :class="{ 'correct-answer': canManage && question.correctAnswer === 'D' }">
              {{ question.optionD }}
            </span>
          </label>
        </div>

        <p v-if="canManage" class="manager-info-text">
          Managers can review quiz content, but only employees can submit answers.
        </p>

        <button
          v-if="quizQuestions.length && canTakeQuiz"
          class="primary-button"
          @click="submitQuiz"
        >
          Submit Quiz
        </button>

        <p
          v-if="quizResult"
          :class="quizResult.passed ? 'ok-text feedback-message' : 'warning-text feedback-message'"
          :role="quizResult.passed ? 'status' : 'alert'"
        >
          {{ quizResult.message }}
        </p>
      </div>
    </div>

    <div v-if="questionDocument" class="overlay">
      <div class="modal">
        <div class="card-header">
          <h3>Add Quiz Question</h3>
          <button
            class="close-button"
            @click="closeQuestionManager"
            aria-label="Close question manager"
          >
            ✕
          </button>
        </div>

        <p class="card-description">
          Add a new quiz question for <strong>{{ questionDocument.title }}</strong>.
        </p>

        <div class="form-group">
          <label for="quiz-question">Question</label>
          <textarea id="quiz-question" v-model="newQuestion.question" rows="3" placeholder="Question" />
        </div>

        <div class="form-group">
          <label for="option-a">Option A</label>
          <input id="option-a" v-model="newQuestion.optionA" type="text" placeholder="Option A" />
        </div>

        <div class="form-group">
          <label for="option-b">Option B</label>
          <input id="option-b" v-model="newQuestion.optionB" type="text" placeholder="Option B" />
        </div>

        <div class="form-group">
          <label for="option-c">Option C</label>
          <input id="option-c" v-model="newQuestion.optionC" type="text" placeholder="Option C" />
        </div>

        <div class="form-group">
          <label for="option-d">Option D</label>
          <input id="option-d" v-model="newQuestion.optionD" type="text" placeholder="Option D" />
        </div>

        <div class="form-group">
          <label for="correct-answer">Correct answer</label>
          <select id="correct-answer" v-model="newQuestion.correctAnswer">
            <option value="A">Correct answer: A</option>
            <option value="B">Correct answer: B</option>
            <option value="C">Correct answer: C</option>
            <option value="D">Correct answer: D</option>
          </select>
        </div>

        <button class="primary-button" @click="saveQuestion">Save Question</button>

        <p v-if="questionMessage" class="ok-text feedback-message" role="status" aria-live="polite">
          {{ questionMessage }}
        </p>
        <p v-if="questionError" class="warning-text feedback-message" role="alert">
          {{ questionError }}
        </p>
      </div>
    </div>

    <div v-if="practicalDocument" class="overlay">
      <div class="modal">
        <div class="card-header">
          <h3>Approve Practical Training</h3>
          <button
            class="close-button"
            @click="closePracticalModal"
            aria-label="Close practical training modal"
          >
            ✕
          </button>
        </div>

        <p class="card-description">
          Select an employee and approve practical training for
          <strong>{{ practicalDocument.title }}</strong>.
        </p>

        <div class="form-group">
          <label for="practical-employee">Employee</label>
          <select id="practical-employee" v-model="practicalEmployeeId">
            <option value="" disabled>Select employee</option>
            <option v-for="employee in employees" :key="employee.id" :value="employee.id">
              {{ employee.email }}
            </option>
          </select>
        </div>

        <div class="form-group">
          <label for="practical-note">Manager note</label>
          <textarea id="practical-note" v-model="practicalNote" rows="4" placeholder="Optional manager note" />
        </div>

        <button class="primary-button" @click="approvePracticalTraining">Approve</button>

        <p v-if="practicalMessage" class="ok-text feedback-message" role="status" aria-live="polite">
          {{ practicalMessage }}
        </p>
        <p v-if="practicalError" class="warning-text feedback-message" role="alert">
          {{ practicalError }}
        </p>
      </div>
    </div>
  </div>
</template>

<script setup>
/**
 * TrainingView
 *
 * Training and certification management view for the IK-Alkohol and IK-Mat modules.
 * Allows employees to read training documents, complete quizzes, and acknowledge policies.
 * Managers and admins can upload documents, add quiz questions, and sign off
 * on practical training completions for other employees.
 */
import { computed, onMounted, ref, watch } from 'vue'
import { useAuthStore } from '@/stores/auth'
import trainingApi from '@/api/training'
import { useRouter } from 'vue-router'

const router = useRouter()
const authStore = useAuthStore()

const policies = ref([])
const materials = ref([])
const statuses = ref([])
const myCertifications = ref([])
const myAcknowledgements = ref([])
const teamCertifications = ref([])
const teamAcknowledgements = ref([])
const employees = ref([])

const selectedDocument = ref(null)
const selectedTraining = ref(null)
const questionDocument = ref(null)
const practicalDocument = ref(null)

const quizQuestions = ref([])
const quizAnswers = ref({})
const quizResult = ref(null)

const employeeFilter = ref('')
const selectedEmployee = ref('')
const policyEmployeeFilter = ref('')
const selectedPolicyEmployee = ref('')

const managerMessage = ref('')
const managerError = ref('')
const questionMessage = ref('')
const questionError = ref('')
const practicalMessage = ref('')
const practicalError = ref('')
const actionMessage = ref('')
const actionError = ref('')

const practicalEmployeeId = ref('')
const practicalNote = ref('')

const fileInputRef = ref(null)
const selectedFile = ref(null)
const selectedFileName = ref('')
const documentPreviewUrl = ref('')

const newDocument = ref({
  title: '',
  description: '',
  content: '',
  type: 'POLICY',
  completionType: 'READ_ACKNOWLEDGE',
})

const newQuestion = ref({
  question: '',
  optionA: '',
  optionB: '',
  optionC: '',
  optionD: '',
  correctAnswer: 'A',
})

const canManage = computed(() => ['MANAGER', 'ADMIN'].includes(authStore.user?.role))
const canTakeQuiz = computed(() => authStore.user?.role === 'EMPLOYEE')

watch(
  () => newDocument.value.type,
  (type) => {
    if (type === 'POLICY') {
      newDocument.value.completionType = 'READ_ACKNOWLEDGE'
    } else if (newDocument.value.completionType === 'READ_ACKNOWLEDGE') {
      newDocument.value.completionType = 'QUIZ'
    }
  },
)

const employeesForFilter = computed(() => {
  const unique = new Map()

  teamCertifications.value.forEach((c) => {
    if (c.user?.email) {
      unique.set(c.user.email, c.user.email)
    }
  })

  return Array.from(unique.values())
})

const filteredTeamCertifications = computed(() => {
  return teamCertifications.value.filter((cert) => {
    const matchesText = cert.user?.email?.toLowerCase().includes(employeeFilter.value.toLowerCase())

    const matchesDropdown = !selectedEmployee.value || cert.user?.email === selectedEmployee.value

    return matchesText && matchesDropdown
  })
})

const policyEmployees = computed(() => {
  const unique = new Map()

  teamAcknowledgements.value.forEach((ack) => {
    if (ack.user?.email) {
      unique.set(ack.user.email, ack.user.email)
    }
  })

  return Array.from(unique.values())
})

const filteredTeamAcknowledgements = computed(() => {
  return teamAcknowledgements.value.filter((ack) => {
    const matchesText = ack.user?.email
      ?.toLowerCase()
      .includes(policyEmployeeFilter.value.toLowerCase())

    const matchesDropdown =
      !selectedPolicyEmployee.value || ack.user?.email === selectedPolicyEmployee.value

    return matchesText && matchesDropdown
  })
})

function formatDate(dateTime) {
  if (!dateTime) return '-'
  return new Date(dateTime).toLocaleString('en-GB')
}

function getCompletionTypeLabel(type) {
  if (type === 'READ_ACKNOWLEDGE') return 'Read & acknowledge'
  if (type === 'QUIZ') return 'Quiz required'
  if (type === 'PRACTICAL_SIGN_OFF') return 'Practical approval'
  return 'Training'
}

function getTrainingStatus(documentId) {
  return statuses.value.find((s) => s.documentId === documentId)?.status || 'NOT_STARTED'
}

function formatStatus(status) {
  if (status === 'PASSED') return 'Passed'
  if (status === 'NOT_PASSED') return 'Not passed'
  if (status === 'ACKNOWLEDGED') return 'Acknowledged'
  if (status === 'APPROVED') return 'Approved'
  if (status === 'PENDING_MANAGER_APPROVAL') return 'Pending approval'
  return 'Not started'
}

function statusClass(status) {
  return {
    passed: status === 'PASSED' || status === 'ACKNOWLEDGED' || status === 'APPROVED',
    failed: status === 'NOT_PASSED',
    neutral: status === 'NOT_STARTED' || status === 'PENDING_MANAGER_APPROVAL',
  }
}

function handleFileUpload(event) {
  const file = event.target.files?.[0] || null
  selectedFile.value = file
  selectedFileName.value = file ? file.name : ''
}

function clearSelectedFile() {
  selectedFile.value = null
  selectedFileName.value = ''

  if (fileInputRef.value) {
    fileInputRef.value.value = ''
  }
}

function isPdfDocument(document) {
  return document?.fileName?.toLowerCase().endsWith('.pdf')
}

async function loadData() {
  try {
    const [policiesRes, materialsRes] = await Promise.all([
      trainingApi.getPolicies(),
      trainingApi.getMaterials(),
    ])

    policies.value = Array.isArray(policiesRes.data) ? policiesRes.data : []
    materials.value = Array.isArray(materialsRes.data) ? materialsRes.data : []

    if (authStore.user?.role === 'EMPLOYEE') {
      const [statusesRes, myCertsRes] = await Promise.all([
        trainingApi.getMyStatuses(),
        trainingApi.getMyCertifications(),
      ])

      statuses.value = Array.isArray(statusesRes.data) ? statusesRes.data : []
      myCertifications.value = Array.isArray(myCertsRes.data) ? myCertsRes.data : []

      teamCertifications.value = []
      teamAcknowledgements.value = []
      employees.value = []
    }

    if (canManage.value) {
      const [teamRes, employeesRes, teamAckRes] = await Promise.all([
        trainingApi.getTeamCertifications(),
        trainingApi.getEmployees(),
        trainingApi.getTeamAcknowledgements(),
      ])

      teamCertifications.value = Array.isArray(teamRes.data) ? teamRes.data : []
      employees.value = Array.isArray(employeesRes.data) ? employeesRes.data : []
      teamAcknowledgements.value = Array.isArray(teamAckRes.data) ? teamAckRes.data : []

      statuses.value = []
      myCertifications.value = []
    }
  } catch (error) {
    console.error('Failed to load training data:', error)
  }
}

async function createDocument() {
  managerMessage.value = ''
  managerError.value = ''

  try {
    if (selectedFile.value) {
      const formData = new FormData()
      formData.append('file', selectedFile.value)
      formData.append('title', newDocument.value.title)
      formData.append('description', newDocument.value.description)
      formData.append('content', newDocument.value.content || '')
      formData.append('type', newDocument.value.type)
      formData.append('completionType', newDocument.value.completionType)

      await trainingApi.uploadDocument(formData)
    } else {
      await trainingApi.createDocument({
        title: newDocument.value.title,
        description: newDocument.value.description,
        content: newDocument.value.content,
        type: newDocument.value.type,
        completionType: newDocument.value.completionType,
      })
    }

    managerMessage.value = 'Document saved successfully.'
    managerError.value = ''

    newDocument.value = {
      title: '',
      description: '',
      content: '',
      type: 'POLICY',
      completionType: 'READ_ACKNOWLEDGE',
    }

    clearSelectedFile()
    await loadData()
  } catch (error) {
    console.error('FULL ERROR:', error)
    console.error('DATA:', error.response?.data)
    console.error('STATUS:', error.response?.status)

    managerError.value =
      error.response?.data?.message || error.response?.data || 'Failed to save document.'
  }
}

async function openDocument(document) {
  selectedDocument.value = document

  if (documentPreviewUrl.value) {
    URL.revokeObjectURL(documentPreviewUrl.value)
    documentPreviewUrl.value = ''
  }

  if (document.filePath || document.fileName) {
    try {
      const response = await trainingApi.downloadDocumentFile(document.id)
      const contentType = response.headers['content-type'] || 'application/octet-stream'
      const blob = new Blob([response.data], { type: contentType })
      documentPreviewUrl.value = URL.createObjectURL(blob)
    } catch (error) {
      console.error('Failed to load document file:', error)
      documentPreviewUrl.value = ''
    }
  }
}

function closeDocument() {
  selectedDocument.value = null

  if (documentPreviewUrl.value) {
    URL.revokeObjectURL(documentPreviewUrl.value)
    documentPreviewUrl.value = ''
  }
}

async function openQuiz(material) {
  selectedTraining.value = material
  quizResult.value = null
  quizAnswers.value = {}

  try {
    const response = await trainingApi.getQuiz(material.id)
    quizQuestions.value = Array.isArray(response.data) ? response.data : []
  } catch (error) {
    console.error('Failed to load quiz:', error)
    quizQuestions.value = []
  }
}

function closeQuiz() {
  selectedTraining.value = null
  quizQuestions.value = []
  quizAnswers.value = {}
  quizResult.value = null
}

async function submitQuiz() {
  actionMessage.value = ''
  actionError.value = ''

  try {
    const response = await trainingApi.submitQuiz(selectedTraining.value.id, quizAnswers.value)
    quizResult.value = response.data

    await loadData()

    if (response.data?.passed) {
      actionMessage.value = 'Quiz passed successfully.'
    } else {
      actionMessage.value = 'Quiz submitted.'
    }
  } catch (error) {
    console.error('Failed to submit quiz:', error)
    console.error('QUIZ DATA:', error.response?.data)
    console.error('QUIZ STATUS:', error.response?.status)

    actionError.value =
      error.response?.data?.message || error.response?.data || 'Failed to submit quiz.'
  }
}

async function acknowledgeDocument(documentId) {
  actionMessage.value = ''
  actionError.value = ''

  try {
    await trainingApi.acknowledgeDocument(documentId)
    await loadData()
    actionMessage.value = 'Policy acknowledged successfully.'
  } catch (error) {
    console.error('Failed to acknowledge document:', error)
    console.error('ACK DATA:', error.response?.data)
    console.error('ACK STATUS:', error.response?.status)

    actionError.value =
      error.response?.data?.message || error.response?.data || 'Failed to acknowledge document.'
  }
}

function openQuestionManager(document) {
  questionDocument.value = document
  questionMessage.value = ''
  questionError.value = ''
  newQuestion.value = {
    question: '',
    optionA: '',
    optionB: '',
    optionC: '',
    optionD: '',
    correctAnswer: 'A',
  }
}

function closeQuestionManager() {
  questionDocument.value = null
  questionMessage.value = ''
  questionError.value = ''
}

async function saveQuestion() {
  questionMessage.value = ''
  questionError.value = ''

  try {
    await trainingApi.addQuizQuestion(questionDocument.value.id, newQuestion.value)
    questionMessage.value = 'Quiz question saved successfully.'
    questionError.value = ''

    newQuestion.value = {
      question: '',
      optionA: '',
      optionB: '',
      optionC: '',
      optionD: '',
      correctAnswer: 'A',
    }
  } catch (error) {
    console.error('FULL QUIZ ERROR:', error)
    console.error('QUIZ DATA:', error.response?.data)
    console.error('QUIZ STATUS:', error.response?.status)

    questionError.value =
      error.response?.data?.message || error.response?.data || 'Failed to save quiz question.'
  }
}

function openPracticalModal(document) {
  practicalDocument.value = document
  practicalEmployeeId.value = ''
  practicalNote.value = ''
  practicalMessage.value = ''
  practicalError.value = ''
}

function closePracticalModal() {
  practicalDocument.value = null
  practicalEmployeeId.value = ''
  practicalNote.value = ''
  practicalMessage.value = ''
  practicalError.value = ''
}

async function approvePracticalTraining() {
  practicalMessage.value = ''
  practicalError.value = ''

  if (!practicalEmployeeId.value) {
    practicalError.value = 'Please select an employee.'
    return
  }

  try {
    await trainingApi.approvePracticalTraining(practicalDocument.value.id, {
      employeeId: Number(practicalEmployeeId.value),
      note: practicalNote.value,
    })

    practicalMessage.value = 'Practical training approved successfully.'
    await loadData()
  } catch (error) {
    console.error('Failed to approve practical training:', error)
    practicalError.value = error.response?.data?.message || 'Failed to approve practical training.'
  }
}

onMounted(loadData)
</script>

<style scoped>

.welcome-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
}

.welcome-text {
  flex: 1;
}

.training-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
  background: #f7f6f2;
  min-height: 100vh;
}

.form-group {
  display: flex;
  flex-direction: column;
}

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

.feedback-message {
  margin-top: 12px;
}

.selected-file-chip {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  background: #eef4ff;
  color: #1d4ed8;
  border: 1px solid #c7d7fe;
  border-radius: 999px;
  padding: 8px 12px;
  width: fit-content;
  max-width: 100%;
  flex-wrap: wrap;
}

.primary-button:focus-visible,
.secondary-button:focus-visible,
.close-button:focus-visible,
.text-button:focus-visible,
.remove-file-button:focus-visible,
input:focus-visible,
select:focus-visible,
textarea:focus-visible {
  outline: 3px solid #1d4ed8;
  outline-offset: 2px;
}

.training-content {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.welcome-card,
.module-section,
.manager-tools-section,
.upload-card,
.modal,
.stat-card,
.cert-card {
  background: #fff;
  border: 1px solid #ddd;
  border-radius: 10px;
}

.welcome-card,
.module-section,
.manager-tools-section {
  padding: 20px;
}

.welcome-card {
  background: #f9f9f9;
  border: 1px solid #ececec;
}

.sections-grid {
  display: flex;
  flex-direction: column;
  gap: 30px;
}

.module-section h2,
.manager-tools-section h2 {
  margin-top: 0;
  margin-bottom: 16px;
  color: #2c3e50;
}

.section-topbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  margin-bottom: 16px;
}

.filters {
  display: flex;
  gap: 10px;
  align-items: center;
}

.filter-input {
  max-width: 260px;
}

.filter-select {
  padding: 8px 10px;
  border-radius: 6px;
  border: 1px solid #ddd;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 16px;
}

.stat-card,
.cert-card,
.upload-card {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 12px;
}

.training-card-header {
  align-items: flex-start;
}

.card-description {
  color: #555;
  margin-bottom: 14px;
}

.card-actions {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  margin-top: 12px;
}

.manager-inline-tools {
  margin-top: 14px;
}

.text-button {
  background: transparent;
  border: none;
  color: #2563eb;
  font-weight: 600;
  cursor: pointer;
  padding: 0;
}

.completion-badge {
  display: inline-block;
  margin-top: 6px;
  background: #eef4ff;
  color: #1d4ed8;
  border-radius: 999px;
  padding: 6px 10px;
  font-size: 0.8rem;
  font-weight: 700;
}

.status-badge {
  border-radius: 999px;
  padding: 6px 10px;
  font-size: 0.85rem;
  font-weight: 700;
  white-space: nowrap;
}

.status-badge.passed {
  background: #dcfce7;
  color: #166534;
}

.status-badge.failed {
  background: #fee2e2;
  color: #991b1b;
}

.status-badge.neutral {
  background: #e5e7eb;
  color: #374151;
}

.document-content {
  white-space: pre-wrap;
  line-height: 1.6;
  color: #2c3e50;
  max-height: 60vh;
  overflow: auto;
  background: #f8fafc;
  border-radius: 8px;
  padding: 16px;
  border: 1px solid #e5e7eb;
}

.document-fallback {
  margin-top: 16px;
}

.file-viewer {
  margin-top: 16px;
}

.pdf-frame {
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: white;
}

.cert-card {
  display: flex;
  gap: 16px;
  align-items: center;
  border: 1px solid #dbeafe;
  background: linear-gradient(to bottom right, #ffffff, #f8fbff);
}

.cert-icon {
  width: 52px;
  height: 52px;
  min-width: 52px;
  border-radius: 50%;
  background: #dbeafe;
  color: #1d4ed8;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 800;
  font-size: 1.2rem;
}

h3 {
  margin-bottom: 1rem;
  font-size: 1rem;
  font-weight: bold;
  color: #2c3e50;
}

.cert-body h3 {
  margin: 0 0 4px;
}

.cert-subtitle {
  margin: 0 0 8px;
  color: #555;
}

.small-text {
  font-size: 0.9rem;
  color: #666;
}

.form-grid {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 12px;
  margin-bottom: 12px;
}

.file-upload-row {
  margin: 12px 0 16px;
}

.file-upload-label {
  display: block;
  margin-bottom: 8px;
  font-weight: 600;
  color: #2c3e50;
}

.file-upload-box {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.remove-file-button {
  border: none;
  background: transparent;
  color: #1d4ed8;
  font-size: 1rem;
  cursor: pointer;
  padding: 0;
  line-height: 1;
}

input,
select,
textarea {
  width: 100%;
  padding: 12px;
  border: 1px solid #d6d6d6;
  border-radius: 8px;
  font: inherit;
  box-sizing: border-box;
  margin-bottom: 12px;
}

.primary-button,
.secondary-button,
.close-button {
  border: none;
  border-radius: 8px;
  padding: 10px 16px;
  font: inherit;
  cursor: pointer;
}

.primary-button {
  background: #2563eb;
  color: white;
}

.secondary-button {
  background: #eef4ff;
  color: #2563eb;
}

.close-button {
  background: #f3f4f6;
  min-width: 40px;
}

.primary-button:hover,
.secondary-button:hover,
.close-button:hover,
.text-button:hover,
.remove-file-button:hover {
  opacity: 0.92;
}

.ok-text {
  color: #15803d;
  font-weight: 600;
}

.warning-text {
  color: #dc2626;
  font-weight: 600;
}

.info-box {
  margin-top: 14px;
  padding: 12px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  color: #475569;
}

.team-table-wrapper {
  overflow-x: auto;
}

.team-table {
  width: 100%;
  border-collapse: collapse;
}

.team-table th,
.team-table td {
  text-align: left;
  padding: 12px;
  border-bottom: 1px solid #e5e7eb;
}

.overlay {
  position: fixed;
  inset: 0;
  background: rgba(15, 23, 42, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  z-index: 50;
}

.modal {
  width: min(760px, 100%);
  max-height: 85vh;
  overflow-y: auto;
  padding: 20px;
}

.large-modal {
  width: min(900px, 100%);
}

.quiz-question {
  margin-bottom: 20px;
  padding: 16px;
  background: #f8fafc;
  border-radius: 8px;
  border: 1px solid #e5e7eb;
}

.quiz-option {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-top: 10px;
  cursor: pointer;
}

.quiz-option input[type='radio'] {
  margin: 0;
  width: auto;
  transform: none;
  flex-shrink: 0;
}

.quiz-option span {
  display: inline-block;
}

.correct-answer {
  font-weight: 700;
  color: #15803d;
}

.manager-info-text {
  margin-top: 12px;
  color: #475569;
  font-weight: 500;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  padding: 12px;
}

.empty-state {
  padding: 24px 0;
  color: #555;
}

@media (max-width: 768px) {
  .training-page {
    padding: 16px;
  }

  .welcome-header {
    flex-direction: column;
    align-items: stretch;
  }

  .form-grid {
    grid-template-columns: 1fr;
  }

  .section-topbar {
    flex-direction: column;
    align-items: stretch;
  }

  .back-btn-minimal {
    width: 100%;
    text-align: center;
  }

  .section-topbar {
    flex-direction: column;
    align-items: stretch;
  }

  .filters {
    flex-direction: column;
    align-items: stretch;
  }

  .filter-input {
    max-width: none;
  }

  .stats-grid {
    grid-template-columns: 1fr;
  }

  .card-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .card-actions {
    flex-direction: column;
    align-items: stretch;
  }

  .primary-button,
  .secondary-button {
    width: 100%;
  }

  .overlay {
    padding: 12px;
  }

  .modal,
  .large-modal {
    width: 100%;
    max-height: 90vh;
    padding: 16px;
  }
}
</style>
