<template>
  <section class="mb-4">
    <div class="d-flex align-items-center justify-content-between">
      <div class="btn-group flex-wrap">
        <button
          class="btn btn-sm btn-outline-success"
          :class="{ active: !category }"
          @click="setCategory(undefined)"
        >
          Сите
        </button>
        <button
          v-for="item in categories"
          :key="item"
          class="btn btn-sm btn-outline-success"
          :class="{ active: category === item }"
          @click="setCategory(item)"
        >
          {{ item }}
        </button>
      </div>
    </div>
  </section>

  <section>
    <div v-if="loading" class="text-center py-5">Се вчитува…</div>

    <div v-else class="row g-4">
      <div v-for="n in items" :key="n.id" class="col-12 col-sm-6 col-lg-4">
        <NewsCard :item="n" />
      </div>
    </div>

    <div class="d-flex justify-content-center gap-2 mt-4" v-if="total > size">
      <button
        class="btn btn-outline-success"
        :disabled="page === 0"
        @click="
          page--;
          load();
        "
      >
        Претходна
      </button>
      <button
        class="btn btn-outline-success"
        :disabled="(page + 1) * size >= total"
        @click="
          page++;
          load();
        "
      >
        Следна
      </button>
    </div>
  </section>
</template>
<script setup>
  import { onMounted, ref, watch } from 'vue';
  import { useRoute, useRouter } from 'vue-router';
  import { api } from '../api';
  import NewsCard from '../components/NewsCard.vue';

  const route = useRoute();
  const router = useRouter();

  const items = ref([]);
  const loading = ref(false);
  const total = ref(0);
  const page = ref(0);
  const size = ref(12);

  const categories = ['Рециклирање', 'Енергија', 'Природа', 'Клима'];

  // читај ја категоријата од URL
  const category = ref(route.query.category ? String(route.query.category) : undefined);

  async function load() {
    loading.value = true;
    const params = { page: page.value, size: size.value };
    if (category.value) params.category = category.value;
    try {
      const { data } = await api.get('/news', { params });
      items.value = data?.content ?? [];
      total.value = data?.totalElements ?? 0;
    } finally {
      loading.value = false;
    }
  }

  function setCategory(cat) {
    category.value = cat;
    page.value = 0;
    // ажурирај URL query без reload
    router.replace({ query: cat ? { category: cat } : {} });
    load();
  }

  // реагирај ако корисникот директно смени query во URL
  watch(
    () => route.query.category,
    (q) => {
      category.value = q ? String(q) : undefined;
      page.value = 0;
      load();
    }
  );

  onMounted(load);
</script>
