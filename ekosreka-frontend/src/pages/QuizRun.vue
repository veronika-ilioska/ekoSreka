<template>
  <section class="quiz-run-page">
    <div class="quiz-run-shell">
      <div v-if="loading" class="state-card">Квизот се вчитува...</div>
      <div v-else-if="loadError" class="alert alert-danger">{{ loadError }}</div>

      <template v-else-if="quiz">
        <header class="run-hero">
          <RouterLink class="back-link" to="/quizzes">Назад кон квизови</RouterLink>
          <h1>{{ quiz.title }}</h1>
          <p>{{ quiz.description }}</p>
          <div class="run-meta">
            <span>{{ levelLabel(quiz.level) }}</span>
            <span>{{ quiz.timeMinutes || 5 }} мин</span>
            <span>{{ totalQs }} прашања</span>
          </div>
          <div v-if="!result" class="progress-track" aria-label="Напредок во квизот">
            <div class="progress-bar" :style="{ width: progressPct + '%' }"></div>
          </div>
        </header>

        <article v-if="!result" class="question-card">
          <div class="question-topline">
            <span>Прашање {{ currentIndex + 1 }} од {{ totalQs }}</span>
          </div>
          <h2>{{ currentQuestion.text }}</h2>
          <div class="options">
            <label
              v-for="option in currentQuestion.options"
              :key="option.id"
              class="option"
              :class="{ selected: answers[currentQuestion.id] === option.id }"
            >
              <input
                type="radio"
                :name="`q_${currentQuestion.id}`"
                :value="option.id"
                :checked="answers[currentQuestion.id] === option.id"
                @change="pick(currentQuestion.id, option.id)"
              />
              <span>{{ option.text }}</span>
            </label>
          </div>
          <div class="question-actions mt-2">
            <button class="btn btn-outline-secondary" type="button" :disabled="currentIndex === 0" @click="previousQuestion">
              Претходно
            </button>
            <button v-if="!isLastQuestion" class="btn eco-btn" type="button" :disabled="!answers[currentQuestion.id]" @click="nextQuestion">
              Следно
            </button>
            <button v-else class="btn eco-btn" type="button" :disabled="answeredCount < totalQs || submitting" @click="submitQuiz">
              {{ submitting ? 'Се испраќа...' : 'Испрати квиз' }}
            </button>
          </div>
        </article>

        <section v-else class="results-panel">
          <div class="score-card">
            <div>
              <span>Твој резултат</span>
              <strong>{{ result.score }}/{{ result.total }}</strong>
              <p>{{ scoreMessage }}</p>
            </div>
            <div class="score-ring" :style="{ '--score': scorePct + '%' }">
              <span>{{ scorePct }}%</span>
            </div>
          </div>

          <div class="review-heading">
            <div>
              <h2>Преглед на одговори</h2>
              <p>Погрешните одговори се означени со црвено, а точниот одговор е прикажан веднаш под нив.</p>
            </div>
            <div class="review-counts">
              <span class="count-pill count-correct">{{ correctCount }} точни</span>
              <span class="count-pill count-wrong">{{ wrongCount }} погрешни</span>
              <span class="count-pill">{{ resultTotal }} прашања</span>
            </div>
          </div>

          <div class="result-list">
            <article v-for="detail in result.details" :key="detail.questionId" class="result-row" :class="detail.correct ? 'correct' : 'incorrect'">
              <div class="result-content">
                <div class="result-topline">
                  <span class="status-badge">{{ detail.correct ? 'Точно' : 'Погрешно' }}</span>
                </div>
                <h3>{{ questionText(detail.questionId) }}</h3>
                <div class="answer-box" :class="detail.correct ? 'answer-correct' : 'answer-wrong'">
                  <span>Твој одговор</span>
                  <strong>{{ optionText(detail.questionId, detail.chosenOptionId) }}</strong>
                </div>
                <div v-if="!detail.correct" class="answer-box answer-correct">
                  <span>Точен одговор</span>
                  <strong>{{ optionText(detail.questionId, detail.correctOptionId) }}</strong>
                </div>
              </div>
            </article>
          </div>
          <div class="results-actions mt-2">
            <button class="btn btn-outline-success" type="button" @click="restartQuiz">Обиди се повторно</button>
            <RouterLink class="btn eco-btn" to="/quizzes">Избери друг квиз</RouterLink>
          </div>
        </section>
      </template>
    </div>
  </section>
</template>

<script setup>
  import { computed, onMounted, ref } from 'vue';
  import { RouterLink, useRoute } from 'vue-router';
  import  api  from '../api';
  import { useAuthStore } from '../stores/authStore';

  const authStore = useAuthStore();
  const route = useRoute();
  const quizId = Number(route.params.id);
  const quiz = ref(null);
  const answers = ref({});
  const result = ref(null);
  const currentIndex = ref(0);
  const loading = ref(false);
  const submitting = ref(false);
  const loadError = ref('');
  const totalQs = computed(() => quiz.value?.questions?.length || 0);
  const answeredCount = computed(() => {
    if (!quiz.value?.questions?.length) return 0;
    return quiz.value.questions.filter((question) => answers.value[question.id] !== undefined && answers.value[question.id] !== null).length;
  });
  const progressPct = computed(() => (totalQs.value ? Math.round((answeredCount.value / totalQs.value) * 100) : 0));
  const currentQuestion = computed(() => quiz.value.questions[currentIndex.value]);
  const isLastQuestion = computed(() => currentIndex.value >= totalQs.value - 1);
  const scoreMessage = computed(() => {
    if (!result.value?.total) return '';
    const pct = result.value.score / result.value.total;
    if (pct >= 0.8) return 'Одлична работа. Твоето еко знаење е силно.';
    if (pct >= 0.5) return 'Добар почеток. Провери ги пропуштените одговори и обиди се повторно.';
    return 'Продолжи. Секој квиз ја прави следната еко одлука полесна.';
  });
  const scorePct = computed(() => {
    if (!result.value?.total) return 0;
    return Math.round((result.value.score / result.value.total) * 100);
  });
  const resultTotal = computed(() => result.value?.total ?? result.value?.details?.length ?? 0);
  const correctCount = computed(() => result.value?.score ?? 0);
  const wrongCount = computed(() => Math.max(0, resultTotal.value - correctCount.value));

  function levelLabel(level) {
    const labels = { BEGINNER: 'Почетно', INTERMEDIATE: 'Средно', ADVANCED: 'Напредно', FUN: 'Забавно' };
    return labels[level] || level;
  }
  function pick(questionId, optionId) {
    answers.value = { ...answers.value, [questionId]: optionId };
  }
  function nextQuestion() {
    if (!isLastQuestion.value) currentIndex.value += 1;
  }
  function previousQuestion() {
    if (currentIndex.value > 0) currentIndex.value -= 1;
  }
  async function submitQuiz() {
    submitting.value = true;
    try {
      const payload = Object.entries(answers.value).map(([qid, oid]) => ({ questionId: Number(qid), optionId: Number(oid) }));
      const headers = authStore.userId ? { 'X-User-Id': authStore.userId } : {};
      const { data } = await api.post(`/quizzes/${quizId}/submit`, payload, { headers });
      result.value = data;
    } finally {
      submitting.value = false;
    }
  }
  function restartQuiz() {
    answers.value = {};
    result.value = null;
    currentIndex.value = 0;
  }
  function optionText(questionId, optionId) {
    const question = quiz.value?.questions?.find((item) => item.id === questionId);
    const option = question?.options?.find((item) => item.id === optionId);
    return option?.text ?? 'Нема одговор';
  }
  function questionText(questionId) {
    return quiz.value?.questions?.find((item) => item.id === questionId)?.text ?? '';
  }
  async function loadQuiz() {
    loading.value = true;
    loadError.value = '';
    try {
      const { data } = await api.get(`/quizzes/${quizId}`);
      quiz.value = data;
    } catch {
      loadError.value = 'Овој квиз не може да се вчита.';
    } finally {
      loading.value = false;
    }
  }
  onMounted(loadQuiz);
</script>

<style scoped>
  .quiz-run-page {
    color: var(--eco-text-dark);
    padding: 1rem 0 4rem;
  }
  .quiz-run-shell {
    margin: 0 auto;
    max-width: 960px;
  }
  .run-hero,
  .question-card,
  .results-panel,
  .state-card {
    background: var(--eco-card-bg);
    border: 1px solid rgba(69, 128, 81, 0.15);
    border-radius: 8px;
    box-shadow: 0 10px 30px rgba(27, 42, 27, 0.08);
    padding: 1.5rem;
  }
  .run-hero {
    background:
      linear-gradient(115deg, rgba(19, 67, 37, 0.96) 0%, rgba(46, 125, 50, 0.9) 50%, rgba(46, 125, 50, 0.38) 100%),
      url('../img/quiz.png') right center / contain no-repeat;
    color: #fff;
    isolation: isolate;
    margin-bottom: 1rem;
    min-height: 250px;
    overflow: hidden;
    padding: clamp(1.5rem, 4vw, 2.5rem);
    position: relative;
  }
  .run-hero::after {
    border: 1px solid rgba(255, 255, 255, 0.22);
    border-radius: 50%;
    color: rgba(255, 255, 255, 0.2);
    content: '?';
    display: grid;
    font-size: 7rem;
    font-weight: 900;
    height: 160px;
    place-items: center;
    position: absolute;
    right: 260px;
    top: -44px;
    width: 160px;
    z-index: -1;
  }
  .run-hero h1 {
    font-size: clamp(2rem, 5vw, 4rem);
    font-weight: 900;
    letter-spacing: 0;
    line-height: 1;
    margin: 0 0 0.75rem;
    max-width: 760px;
  }
  .run-hero p {
    line-height: 1.7;
    margin: 0 0 1rem;
    max-width: 680px;
    opacity: 0.92;
  }
  .back-link {
    background: rgba(255, 255, 255, 0.16);
    border: 1px solid rgba(255, 255, 255, 0.26);
    border-radius: 8px;
    color: #fff;
    display: inline-block;
    font-weight: 800;
    margin-bottom: 1rem;
    padding: 0.45rem 0.75rem;
    text-decoration: none;
  }
  .run-meta,
  .question-actions,
  .results-actions {
    display: flex;
    flex-wrap: wrap;
    gap: 0.75rem;
  }
  .run-meta {
    margin-bottom: 1rem;
  }
  .run-meta span {
    background: rgba(255, 255, 255, 0.9);
    border: 1px solid rgba(255, 255, 255, 0.24);
    border-radius: 8px;
    color: #1b4d2b;
    font-size: 0.8rem;
    font-weight: 900;
    padding: 0.35rem 0.65rem;
    text-transform: uppercase;
  }
  .progress-track {
    background: rgba(255, 255, 255, 0.28);
    border-radius: 999px;
    border: 1px solid rgba(255, 255, 255, 0.28);
    height: 12px;
    overflow: hidden;
  }
  .progress-bar {
    background: repeating-linear-gradient(45deg, #b9f6ca 0 12px, #8be79f 12px 24px);
    height: 100%;
  }
  .question-card {
    border-top: 6px solid #1b4d2b;
    display: grid;
    gap: 1rem;
    position: relative;
  }
  .question-card::after {
    background: #e8f5e9;
    border-radius: 8px;
    color: #2e7d32;
    content: '?';
    display: grid;
    font-size: 2rem;
    font-weight: 900;
    height: 56px;
    place-items: center;
    position: absolute;
    right: 1rem;
    top: 1rem;
    width: 56px;
  }
  .question-topline {
    color: #2e7d32;
    font-size: 0.8rem;
    font-weight: 900;
    text-transform: uppercase;
  }
  .question-card h2 {
    color: #1b2a1b;
    font-size: 1.45rem;
    font-weight: 900;
    margin: 0;
    padding-right: 72px;
  }
  .options,
  .result-list {
    display: grid;
    gap: 0.75rem;
  }
  .option {
    align-items: center;
    background: #fff;
    border: 1px solid rgba(69, 128, 81, 0.18);
    border-radius: 8px;
    cursor: pointer;
    display: flex;
    gap: 0.8rem;
    padding: 0.95rem 1rem;
    transition:
      background 0.2s ease,
      border-color 0.2s ease,
      transform 0.2s ease;
  }
  .option:hover {
    transform: translateX(2px);
  }
  .option.selected {
    background: #e8f5e9;
    border-color: rgba(46, 125, 50, 0.5);
    box-shadow: 0 8px 18px rgba(46, 125, 50, 0.12);
  }
  .option input {
    accent-color: #2e7d32;
  }
  .score-card {
    align-items: center;
    background:
      linear-gradient(135deg, rgba(232, 245, 233, 0.96), rgba(255, 255, 255, 0.96)),
      url('../img/quiz.png') right bottom / 110px no-repeat;
    border: 1px solid rgba(69, 128, 81, 0.15);
    border-radius: 8px;
    display: flex;
    gap: 1.25rem;
    justify-content: space-between;
    padding: 1rem;
  }
  .score-card > div:first-child {
    display: grid;
    gap: 0.35rem;
  }
  .score-card span,
  .review-heading span,
  .answer-box span {
    color: #506650;
    font-size: 0.78rem;
    font-weight: 800;
    text-transform: uppercase;
  }
  .score-card strong {
    color: #1b2a1b;
    font-size: clamp(2.4rem, 8vw, 4rem);
    line-height: 1;
  }
  .score-card p {
    color: #506650;
    margin: 0;
  }
  .score-ring {
    align-items: center;
    aspect-ratio: 1;
    background:
      radial-gradient(circle at center, #fff 58%, transparent 60%),
      conic-gradient(#2e7d32 var(--score), rgba(46, 125, 50, 0.14) 0);
    border-radius: 50%;
    display: flex;
    flex: 0 0 112px;
    justify-content: center;
  }
  .score-ring span {
    color: #1b2a1b;
    font-size: 1.25rem;
  }
  .review-heading {
    align-items: end;
    display: flex;
    gap: 1rem;
    justify-content: space-between;
    margin: 1.25rem 0 0.85rem;
  }
  .review-counts {
    display: flex;
    flex-wrap: wrap;
    gap: 0.5rem;
    justify-content: flex-end;
  }
  .count-pill {
    background: rgba(80, 102, 80, 0.12);
    border-radius: 999px;
    padding: 0.4rem 0.7rem;
    white-space: nowrap;
  }
  .count-correct {
    background: #dcfce7;
    color: #166534 !important;
  }
  .count-wrong {
    background: #fee2e2;
    color: #991b1b !important;
  }
  .review-heading h2 {
    color: #1b2a1b;
    font-size: 1.35rem;
    font-weight: 800;
    margin: 0 0 0.2rem;
  }
  .review-heading p {
    color: #506650;
    margin: 0;
  }
  .result-row {
    border: 1px solid rgba(69, 128, 81, 0.14);
    border-left: 6px solid #2e7d32;
    border-radius: 8px;
    padding: 1rem;
  }
  .result-row.correct {
    background: #f0fdf4;
    border-left-color: #2e7d32;
  }
  .result-row.incorrect {
    background: #fff5f5;
    border-color: rgba(198, 40, 40, 0.22);
    border-left-color: #c62828;
  }
  .result-content {
    display: grid;
    gap: 0.75rem;
  }
  .result-topline {
    display: flex;
  }
  .status-badge {
    border-radius: 999px;
    font-size: 0.78rem;
    font-weight: 800;
    padding: 0.35rem 0.7rem;
    text-transform: uppercase;
  }
  .correct .status-badge {
    background: #2e7d32;
    color: #fff;
  }
  .incorrect .status-badge {
    background: #c62828;
    color: #fff;
  }
  .result-row h3 {
    color: #1b2a1b;
    font-size: 1.05rem;
    font-weight: 800;
    margin: 0;
  }
  .answer-box {
    border-radius: 8px;
    display: grid;
    gap: 0.25rem;
    padding: 0.85rem 1rem;
  }
  .answer-box strong {
    color: #1b2a1b;
  }
  .answer-correct {
    background: #dcfce7;
    border: 1px solid #86efac;
  }
  .answer-wrong {
    background: #fee2e2;
    border: 1px solid #fca5a5;
  }
  .answer-wrong span,
  .incorrect .answer-wrong strong {
    color: #991b1b;
  }
  @media (max-width: 640px) {
    .score-card,
    .review-heading {
      align-items: stretch;
      flex-direction: column;
    }
    .score-ring {
      align-self: center;
    }
  }
</style>
