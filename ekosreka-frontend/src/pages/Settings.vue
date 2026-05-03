<template>
  <section class="settings-page">
    <div class="settings-shell">
      <header class="settings-hero">
        <div>
          <p class="eyebrow">Settings</p>
          <h1>Make EkoSrekja feel right for you</h1>
          <p>Choose how the app behaves, what updates matter, and where your eco activity starts.</p>
        </div>
        <RouterLink class="btn hero-button" to="/profile">Edit profile</RouterLink>
      </header>

      <div v-if="savedMessage" class="alert alert-success">{{ savedMessage }}</div>

      <div class="settings-grid">
        <article class="settings-panel">
          <div class="panel-heading">
            <h2>Preferences</h2>
            <p>Saved on this device for quick, comfortable browsing.</p>
          </div>

          <form class="settings-form" @submit.prevent="saveSettings">
            <div class="field">
              <label for="startPage">Start page</label>
              <select id="startPage" v-model="settings.startPage" class="form-select">
                <option value="/">Home</option>
                <option value="/news">News</option>
                <option value="/fun">Fun</option>
                <option value="/quizzes">Quizzes</option>
              </select>
            </div>

            <div class="field">
              <label for="contentDensity">Content density</label>
              <select id="contentDensity" v-model="settings.contentDensity" class="form-select">
                <option value="comfortable">Comfortable</option>
                <option value="compact">Compact</option>
              </select>
            </div>

            <div class="field">
              <label for="ecoGoal">Eco goal</label>
              <select id="ecoGoal" v-model="settings.ecoGoal" class="form-select">
                <option value="learn">Learn more each week</option>
                <option value="reduce">Reduce household waste</option>
                <option value="energy">Save energy</option>
                <option value="nature">Spend more time in nature</option>
              </select>
            </div>

            <div class="toggle-list">
              <label class="toggle-row">
                <span>
                  <strong>Weekly eco digest</strong>
                  <small>Show reminders for recent news and activity ideas.</small>
                </span>
                <input v-model="settings.weeklyDigest" type="checkbox" />
              </label>

              <label class="toggle-row">
                <span>
                  <strong>Quiz reminders</strong>
                  <small>Keep quiz suggestions visible around the app.</small>
                </span>
                <input v-model="settings.quizReminders" type="checkbox" />
              </label>

              <label class="toggle-row">
                <span>
                  <strong>Reduced motion</strong>
                  <small>Use calmer hover and transition effects where supported.</small>
                </span>
                <input v-model="settings.reducedMotion" type="checkbox" />
              </label>
            </div>

            <div class="actions">
              <button class="btn eco-btn" type="submit">Save settings</button>
              <button class="btn btn-outline-secondary" type="button" @click="resetSettings">
                Reset
              </button>
            </div>
          </form>
        </article>

        <aside class="settings-stack">
          <article class="settings-panel account-card">
            <h2>Account</h2>
            <div class="account-row">
              <span>Name</span>
              <strong>{{ displayName }}</strong>
            </div>
            <div class="account-row">
              <span>Email</span>
              <strong>{{ authStore.user?.email || 'Not available' }}</strong>
            </div>
            <RouterLink class="btn btn-outline-success w-100" to="/profile">
              Manage account details
            </RouterLink>
          </article>

          <article class="settings-panel">
            <h2>Quick Links</h2>
            <div class="quick-links">
              <RouterLink :to="settings.startPage">Open start page</RouterLink>
              <RouterLink to="/profile">Change password</RouterLink>
              <button type="button" @click="logout">Log out</button>
            </div>
          </article>
        </aside>
      </div>
    </div>
  </section>
</template>

<script setup>
  import { computed, onMounted, reactive, ref } from 'vue';
  import { useRouter } from 'vue-router';
  import { useAuthStore } from '../stores/authStore';

  const STORAGE_KEY = 'ekosrekja.settings';

  const router = useRouter();
  const authStore = useAuthStore();
  const savedMessage = ref('');

  const defaults = {
    startPage: '/',
    contentDensity: 'comfortable',
    ecoGoal: 'learn',
    weeklyDigest: true,
    quizReminders: true,
    reducedMotion: false,
  };

  const settings = reactive({ ...defaults });

  const displayName = computed(() => {
    const firstName = authStore.user?.firstName?.trim();
    const lastName = authStore.user?.lastName?.trim();
    return [firstName, lastName].filter(Boolean).join(' ') || authStore.userName;
  });

  function applySettings() {
    document.documentElement.dataset.contentDensity = settings.contentDensity;
    document.documentElement.dataset.reducedMotion = settings.reducedMotion ? 'true' : 'false';
  }

  function loadSettings() {
    const saved = localStorage.getItem(STORAGE_KEY);
    if (saved) {
      Object.assign(settings, defaults, JSON.parse(saved));
    }
    applySettings();
  }

  function saveSettings() {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(settings));
    applySettings();
    savedMessage.value = 'Settings saved successfully.';
    window.setTimeout(() => {
      savedMessage.value = '';
    }, 2500);
  }

  function resetSettings() {
    Object.assign(settings, defaults);
    saveSettings();
  }

  function logout() {
    authStore.logout();
    router.push('/');
  }

  onMounted(() => {
    if (!authStore.isAuthenticated) {
      router.push('/login');
      return;
    }

    loadSettings();
  });
</script>

<style scoped>
  .settings-page {
    color: var(--eco-text-dark);
    padding: 1rem 0 4rem;
  }

  .settings-shell {
    margin: 0 auto;
    max-width: 1120px;
  }

  .settings-hero {
    align-items: center;
    background: linear-gradient(135deg, #458051 0%, #66bb6a 100%);
    border-radius: 8px;
    color: #fff;
    display: grid;
    gap: 1.5rem;
    grid-template-columns: minmax(0, 1fr) auto;
    margin-bottom: 1.5rem;
    padding: 2rem;
  }

  .eyebrow {
    font-size: 0.78rem;
    font-weight: 800;
    letter-spacing: 0.08em;
    margin: 0 0 0.45rem;
    text-transform: uppercase;
  }

  .settings-hero h1 {
    font-size: clamp(2rem, 5vw, 3.3rem);
    font-weight: 800;
    letter-spacing: 0;
    line-height: 1.05;
    margin: 0 0 0.75rem;
  }

  .settings-hero p {
    margin: 0;
    max-width: 680px;
    opacity: 0.92;
  }

  .hero-button {
    background: rgba(255, 255, 255, 0.18);
    border: 1px solid rgba(255, 255, 255, 0.55);
    color: #fff;
    font-weight: 800;
    min-width: 128px;
  }

  .settings-grid {
    display: grid;
    gap: 1.5rem;
    grid-template-columns: minmax(0, 1fr) 360px;
  }

  .settings-panel {
    background: var(--eco-card-bg);
    border: 1px solid rgba(69, 128, 81, 0.15);
    border-radius: 8px;
    box-shadow: 0 8px 24px rgba(27, 42, 27, 0.08);
    padding: 1.5rem;
  }

  .settings-panel h2 {
    color: #1f3b1f;
    font-size: 1.3rem;
    font-weight: 800;
    letter-spacing: 0;
    margin: 0 0 0.35rem;
  }

  .panel-heading {
    margin-bottom: 1.5rem;
  }

  .panel-heading p {
    color: #506650;
    margin: 0;
  }

  .settings-form,
  .settings-stack {
    display: grid;
    gap: 1rem;
  }

  .field {
    display: grid;
    gap: 0.45rem;
  }

  .field label {
    color: #506650;
    font-size: 0.88rem;
    font-weight: 800;
  }

  .form-select {
    border: 1px solid rgba(69, 128, 81, 0.28);
    border-radius: 8px;
    min-height: 44px;
  }

  .form-select:focus {
    border-color: #458051;
    box-shadow: 0 0 0 0.2rem rgba(102, 187, 106, 0.2);
  }

  .toggle-list {
    border-top: 1px solid rgba(69, 128, 81, 0.15);
    display: grid;
    margin-top: 0.5rem;
  }

  .toggle-row {
    align-items: center;
    border-bottom: 1px solid rgba(69, 128, 81, 0.15);
    display: flex;
    gap: 1rem;
    justify-content: space-between;
    padding: 1rem 0;
  }

  .toggle-row span {
    display: grid;
    gap: 0.2rem;
  }

  .toggle-row strong {
    color: #1b2a1b;
  }

  .toggle-row small {
    color: #506650;
  }

  .toggle-row input {
    accent-color: #458051;
    height: 1.2rem;
    width: 1.2rem;
  }

  .actions {
    display: flex;
    gap: 0.75rem;
    margin-top: 0.5rem;
  }

  .account-card {
    display: grid;
    gap: 1rem;
  }

  .account-row {
    border-bottom: 1px solid rgba(69, 128, 81, 0.15);
    display: grid;
    gap: 0.25rem;
    padding-bottom: 0.85rem;
  }

  .account-row span {
    color: #506650;
    font-size: 0.82rem;
    font-weight: 800;
    text-transform: uppercase;
  }

  .account-row strong {
    color: #1b2a1b;
    overflow-wrap: anywhere;
  }

  .quick-links {
    display: grid;
    gap: 0.5rem;
    margin-top: 1rem;
  }

  .quick-links a,
  .quick-links button {
    background: rgba(102, 187, 106, 0.1);
    border: 1px solid rgba(69, 128, 81, 0.14);
    border-radius: 8px;
    color: #2e7d32;
    font-weight: 800;
    padding: 0.75rem 0.9rem;
    text-align: left;
    text-decoration: none;
  }

  .quick-links button {
    color: #b42318;
  }

  .alert {
    border-radius: 8px;
  }

  @media (max-width: 991px) {
    .settings-grid,
    .settings-hero {
      grid-template-columns: 1fr;
    }

    .hero-button {
      justify-self: start;
    }
  }

  @media (max-width: 576px) {
    .settings-hero,
    .settings-panel {
      padding: 1.25rem;
    }

    .actions,
    .toggle-row {
      align-items: stretch;
      flex-direction: column;
    }
  }
</style>
