<template>
  <section class="eco-section eco-games">
    <div class="container">
      <header class="games-header">
        <h2 class="eco-section-title">Забавни игри</h2>

        <!-- Desktop tools -->
        <div class="tools desktop-only">
          <input v-model="q" @input="debouncedSearch" type="search" class="search" placeholder="Пребарај по наслов..." />
          <select v-model="difficulty" @change="goPage(0)" class="select">
            <option value="">Сите тежини</option>
            <option value="EASY">Easy</option>
            <option value="MEDIUM">Medium</option>
            <option value="HARD">Hard</option>
          </select>
          <select v-model.number="size" @change="goPage(0)" class="select">
            <option :value="8">8</option>
            <option :value="12">12</option>
            <option :value="24">24</option>
          </select>
        </div>

        <!-- Mobile toggle button -->
        <button class="filters-toggle mobile-only" @click="mobileFiltersOpen = !mobileFiltersOpen" :aria-expanded="mobileFiltersOpen">
          Филтри
          <span class="chev" :class="{open: mobileFiltersOpen}">▾</span>
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
            <span>Тежина</span>
            <select v-model="difficulty" class="select">
              <option value="">Сите тежини</option>
              <option value="EASY">Easy</option>
              <option value="MEDIUM">Medium</option>
              <option value="HARD">Hard</option>
            </select>
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
        <article v-for="g in items" :key="g.id" class="card">
          <div class="thumb-wrap">
            <img :src="g.thumbnailUrl || 'https://placehold.co/600x400?text=Game'" :alt="g.title" class="thumb" loading="lazy" />
            <span class="badge badge-diff">{{ g.difficulty }}</span>
          </div>
          <div class="body">
            <h3 class="title" :title="g.title">{{ g.title }}</h3>
            <p class="desc">{{ truncate(g.description, 120) }}</p>
            <details class="rules" v-if="g.rules">
              <summary>Правила</summary>
              <p>{{ truncate(g.rules, 200) }}</p>
            </details>
          </div>
        </article>
      </div>

      <div v-if="!loading && items.length===0" class="empty">
        <p>Нема резултати.</p>
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
const difficulty = ref('')
const loading = ref(false)

const mobileFiltersOpen = ref(false)

async function load() {
  loading.value = true
  try {
    const url = new URL('/api/games', window.location.origin)
    url.searchParams.set('page', page.value)
    url.searchParams.set('size', size.value)
    if (q.value.trim()) url.searchParams.set('q', q.value.trim())
    if (difficulty.value) url.searchParams.set('difficulty', difficulty.value)

    const res = await fetch(url, { headers: { Accept: 'application/json' } })
    if (!res.ok) throw new Error('HTTP ' + res.status)
    const data = await res.json()
    items.splice(0, items.length, ...(data.content ?? []))
    totalPages.value = data.totalPages ?? 0
  } catch (e) {
    console.error('Load games error:', e)
    items.splice(0, items.length); totalPages.value = 0
  } finally { loading.value = false }
}

function goPage(p){ if (p<0) p=0; page.value=p; load() }
let t=null; function debouncedSearch(){ page.value=0; clearTimeout(t); t=setTimeout(load,300) }
function truncate(s,n=120){ if(!s) return ''; return s.length>n ? s.slice(0,n-1)+'…' : s }

/* Mobile panel helpers */
function applyMobile(){
  page.value = 0
  mobileFiltersOpen.value = false
  load()
}
function resetMobile(){
  q.value = ''; difficulty.value = ''; size.value = 12
  applyMobile()
}

onMounted(load)
</script>

<style scoped>
.eco-section { padding: clamp(24px, 3vw, 40px) 0; }
.container { max-width: 1120px; margin: 0 auto; padding: 0 16px; }

/* Header */
.games-header { display:flex; align-items:center; justify-content:space-between; gap:12px; margin-bottom:16px; }
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
  display:grid; gap:10px; margin: 8px 0 14px;
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

.thumb-wrap{ position:relative; aspect-ratio:4/3; background:#111; }
.thumb{ width:100%; height:100%; object-fit:cover; display:block; }
.badge-diff{ position:absolute; top:8px; left:8px; background:#ffffffdb; color:#111; border-radius:999px; padding:4px 10px; font-weight:700; }

.body{ padding:12px; display:grid; gap:6px; }
.title{ margin:0; font-size:1rem; font-weight:700; }
.desc{ margin:0; color:#4b5563; font-size:.94rem; min-height:2.6em; }
.rules summary{ cursor:pointer; color:#2563eb; }
.rules p{ margin:.3rem 0 0; color:#374151; }

.pager{ margin-top:18px; display:flex; gap:10px; justify-content:center; align-items:center; }
.btn:disabled{ opacity:.5; cursor:not-allowed; }
.pageinfo{ color:#6b7280; }

/* Skeletons */
.skeleton .thumb{ background:#e5e7eb; }
.skeleton .line{ height:10px; background:#e5e7eb; border-radius:8px; margin:6px 0; }
.skeleton .w60{ width:60%; } .skeleton .w90{ width:90%; }

/* Responsive switches */
@media (max-width: 768px){
  .desktop-only{ display:none; }
  .mobile-only{ display:block; }
}
</style>
