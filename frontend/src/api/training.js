import api from '@/api/axios'

export default {
  getPolicies() {
    return api.get('/training/policies')
  },

  getMaterials() {
    return api.get('/training/materials')
  },

  getMyStatuses() {
    return api.get('/training/statuses/me')
  },

  getMyCertifications() {
    return api.get('/training/certifications/me')
  },

  getTeamCertifications() {
    return api.get('/training/certifications/team')
  },

  createDocument(payload) {
    return api.post('/training/documents', payload)
  },

  uploadDocument(formData) {
    return api.post('/training/documents/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    })
  },

  downloadDocumentFile(documentId) {
    return api.get(`/training/documents/${documentId}/file`, {
      responseType: 'blob',
    })
  },

  getQuiz(documentId) {
    return api.get(`/training/${documentId}/quiz`)
  },

  submitQuiz(documentId, answers) {
    return api.post(`/training/${documentId}/quiz/submit`, { answers })
  },

  addQuizQuestion(documentId, payload) {
    return api.post(`/training/${documentId}/quiz/questions`, payload)
  },

  acknowledgeDocument(documentId) {
    return api.post(`/training/${documentId}/acknowledge`)
  },

  approvePracticalTraining(documentId, payload) {
    return api.post(`/training/${documentId}/practical-signoff`, payload)
  },

  getEmployees() {
    return api.get('/training/employees')
  },

  getMyAcknowledgements() {
    return api.get('/training/acknowledgements/me')
  },

  getTeamAcknowledgements() {
    return api.get('/training/acknowledgements/team')
  },
}
