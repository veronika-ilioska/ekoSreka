<template>
  <section class="admin-page">
    <div class="admin-shell">
      <header class="admin-hero">
        <p class="eyebrow">Admin</p>
        <h1>Manage content</h1>
        <p>Add news and Fun section content, or remove outdated items.</p>
      </header>

      <div v-if="pageError" class="alert alert-danger">{{ pageError }}</div>
      <div v-if="successMessage" class="alert alert-success">{{ successMessage }}</div>

      <div class="admin-layout">
        <aside class="admin-tabs">
          <button
            v-for="section in sections"
            :key="section.key"
            type="button"
            :class="{ active: activeSection === section.key }"
            @click="selectSection(section.key)"
          >
            <strong>{{ section.label }}</strong>
            <span>{{ section.description }}</span>
          </button>
        </aside>

        <main class="admin-panel">
          <div class="panel-heading">
            <div>
              <h2>{{ currentSection.label }}</h2>
              <p>{{ currentSection.description }}</p>
            </div>
            <button class="btn btn-outline-success" type="button" @click="loadItems">Refresh</button>
          </div>

          <form v-if="activeSection !== 'quizzes'" class="content-form" @submit.prevent="createItem">
            <div
              v-for="field in currentSection.fields"
              :key="field.name"
              class="field"
              :class="{ wide: field.type === 'textarea' }"
            >
              <label :for="field.name">{{ field.label }}</label>
              <textarea
                v-if="field.type === 'textarea'"
                :id="field.name"
                v-model.trim="form[field.name]"
                class="form-control"
                rows="4"
                :required="field.required"
              />
              <select
                v-else-if="field.type === 'select'"
                :id="field.name"
                v-model="form[field.name]"
                class="form-select"
                :required="field.required"
              >
                <option value="" disabled>Select...</option>
                <option v-for="option in field.options" :key="option" :value="option">
                  {{ option }}
                </option>
              </select>
              <input
                v-else
                :id="field.name"
                v-model.trim="form[field.name]"
                class="form-control"
                :type="field.type"
                :required="field.required"
              />
            </div>

            <div class="form-actions">
              <button class="btn eco-btn" type="submit" :disabled="saving">
                {{ saving ? 'Saving...' : `Add ${currentSection.singular}` }}
              </button>
              <button class="btn btn-outline-secondary" type="button" @click="resetForm">Clear</button>
            </div>
          </form>

          <form v-else class="quiz-builder" @submit.prevent="createItem">
            <div class="content-form quiz-base">
              <div class="field">
                <label for="quizTitle">Title</label>
                <input id="quizTitle" v-model.trim="form.title" class="form-control" required />
              </div>
              <div class="field">
                <label for="quizLevel">Level</label>
                <select id="quizLevel" v-model="form.level" class="form-select" required>
                  <option value="" disabled>Select...</option>
                  <option value="BEGINNER">BEGINNER</option>
                  <option value="INTERMEDIATE">INTERMEDIATE</option>
                  <option value="ADVANCED">ADVANCED</option>
                  <option value="FUN">FUN</option>
                </select>
              </div>
              <div class="field">
                <label for="quizTime">Time minutes</label>
                <input id="quizTime" v-model.number="form.timeMinutes" class="form-control" min="1" type="number" required />
              </div>
              <div class="field wide">
                <label for="quizDescription">Description</label>
                <textarea id="quizDescription" v-model.trim="form.description" class="form-control" rows="3" required />
              </div>
            </div>

            <div class="builder-heading">
              <h3>Questions</h3>
              <button class="btn btn-outline-success" type="button" @click="addQuestion">Add question</button>
            </div>

            <article v-for="(question, qIndex) in quizQuestions" :key="question.uid" class="question-editor">
              <div class="question-editor-top">
                <strong>Question {{ qIndex + 1 }}</strong>
                <button
                  class="btn btn-sm btn-outline-danger"
                  type="button"
                  :disabled="quizQuestions.length === 1"
                  @click="removeQuestion(qIndex)"
                >
                  Remove
                </button>
              </div>
              <div class="field">
                <label :for="`question-${question.uid}`">Question text</label>
                <textarea :id="`question-${question.uid}`" v-model.trim="question.text" class="form-control" rows="2" required />
              </div>

              <div class="option-grid">
                <label v-for="(option, oIndex) in question.options" :key="option.uid" class="option-editor">
                  <span>Option {{ oIndex + 1 }}</span>
                  <input v-model.trim="option.text" class="form-control" required />
                  <span class="correct-toggle">
                    <input
                      type="radio"
                      :name="`correct-${question.uid}`"
                      :checked="option.correct"
                      @change="markCorrect(question, oIndex)"
                    />
                    Correct
                  </span>
                </label>
              </div>
            </article>

            <div class="form-actions">
              <button class="btn eco-btn" type="submit" :disabled="saving">
                {{ saving ? 'Saving...' : 'Add quiz' }}
              </button>
              <button class="btn btn-outline-secondary" type="button" @click="resetForm">Clear</button>
            </div>
          </form>

          <div class="items-list">
            <article v-if="loading" class="empty-state">Loading...</article>
            <article v-else-if="items.length === 0" class="empty-state">No items yet.</article>
            <article v-for="item in items" v-else :key="item.id" class="item-row">
              <div>
                <h3>{{ item.title || `${currentSection.singular} #${item.id}` }}</h3>
                <p>{{ itemSubtitle(item) }}</p>
              </div>
              <button class="btn btn-outline-danger" type="button" @click="deleteItem(item)">
                Delete
              </button>
            </article>
          </div>
        </main>
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

  const zodiacSigns = [
    'ARIES',
    'TAURUS',
    'GEMINI',
    'CANCER',
    'LEO',
    'VIRGO',
    'LIBRA',
    'SCORPIO',
    'SAGITTARIUS',
    'CAPRICORN',
    'AQUARIUS',
    'PISCES',
  ];

  const sections = [
    {
      key: 'news',
      label: 'News',
      singular: 'news article',
      description: 'Publish articles for the news page.',
      endpoint: '/admin/news',
      fields: [
        { name: 'title', label: 'Title', type: 'text', required: true },
        { name: 'category', label: 'Category', type: 'text', required: true },
        { name: 'coverUrl', label: 'Cover image URL', type: 'url' },
        { name: 'content', label: 'Full news content', type: 'textarea', required: true },
      ],
    },
    {
      key: 'quizzes',
      label: 'Quizzes',
      singular: 'quiz',
      description: 'Create quizzes with questions and correct answers.',
      endpoint: '/admin/quizzes',
      fields: [],
    },
    {
      key: 'photos',
      label: 'Photos',
      singular: 'photo',
      description: 'Add image entries for the photos section.',
      endpoint: '/admin/photos',
      fields: [
        { name: 'title', label: 'Title', type: 'text', required: true },
        { name: 'url', label: 'Image URL', type: 'url', required: true },
        { name: 'thumbnailUrl', label: 'Thumbnail URL', type: 'url' },
        { name: 'tags', label: 'Tags', type: 'text' },
        { name: 'description', label: 'Description', type: 'textarea' },
      ],
    },
    {
      key: 'videos',
      label: 'Videos',
      singular: 'video',
      description: 'Add video records and references.',
      endpoint: '/admin/videos',
      fields: [
        { name: 'title', label: 'Title', type: 'text', required: true },
        { name: 'source', label: 'Source', type: 'text', required: true },
        { name: 'ref', label: 'Video reference or URL', type: 'text', required: true },
        { name: 'durationSec', label: 'Duration seconds', type: 'number' },
        { name: 'description', label: 'Description', type: 'textarea' },
      ],
    },
    {
      key: 'games',
      label: 'Games',
      singular: 'game',
      description: 'Create mini games and home challenges.',
      endpoint: '/admin/games',
      fields: [
        { name: 'title', label: 'Title', type: 'text', required: true },
        {
          name: 'difficulty',
          label: 'Difficulty',
          type: 'select',
          options: ['EASY', 'MEDIUM', 'HARD'],
          required: true,
        },
        { name: 'thumbnailUrl', label: 'Thumbnail URL', type: 'url' },
        { name: 'description', label: 'Description', type: 'textarea', required: true },
        { name: 'rules', label: 'Rules', type: 'textarea' },
      ],
    },
    {
      key: 'horoscope',
      label: 'Horoscope',
      singular: 'horoscope entry',
      description: 'Add eco horoscope entries by sign and date.',
      endpoint: '/admin/horoscope',
      fields: [
        { name: 'sign', label: 'Sign', type: 'select', options: zodiacSigns, required: true },
        {
          name: 'periodType',
          label: 'Period',
          type: 'select',
          options: ['DAILY', 'WEEKLY', 'MONTHLY'],
          required: true,
        },
        { name: 'periodDate', label: 'Date', type: 'date', required: true },
        { name: 'title', label: 'Title', type: 'text', required: true },
        { name: 'content', label: 'Content', type: 'textarea', required: true },
        { name: 'ecoTip', label: 'Eco tip', type: 'textarea' },
      ],
    },
  ];

  const activeSection = ref('news');
  const items = ref([]);
  const loading = ref(false);
  const saving = ref(false);
  const pageError = ref('');
  const successMessage = ref('');
  const form = reactive({});
  const quizQuestions = ref([]);

  const currentSection = computed(() => sections.find((section) => section.key === activeSection.value));

  const adminHeaders = computed(() => ({
    'X-User-Id': authStore.userId,
  }));

  function resetForm() {
    Object.keys(form).forEach((key) => delete form[key]);
    if (activeSection.value === 'quizzes') {
      form.title = '';
      form.level = '';
      form.timeMinutes = 5;
      form.description = '';
      quizQuestions.value = [newQuestion()];
      return;
    }

    currentSection.value.fields.forEach((field) => {
      form[field.name] = '';
    });
  }

  function selectSection(key) {
    activeSection.value = key;
    pageError.value = '';
    successMessage.value = '';
    resetForm();
    loadItems();
  }

  function cleanPayload() {
    if (activeSection.value === 'quizzes') {
      return {
        title: form.title,
        level: form.level,
        timeMinutes: Number(form.timeMinutes || 5),
        description: form.description,
        questions: quizQuestions.value.map((question) => ({
          text: question.text,
          options: question.options.map((option) => ({
            text: option.text,
            correct: option.correct,
          })),
        })),
      };
    }

    return currentSection.value.fields.reduce((payload, field) => {
      const value = form[field.name];
      if (value === '' || value === null || value === undefined) return payload;
      payload[field.name] = field.type === 'number' ? Number(value) : value;
      return payload;
    }, {});
  }

  function itemSubtitle(item) {
    if (activeSection.value === 'news') return `${item.category || 'Uncategorized'} - ${formatDate(item.createdAt)}`;
    if (activeSection.value === 'quizzes') return `${item.level || 'No level'} - ${item.timeMinutes || 5} min`;
    if (activeSection.value === 'photos') return item.tags || item.description || item.url || '';
    if (activeSection.value === 'videos') return `${item.source || 'Video'} - ${item.ref || ''}`;
    if (activeSection.value === 'games') return `${item.difficulty || 'No difficulty'} - ${item.description || ''}`;
    return `${item.sign || ''} - ${item.periodType || ''} - ${item.periodDate || ''}`;
  }

  function formatDate(value) {
    if (!value) return 'No date';
    return new Date(value).toLocaleDateString();
  }

  function newQuestion() {
    return {
      uid: crypto.randomUUID(),
      text: '',
      options: Array.from({ length: 4 }, (_, index) => ({
        uid: crypto.randomUUID(),
        text: '',
        correct: index === 0,
      })),
    };
  }

  function addQuestion() {
    quizQuestions.value.push(newQuestion());
  }

  function removeQuestion(index) {
    if (quizQuestions.value.length === 1) return;
    quizQuestions.value.splice(index, 1);
  }

  function markCorrect(question, optionIndex) {
    question.options.forEach((option, index) => {
      option.correct = index === optionIndex;
    });
  }

  async function loadItems() {
    loading.value = true;
    pageError.value = '';
    try {
      const { data } = await api.get(currentSection.value.endpoint, {
        params: { size: 20 },
        headers: adminHeaders.value,
      });
      items.value = data?.content ?? [];
    } catch (error) {
      pageError.value = error.response?.data?.message || 'Could not load admin content.';
      items.value = [];
    } finally {
      loading.value = false;
    }
  }

  async function createItem() {
    saving.value = true;
    pageError.value = '';
    successMessage.value = '';
    try {
      await api.post(currentSection.value.endpoint, cleanPayload(), {
        headers: adminHeaders.value,
      });
      successMessage.value = `${currentSection.value.singular} added successfully.`;
      resetForm();
      await loadItems();
    } catch (error) {
      pageError.value = error.response?.data?.message || `Could not add ${currentSection.value.singular}.`;
    } finally {
      saving.value = false;
    }
  }

  async function deleteItem(item) {
    if (!confirm(`Delete "${item.title || currentSection.value.singular}"?`)) return;
    pageError.value = '';
    successMessage.value = '';
    try {
      await api.delete(`${currentSection.value.endpoint}/${item.id}`, {
        headers: adminHeaders.value,
      });
      successMessage.value = `${currentSection.value.singular} deleted.`;
      await loadItems();
    } catch (error) {
      pageError.value = error.response?.data?.message || `Could not delete ${currentSection.value.singular}.`;
    }
  }

  onMounted(() => {
    if (!authStore.isAuthenticated) {
      router.push('/login');
      return;
    }

    resetForm();
    loadItems();
  });
</script>

<style scoped>
  .admin-page {
    color: var(--eco-text-dark);
    padding: 1rem 0 4rem;
  }

  .admin-shell {
    margin: 0 auto;
    max-width: 1200px;
  }

  .admin-hero {
    background: linear-gradient(135deg, #244d2b 0%, #458051 100%);
    border-radius: 8px;
    color: #fff;
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

  .admin-hero h1 {
    font-size: clamp(2rem, 5vw, 3.4rem);
    font-weight: 800;
    letter-spacing: 0;
    line-height: 1.05;
    margin: 0 0 0.75rem;
  }

  .admin-hero p {
    margin: 0;
    opacity: 0.92;
  }

  .admin-layout {
    display: grid;
    gap: 1.25rem;
    grid-template-columns: 280px minmax(0, 1fr);
  }

  .admin-tabs {
    align-content: start;
    display: grid;
    gap: 0.65rem;
  }

  .admin-tabs button,
  .admin-panel {
    background: var(--eco-card-bg);
    border: 1px solid rgba(69, 128, 81, 0.15);
    border-radius: 8px;
    box-shadow: 0 8px 24px rgba(27, 42, 27, 0.08);
  }

  .admin-tabs button {
    color: #1b2a1b;
    display: grid;
    gap: 0.25rem;
    padding: 1rem;
    text-align: left;
  }

  .admin-tabs button.active {
    background: #458051;
    color: #fff;
  }

  .admin-tabs span {
    color: #506650;
    font-size: 0.88rem;
  }

  .admin-tabs button.active span {
    color: rgba(255, 255, 255, 0.86);
  }

  .admin-panel {
    padding: 1.5rem;
  }

  .panel-heading {
    align-items: start;
    display: flex;
    gap: 1rem;
    justify-content: space-between;
    margin-bottom: 1.25rem;
  }

  .panel-heading h2 {
    color: #1f3b1f;
    font-size: 1.45rem;
    font-weight: 800;
    letter-spacing: 0;
    margin: 0 0 0.25rem;
  }

  .panel-heading p {
    color: #506650;
    margin: 0;
  }

  .content-form {
    border-bottom: 1px solid rgba(69, 128, 81, 0.15);
    display: grid;
    gap: 1rem;
    grid-template-columns: repeat(2, minmax(0, 1fr));
    margin-bottom: 1.25rem;
    padding-bottom: 1.25rem;
  }

  .quiz-builder {
    border-bottom: 1px solid rgba(69, 128, 81, 0.15);
    display: grid;
    gap: 1rem;
    margin-bottom: 1.25rem;
    padding-bottom: 1.25rem;
  }

  .quiz-base {
    border-bottom: none;
    margin-bottom: 0;
    padding-bottom: 0;
  }

  .builder-heading,
  .question-editor-top {
    align-items: center;
    display: flex;
    gap: 1rem;
    justify-content: space-between;
  }

  .builder-heading h3 {
    color: #1f3b1f;
    font-size: 1.1rem;
    font-weight: 800;
    letter-spacing: 0;
    margin: 0;
  }

  .question-editor {
    background: rgba(102, 187, 106, 0.08);
    border: 1px solid rgba(69, 128, 81, 0.14);
    border-radius: 8px;
    display: grid;
    gap: 0.9rem;
    padding: 1rem;
  }

  .question-editor-top strong {
    color: #1b2a1b;
  }

  .option-grid {
    display: grid;
    gap: 0.75rem;
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .option-editor {
    background: #fff;
    border: 1px solid rgba(69, 128, 81, 0.12);
    border-radius: 8px;
    display: grid;
    gap: 0.45rem;
    padding: 0.8rem;
  }

  .option-editor > span:first-child,
  .correct-toggle {
    color: #506650;
    font-size: 0.82rem;
    font-weight: 800;
  }

  .correct-toggle {
    align-items: center;
    display: inline-flex;
    gap: 0.4rem;
  }

  .correct-toggle input {
    accent-color: #458051;
  }

  .field {
    display: grid;
    gap: 0.45rem;
  }

  .field.wide,
  .form-actions {
    grid-column: 1 / -1;
  }

  .field label {
    color: #506650;
    font-size: 0.88rem;
    font-weight: 800;
  }

  .form-control,
  .form-select {
    border: 1px solid rgba(69, 128, 81, 0.28);
    border-radius: 8px;
  }

  .form-actions {
    display: flex;
    gap: 0.75rem;
  }

  .items-list {
    display: grid;
    gap: 0.75rem;
  }

  .item-row,
  .empty-state {
    align-items: center;
    background: rgba(102, 187, 106, 0.08);
    border: 1px solid rgba(69, 128, 81, 0.12);
    border-radius: 8px;
    display: flex;
    gap: 1rem;
    justify-content: space-between;
    padding: 1rem;
  }

  .item-row h3 {
    color: #1b2a1b;
    font-size: 1rem;
    font-weight: 800;
    margin: 0 0 0.25rem;
  }

  .item-row p {
    color: #506650;
    margin: 0;
    overflow-wrap: anywhere;
  }

  .alert {
    border-radius: 8px;
  }

  @media (max-width: 991px) {
    .admin-layout {
      grid-template-columns: 1fr;
    }

    .admin-tabs {
      grid-template-columns: repeat(2, minmax(0, 1fr));
    }
  }

  @media (max-width: 640px) {
    .admin-hero,
    .admin-panel {
      padding: 1.25rem;
    }

    .admin-tabs,
    .content-form,
    .option-grid {
      grid-template-columns: 1fr;
    }

    .panel-heading,
    .item-row,
    .form-actions,
    .builder-heading,
    .question-editor-top {
      align-items: stretch;
      flex-direction: column;
    }
  }
</style>
