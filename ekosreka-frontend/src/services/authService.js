import  api  from '../api';

export const authService = {
  async login(username, password) {
    const response = await api.post('/auth/login', { username, password });
    if (response.data.id) {
      localStorage.setItem('userId', response.data.id);
      localStorage.setItem('user', JSON.stringify(response.data));
    }
    return response.data;
  },

  async signup(username, email, password, confirmPassword, firstName, lastName) {
    const response = await api.post('/auth/signup', {
      username,
      email,
      password,
      confirmPassword,
      firstName,
      lastName,
    });
    if (response.data.id) {
      localStorage.setItem('userId', response.data.id);
      localStorage.setItem('user', JSON.stringify(response.data));
    }
    return response.data;
  },

  logout() {
    localStorage.removeItem('userId');
    localStorage.removeItem('user');
  },

  getUserId() {
    return localStorage.getItem('userId');
  },

  getUser() {
    const user = localStorage.getItem('user');
    return user ? JSON.parse(user) : null;
  },

  isAuthenticated() {
    return !!this.getUserId();
  },

  initializeAuth() {
    const user = this.getUser();
    if (user) {
      // Auth is managed via userId in localStorage
    }
  },
};
