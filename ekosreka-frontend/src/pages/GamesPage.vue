<template>
  <section class="eco-section media-page">
    <div class="container">
      <header class="media-hero">
        <div>
          <p class="eyebrow">Еко игри</p>
          <h1>Забавни игри</h1>
          <p>Играј мини-игри и предизвици што помагаат зелените навики да се учат полесно.</p>
        </div>
        <div class="hero-count">
          <strong>{{ totalItems }}</strong>
          <span>објави</span>
        </div>
      </header>

      <div class="toolbar">
        <input v-model="q" @input="debouncedSearch" type="search" class="control" placeholder="Пребарај по наслов..." />
        <select v-model="difficulty" @change="goPage(0)" class="control">
          <option value="">Сите тежини</option>
          <option value="EASY">Лесно</option>
          <option value="MEDIUM">Средно</option>
          <option value="HARD">Тешко</option>
        </select>
        <select v-model.number="size" @change="goPage(0)" class="control">
          <option :value="8">8</option>
          <option :value="12">12</option>
          <option :value="24">24</option>
        </select>
      </div>

      <div v-if="loading && items.length === 0" class="grid">
        <article v-for="i in size" :key="i" class="card skeleton">
          <div class="thumb"></div>
          <div class="line w60"></div>
          <div class="line w90"></div>
        </article>
      </div>

      <div v-else-if="items.length" class="grid">
        <article v-for="game in items" :key="game.id" class="card">
          <div class="thumb-wrap thumb-wrap--game">
            <img :src="game.thumbnailUrl || 'https://placehold.co/600x400?text=Game'" :alt="game.title" class="thumb" loading="lazy" />
          </div>
          <div class="meta-top">
            <span class="chip">{{ difficultyLabel(game.difficulty) }}</span>
          </div>
          <h2 class="title">{{ game.title }}</h2>
          <p class="content">{{ truncate(game.description, 130) }}</p>
          <details v-if="game.rules" class="rules">
            <summary>Правила</summary>
            <p>{{ truncate(game.rules, 220) }}</p>
          </details>
        </article>
      </div>

      <article v-else class="card empty">
        <p>Нема додадени игри.</p>
      </article>

      <footer class="pager" v-if="totalPages > 1">
        <button class="btn" :disabled="page === 0 || loading" @click="goPage(page - 1)">Претходна</button>
        <span>Страна {{ page + 1 }} од {{ totalPages }}</span>
        <button class="btn" :disabled="page >= totalPages - 1 || loading" @click="goPage(page + 1)">Следна</button>
      </footer>
    </div>
  </section>
</template>

<script setup>
  import { onMounted, ref } from 'vue';
  import { api } from '../api';

  const items = ref([]);
  const page = ref(0);
  const size = ref(12);
  const totalPages = ref(0);
  const totalItems = ref(0);
  const q = ref('');
  const difficulty = ref('');
  const loading = ref(false);

  async function load() {
    loading.value = true;
    try {
      const params = { page: page.value, size: size.value, q: q.value.trim() };
      if (difficulty.value) params.difficulty = difficulty.value;
      const { data } = await api.get('/games', { params });
      items.value = data.content ?? [];
      totalPages.value = data.totalPages ?? 0;
      totalItems.value = data.totalElements ?? items.value.length;
    } catch (error) {
      console.error('Грешка при вчитување игри:', error);
      items.value = [];
      totalPages.value = 0;
      totalItems.value = 0;
    } finally {
      loading.value = false;
    }
  }

  function difficultyLabel(value) {
    return { EASY: 'Лесно', MEDIUM: 'Средно', HARD: 'Тешко' }[value] || value || 'Игра';
  }

  function goPage(nextPage) {
    page.value = Math.max(0, nextPage);
    load();
  }

  let timer = null;
  function debouncedSearch() {
    page.value = 0;
    clearTimeout(timer);
    timer = setTimeout(load, 300);
  }

  function truncate(value, length = 120) {
    if (!value) return '';
    return value.length > length ? `${value.slice(0, length - 1)}...` : value;
  }

  onMounted(load);
</script>

<style scoped>
  .media-page { color: #1b2a1b; padding: clamp(24px, 3vw, 40px) 0; }
  .container { max-width: 1040px; margin: 0 auto; padding: 0 16px; }
  .media-hero { align-items: end; background: linear-gradient(135deg, #244d2b 0%, #66bb6a 100%); border-radius: 8px; color: #fff; display: grid; gap: 1.5rem; grid-template-columns: minmax(0, 1fr) 160px; margin-bottom: 1rem; padding: clamp(1.5rem, 4vw, 2.25rem); }
  .eyebrow { font-size: 0.78rem; font-weight: 800; letter-spacing: 0.08em; margin: 0 0 0.55rem; text-transform: uppercase; }
  .media-hero h1 { font-size: clamp(2rem, 5vw, 3.8rem); font-weight: 800; line-height: 1.05; margin: 0 0 0.75rem; }
  .media-hero p { margin: 0; max-width: 640px; opacity: 0.92; }
  .hero-count { background: rgba(255, 255, 255, 0.16); border: 1px solid rgba(255, 255, 255, 0.3); border-radius: 8px; padding: 1rem; }
  .hero-count strong { display: block; font-size: 2.4rem; line-height: 1; }
  .hero-count span { font-size: 0.8rem; font-weight: 800; text-transform: uppercase; }
  .toolbar { display: flex; gap: 0.75rem; justify-content: flex-end; margin-bottom: 1rem; }
  .control, .btn { background: #fff; border: 1px solid #d7e6d8; border-radius: 8px; color: #1b2a1b; padding: 0.65rem 1rem; }
  .control:focus { border-color: #66bb6a; box-shadow: 0 0 0 3px rgba(102, 187, 106, 0.2); outline: none; }
  .grid { display: grid; gap: 1rem; grid-template-columns: repeat(2, minmax(0, 1fr)); }
  .card { background: #fff; border: 1px solid rgba(69, 128, 81, 0.14); border-radius: 8px; box-shadow: 0 8px 24px rgba(27, 42, 27, 0.08); color: #1b2a1b; display: grid; gap: 0.85rem; overflow: hidden; padding: 1rem; }
  .thumb-wrap { aspect-ratio: 4 / 3; background: #eaf6ea; border-radius: 6px; overflow: hidden; margin: -1rem -1rem 0; }
  .thumb { display: block; height: 100%; object-fit: cover; width: 100%; }
  .meta-top { display: flex; flex-wrap: wrap; gap: 0.5rem; }
  .chip { background: #eaf6ea; border-radius: 999px; color: #24552d; font-size: 0.78rem; font-weight: 800; padding: 0.35rem 0.7rem; }
  .title { color: #1b2a1b; font-size: 1.25rem; font-weight: 800; margin: 0; }
  .content { color: #374151; line-height: 1.65; margin: 0; }
  .rules { background: #f0fdf4; border: 1px solid #bbf7d0; border-radius: 8px; color: #166534; padding: 0.85rem; }
  .rules summary { cursor: pointer; font-weight: 800; }
  .rules p { margin: 0.5rem 0 0; }
  .pager { align-items: center; color: #1b2a1b; display: flex; gap: 0.75rem; justify-content: center; margin-top: 1.25rem; }
  .btn:disabled { cursor: not-allowed; opacity: 0.55; }
  .empty { color: #6b7280; text-align: center; }
  .skeleton .thumb, .skeleton .line { background: #e5e7eb; border-radius: 8px; }
  .skeleton .thumb { aspect-ratio: 4 / 3; margin: -1rem -1rem 0; }
  .skeleton .line { height: 12px; }
  .skeleton .w60 { width: 60%; }
  .skeleton .w90 { width: 90%; }
  @media (max-width: 760px) {
    .media-hero, .grid { grid-template-columns: 1fr; }
    .toolbar { align-items: stretch; flex-direction: column; }
  }
</style>
