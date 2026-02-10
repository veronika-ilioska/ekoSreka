<template>
  <HeaderBar />
  <main class="container mt-4">
    <RouterView />
  </main>
  <FooterBar />
</template>

<script setup >
import HeaderBar from './components/HeaderBar.vue';
import FooterBar from './components/FooterBar.vue';
</script>
