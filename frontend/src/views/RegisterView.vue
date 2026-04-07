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
  min-height: 100vh;
  background-color: #f7f6f2;
  padding: 12px;
}

.register-container {
  background: white;
  width: 100%;
  max-width: 440px;
  padding: 2rem 1.5rem;
  border-radius: 16px;
  border: 1px solid #e0dfd8;
}

h2 {
  text-align: center;
  color: #3C3489;
  margin-bottom: 4px;
}

.subtitle {
  text-align: center;
  color: #666;
  font-size: 0.9rem;
  margin-bottom: 1.5rem;
}

.form-group {
  margin-bottom: 1rem;
}

label {
  display: block;
  font-weight: 600;
  font-size: 0.85rem;
  margin-bottom: 4px;
}

input, select {
  width: 100%;
  padding: 12px;
  border: 1.5px solid #e0dfd8;
  border-radius: 10px;
  font-size: 16px;
}

button {
  width: 100%;
  padding: 14px;
  background-color: #4CAF50;
  color: white;
  border: none;
  border-radius: 10px;
  font-weight: 700;
  margin-top: 1rem;
}

.footer-links {
  text-align: center;
  margin-top: 1.5rem;
  font-size: 0.85rem;
}

/* Gjelder både for .login-card og .register-container */
@media (max-width: 350px) {
  .login-page, .register-wrapper {
    padding: 0;
    background: white; /* Gjør hele bakgrunnen hvit på mobil */
    align-items: flex-start; 
  }

  .login-card, .register-container {
    border: none;
    border-radius: 0;
    box-shadow: none;
    padding: 2rem 1.5rem;
    
    /* FIKSEN: Dette tvinger den hvite boksen til å fylle hele skjermhøyden */
    min-height: 100vh; 
    display: flex;
    flex-direction: column;
  }
  
  /* Sørger for at footeren alltid havner nederst i den hvite boksen */
  .footer-links, .footer-link {
    margin-top: auto; 
    padding-bottom: 2rem;
  }
}
</style>