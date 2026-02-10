<template>
  <section class="eco-section eco-videos">
    <div class="container">
      <header class="videos-header">
        <h2 class="eco-section-title">Видеа</h2>

        <!-- Desktop filters -->
        <div class="tools desktop-only">
          <input v-model="q" @input="debouncedSearch" type="search" class="search" placeholder="Пребарај по наслов..." />
          <select v-model.number="size" @change="goPage(0)" class="select">
            <option :value="8">8</option>
            <option :value="12">12</option>
            <option :value="24">24</option>
          </select>
        </div>

        <!-- Mobile toggle -->
        <button class="filters-toggle mobile-only" @click="mobileFiltersOpen = !mobileFiltersOpen" :aria-expanded="mobileFiltersOpen">
          Филтри <span class="chev" :class="{ open: mobileFiltersOpen }">▾</span>
        </button>
      </header>

      <!-- Mobile filters panel -->
      <transition name="slide">
        <form v-if="mobileFiltersOpen" class="filters-panel mobile-only" @submit.prevent="applyMobile">
          <label class="fgroup">
            <span>Пребарај</span>
            <input v-model="q" type="search" class="search" placeholder="Пребарај по наслов..." />
          </label>
          <label class="fgroup">
            <span>По страница</span>
            <select v-model.number="size" class="select">
              <option :value="8">8</option>
              <option :value="12">12</option>
              <option :value="24">24</option>
            </select>
          </label>
          <div class="actions">
            <button type="button" class="btn light" @click="resetMobile">Исчисти</button>
            <button type="submit" class="btn primary">Примени</button>
          </div>
        </form>
      </transition>

      <div class="grid">
        <!-- skeleton -->
        <article v-if="loading && items.length===0" v-for="i in size" :key="'s'+i" class="card skeleton">
          <div class="thumb"></div>
          <div class="body"><div class="line w60"></div><div class="line w90"></div></div>
        </article>

        <!-- items -->
        <article v-for="v in items" :key="v.id" class="card">
          <div class="thumb-wrap">
            <img :src="thumb(v)" :alt="v.title" class="thumb" loading="lazy" />
            <span class="play">▶</span>
          </div>
          <div class="body">
            <h3 class="title" :title="v.title">{{ v.title }}</h3>
            <p class="desc">{{ truncate(v.description, 120) }}</p>
            <div class="meta">
              <span class="tag">{{ (v.source || '').toLowerCase() }}</span>
              <span class="tag" v-if="v.durationSec">{{ fmtDur(v.durationSec) }}</span>
            </div>
          </div>
        </article>
      </div>

      <div v-if="!loading && items.length===0" class="empty">
        <p>Нема резултати за „{{ q }}“.</p>
      </div>

      <footer class="pager" v-if="totalPages>1">
        <button class="btn" :disabled="page===0||loading" @click="goPage(page-1)">« Претходна</button>
        <span class="pageinfo">Страна {{ page+1 }} од {{ totalPages }}</span>
        <button class="btn" :disabled="page>=totalPages-1||loading" @click="goPage(page+1)">Следна »</button>
      </footer>
    </div>
  </section>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'

const items = reactive([])
const page = ref(0)
const size = ref(12)
const totalPages = ref(0)
const q = ref('')
const loading = ref(false)
const mobileFiltersOpen = ref(false)

async function load() {
  loading.value = true
  try {
    const url = new URL('/api/media/videos', window.location.origin)
    url.searchParams.set('page', page.value)
    url.searchParams.set('size', size.value)
    if (q.value.trim()) url.searchParams.set('q', q.value.trim())

    const res = await fetch(url, { headers: { Accept: 'application/json' } })
    if (!res.ok) throw new Error('HTTP ' + res.status)
    const data = await res.json()
    items.splice(0, items.length, ...(data.content ?? []))
    totalPages.value = data.totalPages ?? 0
  } catch (e) {
    console.error('Load videos error:', e)
    items.splice(0, items.length); totalPages.value = 0
  } finally { loading.value = false }
}

function goPage(p){ if (p<0) p=0; page.value=p; load() }
let t=null; function debouncedSearch(){ page.value=0; clearTimeout(t); t=setTimeout(load,300) }

function truncate(s,n=120){ if(!s) return ''; return s.length>n ? s.slice(0,n-1)+'…' : s }
function fmtDur(sec){ const m=Math.floor(sec/60), s=sec%60; return `${m}m ${s}s` }
function thumb(v){
  if ((v.source||'').toUpperCase()==='YOUTUBE' && v.ref) return `https://img.youtube.com/vi/${v.ref}/hqdefault.jpg`
  return v.thumbnailUrl || 'https://placehold.co/600x400?text=Video'
}

/* Mobile helpers */
function applyMobile(){ page.value = 0; mobileFiltersOpen.value = false; load() }
function resetMobile(){ q.value=''; size.value=12; applyMobile() }

onMounted(load)
</script>

<style scoped>
.eco-section { padding: clamp(24px, 3vw, 40px) 0; }
.container { max-width: 1120px; margin: 0 auto; padding: 0 16px; }

.videos-header { display:flex; align-items:center; justify-content:space-between; gap:12px; margin-bottom:16px; }
.tools{ display:flex; gap:10px; align-items:center; }
.search,.select{ padding:10px 12px; border-radius:12px; border:1px solid #e5e7eb; background:#fff; }
.search:focus{ border-color:#60a5fa; box-shadow:0 0 0 3px rgba(96,165,250,.25); }

.desktop-only{ display:flex; }
.mobile-only{ display:none; }

/* Mobile toggle */
.filters-toggle{
  padding:10px 14px; border-radius:10px; border:1px solid #e5e7eb; background:#fff;
}
.filters-toggle .chev{ display:inline-block; transition:transform .2s ease; margin-left:6px; }
.filters-toggle .chev.open{ transform:rotate(180deg); }

/* Mobile panel */
.filters-panel{
  display:grid; gap:10px; margin:8px 0 14px;
  padding:12px; border:1px solid #e5e7eb; border-radius:14px; background:#fafafa;
}
.fgroup{ display:grid; gap:6px; }
.fgroup span{ font-size:.85rem; color:#374151; }
.actions{ display:flex; gap:10px; }
.btn{ padding:10px 14px; border-radius:10px; border:1px solid #e5e7eb; background:#fff; }
.btn.light{ background:#fff; }
.btn.primary{ background:#2563eb; color:#fff; border-color:#1d4ed8; }

/* Transition */
.slide-enter-active,.slide-leave-active{ transition:max-height .2s ease, opacity .2s ease; }
.slide-enter-from,.slide-leave-to{ max-height:0; opacity:0; }
.slide-enter-to,.slide-leave-from{ max-height:320px; opacity:1; }

/* Grid & cards */
.grid{ display:grid; gap:16px; grid-template-columns:repeat(12,1fr); }
.card{ grid-column:span 12; display:grid; grid-template-rows:auto 1fr; overflow:hidden; border-radius:16px; background:#fff; box-shadow:0 6px 18px rgba(0,0,0,.06); transition:transform .2s, box-shadow .2s; }
.card:hover{ transform:translateY(-2px); box-shadow:0 10px 24px rgba(0,0,0,.10); }
@media (min-width:640px){ .card{ grid-column:span 6; } }
@media (min-width:992px){ .card{ grid-column:span 3; } }

.thumb-wrap{ position:relative; aspect-ratio:16/9; background:#111; }
.thumb{ width:100%; height:100%; object-fit:cover; display:block; opacity:.9; }
.play{ position:absolute; inset:auto auto 8px 8px; background:#ffffffdb; color:#111; border-radius:999px; padding:6px 10px; font-weight:700; }

.body{ padding:12px; display:grid; gap:6px; }
.title{ margin:0; font-size:1rem; font-weight:700; }
.desc{ margin:0; color:#4b5563; font-size:.94rem; min-height:2.6em; }
.meta{ display:flex; gap:6px; flex-wrap:wrap; margin-top:2px; }
.tag{ font-size:.75rem; padding:4px 8px; border-radius:999px; background:#eef2ff; color:#3730a3; }

.pager{ margin-top:18px; display:flex; gap:10px; justify-content:center; align-items:center; }
.btn{ padding:10px 14px; border-radius:10px; border:1px solid #e5e7eb; background:#fff; }
.btn:disabled{ opacity:.5; cursor:not-allowed; }
.pageinfo{ color:#6b7280; }

.skeleton .thumb{ background:#e5e7eb; }
.skeleton .line{ height:10px; background:#e5e7eb; border-radius:8px; margin:6px 0; }
.skeleton .w60{ width:60%; } .skeleton .w90{ width:90%; }

/* Responsive switch */
@media (max-width: 768px){
  .desktop-only{ display:none; }
  .mobile-only{ display:block; }
}
</style>
