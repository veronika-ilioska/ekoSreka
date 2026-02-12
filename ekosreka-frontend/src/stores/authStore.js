import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import { authService } from '../services/authService';

export const useAuthStore = defineStore('auth', () => {
  const user = ref(authService.getUser());
  const token = ref(authService.getToken());
  const loading = ref(false);
  const error = ref(null);

  const isAuthenticated = computed(() => !!token.value);
  const userName = computed(() => user.value?.name || 'User');

  async function login(email, password) {
    loading.value = true;
    error.value = null;
    try {
      const data = await authService.login(email, password);
      user.value = data.user;
      token.value = data.token;
      return data;
    } catch (err) {
      error.value = err.response?.data?.message || 'Login failed';
      throw err;
    } finally {
      loading.value = false;
    }
  }

  async function signup(name, email, password, passwordConfirm) {
    loading.value = true;
    error.value = null;
    try {
      const data = await authService.signup(name, email, password, passwordConfirm);
      user.value = data.user;
      token.value = data.token;
      return data;
    } catch (err) {
      error.value = err.response?.data?.message || 'Signup failed';
      throw err;
    } finally {
      loading.value = false;
    }
  }

  function logout() {
    authService.logout();
    user.value = null;
    token.value = null;
    error.value = null;
  }

  function clearError() {
    error.value = null;
  }

  return {
    user,
    token,
    loading,
    error,
    isAuthenticated,
    userName,
    login,
    signup,
    logout,
    clearError,
  };
});
