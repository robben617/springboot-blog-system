<template>
  <div class="admin-tags">
    <div class="page-header">
      <h2 class="page-title">标签管理</h2>
      <el-button type="primary" @click="openCreate">
        <el-icon><Plus /></el-icon>
        新增标签
      </el-button>
    </div>

    <div class="card">
      <el-table :data="tags" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="标签名称" min-width="200">
          <template #default="{ row }">
            <el-tag effect="plain">{{ row.name }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180">
          <template #default="{ row }">{{ formatDate(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="openEdit(row)">编辑</el-button>
            <el-button link type="danger" size="small" @click="removeTag(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="!loading && tags.length === 0" description="还没有标签，点击右上角新增" />
    </div>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑标签' : '新增标签'" width="420px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入标签名称" maxlength="20" show-word-limit />
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

const tags = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const submitting = ref(false)
const isEdit = ref(false)
const editId = ref(null)
const formRef = ref(null)

const form = reactive({
  name: ''
})

const rules = {
  name: [
    { required: true, message: '请输入标签名称', trigger: 'blur' },
    { min: 1, max: 20, message: '长度需在 1 到 20 个字符之间', trigger: 'blur' }
  ]
}

async function loadTags() {
  loading.value = true
  try {
    tags.value = (await commonApi.getTags()) || []
  } catch (e) {
    tags.value = []
  } finally {
    loading.value = false
  }
}

function openCreate() {
  isEdit.value = false
  editId.value = null
  form.name = ''
  dialogVisible.value = true
  formRef.value?.clearValidate()
}

function openEdit(row) {
  isEdit.value = true
  editId.value = row.id
  form.name = row.name
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
    const payload = { name: form.name.trim() }
    if (isEdit.value) {
      await adminApi.updateTag(editId.value, payload)
      ElMessage.success('标签已更新')
    } else {
      await adminApi.createTag(payload)
      ElMessage.success('标签已新增')
    }
    dialogVisible.value = false
    loadTags()
  } catch (e) {
    // 错误已由拦截器提示
  } finally {
    submitting.value = false
  }
}

async function removeTag(row) {
  try {
    await ElMessageBox.confirm(
      `确定删除标签「${row.name}」吗？删除后会自动解除它与所有文章的关联。`,
      '删除标签',
      { type: 'warning', confirmButtonText: '确定删除', cancelButtonText: '取消' }
    )
  } catch (e) {
    return
  }
  try {
    await adminApi.deleteTag(row.id)
    ElMessage.success('标签已删除')
    loadTags()
  } catch (e) {
    // 错误已由拦截器提示
  }
}

function formatDate(dateStr) {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleString('zh-CN')
}

onMounted(loadTags)
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
