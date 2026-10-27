<template>
  <div>
    <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:12px">
      <h2>客户列表</h2>
      <el-button type="primary" @click="dialogVisible = true">新建客户</el-button>
    </div>
    <el-table :data="customers" border stripe>
      <el-table-column prop="id" label="ID" width="80"/>
      <el-table-column prop="name" label="客户名"/>
      <el-table-column prop="contact" label="联系人"/>
      <el-table-column prop="phone" label="电话"/>
      <el-table-column label="操作" width="120">
        <template #default="{ row }">
          <el-button size="small" @click="$router.push(`/tickets?customerId=${row.id}`)">工单</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" title="新建客户" width="420px">
      <el-form :model="form" label-width="60px">
        <el-form-item label="名称"><el-input v-model="form.name"/></el-form-item>
        <el-form-item label="联系人"><el-input v-model="form.contact"/></el-form-item>
        <el-form-item label="电话"><el-input v-model="form.phone"/></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import api from '../api'

const customers = ref([])
const dialogVisible = ref(false)
const saving = ref(false)
const form = ref({ name: '', contact: '', phone: '' })

async function load() {
  const res = await api.get('/customers')
  if (res.code === 0) customers.value = res.data.records || []
}
onMounted(load)

async function submit() {
  saving.value = true
  try {
    const res = await api.post('/customers', form.value)
    if (res.code === 0) {
      ElMessage.success('创建成功')
      dialogVisible.value = false
      form.value = { name: '', contact: '', phone: '' }
      load()
    } else {
      ElMessage.error(res.message || '创建失败')
    }
  } finally { saving.value = false }
}
</script>
