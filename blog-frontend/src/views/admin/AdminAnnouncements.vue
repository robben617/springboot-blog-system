<template>
  <div class="admin-announcements">
    <div class="page-header">
      <h2 class="page-title">公告管理</h2>
      <el-button type="primary" @click="openCreate">
        <el-icon><Plus /></el-icon>
        新增公告
      </el-button>
    </div>

    <div class="card">
      <el-table :data="announcements" v-loading="loading">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="title" label="标题" min-width="220" show-overflow-tooltip />
        <el-table-column prop="content" label="内容" min-width="260" show-overflow-tooltip />
        <el-table-column prop="createTime" label="发布时间" width="170">
          <template #default="{ row }">{{ formatDate(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="openEdit(row)">编辑</el-button>
            <el-button link type="danger" size="small" @click="removeAnnouncement(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="!loading && announcements.length === 0" description="还没有公告" />
    </div>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑公告' : '新增公告'" width="520px">
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入公告标题" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="6"
            placeholder="请输入公告内容"
            maxlength="1000"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { adminApi } from '@/api/admin'
import { commonApi } from '@/api/common'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'

const announcements = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const submitting = ref(false)
const isEdit = ref(false)
const editId = ref(null)
const formRef = ref(null)

const form = reactive({
  title: '',
  content: ''
})

const rules = {
  title: [{ required: true, message: '请输入公告标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入公告内容', trigger: 'blur' }]
}

async function loadAnnouncements() {
  loading.value = true
  try {
    announcements.value = (await commonApi.getAnnouncements()) || []
  } catch (e) {
    announcements.value = []
  } finally {
    loading.value = false
  }
}

function openCreate() {
  isEdit.value = false
  editId.value = null
  form.title = ''
  form.content = ''
  dialogVisible.value = true
  formRef.value?.clearValidate()
}

function openEdit(row) {
  isEdit.value = true
  editId.value = row.id
  form.title = row.title || ''
  form.content = row.content || ''
  dialogVisible.value = true
  formRef.value?.clearValidate()
}

async function submit() {
  try {
    await formRef.value.validate()
  } catch (e) {
    return
  }
  submitting.value = true
  try {
    const payload = { title: form.title.trim(), content: form.content.trim() }
    if (isEdit.value) {
      await adminApi.updateAnnouncement(editId.value, payload)
      ElMessage.success('公告已更新')
    } else {
      await adminApi.createAnnouncement(payload)
      ElMessage.success('公告已发布')
    }
    dialogVisible.value = false
    loadAnnouncements()
  } catch (e) {
    // 错误已由拦截器提示
  } finally {
    submitting.value = false
  }
}

async function removeAnnouncement(row) {
  try {
    await ElMessageBox.confirm(`确定删除公告「${row.title}」吗？`, '删除公告', {
      type: 'warning',
      confirmButtonText: '确定删除',
      cancelButtonText: '取消'
    })
  } catch (e) {
    return
  }
  try {
    await adminApi.deleteAnnouncement(row.id)
    ElMessage.success('公告已删除')
    loadAnnouncements()
  } catch (e) {
    // 错误已由拦截器提示
  }
}

function formatDate(dateStr) {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}

onMounted(loadAnnouncements)
</script>

<style scoped>
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 18px;
}

.page-title {
  margin-bottom: 0;
}

.card {
  padding: 8px 18px 16px;
}
</style>
