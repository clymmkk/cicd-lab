import { createRouter, createWebHashHistory } from 'vue-router';
import HistoryView from '../views/HistoryView.vue';
import OperationsView from '../views/OperationsView.vue';

const routes = [
  {
    path: '/',
    redirect: '/ops'
  },
  {
    path: '/ops',
    name: 'operations',
    component: OperationsView
  },
  {
    path: '/history',
    name: 'history',
    component: HistoryView
  }
];

const router = createRouter({
  history: createWebHashHistory(),
  routes
});

export default router;
