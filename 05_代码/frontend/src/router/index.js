import { createRouter, createWebHistory } from 'vue-router'
import Login from '../views/Login.vue'
import CustomerList from '../views/CustomerList.vue'
import TicketList from '../views/TicketList.vue'

export default createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/login', component: Login },
    { path: '/customers', component: CustomerList },
    { path: '/tickets', component: TicketList },
    { path: '/', redirect: '/customers' }
  ]
})
