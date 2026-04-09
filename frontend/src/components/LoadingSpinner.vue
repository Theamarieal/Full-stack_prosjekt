<script setup>
/**
 * LoadingSpinner
 *
 * Reusable loading indicator displayed while async operations are pending.
 * Optionally shows a message below the spinner.
 *
 * @prop {string} [message=''] - Optional message displayed below the spinner.
 */
defineProps({
  message: {
    type: String,
    default: '',
  }
})
</script>

<template>
  <div class="spinner-wrapper" role="status" aria-label="Loading">
    <div class="spinner"></div>
    <p v-if="message" class="spinner-message">{{message}}</p>
  </div>
</template>

<style scoped>
.spinner-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px;
  gap: 12px;
}
.spinner {
  width: 36px;
  height: 36px;
  border: 4px solid #e5e7eb;
  border-top-color: #2563eb;
  border-radius: 50%;
  animation: spin 0.7s linear infinite;
}
@keyframes spin {
  to {transform: rotate(360deg);}
}
.spinner-message {
  color: #6b7280;
  font-size: 0.95rem;
}
</style>
