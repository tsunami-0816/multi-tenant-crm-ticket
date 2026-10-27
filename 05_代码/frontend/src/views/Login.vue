<template>
  <el-card style="max-width:400px;margin:100px auto">
    <h2>登录</h2>
    <el-form :model="form" @submit.prevent="login">
      <el-form-item><el-input v-model="form.username" placeholder="用户名"/></el-form-item>
      <el-form-item><el-input v-model="form.password" type="password" placeholder="密码"/></el-form-item>
      <el-button type="primary" @click="login">登录</el-button>
    </el-form>
  </el-card>
</template>

<script setup>
import { reactive } from 'vue'
import { useRouter } from 'vue-router'
import api from '../api'
import { ElMessage } from 'element-plus'

const form = reactive({ username: '', password: '' })
const router = useRouter()

async function login() {
  const res = await api.post('/auth/login', form)
  if (res.code === 0) {
    localStorage.setItem('token', res.data.token)
    localStorage.setItem('tenantId', res.data.tenantId)
    router.push('/customers')
  } else {
    ElMessage.error(res.message)
  }
}
</script>
