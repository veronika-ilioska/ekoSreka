<template>
  <RouterLink class="news-card-link" :to="`/news/${item.id}`">
    <article class="eco-card h-100 news-card">
      <img :src="item.coverUrl || '/cover-placeholder.jpg'" class="eco-cover" alt="" />
      <div class="p-3">
        <div class="d-flex justify-content-between align-items-start gap-2 mb-2">
          <span v-if="item.category" class="eco-tag text-uppercase">{{ item.category }}</span>
          <small class="text-muted ms-auto" v-if="item.publishedAt">
            {{ new Date(item.publishedAt).toLocaleDateString() }}
          </small>
        </div>
        <h5 class="mb-2">{{ item.title }}</h5>
        <p class="mb-0 text-muted">
          {{ excerpt }}
        </p>
      </div>
    </article>
  </RouterLink>
</template>

<script setup>
  import { computed } from 'vue';

  const props = defineProps({
    item: {
      type: Object,
      required: true,
      default: () => ({
        id: 0,
        title: '',
        content: '',
        category: '',
        coverUrl: '',
        publishedAt: '',
      }),
    },
  });

  const excerpt = computed(() => {
    const content = props.item.content || '';
    return `${content.slice(0, 140)}${content.length > 140 ? '...' : ''}`;
  });
</script>

<style scoped>
  .news-card-link {
    color: inherit;
    display: block;
    height: 100%;
    text-decoration: none;
  }

  .news-card {
    border-radius: 8px;
    transition:
      box-shadow 0.2s ease,
      transform 0.2s ease;
  }

  .news-card-link:hover .news-card {
    box-shadow: 0 10px 24px rgba(27, 42, 27, 0.18);
    transform: translateY(-3px);
  }
</style>
