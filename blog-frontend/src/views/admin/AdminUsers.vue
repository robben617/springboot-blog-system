<template>
  <div class="admin-users">
    <h2 class="page-title">用户管理</h2>

    <div class="card filter-bar">
      <el-input
        v-model="keyword"
        placeholder="搜索用户名或昵称"
        clearable
        style="width: 240px"
        @keyup.enter="handleSearch"
        @clear="handleSearch"
      >
        <template #prefix><el-icon><Search /></el-icon></template>
      </el-input>
      <el-select v-model="statusFilter" placeholder="账号状态" clearable style="width: 140px" @change="handleSearch">
        <el-option label="启用" :value="1" />
        <el-option label="禁用" :value="0" />
      </el-select>
      <el-button type="primary" @click="handleSearch">查询</el-button>
      <el-button @click="handleReset">重置</el-button>
    </div>

    <div class="card">
      <el-table :data="users" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="username" label="用户名" width="140" />
        <el-table-column prop="nickname" label="昵称" width="140" />
        <el-table-column prop="email" label="邮箱" min-width="180" show-overflow-tooltip>
          <template #default="{ row }">{{ row.email || '—' }}</template>
        </el-table-column>
        <el-table-column prop="role" label="角色" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.role === 1" type="danger" size="small">管理员</el-tag>
            <el-tag v-else type="info" size="small">普通用户</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.status === 1" type="success" size="small">启用</el-tag>
            <el-tag v-else type="warning" size="small">禁用</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="注册时间" width="170">
          <template #default="{ row }">{{ formatDate(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="230" fixed="right">
          <template #default="{ row }">
            <el-button
              link
              :type="row.status === 1 ? 'warning' : 'success'"
              size="small"
              @click="toggleStatus(row)"
            >
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-button link type="primary" size="small" @click="resetPassword(row)">重置密码</el-button>
            <el-button
              link
              type="danger"
              size="small"
              :disabled="row.role === 1"
              @click="removeUser(row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-if="total > 0"
        v-model:current-page="pageNum"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="loadUsers"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { adminApi } from '@/api/admin'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'

const users = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const loading = ref(false)
const keyword = ref('')
const statusFilter = ref(null)

async function loadUsers() {
  loading.value = true
  try {
    const data = await adminApi.getUsers({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      keyword: keyword.value.trim() || undefined,
      status: statusFilter.value === null ? undefined : statusFilter.value
    })
    users.value = data.list || []
    total.value = data.total || 0
  } catch (e) {
    users.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pageNum.value = 1
  loadUsers()
}

function handleReset() {
  keyword.value = ''
  statusFilter.value = null
  handleSearch()
}

function handleSizeChange(size) {
  pageSize.value = size
  pageNum.value = 1
  loadUsers()
}

async function toggleStatus(row) {
  const next = row.status === 1 ? 0 : 1
  try {
    await adminApi.updateUserStatus(row.id, next)
    ElMessage.success(next === 1 ? '已启用该账号' : '已禁用该账号')
    loadUsers()
  } catch (e) {
    // 错误已由拦截器提示
  }
}

async function resetPassword(row) {
  try {
    await ElMessageBox.confirm(
      `确定将用户「${row.nickname || row.username}」的密码重置为系统初始密码 abc123456 吗？`,
      '重置密码',
      { type: 'warning', confirmButtonText: '确定重置', cancelButtonText: '取消' }
    )
  } catch (e) {
    return
  }
  try {
    await adminApi.resetUserPassword(row.id)
    ElMessage.success('密码已重置为 abc123456')
  } catch (e) {
    // 错误已由拦截器提示
  }
}

async function removeUser(row) {
  try {
    await ElMessageBox.confirm(
      `删除用户「${row.nickname || row.username}」会同时删除其全部文章与评论，确定继续吗？`,
      '删除用户',
      { type: 'warning', confirmButtonText: '确定删除', cancelButtonText: '取消' }
    )
  } catch (e) {
    return
  }
  try {
    await adminApi.deleteUser(row.id)
    ElMessage.success('用户已删除')
    if (users.value.length === 1 && pageNum.value > 1) {
      pageNum.value -= 1
    }
    loadUsers()
  } catch (e) {
    // 错误已由拦截器提示
  }
}

function formatDate(dateStr) {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleString('zh-CN')
}

onMounted(loadUsers)
</script>

<style scoped>
.filter-bar {
  display: flex;
  gap: 12px;
  align-items: center;
  margin-bottom: 16px;
  padding: 14px 18px;
  flex-wrap: wrap;
}

.card {
  padding: 8px 18px 16px;
}
</style>
