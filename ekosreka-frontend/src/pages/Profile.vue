<template>
  <section class="profile-page">
    <div class="profile-shell">
      <header class="profile-hero">
        <div class="avatar" aria-hidden="true">{{ initials }}</div>
        <div class="hero-copy">
          <p class="eyebrow">My profile</p>
          <h1>{{ displayName }}</h1>
          <p>{{ user?.email || 'Keep your account details up to date.' }}</p>
        </div>
        <button v-if="!isEditing" class="btn eco-btn edit-button" type="button" @click="startEditing">
          <i class="bi bi-pencil-square me-2"></i>
          Edit
        </button>
      </header>

      <div v-if="successMessage" class="alert alert-success">{{ successMessage }}</div>
      <div v-if="pageError" class="alert alert-danger">{{ pageError }}</div>

      <div class="profile-grid">
        <article class="profile-panel">
          <div class="panel-heading">
            <h2>Account Details</h2>
            <p>These details identify your account across EkoSrekja.</p>
          </div>

          <div v-if="!isEditing" class="details-list">
            <div class="detail-row">
              <span>First name</span>
              <strong>{{ user?.firstName || 'Not set' }}</strong>
            </div>
            <div class="detail-row">
              <span>Last name</span>
              <strong>{{ user?.lastName || 'Not set' }}</strong>
            </div>
            <div class="detail-row">
              <span>Username</span>
              <strong>{{ user?.username }}</strong>
            </div>
            <div class="detail-row">
              <span>Email</span>
              <strong>{{ user?.email }}</strong>
            </div>
            <div class="detail-row">
              <span>Member since</span>
              <strong>{{ memberSince }}</strong>
            </div>
          </div>

          <form v-else class="profile-form" @submit.prevent="saveProfile">
            <div class="form-field">
              <label for="firstName">First name</label>
              <input id="firstName" v-model.trim="form.firstName" type="text" class="form-control" />
            </div>
            <div class="form-field">
              <label for="lastName">Last name</label>
              <input id="lastName" v-model.trim="form.lastName" type="text" class="form-control" />
            </div>
            <div class="form-field">
              <label for="email">Email</label>
              <input
                id="email"
                v-model.trim="form.email"
                type="email"
                class="form-control"
                required
              />
            </div>
            <div class="form-field">
              <label for="username">Username</label>
              <input id="username" :value="user?.username" type="text" class="form-control" disabled />
              <small>Username cannot be changed.</small>
            </div>

            <div v-if="formError" class="alert alert-danger">{{ formError }}</div>

            <div class="form-actions">
              <button class="btn eco-btn" type="submit" :disabled="isSaving">
                {{ isSaving ? 'Saving...' : 'Save changes' }}
              </button>
              <button class="btn btn-outline-secondary" type="button" @click="cancelEditing">
                Cancel
              </button>
            </div>
          </form>
        </article>

        <aside class="profile-panel side-panel">
          <h2>Security</h2>
          <p class="side-copy">Change your password after confirming the current one.</p>

          <form class="password-form" @submit.prevent="changePassword">
            <div class="form-field">
              <label for="currentPassword">Current password</label>
              <input
                id="currentPassword"
                v-model="passwordForm.currentPassword"
                type="password"
                class="form-control"
                autocomplete="current-password"
              />
            </div>
            <div class="form-field">
              <label for="newPassword">New password</label>
              <input
                id="newPassword"
                v-model="passwordForm.newPassword"
                type="password"
                class="form-control"
                autocomplete="new-password"
              />
            </div>
            <div class="form-field">
              <label for="confirmPassword">Confirm password</label>
              <input
                id="confirmPassword"
                v-model="passwordForm.confirmPassword"
                type="password"
                class="form-control"
                autocomplete="new-password"
              />
            </div>

            <div v-if="passwordError" class="alert alert-danger">{{ passwordError }}</div>

            <button class="btn btn-outline-success w-100" type="submit" :disabled="isChangingPassword">
              {{ isChangingPassword ? 'Updating...' : 'Update password' }}
            </button>
          </form>
        </aside>
      </div>
    </div>
  </section>
</template>

<script setup>
  import { computed, onMounted, reactive, ref } from 'vue';
  import { useRouter } from 'vue-router';
  import { api } from '../api';
  import { useAuthStore } from '../stores/authStore';

  const router = useRouter();
  const authStore = useAuthStore();

  const user = computed(() => authStore.user);
  const isEditing = ref(false);
  const isSaving = ref(false);
  const isChangingPassword = ref(false);
  const pageError = ref('');
  const formError = ref('');
  const passwordError = ref('');
  const successMessage = ref('');

  const form = reactive({
    firstName: '',
    lastName: '',
    email: '',
  });

  const passwordForm = reactive({
    currentPassword: '',
    newPassword: '',
    confirmPassword: '',
  });

  const displayName = computed(() => {
    const firstName = user.value?.firstName?.trim();
    const lastName = user.value?.lastName?.trim();
    const fullName = [firstName, lastName].filter(Boolean).join(' ');
    return fullName || user.value?.username || 'Your profile';
  });

  const initials = computed(() => {
    const source = displayName.value || user.value?.email || 'User';
    return source
      .split(/\s+/)
      .filter(Boolean)
      .slice(0, 2)
      .map((part) => part[0])
      .join('')
      .toUpperCase();
  });

  const memberSince = computed(() => {
    if (!user.value?.createdAt) return 'Unknown';
    return new Date(Number(user.value.createdAt)).toLocaleDateString(undefined, {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
    });
  });

  function fillForm() {
    form.firstName = user.value?.firstName || '';
    form.lastName = user.value?.lastName || '';
    form.email = user.value?.email || '';
  }

  function requireLogin() {
    if (!authStore.isAuthenticated || !authStore.userId) {
      router.push('/login');
      return false;
    }
    return true;
  }

  async function loadProfile() {
    if (!requireLogin()) return;

    try {
      const { data } = await api.get(`/profile/${authStore.userId}`);
      authStore.updateUser(data);
      fillForm();
    } catch (error) {
      pageError.value = error.response?.data?.message || 'Could not load your profile.';
    }
  }

  function startEditing() {
    successMessage.value = '';
    formError.value = '';
    fillForm();
    isEditing.value = true;
  }

  function cancelEditing() {
    isEditing.value = false;
    formError.value = '';
    fillForm();
  }

  async function saveProfile() {
    if (!requireLogin()) return;
    formError.value = '';
    successMessage.value = '';

    if (!form.email) {
      formError.value = 'Email is required.';
      return;
    }

    isSaving.value = true;
    try {
      const { data } = await api.put(`/profile/${authStore.userId}`, {
        firstName: form.firstName,
        lastName: form.lastName,
        email: form.email,
      });

      authStore.updateUser(data);
      isEditing.value = false;
      successMessage.value = 'Profile updated successfully.';
    } catch (error) {
      formError.value = error.response?.data?.message || 'Could not update your profile.';
    } finally {
      isSaving.value = false;
    }
  }

  async function changePassword() {
    if (!requireLogin()) return;
    passwordError.value = '';
    successMessage.value = '';

    if (!passwordForm.currentPassword || !passwordForm.newPassword) {
      passwordError.value = 'Current and new password are required.';
      return;
    }

    if (passwordForm.newPassword.length < 6) {
      passwordError.value = 'New password must be at least 6 characters.';
      return;
    }

    if (passwordForm.newPassword !== passwordForm.confirmPassword) {
      passwordError.value = 'New passwords do not match.';
      return;
    }

    isChangingPassword.value = true;
    try {
      await api.post(`/profile/${authStore.userId}/change-password`, {
        currentPassword: passwordForm.currentPassword,
        newPassword: passwordForm.newPassword,
      });

      passwordForm.currentPassword = '';
      passwordForm.newPassword = '';
      passwordForm.confirmPassword = '';
      successMessage.value = 'Password updated successfully.';
    } catch (error) {
      passwordError.value = error.response?.data || 'Could not update your password.';
    } finally {
      isChangingPassword.value = false;
    }
  }

  onMounted(loadProfile);
</script>

<style scoped>
  .profile-page {
    color: var(--eco-text-dark);
    padding: 1rem 0 4rem;
  }

  .profile-shell {
    max-width: 1120px;
    margin: 0 auto;
  }

  .profile-hero {
    align-items: center;
    background: linear-gradient(135deg, #458051 0%, #66bb6a 100%);
    border-radius: 8px;
    color: #fff;
    display: grid;
    gap: 1.5rem;
    grid-template-columns: auto 1fr auto;
    margin-bottom: 1.5rem;
    padding: 2rem;
  }

  .avatar {
    align-items: center;
    aspect-ratio: 1;
    background: rgba(255, 255, 255, 0.2);
    border: 3px solid rgba(255, 255, 255, 0.75);
    border-radius: 50%;
    display: flex;
    font-size: 2rem;
    font-weight: 800;
    justify-content: center;
    width: 96px;
  }

  .hero-copy h1 {
    font-size: 2rem;
    font-weight: 800;
    letter-spacing: 0;
    margin: 0 0 0.35rem;
  }

  .hero-copy p {
    margin: 0;
    opacity: 0.9;
  }

  .eyebrow {
    font-size: 0.78rem;
    font-weight: 800;
    letter-spacing: 0.08em;
    text-transform: uppercase;
  }

  .edit-button {
    min-width: 112px;
  }

  .profile-grid {
    display: grid;
    gap: 1.5rem;
    grid-template-columns: minmax(0, 1fr) 360px;
  }

  .profile-panel {
    background: var(--eco-card-bg);
    border: 1px solid rgba(69, 128, 81, 0.15);
    border-radius: 8px;
    box-shadow: 0 8px 24px rgba(27, 42, 27, 0.08);
    padding: 1.5rem;
  }

  .panel-heading,
  .side-panel > h2 {
    margin-bottom: 1.5rem;
  }

  .profile-panel h2 {
    color: #1f3b1f;
    font-size: 1.3rem;
    font-weight: 800;
    letter-spacing: 0;
    margin: 0 0 0.35rem;
  }

  .panel-heading p,
  .side-copy {
    color: #506650;
    margin: 0;
  }

  .details-list {
    border-top: 1px solid rgba(69, 128, 81, 0.15);
  }

  .detail-row {
    align-items: center;
    border-bottom: 1px solid rgba(69, 128, 81, 0.15);
    display: flex;
    gap: 1rem;
    justify-content: space-between;
    padding: 1rem 0;
  }

  .detail-row span,
  .form-field label {
    color: #506650;
    font-size: 0.88rem;
    font-weight: 700;
  }

  .detail-row strong {
    color: #1b2a1b;
    overflow-wrap: anywhere;
    text-align: right;
  }

  .profile-form,
  .password-form {
    display: grid;
    gap: 1rem;
  }

  .form-field {
    display: grid;
    gap: 0.45rem;
  }

  .form-control {
    border: 1px solid rgba(69, 128, 81, 0.28);
    border-radius: 8px;
    min-height: 44px;
  }

  .form-control:focus {
    border-color: #458051;
    box-shadow: 0 0 0 0.2rem rgba(102, 187, 106, 0.2);
  }

  .form-field small {
    color: #687868;
  }

  .form-actions {
    display: flex;
    gap: 0.75rem;
    margin-top: 0.5rem;
  }

  .alert {
    border-radius: 8px;
  }

  @media (max-width: 991px) {
    .profile-grid {
      grid-template-columns: 1fr;
    }

    .profile-hero {
      grid-template-columns: auto 1fr;
    }

    .edit-button {
      grid-column: 1 / -1;
      justify-self: start;
    }
  }

  @media (max-width: 576px) {
    .profile-hero {
      grid-template-columns: 1fr;
      padding: 1.5rem;
      text-align: center;
    }

    .avatar {
      justify-self: center;
    }

    .edit-button {
      justify-self: stretch;
    }

    .detail-row,
    .form-actions {
      align-items: stretch;
      flex-direction: column;
    }

    .detail-row strong {
      text-align: left;
    }
  }
</style>
