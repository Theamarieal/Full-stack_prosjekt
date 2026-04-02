<template>
  <div class="register-wrapper">
    <div class="register-container">
      <h2>Create user</h2>
      <p class="subtitle">Become a part of Testrestaurant AS</p>

      <form @submit.prevent="handleRegister">
        <div class="form-group">
          <label>E-mail</label>
          <input 
            v-model="email" 
            type="email" 
            placeholder="name@bedrift.no" 
            required 
          />
        </div>

        <div class="form-group">
          <label>Password</label>
          <input 
            v-model="password" 
            type="password" 
            placeholder="At least 8 characters" 
            required 
          />
        </div>
        
        <div class="form-group">
          <label>Your role</label>
          <select v-model="role">
            <option value="EMPLOYEE">Employee</option>
            <option value="MANAGER">Manager</option>
          </select>
        </div>

        <button type="submit" :disabled="loading">
          {{ loading ? 'Creating account...' : 'Sign me up' }}
        </button>
      </form>

      <p v-if="error" class="error-message">{{ error }}</p>
      
      <div class="footer-links">
        <span>Do you already have a user? </span>
        <router-link to="/login">Log in here</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useAuthStore } from '@/stores/auth';
import { useRouter } from 'vue-router';

const auth = useAuthStore();
const router = useRouter();

const email = ref('');
const password = ref('');
const role = ref('EMPLOYEE');
const error = ref('');
const loading = ref(false);

const handleRegister = async () => {
  error.value = '';
  loading.value = true;

  try {
    // IMPORTANT: we send arguments each by each like they're defined in auth.js
    // email, password, role, organizationId (we use 1 as standard)
    const success = await auth.register(
      email.value, 
      password.value, 
      role.value, 
      1
    );

    if (success) {
      // send user to dashboard if signup went well
      router.push('/dashboard');
    } else {
      error.value = "Something went wrong. Try again.";
    }
  } catch (err) {
    error.value = "Could not create user. Maybe e-mail is already in use?";
  } finally {
    loading.value = false;
  }
};
</script>

<style scoped>
.register-wrapper {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 80vh;
}

.register-container {
  background: white;
  padding: 2rem;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
  width: 100%;
  max-width: 400px;
}

h2 {
  margin-bottom: 0.5rem;
  text-align: center;
}

.subtitle {
  text-align: center;
  color: #666;
  margin-bottom: 1.5rem;
}

.form-group {
  margin-bottom: 1rem;
  display: flex;
  flex-direction: column;
}

label {
  margin-bottom: 0.3rem;
  font-weight: bold;
  font-size: 0.9rem;
}

input, select {
  padding: 0.6rem;
  border: 1px solid #ccc;
  border-radius: 4px;
  font-size: 1rem;
}

button {
  width: 100%;
  padding: 0.8rem;
  background-color: #4CAF50;
  color: white;
  border: none;
  border-radius: 4px;
  font-size: 1rem;
  cursor: pointer;
  margin-top: 1rem;
}

button:disabled {
  background-color: #ccc;
}

.error-message {
  color: #d32f2f;
  background: #ffebee;
  padding: 0.5rem;
  border-radius: 4px;
  margin-top: 1rem;
  text-align: center;
}

.footer-links {
  margin-top: 1.5rem;
  text-align: center;
  font-size: 0.9rem;
}

a {
  color: #4CAF50;
  text-decoration: none;
  font-weight: bold;
}
</style>