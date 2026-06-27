<template>
  <section class="eco-section eco-horoscope">
    <div class="container">
      <header class="hor-hero">
        <div>
          <p class="eyebrow">Еко хороскоп</p>
          <h1>Сите хороскопски објави</h1>
          <p>Разгледај ја целата историја на еко хороскопи што биле додадени во секцијата.</p>
        </div>
        <div class="hero-count">
          <strong>{{ totalItems }}</strong>
          <span>објави</span>
        </div>
      </header>

      <div v-if="loading && entries.length === 0" class="grid">
        <article v-for="i in size" :key="i" class="card skeleton">
          <div class="line w40"></div>
          <div class="line w90"></div>
          <div class="line w70"></div>
        </article>
      </div>

      <div v-else-if="entries.length" class="grid">
        <article v-for="entry in entries" :key="entry.id" class="card">
          <div class="meta-top">
            <span class="chip">{{ signLabel(entry.sign) }}</span>
            <span class="chip chip-date">{{ formatUploadTime(entry.createdAt) }}</span>
          </div>
          <h2 class="title">{{ entry.title }}</h2>
          <p class="content">{{ entry.content }}</p>
          <div class="eco-tip" v-if="entry.ecoTip">
            <strong>Еко совет:</strong>
            <span>{{ entry.ecoTip }}</span>
          </div>
        </article>
      </div>

      <article v-else class="card empty">
        <p>Сè уште нема додадени хороскопски објави.</p>
      </article>

      <footer class="pager" v-if="totalPages > 1">
        <button class="btn" :disabled="page === 0 || loading" @click="goPage(page - 1)">
          Претходна
        </button>
        <span>Страна {{ page + 1 }} од {{ totalPages }}</span>
        <button class="btn" :disabled="page >= totalPages - 1 || loading" @click="goPage(page + 1)">
          Следна
        </button>
      </footer>
    </div>
  </section>
</template>

<script setup>
  import { onMounted, ref } from 'vue';
  import  api  from '../api';

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

  const entries = ref([]);
  const loading = ref(false);
  const page = ref(0);
  const size = ref(12);
  const totalPages = ref(0);
  const totalItems = ref(0);

  function signLabel(value) {
    return signs.find((sign) => sign.value === value)?.label || value;
  }

  function formatUploadTime(value) {
    if (!value) return 'Непознато време';
    return new Date(value).toLocaleString('mk-MK', {
      dateStyle: 'medium',
      timeStyle: 'short',
    });
  }

  async function loadEntries() {
    loading.value = true;
    try {
      const { data } = await api.get('/horoscope', {
        params: { page: page.value, size: size.value },
      });
      entries.value = data.content ?? [];
      totalPages.value = data.totalPages ?? 0;
      totalItems.value = data.totalElements ?? entries.value.length;
    } catch (error) {
      if (error?.response?.status === 404) {
        await loadLegacyEntries();
      } else {
        console.error('Грешка при вчитување хороскоп:', error);
        entries.value = [];
        totalPages.value = 0;
        totalItems.value = 0;
      }
    } finally {
      loading.value = false;
    }
  }

  async function loadLegacyEntries() {
    const countRequest = api.get('/horoscope/count').catch(() => ({ data: { count: 0 } }));
    const entryRequests = signs.map((sign) =>
      api
        .get('/horoscope/latest', { params: { sign: sign.value } })
        .then(({ data }) => data)
        .catch(() => null)
    );
    const [countResponse, ...latestEntries] = await Promise.all([countRequest, ...entryRequests]);
    entries.value = latestEntries.filter(Boolean);
    totalPages.value = 1;
    totalItems.value = Number(countResponse.data?.count ?? entries.value.length);
  }

  function goPage(nextPage) {
    page.value = Math.max(0, nextPage);
    loadEntries();
  }

  onMounted(loadEntries);
</script>

<style scoped>
  .eco-section {
    color: #1b2a1b;
    padding: clamp(24px, 3vw, 40px) 0;
  }

  .container {
    max-width: 1040px;
    margin: 0 auto;
    padding: 0 16px;
  }

  .hor-hero {
    align-items: end;
    background: linear-gradient(135deg, #244d2b 0%, #66bb6a 100%);
    border-radius: 8px;
    color: #fff;
    display: grid;
    gap: 1.5rem;
    grid-template-columns: minmax(0, 1fr) 160px;
    margin-bottom: 1.25rem;
    padding: clamp(1.5rem, 4vw, 2.25rem);
  }

  .eyebrow {
    font-size: 0.78rem;
    font-weight: 800;
    letter-spacing: 0.08em;
    margin: 0 0 0.55rem;
    text-transform: uppercase;
  }

  .hor-hero h1 {
    font-size: clamp(2rem, 5vw, 3.8rem);
    font-weight: 800;
    line-height: 1.05;
    margin: 0 0 0.75rem;
  }

  .hor-hero p {
    margin: 0;
    max-width: 640px;
    opacity: 0.92;
  }

  .hero-count {
    background: rgba(255, 255, 255, 0.16);
    border: 1px solid rgba(255, 255, 255, 0.3);
    border-radius: 8px;
    padding: 1rem;
  }

  .hero-count strong {
    display: block;
    font-size: 2.4rem;
    line-height: 1;
  }

  .hero-count span {
    font-size: 0.8rem;
    font-weight: 800;
    text-transform: uppercase;
  }

  .grid {
    display: grid;
    gap: 1rem;
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .card {
    background: #fff;
    border: 1px solid rgba(69, 128, 81, 0.14);
    border-radius: 8px;
    box-shadow: 0 8px 24px rgba(27, 42, 27, 0.08);
    color: #1b2a1b;
    display: grid;
    gap: 0.85rem;
    padding: 1rem;
  }

  .meta-top {
    display: flex;
    flex-wrap: wrap;
    gap: 0.5rem;
  }

  .chip {
    background: #eaf6ea;
    border-radius: 999px;
    color: #24552d;
    font-size: 0.78rem;
    font-weight: 800;
    padding: 0.35rem 0.7rem;
  }

  .chip-date {
    background: #fff7ed;
    color: #9a3412;
  }

  .title {
    color: #1b2a1b;
    font-size: 1.25rem;
    font-weight: 800;
    margin: 0;
  }

  .content {
    color: #374151;
    line-height: 1.65;
    margin: 0;
  }

  .eco-tip {
    background: #f0fdf4;
    border: 1px solid #bbf7d0;
    border-radius: 8px;
    color: #166534;
    display: grid;
    gap: 0.2rem;
    padding: 0.85rem;
  }

  .pager {
    align-items: center;
    color: #1b2a1b;
    display: flex;
    gap: 0.75rem;
    justify-content: center;
    margin-top: 1.25rem;
  }

  .btn {
    background: #fff;
    border: 1px solid #d7e6d8;
    border-radius: 8px;
    padding: 0.65rem 1rem;
  }

  .skeleton .line {
    background: #e5e7eb;
    border-radius: 10px;
    height: 12px;
  }

  .skeleton .w40 {
    width: 40%;
  }

  .skeleton .w90 {
    width: 90%;
  }

  .skeleton .w70 {
    width: 70%;
  }

  .empty {
    color: #6b7280;
    text-align: center;
  }

  @media (max-width: 760px) {
    .hor-hero,
    .grid {
      grid-template-columns: 1fr;
    }
  }
</style>
