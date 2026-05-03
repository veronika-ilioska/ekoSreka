<template>
  <RouterLink :to="`/news/${item.id}`" class="news-card-link">
    <article class="eco-card h-100">
      <img :src="item.coverUrl || '/cover-placeholder.jpg'" class="eco-cover" alt="" />
      <div class="p-3">
        <div class="d-flex justify-content-between align-items-start gap-2 mb-2">
          <span v-if="item.category" class="eco-tag text-uppercase">{{ item.category }}</span>
          <small class="text-muted ms-auto" v-if="item.publishedAt">{{
            new Date(item.publishedAt).toLocaleDateString()
          }}</small>
        </div>
        <h5 class="mb-2">{{ item.title }}</h5>
        <p class="mb-0 text-muted">
          {{ (item.content || '').slice(0, 140) }}{{ (item.content || '').length > 140 ? '…' : '' }}
        </p>
      </div>
    </article>
  </RouterLink>
</template>
<script setup>
  defineProps({
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
</script>

<style scoped>
.news-card-link {
  text-decoration: none;
  color: inherit;
  transition: all 0.3s ease;
}

.news-card-link:hover {
  transform: translateY(-4px);
}

.news-card-link:hover .eco-card {
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}
</style>

