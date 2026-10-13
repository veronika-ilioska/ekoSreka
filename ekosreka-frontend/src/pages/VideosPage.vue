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
        <h2 class="library-title">Видео библиотека</h2>
        <div class="toolbar-controls">
        <input v-model="q" @input="debouncedSearch" aria-label="Пребарај видеа" type="search" class="control search-control" placeholder="Пребарај по наслов..." />
        <select aria-label="Видеа по страница" v-model.number="size" @change="goPage(0)" class="control">
          <option :value="8">8 по страница</option>
          <option :value="12">12 по страница</option>
          <option :value="24">24 по страница</option>
        </select>
        </div>
      </div>

      <article v-if="loadError" class="empty" role="alert">
        <h2>Видеата не може да се вчитаат.</h2>
        <p>Обидете се повторно за кратко.</p>
        <button class="btn" @click="load">Обиди се повторно</button>
      </article>
      <div v-else-if="loading && items.length === 0" class="grid" aria-label="Се вчитуваат видеа" aria-busy="true">
        <article v-for="i in size" :key="i" class="card skeleton">
          <div class="thumb"></div>
          <div class="line w60"></div>
          <div class="line w90"></div>
        </article>
      </div>

      <div v-else-if="items.length" class="grid">
        <article v-for="video in items" :key="video.id" class="card video-card" role="button" tabindex="0"
          :aria-label="`Пушти видео: ${video.title}`" @click="openVideo(video)"
          @keydown.enter.prevent="openVideo(video)" @keydown.space.prevent="openVideo(video)">
          <div class="thumb-wrap thumb-wrap--video">
            <video v-if="isUploadedVideo(video)" :src="video.ref" class="thumb" muted preload="metadata"></video>
            <img v-else :src="thumbnail(video)" :alt="video.title" class="thumb" loading="lazy" />
            <span class="play-overlay" aria-hidden="true"><FontAwesomeIcon :icon="faPlay" /></span>
            <span class="watch-label">Гледај видео ↗</span>
            <span v-if="video.durationSec" class="duration">{{ formatDuration(video.durationSec) }}</span>
          </div>
          <div class="card-body">
          <div class="meta-top">
            <span class="chip">{{ sourceLabel(video.source) }}</span>
            <span v-if="video.icon" class="custom-icon" aria-hidden="true">{{ video.icon }}</span>
          </div>
          <h2 class="title">{{ video.title }}</h2>
          <p class="content">{{ truncate(video.description, 130) }}</p>
          </div>
        </article>
      </div>

      <article v-else class="empty">
        <FontAwesomeIcon :icon="faPlay" class="empty-icon" aria-hidden="true" />
        <h2>{{ q ? 'Нема резултати за ова пребарување.' : 'Наскоро, нови еко приказни.' }}</h2>
        <p>{{ q ? 'Пробај со друг наслов или прикажи ги сите видеа.' : 'Тука ќе ги најдеш нашите следни видеа.' }}</p>
        <button v-if="q" class="btn" @click="q = ''; goPage(0)">Прикажи ги сите</button>
      </article>

      <footer class="pager" v-if="totalPages > 1">
        <button class="btn" :disabled="page === 0 || loading" @click="goPage(page - 1)">Претходна</button>
        <span>Страна {{ page + 1 }} од {{ totalPages }}</span>
        <button class="btn" :disabled="page >= totalPages - 1 || loading" @click="goPage(page + 1)">Следна</button>
      </footer>
      <dialog ref="videoDialog" class="video-dialog" aria-labelledby="video-title" @close="stopVideo" @click="closeOnBackdrop">
        <template v-if="selectedVideo">
          <div class="player-heading">
            <h2 id="video-title">{{ selectedVideo.title }}</h2>
            <button class="btn" type="button" autofocus @click="videoDialog.close()">Затвори ✕</button>
          </div>
          <video v-if="isUploadedVideo(selectedVideo)" ref="player" :src="selectedVideo.ref" class="player"
            controls autoplay playsinline preload="auto" @error="playbackMessage = 'Видеото не може да се вчита. Обидете се повторно.'"
            @playing="playbackMessage = ''"></video>
          <iframe v-else-if="youtubeId(selectedVideo)" class="player"
            :src="`https://www.youtube.com/embed/${youtubeId(selectedVideo)}?autoplay=1&playsinline=1`"
            :title="selectedVideo.title" allow="autoplay; encrypted-media; picture-in-picture; fullscreen" allowfullscreen></iframe>
          <p v-else role="status">Видеото моментално не е достапно за репродукција.</p>
          <p v-if="playbackMessage" role="status">{{ playbackMessage }}</p>
          <p v-if="selectedVideo.description" class="full-description">{{ selectedVideo.description }}</p>
        </template>
      </dialog>
    </div>
  </section>
</template>

<script setup>
  import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome';
  import { faPlay } from '@fortawesome/free-solid-svg-icons';
  import { nextTick, onBeforeUnmount, onMounted, ref } from 'vue';
  import  api  from '../api';

  const byPrefixAndName = { fas: { play: faPlay } };
  const items = ref([]);
  const page = ref(0);
  const size = ref(12);
  const totalPages = ref(0);
  const totalItems = ref(0);
  const q = ref('');
  const loading = ref(false);
  const loadError = ref(false);
  const videoDialog = ref(null);
  const selectedVideo = ref(null);
  const player = ref(null);
  const playbackMessage = ref('');

  async function openVideo(video) {
    selectedVideo.value = video;
    playbackMessage.value = '';
    await nextTick();
    videoDialog.value.showModal();
    const activePlayer = player.value;
    if (activePlayer) {
      try { await activePlayer.play(); }
      catch (error) {
        if (player.value === activePlayer && error.name === 'NotAllowedError') {
          playbackMessage.value = 'Притиснете ▶ за да го пуштите видеото.';
        }
      }
    }
  }
  function stopVideo() {
    player.value?.pause();
    selectedVideo.value = null;
    playbackMessage.value = '';
  }
  function closeOnBackdrop(event) {
    if (event.target !== videoDialog.value) return;
    const rect = videoDialog.value.getBoundingClientRect();
    if (event.clientX < rect.left || event.clientX > rect.right || event.clientY < rect.top || event.clientY > rect.bottom) videoDialog.value.close();
  }
  function youtubeId(video) {
    if ((video.source || '').toUpperCase() !== 'YOUTUBE') return '';
    const value = (video.ref || '').trim();
    if (/^[\w-]{11}$/.test(value)) return value;
    try {
      const url = new URL(value);
      const host = url.hostname.toLowerCase();
      let id = '';
      if (host === 'youtu.be') id = url.pathname.slice(1);
      if (['youtube.com', 'www.youtube.com', 'm.youtube.com'].includes(host)) {
        id = url.searchParams.get('v') || url.pathname.match(/^\/(?:embed|shorts)\/([^/]+)/)?.[1] || '';
      }
      return /^[\w-]{11}$/.test(id) ? id : '';
    } catch { return ''; }
  }

  async function load() {
    loading.value = true;
    loadError.value = false;
    try {
      const { data } = await api.get('/media/videos', {
        params: { page: page.value, size: size.value, q: q.value.trim() },
      });
      items.value = data.content ?? [];
      totalPages.value = data.totalPages ?? 0;
      totalItems.value = data.totalElements ?? items.value.length;
    } catch (error) {
      loadError.value = true;
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
    return `${minutes}:${String(rest).padStart(2, '0')}`;
  }

  function sourceLabel(source) {
    return (source || '').toUpperCase() === 'YOUTUBE' ? 'YouTube' : 'ЕкоСреќа';
  }

  function thumbnail(video) {
    if (youtubeId(video)) {
      return `https://img.youtube.com/vi/${youtubeId(video)}/hqdefault.jpg`;
    }
    return video.thumbnailUrl || 'https://placehold.co/600x400?text=Video';
  }

  function isUploadedVideo(video) {
    return (video.source || '').toUpperCase() === 'UPLOAD' && Boolean(video.ref);
  }

  onMounted(load);
  onBeforeUnmount(() => {
    clearTimeout(timer);
    player.value?.pause();
  });
</script>

<style scoped>
.media-page { color: #183d2a; padding: 2rem 0 5rem; }
.container { max-width: 1180px; margin: 0 auto; padding: 0 24px; }
.media-hero { align-items: end; background: linear-gradient(115deg, #102817 0%, #24552d 54%, #1b2a1b 100%); border-radius: 8px; color: #fff; display: grid; gap: 1.5rem; grid-template-columns: minmax(0, 1fr) 220px; isolation: isolate; margin-bottom: 1.25rem; min-height: 260px; overflow: hidden; padding: clamp(1.5rem, 4vw, 2.5rem); position: relative; }
.media-hero::after { background: rgba(255,255,255,0.14); border: 1px solid rgba(255,255,255,0.22); border-radius: 50%; content: ''; height: 150px; position: absolute; right: 300px; top: -34px; width: 150px; z-index: -1; }
.eyebrow { font-size: 0.78rem; font-weight: 900; letter-spacing: 0.08em; margin: 0 0 0.55rem; text-transform: uppercase; }
.media-hero h1 { font-size: clamp(2.2rem, 6vw, 4.4rem); font-weight: 900; letter-spacing: 0; line-height: 0.98; margin: 0 0 0.75rem; }
.media-hero p { line-height: 1.7; margin: 0; max-width: 640px; opacity: 0.92; }
.hero-count { background: rgba(255, 255, 255, 0.92); border: 1px solid rgba(255, 255, 255, 0.5); border-radius: 8px; color: #1b2a1b; overflow: hidden; padding: 1rem; position: relative; }
.hero-count-icon { color: rgba(46, 125, 50, 0.16); font-size: 3.2rem; position: absolute; right: 0.8rem; top: 0.65rem; }
.hero-count strong { display: block; font-size: 3rem; font-weight: 900; line-height: 1; }
.hero-count span { color: #2e7d32; font-size: 0.78rem; font-weight: 900; text-transform: uppercase; }
.toolbar { display: flex; align-items: center; justify-content: space-between; gap: 1rem; padding: 1.75rem 0; }
.library-title { margin: 0; font-size: 1.15rem; font-weight: 750; }
.toolbar-controls { display: flex; gap: .6rem; }
.control, .btn { border: 1px solid #ccdad0; border-radius: 10px; background: #fff; color: #244c32; padding: .7rem 1rem; min-height: 44px; font-size: .9rem; }
.search-control { width: 260px; }
.control:focus-visible, .btn:focus-visible, .video-card:focus-visible { outline: 3px solid #5d985d; outline-offset: 3px; }
.btn:hover { background: #edf5e9; }
.btn:disabled { opacity: .45; cursor: not-allowed; }
.grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 2rem; }
.card { background: #fff; border: 1px solid #dfE7df; border-radius: 16px; overflow: hidden; padding: 0; min-width: 0; }
.video-card { cursor: pointer; transition: box-shadow .25s ease, border-color .25s ease; }
.thumb-wrap { aspect-ratio: 16 / 9; position: relative; background: #173729; overflow: hidden; }
.thumb-wrap::after { content: ''; position: absolute; inset: 0; background: linear-gradient(transparent 45%, #081a15a6); pointer-events: none; }
.thumb { display: block; width: 100%; height: 100%; object-fit: cover; transition: transform .45s ease; }
.play-overlay { position: absolute; z-index: 1; top: 50%; left: 50%; transform: translate(-50%, -50%); display: grid; place-items: center; width: 66px; height: 66px; border-radius: 50%; background: #ffffffeb; color: #234d31; box-shadow: 0 4px 24px #0002; transition: transform .3s cubic-bezier(.2,1.4,.4,1), background .2s ease; }
.play-overlay svg { margin-left: 4px; font-size: 23px; }
.watch-label { position: absolute; bottom: 16px; left: 20px; z-index: 1; color: #fff; font-size: .8rem; font-weight: 700; }
.duration { position: absolute; bottom: 13px; right: 16px; z-index: 1; background: #102519e0; color: #fff; border-radius: 5px; padding: 4px 8px; font-size: .78rem; font-variant-numeric: tabular-nums; }
.card-body { padding: 1.25rem 1.4rem 1.5rem; }
.meta-top { display: flex; justify-content: space-between; align-items: center; margin-bottom: .65rem; }
.chip { color: #51704f; font-size: .7rem; font-weight: 800; letter-spacing: .08em; text-transform: uppercase; }
.custom-icon { font-size: 1rem; }
.title { margin: 0 0 .5rem; color: #183d2a; font-size: 1.25rem; font-weight: 750; line-height: 1.4; overflow-wrap: anywhere; }
.content { margin: 0; font-size: .92rem; line-height: 1.65; color: #667168; }
@media (hover: hover) { .video-card:hover { border-color: #99b99c; box-shadow: 0 12px 30px #173d2912; } .video-card:hover .thumb { transform: scale(1.035); } .video-card:hover .play-overlay { transform: translate(-50%, -50%) scale(1.12); background: #dcedce; } }
.pager { display: flex; align-items: center; justify-content: center; gap: 1rem; margin-top: 2.5rem; font-size: .85rem; }
.empty { padding: 4rem 1rem; text-align: center; border: 1px dashed #c2d4c3; border-radius: 16px; background: #f5f8f2; }
.empty h2 { font-size: 1.3rem; margin: 1rem 0 .6rem; }
.empty p { color: #667168; }
.empty-icon { font-size: 2rem; color: #689464; }
.skeleton { padding-bottom: 1.5rem; }
.skeleton .thumb { aspect-ratio: 16 / 9; height: auto; background: #dce6db; }
.skeleton .line { height: 14px; background: #e8eee6; margin: 1rem 1.4rem 0; border-radius: 4px; }
.w60 { width: 60%; } .w90 { width: 85%; }
.video-dialog { width: min(1060px, 95vw); max-height: 92vh; padding: 0; overflow: auto; border: 1px solid #3c5144; border-radius: 16px; background: #101d16; color: #f2f7ef; box-shadow: 0 24px 90px #0006; }
.video-dialog::backdrop { background: #06130bdb; backdrop-filter: blur(5px); }
.player-heading { display: flex; align-items: center; justify-content: space-between; gap: 1rem; padding: 1.2rem 1.5rem; }
.player-heading h2 { color: #f2f7ef; margin: 0; font-size: 1.2rem; line-height: 1.4; overflow-wrap: anywhere; }
.player-heading .btn { flex-shrink: 0; background: #23362a; color: #fff; border-color: #425549; }
.player { display: block; width: 100%; aspect-ratio: 16 / 9; max-height: 65vh; border: 0; background: #000; }
.video-dialog p { margin: 0; padding: 1.25rem 1.5rem; color: #c1d0c4; line-height: 1.7; }
.full-description { white-space: pre-wrap; }
@media (max-width: 760px) { .container { padding: 0 16px; } .media-hero { grid-template-columns: 1fr; } .media-hero::after { display: none; } .toolbar { align-items: stretch; flex-direction: column; } .toolbar-controls { width: 100%; } .search-control { width: 100%; min-width: 0; } .grid { grid-template-columns: 1fr; gap: 1.25rem; } .player-heading { padding: 1rem; } .player-heading h2 { font-size: 1rem; } .pager { flex-wrap: wrap; } }
@media (prefers-reduced-motion: reduce) { .video-card, .thumb, .play-overlay { transition: none; } }
</style>
