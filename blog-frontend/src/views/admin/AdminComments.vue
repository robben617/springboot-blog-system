<template>
  <div class="admin-comments">
    <h2 class="page-title">评论管理</h2>

    <div class="card filter-bar">
      <el-input
        v-model="keyword"
        placeholder="搜索评论内容、评论人或文章标题"
        clearable
        style="width: 280px"
        @keyup.enter="handleSearch"
        @clear="handleSearch"
      >
        <template #prefix><el-icon><Search /></el-icon></template>
      </el-input>
      <el-input
        v-model="articleId"
        placeholder="按文章 ID 筛选"
        clearable
        style="width: 160px"
        @keyup.enter="handleSearch"
        @clear="handleSearch"
      />
      <el-button type="primary" @click="handleSearch">查询</el-button>
      <el-button @click="handleReset">重置</el-button>
    </div>

    <div class="card">
      <el-table :data="comments" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="content" label="评论内容" min-width="260" show-overflow-tooltip />
        <el-table-column prop="userNickname" label="评论人" width="140">
          <template #default="{ row }">{{ row.userNickname || '匿名用户' }}</template>
        </el-table-column>
        <el-table-column label="类型" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.parentId" type="warning" size="small">回复</el-tag>
            <el-tag v-else type="info" size="small">主评论</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="articleTitle" label="所属文章" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">
            <el-link type="primary" :underline="false" @click="goArticle(row.articleId)">
              {{ row.articleTitle || '（文章已删除）' }}
            </el-link>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="评论时间" width="170">
          <template #default="{ row }">{{ formatDate(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button link type="danger" size="small" @click="removeComment(row)">删除</el-button>
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
        @current-change="loadComments"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { adminApi } from '@/api/admin'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'

const router = useRouter()

const comments = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const loading = ref(false)
const keyword = ref('')
const articleId = ref('')

async function loadComments() {
  loading.value = true
  try {
    const data = await adminApi.getComments({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      keyword: keyword.value.trim() || undefined,
      articleId: articleId.value ? Number(articleId.value) : undefined
    })
    comments.value = data.list || []
    total.value = data.total || 0
  } catch (e) {
    comments.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pageNum.value = 1
  loadComments()
}

function handleReset() {
  keyword.value = ''
  articleId.value = ''
  handleSearch()
}

function handleSizeChange(size) {
  pageSize.value = size
  pageNum.value = 1
  loadComments()
}

function goArticle(id) {
  if (id) router.push(`/articles/${id}`)
}

async function removeComment(row) {
  try {
    await ElMessageBox.confirm(
      row.parentId
        ? '删除这条回复后，其内容将不再展示，确定继续吗？'
        : '删除这条评论会同时删除其下全部回复，确定继续吗？',
      '删除评论',
      { type: 'warning', confirmButtonText: '确定删除', cancelButtonText: '取消' }
    )
  } catch (e) {
    return
  }
  try {
    await adminApi.deleteComment(row.id)
    ElMessage.success('评论已删除')
    if (comments.value.length === 1 && pageNum.value > 1) {
      pageNum.value -= 1
    }
    loadComments()
  } catch (e) {
    // 错误已由拦截器提示
  }
}

function formatDate(dateStr) {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleString('zh-CN')
}

onMounted(loadComments)
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
