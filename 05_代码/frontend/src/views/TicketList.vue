<template>
  <div>
    <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:12px">
      <h2>工单列表</h2>
      <el-button @click="$router.push('/customers')">返回客户</el-button>
    </div>

    <el-form :inline="true" :model="filter" style="margin-bottom:12px">
      <el-form-item label="客户">
        <el-select v-model="filter.customerId" placeholder="选择客户" @change="load">
          <el-option v-for="c in customers" :key="c.id" :label="c.name" :value="c.id"/>
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="createVisible = true" :disabled="!filter.customerId">新建工单</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="tickets" border stripe>
      <el-table-column prop="id" label="ID" width="70"/>
      <el-table-column prop="title" label="标题"/>
      <el-table-column prop="status" label="状态" width="110">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)">{{ row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="priority" label="优先级" width="90"/>
      <el-table-column label="操作" width="280">
        <template #default="{ row }">
          <el-button v-if="row.status === 'OPEN'" size="small" type="primary" @click="transition(row, 'PROCESSING')">开始处理</el-button>
          <el-button v-if="row.status === 'PROCESSING'" size="small" type="success" @click="transition(row, 'RESOLVED')">标记解决</el-button>
          <el-button v-if="row.status === 'RESOLVED'" size="small" type="info" @click="transition(row, 'CLOSED')">关闭</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="createVisible" title="新建工单" width="460px">
      <el-form :model="form" label-width="60px">
        <el-form-item label="标题"><el-input v-model="form.title"/></el-form-item>
        <el-form-item label="描述"><el-input v-model="form.description" type="textarea"/></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submit">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import api from '../api'

const route = useRoute()
const customers = ref([])
const tickets = ref([])
const filter = ref({ customerId: route.query.customerId ? Number(route.query.customerId) : null })
const createVisible = ref(false)
const saving = ref(false)
const form = ref({ title: '', description: '' })

function statusType(s) {
  return { OPEN: 'danger', PROCESSING: 'warning', RESOLVED: 'success', CLOSED: 'info' }[s] || ''
}

async function loadCustomers() {
  const res = await api.get('/customers')
  if (res.code === 0) customers.value = res.data.records || []
}
async function load() {
  if (!filter.value.customerId) { tickets.value = []; return }
  // 简单实现：直接拉全部工单（后端暂未按 customerId 过滤，前端过滤）
  const res = await api.get('/tickets')
  if (res.code === 0) {
    tickets.value = (res.data.records || res.data || []).filter(t => t.customerId === filter.value.customerId)
  }
}
onMounted(async () => { await loadCustomers(); await load() })

async function submit() {
  saving.value = true
  try {
    const res = await api.post('/tickets', { ...form.value, customerId: filter.value.customerId })
    if (res.code === 0) {
      ElMessage.success('工单已创建')
      createVisible.value = false
      form.value = { title: '', description: '' }
      load()
    } else {
      ElMessage.error(res.message || '创建失败')
    }
  } finally { saving.value = false }
}

async function transition(row, to) {
  const res = await api.patch(`/tickets/${row.id}`, { status: to })
  if (res.code === 0) {
    ElMessage.success(`已流转为 ${to}`)
    load()
  } else {
    ElMessage.error(res.message || '流转失败')
  }
}
</script>
