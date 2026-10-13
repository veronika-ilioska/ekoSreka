<template>
  <div>
    <div v-if="editable" class="editor-tools">
      <p>Прикачи една слика со двете слики за споредба. Кликни на секоја разлика што играчот треба да ја најде.</p>
      <label>Големина на кругот <input v-model.number="radius" type="range" min="0.01" max="0.12" step="0.005" /></label>
      <button type="button" :disabled="!spots.length" @click="$emit('update:spots', spots.slice(0, -1))">Отстрани последен круг</button>
      <p>Означени разлики: {{ spots.length }}. Кликни на означен круг за да го отстраниш.</p>
    </div>
    <div class="picture" :class="{ interactive: editable || playable }" tabindex="0"
      :aria-label="editable ? 'Означи разлики на сликата' : 'Најди ги разликите. Користи ги стрелките и Enter или кликни на сликата.'"
      @click="clickPicture" @keydown="moveCursor">
      <img :src="src" :alt="alt" draggable="false" @load="ready = true" @error="ready = false; failed = true" />
      <span v-for="entry in visibleSpots" :key="entry.index" class="spot" :style="spotStyle(entry.spot)">{{ entry.index + 1 }}</span>
      <span v-if="keyboard && ready" class="cursor" :style="{ left: `${cursor.x * 100}%`, top: `${cursor.y * 100}%` }">+</span>
    </div>
    <p v-if="failed" role="alert">Сликата не може да се вчита.</p>
    <template v-if="playable && !editable">
      <p aria-live="polite">{{ complete ? 'Браво! Ги најде сите разлики!' : `Пронајдени разлики: ${found.length} / ${spots.length}` }} {{ feedback }}</p>
      <button type="button" @click="restart">Играј повторно</button>
    </template>
    <div v-if="complete" class="confetti" aria-hidden="true">
      <i v-for="n in 65" :key="n" :style="{ left: `${(n * 37) % 100}%`, background: colors[n % colors.length], animationDelay: `${(n % 9) * .12}s`, transform: `rotate(${n * 31}deg)` }" />
    </div>
  </div>
</template>

<script setup>
import { computed, ref, watch } from 'vue';
import { findSpot } from '../utils/differenceGame';
const props = defineProps({ src: String, alt: String, spots: { type: Array, default: () => [] }, editable: Boolean, playable: Boolean });
const emit = defineEmits(['update:spots']);
const radius = ref(.035);
const found = ref([]);
const feedback = ref('');
const ready = ref(false);
const failed = ref(false);
const keyboard = ref(false);
const cursor = ref({ x: .5, y: .5 });
const colors = ['#2e7d32', '#fbbf24', '#ec4899', '#38bdf8', '#a78bfa'];
const complete = computed(() => props.playable && !props.editable && props.spots.length > 0 && found.value.length === props.spots.length);
const visibleSpots = computed(() => props.spots.map((spot, index) => ({ spot, index })).filter(entry => props.editable || found.value.includes(entry.index)));
function spotStyle(spot) { return { left: `${spot.x * 100}%`, top: `${spot.y * 100}%`, width: `${spot.radius * 200}%` }; }
function restart() { found.value = []; feedback.value = ''; }
watch(() => props.src, () => { restart(); ready.value = false; failed.value = false; });
function choose(x, y, rect) {
  if (!ready.value || (!props.editable && !props.playable) || complete.value) return;
  const index = findSpot(props.spots, x, y, rect.width, rect.height);
  if (props.editable) {
    if (index >= 0) emit('update:spots', props.spots.filter((_, i) => i !== index));
    else if (props.spots.length < 100) emit('update:spots', [...props.spots, { x, y, radius: radius.value }]);
  } else if (index >= 0) {
    if (!found.value.includes(index)) { found.value.push(index); feedback.value = 'Точно!'; }
    else feedback.value = 'Веќе ја најде оваа разлика.';
  } else feedback.value = 'Обиди се повторно.';
}
function clickPicture(event) {
  keyboard.value = false;
  const rect = event.currentTarget.getBoundingClientRect();
  choose((event.clientX - rect.left) / rect.width, (event.clientY - rect.top) / rect.height, rect);
}
function moveCursor(event) {
  if (!['ArrowLeft', 'ArrowRight', 'ArrowUp', 'ArrowDown', 'Enter', ' '].includes(event.key)) return;
  event.preventDefault(); keyboard.value = true;
  const step = event.shiftKey ? .005 : .02;
  if (event.key === 'ArrowLeft') cursor.value.x = Math.max(0, cursor.value.x - step);
  if (event.key === 'ArrowRight') cursor.value.x = Math.min(1, cursor.value.x + step);
  if (event.key === 'ArrowUp') cursor.value.y = Math.max(0, cursor.value.y - step);
  if (event.key === 'ArrowDown') cursor.value.y = Math.min(1, cursor.value.y + step);
  if (event.key === 'Enter' || event.key === ' ') choose(cursor.value.x, cursor.value.y, event.currentTarget.getBoundingClientRect());
}
</script>

<style scoped>
.picture { position: relative; width: 100%; line-height: 0; overflow: hidden; }
.picture img { display: block; width: 100%; height: auto; user-select: none; }
.interactive { cursor: crosshair; touch-action: manipulation; }
.picture:focus-visible { outline: 3px solid #2563eb; outline-offset: 3px; }
.spot { position: absolute; aspect-ratio: 1; border: 3px solid #16a34a; background: #22c55e22; border-radius: 50%; transform: translate(-50%, -50%); pointer-events: none; display: grid; place-items: center; color: #fff; text-shadow: 0 1px 3px #000; font-weight: bold; box-sizing: border-box; }
.cursor { position: absolute; color: #fff; text-shadow: 0 0 3px #000; font-size: 28px; pointer-events: none; transform: translate(-50%, -50%); }
.editor-tools { margin-bottom: 1rem; line-height: 1.6; }
button { padding: .5rem 1rem; border: 1px solid #b7d7bd; border-radius: 6px; background: #f0fdf4; color: #166534; }
.confetti { position: fixed; inset: 0; pointer-events: none; overflow: hidden; z-index: 2000; }
.confetti i { position: absolute; top: -20px; width: 10px; height: 18px; animation: fall 3s ease-in forwards; }
@keyframes fall { to { translate: 40px 110vh; rotate: 600deg; } }
@media (prefers-reduced-motion: reduce) { .confetti { display: none; } }
</style>
