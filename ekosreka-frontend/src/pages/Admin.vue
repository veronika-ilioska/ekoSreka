<template>
  <section class="admin-page">
    <div class="admin-shell">
      <header class="admin-hero">
        <p class="eyebrow">Админ</p>
        <h1>Управување со содржина</h1>
        <p>Додавај вести и содржина за забавната секција или бриши застарени објави.</p>
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
            <button class="btn btn-outline-success" type="button" @click="loadItems">Освежи</button>
          </div>

          <form v-if="activeSection !== 'quizzes'" class="content-form" @submit.prevent="createItem">
            <div v-for="field in currentSection.fields" :key="field.name" class="field" :class="{ wide: field.type === 'textarea' }">
              <label :for="field.name">{{ field.label }}</label>
              <textarea
                v-if="field.type === 'textarea'"
                :id="field.name"
                v-model.trim="form[field.name]"
                class="form-control"
                rows="4"
                :required="field.required"
              />
              <select v-else-if="field.type === 'select'" :id="field.name" v-model="form[field.name]" class="form-select" :required="field.required">
                <option value="" disabled>Избери...</option>
                <option v-for="option in field.options" :key="option.value || option" :value="option.value || option">
                  {{ option.label || option }}
                </option>
              </select>
              <input v-else :id="field.name" v-model.trim="form[field.name]" class="form-control" :type="field.type" :required="field.required" />
            </div>

            <div class="form-actions">
              <button class="btn eco-btn" type="submit" :disabled="saving">
                {{ saving ? 'Се зачувува...' : `Додај ${currentSection.singular}` }}
              </button>
              <button class="btn btn-outline-secondary" type="button" @click="resetForm">Исчисти</button>
            </div>
          </form>

          <form v-else class="quiz-builder" @submit.prevent="createItem">
            <div class="content-form quiz-base">
              <div class="field">
                <label for="quizTitle">Наслов</label>
                <input id="quizTitle" v-model.trim="form.title" class="form-control" required />
              </div>
              <div class="field">
                <label for="quizLevel">Ниво</label>
                <select id="quizLevel" v-model="form.level" class="form-select" required>
                  <option value="" disabled>Избери...</option>
                  <option value="BEGINNER">Почетно</option>
                  <option value="INTERMEDIATE">Средно</option>
                  <option value="ADVANCED">Напредно</option>
                  <option value="FUN">Забавно</option>
                </select>
              </div>
              <div class="field">
                <label for="quizTime">Време во минути</label>
                <input id="quizTime" v-model.number="form.timeMinutes" class="form-control" min="1" type="number" required />
              </div>
              <div class="field wide">
                <label for="quizDescription">Опис</label>
                <textarea id="quizDescription" v-model.trim="form.description" class="form-control" rows="3" required />
              </div>
            </div>

            <section class="import-panel">
              <div class="import-heading">
                <div>
                  <h3>Увези прашања</h3>
                  <p>Залепи JSON, CSV, TSV или текст блокови.</p>
                </div>
                <label class="btn btn-outline-success import-file-button">
                  Избери датотека
                  <input type="file" accept=".csv,.json,.txt,.tsv" @change="handleImportFile" />
                </label>
              </div>
              <textarea
                v-model="importText"
                class="form-control import-textarea"
                rows="8"
                placeholder="Прашање | Точен одговор | Погрешен одговор | Погрешен одговор&#10;&#10;Текст на прашање&#10;* Точен одговор&#10;Погрешен одговор&#10;Погрешен одговор"
              />
              <div v-if="importError" class="alert alert-danger">{{ importError }}</div>
              <div class="form-actions import-actions">
                <button class="btn btn-outline-success" type="button" @click="importQuestions">
                  Увези во квизот
                </button>
                <button class="btn btn-outline-secondary" type="button" @click="clearImport">
                  Исчисти увоз
                </button>
              </div>
            </section>

            <div class="builder-heading">
              <h3>Прашања</h3>
              <button class="btn btn-outline-success" type="button" @click="addQuestion">Додај прашање</button>
            </div>

            <article v-for="(question, qIndex) in quizQuestions" :key="question.uid" class="question-editor">
              <div class="question-editor-top">
                <strong>Прашање {{ qIndex + 1 }}</strong>
                <button class="btn btn-sm btn-outline-danger" type="button" :disabled="quizQuestions.length === 1" @click="removeQuestion(qIndex)">
                  Отстрани
                </button>
              </div>
              <div class="field">
                <label :for="`question-${question.uid}`">Текст на прашањето</label>
                <textarea :id="`question-${question.uid}`" v-model.trim="question.text" class="form-control" rows="2" required />
              </div>
              <div class="option-grid">
                <label v-for="(option, oIndex) in question.options" :key="option.uid" class="option-editor">
                  <span>Опција {{ oIndex + 1 }}</span>
                  <input v-model.trim="option.text" class="form-control" required />
                  <span class="correct-toggle">
                    <input type="radio" :name="`correct-${question.uid}`" :checked="option.correct" @change="markCorrect(question, oIndex)" />
                    Точно
                  </span>
                </label>
              </div>
            </article>

            <div class="form-actions">
              <button class="btn eco-btn" type="submit" :disabled="saving">
                {{ saving ? 'Се зачувува...' : 'Додај квиз' }}
              </button>
              <button class="btn btn-outline-secondary" type="button" @click="resetForm">Исчисти</button>
            </div>
          </form>

          <div class="items-list">
            <article v-if="loading" class="empty-state">Се вчитува...</article>
            <article v-else-if="items.length === 0" class="empty-state">Сè уште нема ставки.</article>
            <article v-for="item in items" v-else :key="item.id" class="item-row">
              <div>
                <h3>{{ item.title || `${currentSection.singular} #${item.id}` }}</h3>
                <p>{{ itemSubtitle(item) }}</p>
              </div>
              <button class="btn btn-outline-danger" type="button" @click="deleteItem(item)">Избриши</button>
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
  import  api  from '../api';
  import { useAuthStore } from '../stores/authStore';

  const router = useRouter();
  const authStore = useAuthStore();
  const zodiacSigns = ['ARIES', 'TAURUS', 'GEMINI', 'CANCER', 'LEO', 'VIRGO', 'LIBRA', 'SCORPIO', 'SAGITTARIUS', 'CAPRICORN', 'AQUARIUS', 'PISCES'];
  const difficultyOptions = [
    { value: 'EASY', label: 'Лесно' },
    { value: 'MEDIUM', label: 'Средно' },
    { value: 'HARD', label: 'Тешко' },
  ];
  const periodOptions = [
    { value: 'DAILY', label: 'Дневен' },
    { value: 'WEEKLY', label: 'Неделен' },
    { value: 'MONTHLY', label: 'Месечен' },
  ];
  const newsCategoryOptions = ['Рециклирање', 'Енергија', 'Природа', 'Клима'];
  const sections = [
    {
      key: 'news',
      label: 'Вести',
      singular: 'вест',
      description: 'Објавувај статии за страницата со вести.',
      endpoint: '/admin/news',
      fields: [
        { name: 'title', label: 'Наслов', type: 'text', required: true },
        { name: 'category', label: 'Категорија', type: 'select', options: newsCategoryOptions, required: true },
        { name: 'coverUrl', label: 'URL за насловна слика', type: 'url' },
        { name: 'content', label: 'Целосна содржина на веста', type: 'textarea', required: true },
      ],
    },
    { key: 'quizzes', label: 'Квизови', singular: 'квиз', description: 'Креирај квизови со прашања и точни одговори.', endpoint: '/admin/quizzes', fields: [] },
    {
      key: 'photos',
      label: 'Фотографии',
      singular: 'фотографија',
      description: 'Додавај фотографии во секцијата за фотографии.',
      endpoint: '/admin/photos',
      fields: [
        { name: 'title', label: 'Наслов', type: 'text', required: true },
        { name: 'url', label: 'URL за слика', type: 'url', required: true },
        { name: 'thumbnailUrl', label: 'URL за мала слика', type: 'url' },
        { name: 'tags', label: 'Ознаки', type: 'text' },
        { name: 'description', label: 'Опис', type: 'textarea' },
      ],
    },
    {
      key: 'videos',
      label: 'Видеа',
      singular: 'видео',
      description: 'Додавај видеа и видео референци.',
      endpoint: '/admin/videos',
      fields: [
        { name: 'title', label: 'Наслов', type: 'text', required: true },
        { name: 'source', label: 'Извор', type: 'text', required: true },
        { name: 'icon', label: 'Икона', type: 'text' },
        { name: 'ref', label: 'Видео референца или URL', type: 'text', required: true },
        { name: 'durationSec', label: 'Времетраење во секунди', type: 'number' },
        { name: 'description', label: 'Опис', type: 'textarea' },
      ],
    },
    {
      key: 'games',
      label: 'Игри',
      singular: 'игра',
      description: 'Креирај мини игри и домашни предизвици.',
      endpoint: '/admin/games',
      fields: [
        { name: 'title', label: 'Наслов', type: 'text', required: true },
        { name: 'difficulty', label: 'Тежина', type: 'select', options: difficultyOptions, required: true },
        { name: 'thumbnailUrl', label: 'URL за мала слика', type: 'url' },
        { name: 'description', label: 'Опис', type: 'textarea', required: true },
        { name: 'rules', label: 'Правила', type: 'textarea' },
      ],
    },
    {
      key: 'horoscope',
      label: 'Хороскоп',
      singular: 'хороскопски запис',
      description: 'Додавај еко хороскоп според знак и датум.',
      endpoint: '/admin/horoscope',
      fields: [
        { name: 'sign', label: 'Знак', type: 'select', options: zodiacSigns, required: true },
        { name: 'periodType', label: 'Период', type: 'select', options: periodOptions, required: true },
        { name: 'periodDate', label: 'Датум', type: 'date', required: true },
        { name: 'title', label: 'Наслов', type: 'text', required: true },
        { name: 'content', label: 'Содржина', type: 'textarea', required: true },
        { name: 'ecoTip', label: 'Еко совет', type: 'textarea' },
      ],
    },
  ];
  const activeSection = ref('news');
  const items = ref([]);
  const loading = ref(false);
  const saving = ref(false);
  const pageError = ref('');
  const successMessage = ref('');
  const importText = ref('');
  const importError = ref('');
  const form = reactive({});
  const quizQuestions = ref([]);
  const currentSection = computed(() => sections.find((section) => section.key === activeSection.value));
  const adminHeaders = computed(() => ({ 'X-User-Id': authStore.userId }));

  function resetForm() {
    Object.keys(form).forEach((key) => delete form[key]);
    if (activeSection.value === 'quizzes') {
      form.title = '';
      form.level = '';
      form.timeMinutes = 5;
      form.description = '';
      importText.value = '';
      importError.value = '';
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
          options: question.options.map((option) => ({ text: option.text, correct: option.correct })),
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
    if (activeSection.value === 'news') return `${item.category || 'Без категорија'} - ${formatDate(item.createdAt)}`;
    if (activeSection.value === 'quizzes') return `${item.level || 'Без ниво'} - ${item.timeMinutes || 5} мин`;
    if (activeSection.value === 'photos') return item.tags || item.description || item.url || '';
    if (activeSection.value === 'videos') return `${item.source || 'Видео'} - ${item.ref || ''}`;
    if (activeSection.value === 'games') return `${item.difficulty || 'Без тежина'} - ${item.description || ''}`;
    return `${item.sign || ''} - ${item.periodType || ''} - ${item.periodDate || ''}`;
  }
  function formatDate(value) {
    if (!value) return 'Без датум';
    return new Date(value).toLocaleDateString('mk-MK');
  }
  function newQuestion() {
    return {
      uid: crypto.randomUUID(),
      text: '',
      options: Array.from({ length: 4 }, (_, index) => ({ uid: crypto.randomUUID(), text: '', correct: index === 0 })),
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
  function clearImport() {
    importText.value = '';
    importError.value = '';
  }
  function handleImportFile(event) {
    const file = event.target.files?.[0];
    if (!file) return;

    const reader = new FileReader();
    reader.onload = () => {
      importText.value = String(reader.result || '');
      importError.value = '';
    };
    reader.onerror = () => {
      importError.value = 'Could not read the selected file.';
    };
    reader.readAsText(file);
    event.target.value = '';
  }
  function importQuestions() {
    importError.value = '';
    try {
      const imported = parseImportedQuestions(importText.value);
      if (imported.length === 0) {
        importError.value = 'No valid questions were found.';
        return;
      }
      quizQuestions.value = imported;
      successMessage.value = `${imported.length} question${imported.length === 1 ? '' : 's'} imported into the quiz builder.`;
    } catch (error) {
      importError.value = error.message || 'Questions could not be imported.';
    }
  }
  function parseImportedQuestions(raw) {
    const text = raw.trim();
    if (!text) return [];

    if (text.startsWith('[') || text.startsWith('{')) {
      return parseJsonQuestions(text);
    }

    const nonEmptyLines = text.split(/\r?\n/).map((line) => line.trim()).filter(Boolean);
    const delimiter = detectDelimiter(nonEmptyLines);
    if (delimiter) {
      return parseDelimitedQuestions(nonEmptyLines, delimiter);
    }

    return parseTextBlockQuestions(text);
  }
  function parseJsonQuestions(text) {
    const parsed = JSON.parse(text);
    const items = Array.isArray(parsed) ? parsed : parsed.questions;
    if (!Array.isArray(items)) {
      throw new Error('JSON must be an array or an object with a questions array.');
    }

    return items.map((item, index) => {
      const questionText = item.text || item.question || item.title;
      const rawOptions = item.options || item.answers;
      if (!questionText || !Array.isArray(rawOptions)) {
        throw new Error(`Question ${index + 1} is missing text or answers.`);
      }

      const options = rawOptions.map((option) => {
        if (typeof option === 'string') {
          return importedOption(option, false);
        }
        return importedOption(option.text || option.answer || option.label, Boolean(option.correct || option.isCorrect));
      });

      if (!options.some((option) => option.correct) && item.correctAnswer) {
        const correctIndex = options.findIndex((option) => option.text === item.correctAnswer);
        if (correctIndex >= 0) options[correctIndex].correct = true;
      }

      return normalizeImportedQuestion(questionText, options, index);
    });
  }
  function parseDelimitedQuestions(lines, delimiter) {
    const rows = lines.map((line) => splitDelimitedLine(line, delimiter)).filter((row) => row.length >= 3);
    if (rows.length && /^question$/i.test(rows[0][0].trim())) {
      rows.shift();
    }

    return rows.map((row, index) => {
      const [questionText, correctAnswer, ...wrongAnswers] = row.map((cell) => cell.trim()).filter(Boolean);
      const options = [
        importedOption(correctAnswer, true),
        ...wrongAnswers.map((answer) => importedOption(answer, false)),
      ];
      return normalizeImportedQuestion(questionText, options, index);
    });
  }
  function parseTextBlockQuestions(text) {
    return text.split(/\n\s*\n/).map((block, index) => {
      const lines = block.split(/\r?\n/).map((line) => line.trim()).filter(Boolean);
      const [questionText, ...answerLines] = lines;
      const options = answerLines.map((line) => {
        const marked = isCorrectAnswerLine(line);
        return importedOption(cleanAnswerLine(line), marked);
      });
      if (!options.some((option) => option.correct) && options.length) {
        options[0].correct = true;
      }
      return normalizeImportedQuestion(questionText, options, index);
    });
  }
  function detectDelimiter(lines) {
    const sample = lines.slice(0, 3);
    if (sample.some((line) => splitDelimitedLine(line, '|').length >= 3)) return '|';
    if (sample.some((line) => splitDelimitedLine(line, '\t').length >= 3)) return '\t';
    if (sample.some((line) => splitDelimitedLine(line, ',').length >= 3)) return ',';
    return '';
  }
  function splitDelimitedLine(line, delimiter) {
    if (delimiter === '\t') return line.split('\t');

    const values = [];
    let current = '';
    let quoted = false;
    for (const char of line) {
      if (char === '"') {
        quoted = !quoted;
      } else if (char === delimiter && !quoted) {
        values.push(current);
        current = '';
      } else {
        current += char;
      }
    }
    values.push(current);
    return values;
  }
  function isCorrectAnswerLine(line) {
    return /^(\*|\+|\[x\]|\(x\)|correct:)/i.test(line) || /\(correct\)$/i.test(line);
  }
  function cleanAnswerLine(line) {
    return line
      .replace(/^(\*|\+|\[x\]|\(x\)|correct:)\s*/i, '')
      .replace(/\s*\(correct\)$/i, '')
      .trim();
  }
  function importedOption(text, correct) {
    return {
      uid: crypto.randomUUID(),
      text: String(text || '').trim(),
      correct,
    };
  }
  function normalizeImportedQuestion(questionText, options, index) {
    const cleanOptions = options.filter((option) => option.text);
    if (!questionText || cleanOptions.length < 2) {
      throw new Error(`Question ${index + 1} needs text and at least two answers.`);
    }
    if (!cleanOptions.some((option) => option.correct)) {
      cleanOptions[0].correct = true;
    }
    const firstCorrect = cleanOptions.findIndex((option) => option.correct);
    cleanOptions.forEach((option, optionIndex) => {
      option.correct = optionIndex === firstCorrect;
    });

    return {
      uid: crypto.randomUUID(),
      text: String(questionText).trim(),
      options: cleanOptions,
    };
  }
  async function loadItems() {
    loading.value = true;
    pageError.value = '';
    try {
      const { data } = await api.get(currentSection.value.endpoint, { params: { size: 20 }, headers: adminHeaders.value });
      items.value = data?.content ?? [];
    } catch (error) {
      pageError.value = error.response?.data?.message || 'Админ содржината не може да се вчита.';
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
      await api.post(currentSection.value.endpoint, cleanPayload(), { headers: adminHeaders.value });
      successMessage.value = `${currentSection.value.singular} е успешно додадено.`;
      resetForm();
      await loadItems();
    } catch (error) {
      pageError.value = error.response?.data?.message || `Не може да се додаде ${currentSection.value.singular}.`;
    } finally {
      saving.value = false;
    }
  }
  async function deleteItem(item) {
    if (!confirm(`Избриши "${item.title || currentSection.value.singular}"?`)) return;
    pageError.value = '';
    successMessage.value = '';
    try {
      await api.delete(`${currentSection.value.endpoint}/${item.id}`, { headers: adminHeaders.value });
      successMessage.value = `${currentSection.value.singular} е избришано.`;
      await loadItems();
    } catch (error) {
      pageError.value = error.response?.data?.message || `Не може да се избрише ${currentSection.value.singular}.`;
    }
  }
  onMounted(() => {
    if (!authStore.isAuthenticated || !authStore.isAdmin) {
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
    background:
      linear-gradient(135deg, rgba(27, 77, 43, 0.94), rgba(46, 125, 50, 0.82)),
      url('../img/guide.svg') right 2rem center / 180px no-repeat;
    border-radius: 8px;
    color: #fff;
    margin-bottom: 1.5rem;
    min-height: 260px;
    padding: clamp(1.5rem, 4vw, 2.5rem);
  }
  .admin-hero .eyebrow {
    font-size: 0.78rem;
    font-weight: 900;
    letter-spacing: 0.08em;
    margin: 0 0 0.75rem;
    text-transform: uppercase;
  }
  .admin-hero h1 {
    font-size: clamp(2.2rem, 6vw, 4.4rem);
    font-weight: 900;
    letter-spacing: 0;
    line-height: 0.98;
    margin: 0 0 1rem;
    max-width: 760px;
  }
  .admin-hero p:last-child {
    font-size: 1rem;
    line-height: 1.7;
    margin: 0;
    max-width: 640px;
    opacity: 0.92;
  }
  .admin-layout {
    align-items: start;
    display: grid;
    gap: 1.25rem;
    grid-template-columns: 220px minmax(0, 1fr);
  }
  .admin-tabs,
  .items-list,
  .quiz-builder {
    display: grid;
    gap: 0.75rem;
  }
  .admin-tabs {
    align-self: start;
    position: sticky;
    top: 5.5rem;
  }
  .admin-tabs button,
  .admin-panel,
  .item-row,
  .empty-state,
  .question-editor {
    background: var(--eco-card-bg);
    border: 1px solid rgba(69, 128, 81, 0.15);
    border-radius: 8px;
    box-shadow: 0 10px 30px rgba(27, 42, 27, 0.08);
    padding: 1rem;
  }
  .admin-tabs button {
    display: grid;
    gap: 0.25rem;
    min-height: 0;
    padding: 0.85rem;
    text-align: left;
    transition:
      background 0.2s ease,
      color 0.2s ease,
      transform 0.2s ease;
  }
  .admin-tabs button.active {
    background: #2e7d32;
    color: #fff;
  }
  .admin-tabs button:hover {
    transform: translateY(-1px);
  }
  .admin-tabs button span {
    color: #506650;
    font-size: 0.8rem;
    line-height: 1.45;
  }
  .admin-tabs button.active span {
    color: rgba(255, 255, 255, 0.82);
  }
  .import-panel {
    background: rgba(102, 187, 106, 0.07);
    border: 1px solid rgba(69, 128, 81, 0.15);
    border-radius: 8px;
    display: grid;
    gap: 0.9rem;
    margin-bottom: 1.25rem;
    padding: 1rem;
  }
  .import-heading {
    align-items: center;
    display: flex;
    gap: 1rem;
    justify-content: space-between;
  }
  .import-heading h3,
  .import-heading p {
    margin: 0;
  }
  .import-heading h3,
  .builder-heading h3,
  .panel-heading h2 {
    color: #1b2a1b;
    font-size: 1.35rem;
    font-weight: 900;
  }
  .import-heading p {
    color: #506650;
  }
  .import-file-button input {
    display: none;
  }
  .import-textarea {
    font-family: Consolas, 'Courier New', monospace;
    min-height: 180px;
  }
  .import-actions {
    margin-top: 0;
  }
  .admin-panel {
    display: grid;
    gap: 1.25rem;
  }
  .panel-heading,
  .form-actions,
  .builder-heading,
  .question-editor-top,
  .item-row {
    display: flex;
    gap: 1rem;
    justify-content: space-between;
  }
  .panel-heading {
    align-items: start;
    border-bottom: 1px solid rgba(69, 128, 81, 0.14);
    padding-bottom: 1rem;
  }
  .panel-heading p {
    color: #506650;
    margin: 0;
  }
  .content-form {
    display: grid;
    gap: 1rem;
    grid-template-columns: repeat(2, minmax(0, 1fr));
    margin-bottom: 1.25rem;
  }
  .field,
  .option-editor {
    background: rgba(102, 187, 106, 0.07);
    border: 1px solid rgba(69, 128, 81, 0.14);
    border-radius: 8px;
    display: grid;
    gap: 0.45rem;
    padding: 1rem;
  }
  .field label,
  .option-editor span:first-child {
    color: #24552d;
    font-size: 0.82rem;
    font-weight: 900;
    text-transform: uppercase;
  }
  .form-control,
  .form-select {
    border: 1px solid #d7e6d8;
    border-radius: 8px;
    min-height: 44px;
  }
  .form-control:focus,
  .form-select:focus {
    border-color: #66bb6a;
    box-shadow: 0 0 0 3px rgba(102, 187, 106, 0.2);
  }
  .field.wide,
  .form-actions {
    grid-column: 1 / -1;
  }
  .option-grid {
    display: grid;
    gap: 0.75rem;
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
  .item-row {
    align-items: center;
  }
  .item-row h3 {
    color: #1b2a1b;
    font-size: 1.05rem;
    font-weight: 900;
    margin: 0 0 0.25rem;
  }
  .item-row p,
  .empty-state {
    color: #506650;
  }
  @media (max-width: 991px) {
    .admin-layout,
    .content-form,
    .option-grid {
      grid-template-columns: 1fr;
    }
    .import-heading {
      align-items: stretch;
      flex-direction: column;
    }
    .admin-hero {
      background:
        linear-gradient(135deg, rgba(27, 77, 43, 0.94), rgba(46, 125, 50, 0.84)),
        url('../img/guide.svg') right 1rem bottom 1rem / 120px no-repeat;
    }
    .panel-heading,
    .form-actions,
    .builder-heading,
    .question-editor-top,
    .item-row {
      align-items: stretch;
      flex-direction: column;
    }
  }
</style>
