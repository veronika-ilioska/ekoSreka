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
  import { api } from '../api';

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
      const { data } = await api.post(`/quizzes/${quizId}/submit`, payload);
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
    max-width: 920px;
  }
  .run-hero,
  .question-card,
  .results-panel,
  .state-card {
    background: var(--eco-card-bg);
    border-radius: 8px;
    box-shadow: 0 8px 24px rgba(27, 42, 27, 0.08);
    padding: 1.5rem;
  }
  .run-hero {
    background: linear-gradient(135deg, rgba(36, 77, 43, 0.95), rgba(69, 128, 81, 0.9)), url('../img/quiz.png') center / cover;
    color: #fff;
    margin-bottom: 1rem;
  }
  .back-link {
    color: #fff;
    display: inline-block;
    font-weight: 800;
    margin-bottom: 1rem;
    text-decoration: none;
  }
  .run-meta,
  .question-actions,
  .results-actions {
    display: flex;
    flex-wrap: wrap;
    gap: 0.75rem;
  }
  .progress-track {
    background: rgba(255, 255, 255, 0.24);
    border-radius: 999px;
    height: 9px;
    overflow: hidden;
  }
  .progress-bar {
    background: #b9f6ca;
    height: 100%;
  }
  .options,
  .result-list {
    display: grid;
    gap: 0.75rem;
  }
  .option {
    background: rgba(102, 187, 106, 0.08);
    border: 1px solid rgba(69, 128, 81, 0.18);
    border-radius: 8px;
    cursor: pointer;
    display: flex;
    gap: 0.8rem;
    padding: 0.95rem 1rem;
  }
  .option.selected {
    background: rgba(102, 187, 106, 0.2);
  }
  .score-card {
    align-items: center;
    background: rgba(102, 187, 106, 0.12);
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
