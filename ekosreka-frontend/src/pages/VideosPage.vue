<template>
  <section class="media-page">
    <div class="media-shell">
      <header class="media-hero videos-hero">
        <div>
          <p class="eyebrow">Eco videos</p>
          <h1>Watch quick stories, guides, and ideas worth sharing</h1>
          <p>Short clips and references for learning, inspiration, and practical eco action.</p>
        </div>
        <div class="hero-stat">
          <strong>{{ totalItems }}</strong>
          <span>videos</span>
        </div>
      </header>

      <section class="media-toolbar">
        <label class="search-field">
          <span>Search videos</span>
          <input v-model="q" @input="debouncedSearch" type="search" placeholder="Search by title..." />
        </label>
        <label class="select-field">
          <span>Per page</span>
          <select v-model.number="size" @change="goPage(0)">
            <option :value="8">8</option>
            <option :value="12">12</option>
            <option :value="24">24</option>
          </select>
        </label>
      </section>

      <div v-if="loading" class="state-card">Loading videos...</div>
      <div v-else-if="loadError" class="alert alert-danger">{{ loadError }}</div>
      <div v-else-if="items.length === 0" class="state-card">No videos found.</div>

      <div v-else class="video-grid">
        <article v-for="video in items" :key="video.id" class="video-card">
          <a :href="videoUrl(video)" target="_blank" rel="noreferrer">
            <img :src="thumbnail(video)" :alt="video.title" loading="lazy" />
            <span class="play-badge">Play</span>
          </a>
          <div class="video-body">
            <div class="video-meta">
              <span>{{ video.source || 'Video' }}</span>
              <span v-if="video.durationSec">{{ formatDuration(video.durationSec) }}</span>
            </div>
            <h2>{{ video.title || 'Untitled video' }}</h2>
            <p>{{ truncate(video.description, 130) || 'A quick eco video from the collection.' }}</p>
          </div>
        </article>
      </div>

      <footer class="pager" v-if="totalPages > 1">
        <button class="btn btn-outline-success" :disabled="page === 0 || loading" @click="goPage(page - 1)">
          Previous
        </button>
        <span>Page {{ page + 1 }} of {{ totalPages }}</span>
        <button class="btn btn-outline-success" :disabled="page >= totalPages - 1 || loading" @click="goPage(page + 1)">
          Next
        </button>
      </footer>
    </div>
  </section>
</template>

<script setup>
  import { computed, onMounted, ref } from 'vue';
  import { api } from '../api';
  import fallbackImage from '../img/game.png';

  const items = ref([]);
  const page = ref(0);
  const size = ref(12);
  const totalPages = ref(0);
  const totalItems = ref(0);
  const q = ref('');
  const loading = ref(false);
  const loadError = ref('');
  let searchTimer = null;

  const params = computed(() => {
    const value = { page: page.value, size: size.value };
    if (q.value.trim()) value.q = q.value.trim();
    return value;
  });

  async function load() {
    loading.value = true;
    loadError.value = '';

    try {
      const { data } = await api.get('/media/videos', { params: params.value });
      items.value = data?.content ?? [];
      totalPages.value = data?.totalPages ?? 0;
      totalItems.value = data?.totalElements ?? items.value.length;
    } catch (error) {
      loadError.value = 'Could not load videos.';
      items.value = [];
      totalPages.value = 0;
      totalItems.value = 0;
    } finally {
      loading.value = false;
    }
  }

  function goPage(nextPage) {
    page.value = Math.max(0, nextPage);
    load();
  }

  function debouncedSearch() {
    page.value = 0;
    clearTimeout(searchTimer);
    searchTimer = setTimeout(load, 300);
  }

  function truncate(text, length = 120) {
    if (!text) return '';
    return text.length > length ? `${text.slice(0, length - 1)}...` : text;
  }

  function formatDuration(seconds) {
    const minutes = Math.floor(seconds / 60);
    const rest = seconds % 60;
    return `${minutes}:${String(rest).padStart(2, '0')}`;
  }

  function youtubeId(video) {
    if ((video.source || '').toUpperCase() === 'YOUTUBE' && video.ref) return video.ref;
    return '';
  }

  function thumbnail(video) {
    const id = youtubeId(video);
    if (id) return `https://img.youtube.com/vi/${id}/hqdefault.jpg`;
    return video.thumbnailUrl || fallbackImage;
  }

  function videoUrl(video) {
    const id = youtubeId(video);
    if (id) return `https://www.youtube.com/watch?v=${id}`;
    return video.ref || '#';
  }

  onMounted(load);
</script>

<style scoped>
  .media-page {
    color: var(--eco-text-dark);
    padding: 1rem 0 4rem;
  }

  .media-shell {
    margin: 0 auto;
    max-width: 1120px;
  }

  .media-hero {
    align-items: end;
    border-radius: 8px;
    color: #fff;
    display: grid;
    gap: 1.5rem;
    grid-template-columns: minmax(0, 1fr) 220px;
    min-height: 300px;
    padding: clamp(1.5rem, 4vw, 2.5rem);
  }

  .videos-hero {
    background:
      linear-gradient(135deg, rgba(36, 77, 43, 0.94), rgba(69, 128, 81, 0.82)),
      url('../img/game.png') center / cover;
  }

  .eyebrow {
    font-size: 0.78rem;
    font-weight: 800;
    letter-spacing: 0.08em;
    margin: 0 0 0.65rem;
    text-transform: uppercase;
  }

  .media-hero h1 {
    font-size: clamp(2rem, 5vw, 4rem);
    font-weight: 800;
    letter-spacing: 0;
    line-height: 1.02;
    margin: 0 0 1rem;
    max-width: 780px;
  }

  .media-hero p {
    font-size: 1.04rem;
    line-height: 1.7;
    margin: 0;
    max-width: 620px;
    opacity: 0.94;
  }

  .hero-stat {
    background: rgba(255, 255, 255, 0.16);
    border: 1px solid rgba(255, 255, 255, 0.3);
    border-radius: 8px;
    padding: 1rem;
  }

  .hero-stat strong {
    display: block;
    font-size: 2.3rem;
    line-height: 1;
  }

  .hero-stat span {
    display: block;
    font-size: 0.78rem;
    font-weight: 800;
    margin-top: 0.35rem;
    text-transform: uppercase;
  }

  .media-toolbar {
    align-items: end;
    display: flex;
    gap: 0.75rem;
    justify-content: space-between;
    margin: 1.25rem 0;
  }

  .search-field,
  .select-field {
    display: grid;
    gap: 0.35rem;
  }

  .search-field {
    flex: 1;
  }

  .search-field span,
  .select-field span {
    color: #506650;
    font-size: 0.82rem;
    font-weight: 800;
    text-transform: uppercase;
  }

  input,
  select {
    background: var(--eco-card-bg);
    border: 1px solid rgba(69, 128, 81, 0.25);
    border-radius: 8px;
    min-height: 44px;
    padding: 0.7rem 0.85rem;
  }

  .video-grid {
    display: grid;
    gap: 1rem;
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }

  .video-card {
    background: var(--eco-card-bg);
    border: 1px solid rgba(69, 128, 81, 0.14);
    border-radius: 8px;
    box-shadow: 0 8px 24px rgba(27, 42, 27, 0.08);
    overflow: hidden;
  }

  .video-card a {
    display: block;
    position: relative;
  }

  .video-card img {
    aspect-ratio: 16 / 9;
    object-fit: cover;
    width: 100%;
  }

  .play-badge {
    background: rgba(255, 255, 255, 0.92);
    border-radius: 999px;
    bottom: 0.75rem;
    color: #1b2a1b;
    font-size: 0.78rem;
    font-weight: 800;
    left: 0.75rem;
    padding: 0.35rem 0.65rem;
    position: absolute;
    text-transform: uppercase;
  }

  .video-body {
    display: grid;
    gap: 0.65rem;
    padding: 1rem;
  }

  .video-meta {
    color: #506650;
    display: flex;
    font-size: 0.8rem;
    font-weight: 800;
    justify-content: space-between;
    text-transform: uppercase;
  }

  .video-body h2 {
    color: #1b2a1b;
    font-size: 1.15rem;
    font-weight: 800;
    letter-spacing: 0;
    margin: 0;
  }

  .video-body p {
    color: #506650;
    line-height: 1.55;
    margin: 0;
  }

  .state-card {
    background: var(--eco-card-bg);
    border: 1px solid rgba(69, 128, 81, 0.14);
    border-radius: 8px;
    box-shadow: 0 8px 24px rgba(27, 42, 27, 0.08);
    color: #506650;
    padding: 1.5rem;
    text-align: center;
  }

  .pager {
    align-items: center;
    display: flex;
    gap: 0.75rem;
    justify-content: center;
    margin-top: 1.5rem;
  }

  .pager span {
    color: #506650;
    font-weight: 800;
  }

  @media (max-width: 991px) {
    .media-hero,
    .video-grid {
      grid-template-columns: 1fr;
    }

    .hero-stat {
      max-width: 240px;
    }
  }

  @media (max-width: 640px) {
    .media-toolbar,
    .pager {
      align-items: stretch;
      flex-direction: column;
    }
  }
</style>
