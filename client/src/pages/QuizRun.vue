<template>
  <section v-if="quiz" class="quiz-wrap">
    <header class="hero" :style="{ '--bg': `url(${cover})` }">
      <div class="shade"></div>
      <div class="hero-content container">
        <h1 class="title">{{ quiz.title }}</h1>
        <p class="meta">
          Ниво: {{ quiz.level }} · Време: {{ quiz.timeMinutes }} мин · Прашања: {{ totalQs }}
        </p>

        <div v-if="!result" class="progress">
          <div class="bar" :style="{ width: progressPct + '%' }"></div>
        </div>
        <div v-else class="score-pill">
          Резултат: <strong>{{ result.score }}</strong
          >/<strong>{{ result.total }}</strong>
        </div>
      </div>
    </header>

    <div class="container content">
      <div v-if="!result">
        <div v-for="q in quiz.questions" :key="q.id" class="q-card">
          <h5 class="q-title">Прашање: {{ q.text }}</h5>

          <div class="options">
            <label
              v-for="o in q.options"
              :key="o.id"
              class="option"
              :class="{ selected: answers[q.id] === o.id }"
            >
              <input
                type="radio"
                class="visually-hidden"
                :name="`q_${q.id}`"
                :value="o.id"
                :checked="answers[q.id] === o.id"
                @change="pick(q.id, o.id)"
              />
              <span class="dot" aria-hidden="true"></span>
              <span class="text">{{ o.text }}</span>
            </label>
          </div>
        </div>

        <button
          class="btn eco-btn submit"
          :disabled="answeredCount < totalQs"
          @click="submitQuiz"
          title="Одговори на сите прашања за да испратиш"
        >
          Испрати
        </button>
      </div>

      <div v-else class="results">
        <div
          v-for="r in result.details"
          :key="r.questionId"
          class="res-item"
          :class="r.correct ? 'ok' : 'bad'"
        >
          <div class="res-q">{{ questionText(r.questionId) }}</div>

          <div class="res-a">
            <span class="badge" :class="r.correct ? 'b-ok' : 'b-bad'">
              {{ r.correct ? 'Точно' : 'Неточно' }}
            </span>

            <div class="answers">
              <div class="row">
                <span class="label">Твој одговор:</span>
                <span class="value">{{ optionText(r.questionId, r.chosenOptionId) }}</span>
              </div>
              <div v-if="!r.correct" class="row">
                <span class="label">Точен одговор:</span>
                <span class="value">{{ optionText(r.questionId, r.correctOptionId) }}</span>
              </div>
            </div>
          </div>
        </div>

        <RouterLink to="/quizzes" class="btn outline back">Назад кон квизови</RouterLink>
      </div>
    </div>
  </section>
</template>

<script setup>
  import { ref, onMounted, computed } from 'vue';
  import { useRoute, RouterLink } from 'vue-router';
  import { api } from '../api';

  const route = useRoute();
  const quizId = Number(route.params.id);

  const quiz = ref(null);
  const answers = ref({});
  const result = ref(null);

  onMounted(async () => {
    const { data } = await api.get(`/quizzes/${quizId}`);
    quiz.value = data;
  });

  function pick(questionId, optionId) {
    answers.value = { ...answers.value, [questionId]: optionId };
  }

  async function submitQuiz() {
    const payload = Object.entries(answers.value).map(([qid, oid]) => ({
      questionId: Number(qid),
      optionId: Number(oid),
    }));
    const { data } = await api.post(`/quizzes/${quizId}/submit`, payload);
    result.value = data;
  }

  const cover = computed(
    () => quiz.value?.coverImage || quiz.value?.image || '/images/placeholders/quiz.jpg'
  );

  const totalQs = computed(() => quiz.value?.questions?.length || 0);
  const answeredCount = computed(() => Object.keys(answers.value).length);
  const progressPct = computed(() =>
    totalQs.value ? Math.round((answeredCount.value / totalQs.value) * 100) : 0
  );

  function optionText(questionId, optionId) {
    const q = quiz.value?.questions?.find((x) => x.id === questionId);
    const o = q?.options?.find((x) => x.id === optionId);
    return o?.text ?? '—';
  }
  function questionText(questionId) {
    return quiz.value?.questions?.find((x) => x.id === questionId)?.text ?? '';
  }
</script>

<style scoped>
  .hero {
    position: relative;
    background-image: var(--bg);
    background-size: cover;
    background-position: center;
    min-height: 240px;
    display: flex;
    align-items: flex-end;
    color: #fff;
    isolation: isolate;
  }
  .shade {
    position: absolute;
    inset: 0;
    background: linear-gradient(
      180deg,
      rgba(0, 0, 0, 0.15) 0%,
      rgba(0, 0, 0, 0.45) 55%,
      rgba(0, 0, 0, 0.75) 100%
    );
    z-index: 0;
  }
  .hero-content {
    position: relative;
    z-index: 1;
    padding: 28px 0;
  }
  .title {
    margin: 0 0 6px 0;
    font-weight: 900;
    letter-spacing: 0.2px;
    text-shadow: 0 2px 8px rgba(0, 0, 0, 0.35);
  }
  .meta {
    margin: 0 0 14px 0;
    opacity: 0.9;
  }
  .progress {
    width: 100%;
    height: 8px;
    border-radius: 999px;
    overflow: hidden;
    background: rgba(255, 255, 255, 0.25);
  }
  .progress .bar {
    height: 100%;
    background: rgb(28, 150, 28);
    backdrop-filter: blur(1px);
  }
  .score-pill {
    display: inline-block;
    background: rgba(255, 255, 255, 0.18);
    padding: 8px 14px;
    border-radius: 999px;
    font-weight: 700;
  }

  .content {
    padding: 28px 0 40px;
  }

  .q-card {
    background: #fff;
    border-radius: 16px;
    padding: 18px;
    margin-bottom: 14px;
    box-shadow: 0 6px 18px rgba(0, 0, 0, 0.06);
    border: 1px solid rgba(0, 0, 0, 0.06);
  }
  .q-title {
    margin: 0 0 12px 0;
  }

  .options {
    display: grid;
    gap: 10px;
  }
  .option {
    display: flex;
    align-items: center;
    gap: 10px;
    border: 1px solid #e6e8ec;
    border-radius: 12px;
    padding: 12px 14px;
    cursor: pointer;
    user-select: none;
    transition:
      border-color 0.2s ease,
      box-shadow 0.2s ease,
      background 0.2s ease;
    background: #fafbfc;
  }
  .option:hover {
    border-color: #cbd5e1;
    background: #fff;
  }
  .option.selected {
    border-color: #2bb673;
    box-shadow: 0 6px 14px rgba(43, 182, 115, 0.18);
    background: #f6fffa;
  }
  .dot {
    width: 14px;
    height: 14px;
    border-radius: 50%;
    border: 2px solid #7b8794;
    display: inline-block;
  }
  .option.selected .dot {
    border-color: #2bb673;
    background: #2bb673;
  }
  .text {
    line-height: 1.2;
  }

  .visually-hidden {
    position: absolute !important;
    clip: rect(1px, 1px, 1px, 1px);
    height: 1px;
    width: 1px;
    overflow: hidden;
    white-space: nowrap;
  }

  .btn.eco-btn.submit {
    margin-top: 12px;
    padding: 10px 18px;
    color: #fff;
    background: #2bb673;
    border: none;
    border-radius: 10px;
  }
  .btn.eco-btn.submit:disabled {
    opacity: 0.6;
    cursor: not-allowed;
  }

  .results {
    display: grid;
    gap: 12px;
  }
  .res-item {
    border-radius: 14px;
    padding: 14px 16px;
    border: 1px solid rgba(0, 0, 0, 0.06);
    box-shadow: 0 6px 16px rgba(0, 0, 0, 0.05);
  }
  .res-item.ok {
    background: rgba(43, 182, 115, 0.08);
  }
  .res-item.bad {
    background: rgba(198, 40, 40, 0.08);
  }

  .res-q {
    font-weight: 700;
    margin-bottom: 8px;
  }
  .res-a {
    display: grid;
    gap: 8px;
  }

  .badge {
    display: inline-block;
    padding: 4px 10px;
    border-radius: 999px;
    font-size: 0.8rem;
    font-weight: 700;
  }
  .b-ok {
    background: #2bb673;
    color: #fff;
  }
  .b-bad {
    background: #c62828;
    color: #fff;
  }

  .answers {
    display: grid;
    gap: 4px;
  }
  .row {
    display: flex;
    gap: 6px;
    flex-wrap: wrap;
  }
  .label {
    opacity: 0.75;
  }
  .value {
    font-weight: 600;
  }

  .btn.outline.back {
    margin-top: 6px;
    border: 1px solid #2bb673;
    color: #2bb673;
    padding: 8px 14px;
    border-radius: 10px;
    display: inline-block;
  }
  .btn.outline.back:hover {
    background: #e7f8f0;
  }

  .q-title {
    margin: 0 0 12px 0;
    font-weight: 600;
    font-size: 1rem;
    color: #212529;
  }

  .option .text {
    color: #212529;
    font-size: 0.95rem;
  }

  .label {
    color: #444;
  }
  .value {
    color: #111;
  }
  .res-q {
    color: #444;
  }
</style>
