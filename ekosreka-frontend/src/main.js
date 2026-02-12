import 'bootstrap/dist/css/bootstrap.min.css';
import 'bootstrap/dist/js/bootstrap.bundle.min.js';

import { createApp } from 'vue';
import './style.css';
import App from './App.vue';

import router from './router.js';
import { createPinia } from 'pinia';

createApp(App).use(createPinia()).use(router).mount('#app');
