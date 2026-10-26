<template>
  <div class="admin-categories">
    <div class="page-header">
      <h2 class="page-title">分类管理</h2>
      <el-button type="primary" @click="openCreate">
        <el-icon><Plus /></el-icon>
        新增分类
      </el-button>
    </div>

    <div class="card">
      <el-table :data="categories" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="分类名称" min-width="200" />
        <el-table-column prop="articleCount" label="文章数" width="120">
          <template #default="{ row }">{{ row.articleCount ?? 0 }}</template>
        </el-table-column>
        <el-table-column prop="sort" label="排序" width="100">
          <template #default="{ row }">{{ row.sort ?? 0 }}</template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180">
          <template #default="{ row }">{{ formatDate(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="openEdit(row)">编辑</el-button>
            <el-button link type="danger" size="small" @click="removeCategory(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="!loading && categories.length === 0" description="还没有分类，点击右上角新增" />
    </div>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑分类' : '新增分类'" width="420px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入分类名称" maxlength="20" show-word-limit />
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

const categories = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const submitting = ref(false)
const isEdit = ref(false)
const editId = ref(null)
const formRef = ref(null)

const form = reactive({
  name: '',
  sort: 0
})

const rules = {
  name: [
    { required: true, message: '请输入分类名称', trigger: 'blur' },
    { min: 1, max: 20, message: '长度需在 1 到 20 个字符之间', trigger: 'blur' }
  ]
}

async function loadCategories() {
  loading.value = true
  try {
    categories.value = (await commonApi.getCategories()) || []
  } catch (e) {
    categories.value = []
  } finally {
    loading.value = false
  }
}

function openCreate() {
  isEdit.value = false
  editId.value = null
  form.name = ''
  form.sort = 0
  dialogVisible.value = true
  formRef.value?.clearValidate()
}

function openEdit(row) {
  isEdit.value = true
  editId.value = row.id
  form.name = row.name
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
    const payload = { name: form.name.trim(), sort: form.sort }
    if (isEdit.value) {
      await adminApi.updateCategory(editId.value, payload)
      ElMessage.success('分类已更新')
    } else {
      await adminApi.createCategory(payload)
      ElMessage.success('分类已新增')
    }
    dialogVisible.value = false
    loadCategories()
  } catch (e) {
    // 错误已由拦截器提示
  } finally {
    submitting.value = false
  }
}

async function removeCategory(row) {
  try {
    await ElMessageBox.confirm(
      `确定删除分类「${row.name}」吗？分类下若仍有文章将无法删除。`,
      '删除分类',
      { type: 'warning', confirmButtonText: '确定删除', cancelButtonText: '取消' }
    )
  } catch (e) {
    return
  }
  try {
    await adminApi.deleteCategory(row.id)
    ElMessage.success('分类已删除')
    loadCategories()
  } catch (e) {
    // 错误已由拦截器提示
  }
}

function formatDate(dateStr) {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleString('zh-CN')
}

onMounted(loadCategories)
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

.form-tip {
  margin-left: 10px;
  color: #9ca3af;
  font-size: 12px;
}

.card {
  padding: 8px 18px 16px;
}
</style>
