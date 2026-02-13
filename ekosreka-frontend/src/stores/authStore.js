import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import { authService } from '../services/authService';

export const useAuthStore = defineStore('auth', () => {
  const user = ref(authService.getUser());
  const userId = ref(authService.getUserId());
  const loading = ref(false);
  const error = ref(null);

  const isAuthenticated = computed(() => !!userId.value);
  const userName = computed(() => {
    if (user.value?.username) return user.value.username;
    if (user.value?.firstName) return user.value.firstName;
    return 'User';
  });

  async function login(username, password) {
    loading.value = true;
    error.value = null;
    try {
      const data = await authService.login(username, password);
      user.value = data;
      userId.value = data.id;
      return data;
    } catch (err) {
      error.value = err.response?.data?.message || 'Login failed';
      throw err;
    } finally {
      loading.value = false;
    }
  }

  async function signup(username, email, password, confirmPassword, firstName, lastName) {
    loading.value = true;
    error.value = null;
    try {
      const data = await authService.signup(
        username,
        email,
        password,
        confirmPassword,
        firstName,
        lastName
      );
      user.value = data;
      userId.value = data.id;
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
    userId.value = null;
    error.value = null;
  }

  function clearError() {
    error.value = null;
  }

  return {
    user,
    userId,
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
