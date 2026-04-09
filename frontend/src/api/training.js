import api from '@/api/axios'

/**
 * API service for training and certification endpoints.
 *
 * Provides methods for managing training documents, quizzes, acknowledgements,
 * practical sign-offs, and certifications within the IK-Alkohol and IK-Mat modules.
 *
 * @module trainingApi
 */
export default {
  /**
   * Fetches all policy documents available for the current organization.
   *
   * @returns {Promise<Object>} Axios response containing a list of policy documents.
   */
  getPolicies() {
    return api.get('/training/policies')
  },

  /**
   * Fetches all training material documents available for the current organization.
   *
   * @returns {Promise<Object>} Axios response containing a list of training materials.
   */
  getMaterials() {
    return api.get('/training/materials')
  },

  /**
   * Fetches the training completion statuses for the currently authenticated user.
   *
   * @returns {Promise<Object>} Axios response containing the user's training statuses.
   */
  getMyStatuses() {
    return api.get('/training/statuses/me')
  },

  /**
   * Fetches certifications earned by the currently authenticated user.
   *
   * @returns {Promise<Object>} Axios response containing the user's certifications.
   */
  getMyCertifications() {
    return api.get('/training/certifications/me')
  },

  /**
   * Fetches certifications for all members of the current organization.
   * Intended for use by managers and administrators.
   *
   * @returns {Promise<Object>} Axios response containing team certification data.
   */
  getTeamCertifications() {
    return api.get('/training/certifications/team')
  },

  /**
   * Creates a new training document (e.g. policy or material) via metadata.
   *
   * @param {Object} payload - The document metadata.
   * @param {string} payload.title - Title of the document.
   * @param {string} payload.type - Document type (e.g. 'POLICY', 'MATERIAL').
   * @param {string} payload.completionType - How completion is tracked (e.g. 'QUIZ', 'ACKNOWLEDGEMENT', 'PRACTICAL').
   * @returns {Promise<Object>} Axios response containing the created document.
   */
  createDocument(payload) {
    return api.post('/training/documents', payload)
  },

  /**
   * Uploads a training document as a file (multipart/form-data).
   *
   * @param {FormData} formData - Form data containing the file and metadata.
   * @returns {Promise<Object>} Axios response containing the uploaded document.
   */
  uploadDocument(formData) {
    return api.post('/training/documents/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    })
  },

  /**
   * Downloads the file associated with a training document.
   *
   * @param {number} documentId - The ID of the document to download.
   * @returns {Promise<Blob>} Axios response with the file as a binary blob.
   */
  downloadDocumentFile(documentId) {
    return api.get(`/training/documents/${documentId}/file`, {
      responseType: 'blob',
    })
  },

  /**
   * Fetches the quiz questions for a specific training document.
   *
   * @param {number} documentId - The ID of the document whose quiz to retrieve.
   * @returns {Promise<Object>} Axios response containing quiz questions.
   */
  getQuiz(documentId) {
    return api.get(`/training/${documentId}/quiz`)
  },

  /**
   * Submits quiz answers for a specific training document.
   *
   * @param {number} documentId - The ID of the document being assessed.
   * @param {Array<number>} answers - Array of selected answer indices.
   * @returns {Promise<Object>} Axios response containing the quiz result.
   */
  submitQuiz(documentId, answers) {
    return api.post(`/training/${documentId}/quiz/submit`, { answers })
  },

  /**
   * Adds a new quiz question to a training document.
   * Only accessible to managers and administrators.
   *
   * @param {number} documentId - The ID of the document to add the question to.
   * @param {Object} payload - The quiz question data.
   * @param {string} payload.question - The question text.
   * @param {Array<string>} payload.options - List of answer options.
   * @param {number} payload.correctIndex - Index of the correct answer.
   * @returns {Promise<Object>} Axios response containing the created question.
   */
  addQuizQuestion(documentId, payload) {
    return api.post(`/training/${documentId}/quiz/questions`, payload)
  },

  /**
   * Records that the current user has acknowledged a training document.
   *
   * @param {number} documentId - The ID of the document to acknowledge.
   * @returns {Promise<Object>} Axios response confirming the acknowledgement.
   */
  acknowledgeDocument(documentId) {
    return api.post(`/training/${documentId}/acknowledge`)
  },

  /**
   * Records a practical training sign-off for a specific employee.
   * Only accessible to managers and administrators.
   *
   * @param {number} documentId - The ID of the document the sign-off relates to.
   * @param {Object} payload - The sign-off data.
   * @param {number} payload.employeeId - The ID of the employee being signed off.
   * @param {string} [payload.notes] - Optional notes from the approving manager.
   * @returns {Promise<Object>} Axios response confirming the sign-off.
   */
  approvePracticalTraining(documentId, payload) {
    return api.post(`/training/${documentId}/practical-signoff`, payload)
  },

  /**
   * Fetches a list of employees in the current organization.
   * Used for selecting employees in practical training sign-off forms.
   *
   * @returns {Promise<Object>} Axios response containing a list of employee options.
   */
  getEmployees() {
    return api.get('/training/employees')
  },

  /**
   * Fetches all document acknowledgements made by the currently authenticated user.
   *
   * @returns {Promise<Object>} Axios response containing the user's acknowledgements.
   */
  getMyAcknowledgements() {
    return api.get('/training/acknowledgements/me')
  },

  /**
   * Fetches all document acknowledgements across the current organization.
   * Intended for use by managers and administrators.
   *
   * @returns {Promise<Object>} Axios response containing team acknowledgement data.
   */
  getTeamAcknowledgements() {
    return api.get('/training/acknowledgements/team')
  },
}
