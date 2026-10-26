<template>
  <div class="admin-friend-links">
    <div class="page-header">
      <h2 class="page-title">友链管理</h2>
      <el-button type="primary" @click="openCreate">
        <el-icon><Plus /></el-icon>
        新增友链
      </el-button>
    </div>

    <div class="card">
      <el-table :data="links" v-loading="loading">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="name" label="网站名称" min-width="160" />
        <el-table-column prop="url" label="网站地址" min-width="260" show-overflow-tooltip>
          <template #default="{ row }">
            <a :href="row.url" target="_blank" rel="noopener" class="link-url">{{ row.url }}</a>
          </template>
        </el-table-column>
        <el-table-column prop="sort" label="排序" width="90">
          <template #default="{ row }">{{ row.sort ?? 0 }}</template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170">
          <template #default="{ row }">{{ formatDate(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="openEdit(row)">编辑</el-button>
            <el-button link type="danger" size="small" @click="removeLink(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="!loading && links.length === 0" description="还没有友情链接" />
    </div>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑友链' : '新增友链'" width="460px">
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item label="网站名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入网站名称" maxlength="30" show-word-limit />
        </el-form-item>
        <el-form-item label="网站地址" prop="url">
          <el-input v-model="form.url" placeholder="https://example.com" />
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="form.sort" :min="0" :max="9999" controls-position="right" />
          <span class="form-tip">值越小越靠前</span>
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

const links = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const submitting = ref(false)
const isEdit = ref(false)
const editId = ref(null)
const formRef = ref(null)

const form = reactive({
  name: '',
  url: '',
  sort: 0
})

const rules = {
  name: [{ required: true, message: '请输入网站名称', trigger: 'blur' }],
  url: [
    { required: true, message: '请输入网站地址', trigger: 'blur' },
    {
      pattern: /^https?:\/\/.+/i,
      message: '地址需以 http:// 或 https:// 开头',
      trigger: 'blur'
    }
  ]
}

async function loadLinks() {
  loading.value = true
  try {
    links.value = (await commonApi.getFriendLinks()) || []
  } catch (e) {
    links.value = []
  } finally {
    loading.value = false
  }
}

function openCreate() {
  isEdit.value = false
  editId.value = null
  form.name = ''
  form.url = ''
  form.sort = 0
  dialogVisible.value = true
  formRef.value?.clearValidate()
}

function openEdit(row) {
  isEdit.value = true
  editId.value = row.id
  form.name = row.name || ''
  form.url = row.url || ''
  form.sort = row.sort ?? 0
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
    const payload = { name: form.name.trim(), url: form.url.trim(), sort: form.sort }
    if (isEdit.value) {
      await adminApi.updateFriendLink(editId.value, payload)
      ElMessage.success('友链已更新')
    } else {
      await adminApi.createFriendLink(payload)
      ElMessage.success('友链已新增')
    }
    dialogVisible.value = false
    loadLinks()
  } catch (e) {
    // 错误已由拦截器提示
  } finally {
    submitting.value = false
  }
}

async function removeLink(row) {
  try {
    await ElMessageBox.confirm(`确定删除友链「${row.name}」吗？`, '删除友链', {
      type: 'warning',
      confirmButtonText: '确定删除',
      cancelButtonText: '取消'
    })
  } catch (e) {
    return
  }
  try {
    await adminApi.deleteFriendLink(row.id)
    ElMessage.success('友链已删除')
    loadLinks()
  } catch (e) {
    // 错误已由拦截器提示
  }
}

function formatDate(dateStr) {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

onMounted(loadLinks)
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

.link-url {
  color: var(--color-primary);
}

.link-url:hover {
  text-decoration: underline;
  text-underline-offset: 3px;
}

.form-tip {
  margin-left: 10px;
  color: var(--color-text-4);
  font-size: 12px;
}
</style>
