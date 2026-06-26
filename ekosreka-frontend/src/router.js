import { createRouter, createWebHistory } from 'vue-router';

export default createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', component: () => import('./pages/Home.vue') },
    { path: '/login', component: () => import('./pages/Login.vue') },
    { path: '/signup', component: () => import('./pages/Signup.vue') },
    { path: '/profile', component: () => import('./pages/Profile.vue') },
    { path: '/settings', component: () => import('./pages/Settings.vue') },
    { path: '/admin', component: () => import('./pages/Admin.vue') },
    { path: '/news', component: () => import('./pages/News.vue') },
    { path: '/news/:id', component: () => import('./pages/NewsDetails.vue') },
    { path: '/quizzes', component: () => import('./pages/Quizzes.vue') },
    { path: '/fun', component: () => import('./pages/Fun.vue') },
    { path: '/about', component: () => import('./pages/About.vue') },
    { path: '/shop', component: () => import('./pages/Shop.vue') },
    { path: '/quizzes/:id', component: () => import('./pages/QuizRun.vue') },
    {
      path: '/photos',
      name: 'photos',
      component: () => import('./pages/PhotosPage.vue'), // прилагоди патека
    },
    { path: '/photos/:id', component: () => import('./pages/PhotoDetails.vue') },
    { path: '/videos', component: () => import('./pages/VideosPage.vue') },
    { path: '/games', component: () => import('./pages/GamesPage.vue') },
    { path: '/horoscope', component: () => import('./pages/HoroscopePage.vue') },
  ],
});
