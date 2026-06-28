<template>
  <section class="eco-section media-page">
    <div class="container">
      <header class="media-hero">
        <div>
          <p class="eyebrow">Еко видеа</p>
          <h1>Видеа</h1>
          <p>Гледај ги сите видеа додадени во забавната секција.</p>
        </div>
        <div class="hero-count">
          <FontAwesomeIcon class="hero-count-icon" :icon="byPrefixAndName.fas['play']" />
          <strong>{{ totalItems }}</strong>
          <span>објави</span>
        </div>
      </header>

      <div class="toolbar">
        <input v-model="q" @input="debouncedSearch" type="search" class="control" placeholder="Пребарај по наслов..." />
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
        <article v-for="video in items" :key="video.id" class="card">
          <div class="thumb-wrap thumb-wrap--video">
            <video v-if="isUploadedVideo(video)" :src="video.ref" class="thumb" muted preload="metadata"></video>
            <img v-else :src="thumbnail(video)" :alt="video.title" class="thumb" loading="lazy" />
            <span class="video-icon">{{ videoIcon(video) }}</span>
          </div>
          <div class="meta-top">
            <span class="chip">{{ sourceLabel(video.source) }}</span>
            <span v-if="video.durationSec" class="chip chip-date">{{ formatDuration(video.durationSec) }}</span>
          </div>
          <h2 class="title">{{ video.title }}</h2>
          <p class="content">{{ truncate(video.description, 130) }}</p>
        </article>
      </div>

      <article v-else class="card empty">
        <p>Нема додадени видеа.</p>
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
  import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome';
  import { faPlay } from '@fortawesome/free-solid-svg-icons';
  import { onMounted, ref } from 'vue';
  import  api  from '../api';

  const byPrefixAndName = { fas: { play: faPlay } };
  const items = ref([]);
  const page = ref(0);
  const size = ref(12);
  const totalPages = ref(0);
  const totalItems = ref(0);
  const q = ref('');
  const loading = ref(false);

  async function load() {
    loading.value = true;
    try {
      const { data } = await api.get('/media/videos', {
        params: { page: page.value, size: size.value, q: q.value.trim() },
      });
      items.value = data.content ?? [];
      totalPages.value = data.totalPages ?? 0;
      totalItems.value = data.totalElements ?? items.value.length;
    } catch (error) {
      console.error('Грешка при вчитување видеа:', error);
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

  function formatDuration(seconds) {
    const minutes = Math.floor(seconds / 60);
    const rest = seconds % 60;
    return `${minutes}м ${rest}с`;
  }

  function sourceLabel(source) {
    return source ? String(source).toLowerCase() : 'видео';
  }

  function thumbnail(video) {
    if ((video.source || '').toUpperCase() === 'YOUTUBE' && video.ref) {
      return `https://img.youtube.com/vi/${video.ref}/hqdefault.jpg`;
    }
    return video.thumbnailUrl || 'https://placehold.co/600x400?text=Video';
  }

  function isUploadedVideo(video) {
    return (video.source || '').toUpperCase() === 'UPLOAD' && Boolean(video.ref);
  }

  function videoIcon(video) {
    return video.icon || '▶';
  }

  onMounted(load);
</script>

<style scoped>
  .media-page { color: #1b2a1b; padding: 1rem 0 4rem; }
  .container { max-width: 1120px; margin: 0 auto; padding: 0 16px; }
  .media-hero { align-items: end; background: linear-gradient(115deg, #102817 0%, #24552d 54%, #1b2a1b 100%); border-radius: 8px; color: #fff; display: grid; gap: 1.5rem; grid-template-columns: minmax(0, 1fr) 220px; isolation: isolate; margin-bottom: 1.25rem; min-height: 260px; overflow: hidden; padding: clamp(1.5rem, 4vw, 2.5rem); position: relative; }
  .media-hero::after { background: rgba(255,255,255,0.14); border: 1px solid rgba(255,255,255,0.22); border-radius: 50%; content: ''; height: 150px; position: absolute; right: 300px; top: -34px; width: 150px; z-index: -1; }
  .eyebrow { font-size: 0.78rem; font-weight: 900; letter-spacing: 0.08em; margin: 0 0 0.55rem; text-transform: uppercase; }
  .media-hero h1 { font-size: clamp(2.2rem, 6vw, 4.4rem); font-weight: 900; letter-spacing: 0; line-height: 0.98; margin: 0 0 0.75rem; }
  .media-hero p { line-height: 1.7; margin: 0; max-width: 640px; opacity: 0.92; }
  .hero-count { background: rgba(255, 255, 255, 0.92); border: 1px solid rgba(255, 255, 255, 0.5); border-radius: 8px; color: #1b2a1b; overflow: hidden; padding: 1rem; position: relative; }
  .hero-count-icon { color: rgba(46, 125, 50, 0.16); font-size: 3.2rem; position: absolute; right: 0.8rem; top: 0.65rem; }
  .hero-count strong { display: block; font-size: 3rem; font-weight: 900; line-height: 1; }
  .hero-count span { color: #2e7d32; font-size: 0.78rem; font-weight: 900; text-transform: uppercase; }
  .toolbar { background: rgba(255,255,255,0.72); border: 1px solid rgba(69,128,81,0.12); border-radius: 8px; display: flex; gap: 0.75rem; justify-content: flex-end; margin-bottom: 1rem; padding: 0.5rem; }
  .control, .btn { background: #fff; border: 1px solid #d7e6d8; border-radius: 8px; color: #1b2a1b; min-height: 42px; padding: 0.65rem 1rem; }
  .control:focus { border-color: #66bb6a; box-shadow: 0 0 0 3px rgba(102, 187, 106, 0.2); outline: none; }
  .grid { display: grid; gap: 1.25rem; grid-template-columns: repeat(2, minmax(0, 1fr)); }
  .card { background: #fff; border: 1px solid rgba(69, 128, 81, 0.14); border-radius: 8px; box-shadow: 0 10px 30px rgba(27, 42, 27, 0.08); color: #1b2a1b; display: grid; gap: 0.85rem; overflow: hidden; padding: 1rem; transition: transform 0.2s ease, border-color 0.2s ease; }
  .card:hover { border-color: rgba(69, 128, 81, 0.34); transform: translateY(-2px); }
  .thumb-wrap { aspect-ratio: 16 / 9; background: #111; border-radius: 6px; overflow: hidden; margin: -1rem -1rem 0; position: relative; }
  .thumb-wrap::after { background: linear-gradient(180deg, transparent 30%, rgba(0,0,0,0.48)); content: ''; inset: 0; position: absolute; }
  .thumb { display: block; height: 100%; object-fit: cover; opacity: 0.94; width: 100%; }
  .video-icon { align-items: center; background: rgba(255, 255, 255, 0.94); border-radius: 8px; bottom: 0.85rem; color: #24552d; display: inline-flex; font-size: 1.1rem; font-weight: 900; height: 40px; justify-content: center; left: 0.85rem; min-width: 40px; padding: 0 0.65rem; position: absolute; z-index: 1; }
  .meta-top { display: flex; flex-wrap: wrap; gap: 0.5rem; }
  .chip { background: #eaf6ea; border-radius: 999px; color: #24552d; font-size: 0.78rem; font-weight: 800; padding: 0.35rem 0.7rem; }
  .chip-date { background: #fff7ed; color: #9a3412; }
  .title { color: #1b2a1b; font-size: 1.25rem; font-weight: 900; margin: 0; }
  .content { color: #374151; line-height: 1.65; margin: 0; }
  .pager { align-items: center; color: #1b2a1b; display: flex; gap: 0.75rem; justify-content: center; margin-top: 1.25rem; }
  .btn:disabled { cursor: not-allowed; opacity: 0.55; }
  .empty { color: #6b7280; text-align: center; }
  .skeleton .thumb, .skeleton .line { background: #e5e7eb; border-radius: 8px; }
  .skeleton .thumb { aspect-ratio: 16 / 9; margin: -1rem -1rem 0; }
  .skeleton .line { height: 12px; }
  .skeleton .w60 { width: 60%; }
  .skeleton .w90 { width: 90%; }
  @media (max-width: 760px) {
    .media-hero, .grid { grid-template-columns: 1fr; }
    .media-hero::after { display: none; }
    .toolbar { align-items: stretch; flex-direction: column; }
  }
</style>
