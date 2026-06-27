<template>
  <section class="photo-details">
    <RouterLink class="back-link" to="/photos">Назад кон фотографии</RouterLink>

    <div v-if="loading" class="state">Фотографијата се вчитува...</div>
    <div v-else-if="error" class="alert alert-danger">{{ error }}</div>

    <article v-else-if="photo" class="photo-shell">
      <img :src="photo.url || photo.thumbnailUrl" :alt="photo.title" class="photo-image" />
      <div class="photo-body">
        <div class="meta-top">
          <span v-for="tag in tags" :key="tag" class="chip">{{ tag }}</span>
          <time class="chip chip-date" :datetime="photo.createdAt">{{ postedAt }}</time>
        </div>
        <h1>{{ photo.title }}</h1>
        <p class="description">{{ shortDescription }}</p>
      </div>
    </article>
  </section>
</template>

<script setup>
  import { computed, onMounted, ref } from 'vue';
  import { useRoute } from 'vue-router';
  import  api  from '../api';

  const route = useRoute();
  const photo = ref(null);
  const loading = ref(false);
  const error = ref('');

  const postedAt = computed(() => {
    if (!photo.value?.createdAt) return 'Непознато време';
    return new Date(photo.value.createdAt).toLocaleString('mk-MK', {
      dateStyle: 'medium',
      timeStyle: 'short',
    });
  });

  const shortDescription = computed(() => {
    const text = photo.value?.description || 'Нема додаден опис за оваа фотографија.';
    return text.length > 260 ? `${text.slice(0, 259)}...` : text;
  });

  const tags = computed(() => {
    if (!photo.value?.tags) return [];
    return photo.value.tags
      .split(',')
      .map((tag) => tag.trim())
      .filter(Boolean)
      .slice(0, 4);
  });

  async function loadPhoto() {
    loading.value = true;
    error.value = '';
    try {
      const { data } = await api.get(`/media/photos/${route.params.id}`);
      photo.value = data;
    } catch (err) {
      if (err.response?.status === 404) {
        await loadFromList();
      } else {
        error.value = 'Фотографијата не може да се вчита.';
      }
    } finally {
      loading.value = false;
    }
  }

  async function loadFromList() {
    const { data } = await api.get('/media/photos', { params: { page: 0, size: 1000 } });
    const match = (data.content ?? []).find((item) => String(item.id) === String(route.params.id));
    if (match) {
      photo.value = match;
      return;
    }
    error.value = 'Фотографијата не е пронајдена.';
  }

  onMounted(loadPhoto);
</script>

<style scoped>
  .photo-details {
    color: #1b2a1b;
    margin: 0 auto;
    max-width: 1040px;
    padding: 1rem 16px 4rem;
  }

  .back-link {
    color: #2e7d32;
    display: inline-flex;
    font-weight: 800;
    margin-bottom: 1rem;
    text-decoration: none;
  }

  .photo-shell {
    background: #fff;
    border: 1px solid rgba(69, 128, 81, 0.14);
    border-radius: 8px;
    box-shadow: 0 8px 24px rgba(27, 42, 27, 0.08);
    overflow: hidden;
  }

  .photo-image {
    aspect-ratio: 16 / 9;
    background: #eaf6ea;
    display: block;
    object-fit: cover;
    width: 100%;
  }

  .photo-body {
    display: grid;
    gap: 1rem;
    padding: clamp(1.25rem, 4vw, 2rem);
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

  h1 {
    color: #1b2a1b;
    font-size: clamp(2rem, 5vw, 3.8rem);
    font-weight: 800;
    line-height: 1.05;
    margin: 0;
  }

  .description {
    color: #374151;
    font-size: 1.08rem;
    line-height: 1.7;
    margin: 0;
    max-width: 760px;
  }

  .state {
    background: #fff;
    border-radius: 8px;
    color: #49624d;
    padding: 2rem;
    text-align: center;
  }

  @media (max-width: 720px) {
    .photo-image {
      aspect-ratio: 4 / 3;
    }
  }
</style>
