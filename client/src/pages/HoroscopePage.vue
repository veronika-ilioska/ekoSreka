<template>
  <section class="eco-section eco-horoscope">
    <div class="container">
      <header class="hor-header">
        <h2 class="eco-section-title">Хороскоп</h2>

        <!-- ФИЛТЕР ПАНЕЛ (responsive) -->
        <form class="filters" @submit.prevent="apply">
          <div class="field">
            <label for="sign">Знак</label>
            <select id="sign" v-model="sign">
              <option v-for="z in signs" :key="z.value" :value="z.value">{{ z.label }}</option>
            </select>
          </div>

          <div class="field">
            <label for="ptype">Период</label>
            <select id="ptype" v-model="periodType">
              <option value="DAILY">Дневен</option>
              <option value="WEEKLY">Неделен</option>
              <option value="MONTHLY">Месечен</option>
            </select>
          </div>

          <div class="field">
            <label for="pdate">Датум</label>
            <input id="pdate" type="date" v-model="date" :max="today" />
            <small class="hint">Остави празно за „најново“</small>
          </div>

          <div class="actions">
            <button type="submit" class="btn primary" :disabled="loading">Примени</button>
            <button type="button" class="btn" @click="clearDate" :disabled="loading">
              Без датум
            </button>
          </div>
        </form>
      </header>

      <!-- СОДРЖИНА -->
      <div class="grid">
        <!-- skeleton -->
        <article v-if="loading" class="card skeleton">
          <div class="line w40"></div>
          <div class="line w90"></div>
          <div class="line w70"></div>
        </article>

        <!-- податок -->
        <article v-else-if="item" class="card">
          <div class="meta-top">
            <span class="chip">{{ signLabel(sign) }}</span>
            <span class="chip chip-soft">{{ periodLabel(periodType) }}</span>
            <span class="chip chip-date">{{ item.periodDate }}</span>
          </div>
          <h3 class="title">{{ item.title }}</h3>
          <p class="content">{{ item.content }}</p>

          <div class="eco-tip" v-if="item.ecoTip">
            <span class="leaf" aria-hidden="true">🌿</span>
            <strong>Еко совет:</strong> {{ item.ecoTip }}
          </div>
        </article>

        <!-- празно -->
        <article v-else class="card empty">
          <p>Нема пронајден запис за селекцијата.</p>
        </article>
      </div>
    </div>
  </section>
</template>

<script setup>
  import { ref, onMounted } from 'vue';

  const signs = [
    { value: 'ARIES', label: 'Овен' },
    { value: 'TAURUS', label: 'Бик' },
    { value: 'GEMINI', label: 'Близнаци' },
    { value: 'CANCER', label: 'Рак' },
    { value: 'LEO', label: 'Лав' },
    { value: 'VIRGO', label: 'Дева' },
    { value: 'LIBRA', label: 'Вага' },
    { value: 'SCORPIO', label: 'Шкорпија' },
    { value: 'SAGITTARIUS', label: 'Стрелец' },
    { value: 'CAPRICORN', label: 'Јарец' },
    { value: 'AQUARIUS', label: 'Водолија' },
    { value: 'PISCES', label: 'Риби' },
  ];

  const sign = ref('ARIES');
  const periodType = ref('DAILY');
  const date = ref(''); // формат YYYY-MM-DD
  const today = new Date().toISOString().slice(0, 10);

  const item = ref(null);
  const loading = ref(false);

  function signLabel(val) {
    const s = signs.find((x) => x.value === val);
    return s ? s.label : val;
  }
  function periodLabel(p) {
    return p === 'DAILY' ? 'Дневен' : p === 'WEEKLY' ? 'Неделен' : 'Месечен';
  }

  async function loadLatest() {
    loading.value = true;
    try {
      const url = new URL('/api/horoscope/latest', window.location.origin);
      url.searchParams.set('sign', sign.value);
      const res = await fetch(url, { headers: { Accept: 'application/json' } });
      if (!res.ok) throw new Error('HTTP ' + res.status);
      item.value = await res.json();
    } catch (e) {
      console.error('latest error', e);
      item.value = null;
    } finally {
      loading.value = false;
    }
  }

  async function loadByPeriod() {
    loading.value = true;
    try {
      const url = new URL('/api/horoscope/by-period', window.location.origin);
      url.searchParams.set('sign', sign.value);
      url.searchParams.set('periodType', periodType.value);
      url.searchParams.set('date', date.value);
      const res = await fetch(url, { headers: { Accept: 'application/json' } });
      if (!res.ok) throw new Error('HTTP ' + res.status);
      item.value = await res.json();
    } catch (e) {
      console.error('by-period error', e);
      item.value = null;
    } finally {
      loading.value = false;
    }
  }

  function clearDate() {
    date.value = '';
    apply();
  }
  function apply() {
    if (!date.value) return loadLatest();
    return loadByPeriod();
  }

  onMounted(loadLatest);
</script>

<style scoped>
  .eco-section {
    padding: clamp(24px, 3vw, 40px) 0;
  }
  .container {
    max-width: 860px;
    margin: 0 auto;
    padding: 0 16px;
  }

  .hor-header {
    display: grid;
    gap: 12px;
    margin-bottom: 16px;
  }
  .eco-section-title {
    margin: 0;
  }

  .filters {
    display: grid;
    gap: 10px;
    grid-template-columns: 1fr 1fr 1fr auto auto;
    align-items: end;
  }
  @media (max-width: 720px) {
    .filters {
      grid-template-columns: 1fr 1fr;
    }
    .actions {
      grid-column: 1 / -1;
      display: flex;
      gap: 8px;
    }
  }
  .field {
    display: grid;
    gap: 6px;
  }
  .field label {
    font-size: 0.85rem;
    color: #374151;
  }
  .field select,
  .field input[type='date'] {
    padding: 10px 12px;
    border-radius: 10px;
    border: 1px solid #e5e7eb;
    background: #fff;
  }
  .hint {
    color: #6b7280;
    font-size: 0.78rem;
  }

  .actions {
    display: flex;
    gap: 8px;
  }
  .btn {
    padding: 10px 14px;
    border-radius: 10px;
    border: 1px solid #e5e7eb;
    background: #fff;
  }
  .btn.primary {
    background: #2563eb;
    color: #fff;
    border-color: #1d4ed8;
  }
  .btn:disabled {
    opacity: 0.6;
    cursor: not-allowed;
  }

  .grid {
    display: grid;
    gap: 14px;
  }
  .card {
    background: #fff;
    border-radius: 16px;
    box-shadow: 0 6px 18px rgba(0, 0, 0, 0.06);
    padding: 14px;
    display: grid;
    gap: 10px;
  }
  .meta-top {
    display: flex;
    gap: 8px;
    flex-wrap: wrap;
  }
  .chip {
    padding: 4px 10px;
    border-radius: 999px;
    background: #eef2ff;
    color: #3730a3;
    font-size: 0.8rem;
  }
  .chip-soft {
    background: #e5f4ff;
    color: #0b66a7;
  }
  .chip-date {
    background: #fff7ed;
    color: #9a3412;
  }
  .title {
    margin: 0;
    font-weight: 700;
    font-size: 1.1rem;
  }
  .content {
    margin: 0;
    color: #374151;
    line-height: 1.6;
  }
  .eco-tip {
    background: #f0fdf4;
    color: #166534;
    border: 1px solid #bbf7d0;
    border-radius: 12px;
    padding: 10px 12px;
    display: flex;
    gap: 8px;
    align-items: center;
  }
  .leaf {
    font-size: 1.1rem;
  }

  .skeleton .line {
    height: 12px;
    background: #e5e7eb;
    border-radius: 10px;
  }
  .skeleton .w40 {
    width: 40%;
    margin-bottom: 6px;
  }
  .skeleton .w90 {
    width: 90%;
    margin-bottom: 6px;
  }
  .skeleton .w70 {
    width: 70%;
  }
  .empty {
    text-align: center;
    color: #6b7280;
  }
</style>
