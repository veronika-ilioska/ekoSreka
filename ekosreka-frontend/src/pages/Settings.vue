<template>
  <section class="settings-page">
    <div class="settings-shell">
      <header class="settings-hero">
        <div class="hero-copy">
          <p class="eyebrow">Поставки</p>
          <h1>Прилагоди ја ЕкоСреќа според тебе</h1>
          <p>Избери почетна страница, густина на приказ, потсетници и еко цел што сакаш да ја следиш.</p>
        </div>

        <div class="hero-card">
          <span>Најавен корисник</span>
          <strong>{{ displayName }}</strong>
          <RouterLink class="btn hero-button" to="/profile">Уреди профил</RouterLink>
        </div>
      </header>

      <div v-if="savedMessage" class="alert alert-success settings-alert">{{ savedMessage }}</div>

      <div class="settings-grid">
        <article class="settings-panel preferences-panel">
          <div class="panel-heading">
            <div>
              <h2>Преференции</h2>
              <p>Се зачувуваат на овој уред за побрзо и попријатно користење.</p>
            </div>
          </div>

          <form class="settings-form" @submit.prevent="saveSettings">
            <div class="select-grid">
              <div class="field fancy-field">
                <label for="startPage">Почетна страница</label>
                <select id="startPage" v-model="settings.startPage" class="form-select">
                  <option value="/">Почетна</option>
                  <option value="/news">Вести</option>
                  <option value="/fun">Забава</option>
                  <option value="/quizzes">Квизови</option>
                </select>
              </div>

              <div class="field fancy-field">
                <label for="contentDensity">Густина на содржина</label>
                <select id="contentDensity" v-model="settings.contentDensity" class="form-select">
                  <option value="comfortable">Удобно</option>
                  <option value="compact">Компактно</option>
                </select>
              </div>

              <div class="field fancy-field wide">
                <label for="ecoGoal">Еко цел</label>
                <select id="ecoGoal" v-model="settings.ecoGoal" class="form-select">
                  <option value="learn">Научи повеќе секоја недела</option>
                  <option value="reduce">Намали домашен отпад</option>
                  <option value="energy">Заштеди енергија</option>
                  <option value="nature">Поминувај повеќе време во природа</option>
                </select>
              </div>
            </div>

            <div class="toggle-list">
              <label class="toggle-row">
                <span>
                  <strong>Неделен еко преглед</strong>
                  <small>Прикажува потсетници за најнови вести и идеи за активности.</small>
                </span>
                <input v-model="settings.weeklyDigest" class="switch-input" type="checkbox" />
                <span class="switch" aria-hidden="true"></span>
              </label>

              <label class="toggle-row">
                <span>
                  <strong>Потсетници за квизови</strong>
                  <small>Ги задржува предлозите за квизови видливи во апликацијата.</small>
                </span>
                <input v-model="settings.quizReminders" class="switch-input" type="checkbox" />
                <span class="switch" aria-hidden="true"></span>
              </label>

              <label class="toggle-row">
                <span>
                  <strong>Намалени анимации</strong>
                  <small>Користи посмирени ефекти каде што е поддржано.</small>
                </span>
                <input v-model="settings.reducedMotion" class="switch-input" type="checkbox" />
                <span class="switch" aria-hidden="true"></span>
              </label>
            </div>

            <div class="actions">
              <button class="btn eco-btn" type="submit">Зачувај поставки</button>
              <button class="btn btn-outline-secondary" type="button" @click="resetSettings">
                Ресетирај
              </button>
            </div>
          </form>
        </article>

        <aside class="settings-stack">
          <article class="settings-panel account-card">
            <div class="account-avatar" aria-hidden="true">{{ initials }}</div>
            <h2>Профил</h2>
            <div class="account-row">
              <span>Име</span>
              <strong>{{ displayName }}</strong>
            </div>
            <div class="account-row">
              <span>Е-пошта</span>
              <strong>{{ authStore.user?.email || 'Не е достапно' }}</strong>
            </div>
            <RouterLink class="btn btn-outline-success w-100" to="/profile">
              Уреди детали за профил
            </RouterLink>
          </article>

          <article class="settings-panel quick-card">
            <h2>Брзи линкови</h2>
            <div class="quick-links">
              <RouterLink :to="settings.startPage">
                <span>Почетна страница</span>
                <strong>Отвори ја зачуваната страница</strong>
              </RouterLink>
              <RouterLink to="/profile">
                <span>Безбедност</span>
                <strong>Промени лозинка</strong>
              </RouterLink>
              <button type="button" @click="logout">
                <span>Сесија</span>
                <strong>Одјави се</strong>
              </button>
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

  const initials = computed(() =>
    displayName.value
      .split(/\s+/)
      .filter(Boolean)
      .slice(0, 2)
      .map((part) => part[0])
      .join('')
      .toUpperCase()
  );

  function applySettings() {
    document.documentElement.dataset.contentDensity = settings.contentDensity;
    document.documentElement.dataset.reducedMotion = settings.reducedMotion ? 'true' : 'false';
  }

  function loadSettings() {
    const saved = localStorage.getItem(STORAGE_KEY);
    if (saved) Object.assign(settings, defaults, JSON.parse(saved));
    applySettings();
  }

  function saveSettings() {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(settings));
    applySettings();
    savedMessage.value = 'Поставките се успешно зачувани.';
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
    align-items: end;
    background:
      linear-gradient(135deg, rgba(27, 77, 43, 0.94), rgba(46, 125, 50, 0.82)),
      url('../img/guide.svg') right 2rem center / 180px no-repeat;
    border-radius: 8px;
    color: #fff;
    display: grid;
    gap: 1.5rem;
    grid-template-columns: minmax(0, 1fr) 280px;
    margin-bottom: 1.5rem;
    min-height: 260px;
    padding: clamp(1.5rem, 4vw, 2.5rem);
  }

  .hero-copy {
    max-width: 680px;
  }

  .eyebrow {
    font-size: 0.78rem;
    font-weight: 900;
    letter-spacing: 0.08em;
    margin: 0 0 0.75rem;
    text-transform: uppercase;
  }

  .settings-hero h1 {
    font-size: clamp(2.2rem, 6vw, 4.6rem);
    font-weight: 900;
    letter-spacing: 0;
    line-height: 0.98;
    margin: 0 0 1rem;
  }

  .settings-hero p {
    font-size: 1rem;
    line-height: 1.7;
    margin: 0;
    max-width: 620px;
    opacity: 0.92;
  }

  .hero-card {
    background: rgba(255, 255, 255, 0.16);
    border: 1px solid rgba(255, 255, 255, 0.28);
    border-radius: 8px;
    display: grid;
    gap: 0.75rem;
    padding: 1rem;
  }

  .hero-card span,
  .account-row span,
  .quick-links span {
    color: #506650;
    font-size: 0.76rem;
    font-weight: 900;
    text-transform: uppercase;
  }

  .hero-card span {
    color: rgba(255, 255, 255, 0.76);
  }

  .hero-card strong {
    font-size: 1.2rem;
    line-height: 1.25;
  }

  .hero-button {
    background: #fff;
    border: none;
    color: #24552d;
    font-weight: 900;
  }

  .settings-alert {
    border: 0;
    border-left: 5px solid #2e7d32;
    border-radius: 8px;
    box-shadow: 0 8px 20px rgba(27, 42, 27, 0.08);
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
    box-shadow: 0 10px 30px rgba(27, 42, 27, 0.08);
    padding: 1.5rem;
  }

  .preferences-panel {
    display: grid;
    gap: 1.25rem;
  }

  .panel-heading {
    align-items: start;
    display: flex;
    gap: 1rem;
  }

  .panel-heading h2,
  .settings-panel h2 {
    color: #1b2a1b;
    font-size: 1.35rem;
    font-weight: 900;
    margin: 0 0 0.25rem;
  }

  .panel-heading p {
    color: #506650;
    margin: 0;
  }

  .panel-icon,
  .account-avatar {
    align-items: center;
    background: #e8f5e9;
    border-radius: 8px;
    color: #24552d;
    display: inline-flex;
    font-size: 1.3rem;
    font-weight: 900;
    height: 44px;
    justify-content: center;
    width: 44px;
  }

  .settings-form,
  .settings-stack,
  .quick-links {
    display: grid;
    gap: 1rem;
  }

  .select-grid {
    display: grid;
    gap: 1rem;
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .wide {
    grid-column: 1 / -1;
  }

  .field {
    display: grid;
    gap: 0.45rem;
  }

  .fancy-field {
    background: rgba(102, 187, 106, 0.07);
    border: 1px solid rgba(69, 128, 81, 0.14);
    border-radius: 8px;
    padding: 1rem;
  }

  .field label {
    color: #24552d;
    font-size: 0.82rem;
    font-weight: 900;
    text-transform: uppercase;
  }

  .form-select {
    border: 1px solid #d7e6d8;
    border-radius: 8px;
    min-height: 44px;
  }

  .form-select:focus {
    border-color: #66bb6a;
    box-shadow: 0 0 0 3px rgba(102, 187, 106, 0.2);
  }

  .toggle-list {
    display: grid;
    gap: 0.75rem;
  }

  .toggle-row {
    align-items: center;
    background: #fff;
    border: 1px solid rgba(69, 128, 81, 0.14);
    border-radius: 8px;
    cursor: pointer;
    display: grid;
    gap: 1rem;
    grid-template-columns: minmax(0, 1fr) auto;
    padding: 1rem;
  }

  .toggle-row strong {
    display: block;
    font-weight: 900;
    margin-bottom: 0.25rem;
  }

  .toggle-row small {
    color: #506650;
    line-height: 1.5;
  }

  .switch-input {
    height: 1px;
    opacity: 0;
    position: absolute;
    width: 1px;
  }

  .switch {
    background: #cbd5cb;
    border-radius: 999px;
    display: inline-flex;
    height: 28px;
    padding: 3px;
    transition: background 0.2s ease;
    width: 52px;
  }

  .switch::before {
    background: #fff;
    border-radius: 50%;
    box-shadow: 0 2px 6px rgba(27, 42, 27, 0.25);
    content: '';
    display: block;
    height: 22px;
    transition: transform 0.2s ease;
    width: 22px;
  }

  .switch-input:checked + .switch {
    background: #2e7d32;
  }

  .switch-input:checked + .switch::before {
    transform: translateX(24px);
  }

  .actions {
    display: flex;
    gap: 0.75rem;
    padding-top: 0.25rem;
  }

  .actions .btn {
    min-height: 44px;
    min-width: 130px;
  }

  .account-card {
    display: grid;
    gap: 1rem;
  }

  .account-avatar {
    background: #2e7d32;
    color: #fff;
    font-size: 1rem;
  }

  .account-row {
    border-bottom: 1px solid rgba(69, 128, 81, 0.15);
    display: grid;
    gap: 0.25rem;
    padding-bottom: 0.85rem;
  }

  .account-row strong {
    word-break: break-word;
  }

  .quick-card {
    display: grid;
    gap: 1rem;
  }

  .quick-links a,
  .quick-links button {
    background: rgba(102, 187, 106, 0.1);
    border: 1px solid rgba(69, 128, 81, 0.14);
    border-radius: 8px;
    color: #1b2a1b;
    display: grid;
    gap: 0.2rem;
    padding: 0.85rem 1rem;
    text-align: left;
    text-decoration: none;
    transition:
      background 0.2s ease,
      transform 0.2s ease;
  }

  .quick-links button {
    width: 100%;
  }

  .quick-links a:hover,
  .quick-links button:hover {
    background: rgba(102, 187, 106, 0.18);
    transform: translateY(-1px);
  }

  .quick-links strong {
    font-size: 0.98rem;
  }

  @media (max-width: 991px) {
    .settings-grid,
    .settings-hero,
    .select-grid {
      grid-template-columns: 1fr;
    }

    .settings-hero {
      background:
        linear-gradient(135deg, rgba(27, 77, 43, 0.94), rgba(46, 125, 50, 0.84)),
        url('../img/guide.svg') right 1rem bottom 1rem / 120px no-repeat;
    }

    .actions {
      flex-direction: column;
    }
  }
</style>
