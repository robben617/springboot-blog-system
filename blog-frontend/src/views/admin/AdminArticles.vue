<template>
  <div class="admin-articles">
    <h2 class="page-title">文章管理</h2>

    <div class="card filter-bar">
      <el-input
        v-model="keyword"
        placeholder="搜索标题、作者用户名或昵称"
        clearable
        style="width: 260px"
        @keyup.enter="handleSearch"
        @clear="handleSearch"
      >
        <template #prefix><el-icon><Search /></el-icon></template>
      </el-input>
      <el-select v-model="statusFilter" placeholder="文章状态" clearable style="width: 140px" @change="handleSearch">
        <el-option label="已发布" :value="1" />
        <el-option label="草稿" :value="0" />
      </el-select>
      <el-select v-model="categoryFilter" placeholder="全部分类" clearable style="width: 160px" @change="handleSearch">
        <el-option v-for="cat in categories" :key="cat.id" :label="cat.name" :value="cat.id" />
      </el-select>
      <el-button type="primary" @click="handleSearch">查询</el-button>
      <el-button @click="handleReset">重置</el-button>
    </div>

    <div class="card">
      <el-table :data="articles" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="title" label="标题" min-width="220" show-overflow-tooltip />
        <el-table-column prop="authorNickname" label="作者" width="120">
          <template #default="{ row }">{{ row.authorNickname || row.authorUsername }}</template>
        </el-table-column>
        <el-table-column prop="categoryName" label="分类" width="120">
          <template #default="{ row }">{{ row.categoryName || '未分类' }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag v-if="row.status === 1" type="success" size="small">已发布</el-tag>
            <el-tag v-else type="info" size="small">草稿</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="viewCount" label="浏览" width="80" />
        <el-table-column prop="updateTime" label="更新时间" width="170">
          <template #default="{ row }">{{ formatDate(row.updateTime || row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="viewArticle(row)">查看</el-button>
            <el-button link type="primary" size="small" @click="editArticle(row)">编辑</el-button>
            <el-button
              link
              :type="row.status === 1 ? 'warning' : 'success'"
              size="small"
              @click="toggleStatus(row)"
            >
              {{ row.status === 1 ? '转草稿' : '发布' }}
            </el-button>
            <el-button link type="danger" size="small" @click="removeArticle(row)">删除</el-button>
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
        @current-change="loadArticles"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { adminApi } from '@/api/admin'
import { articleApi } from '@/api/article'
import { commonApi } from '@/api/common'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'

const router = useRouter()

const articles = ref([])
const categories = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const loading = ref(false)
const keyword = ref('')
const statusFilter = ref(null)
const categoryFilter = ref(null)

async function loadArticles() {
  loading.value = true
  try {
    const data = await adminApi.getArticles({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      keyword: keyword.value.trim() || undefined,
      status: statusFilter.value === null ? undefined : statusFilter.value,
      categoryId: categoryFilter.value === null ? undefined : categoryFilter.value
    })
    articles.value = data.list || []
    total.value = data.total || 0
  } catch (e) {
    articles.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

async function loadCategories() {
  try {
    categories.value = (await commonApi.getCategories()) || []
  } catch (e) {
    categories.value = []
  }
}

function handleSearch() {
  pageNum.value = 1
  loadArticles()
}

function handleReset() {
  keyword.value = ''
  statusFilter.value = null
  categoryFilter.value = null
  handleSearch()
}

function handleSizeChange(size) {
  pageSize.value = size
  pageNum.value = 1
  loadArticles()
}

function viewArticle(row) {
  router.push(`/articles/${row.id}`)
}

function editArticle(row) {
  router.push(`/article/${row.id}/edit`)
}

async function toggleStatus(row) {
  try {
    if (row.status === 1) {
      await articleApi.draftArticle(row.id)
      ElMessage.success('已转为草稿')
    } else {
      await articleApi.publishArticle(row.id)
      ElMessage.success('文章已发布')
    }
    loadArticles()
  } catch (e) {
    // 错误已由拦截器提示
  }
}

async function removeArticle(row) {
  try {
    await ElMessageBox.confirm(
      `删除文章「${row.title}」会同时删除其下全部评论，确定继续吗？`,
      '删除文章',
      { type: 'warning', confirmButtonText: '确定删除', cancelButtonText: '取消' }
    )
  } catch (e) {
    return
  }
  try {
    await adminApi.deleteArticle(row.id)
    ElMessage.success('文章已删除')
    if (articles.value.length === 1 && pageNum.value > 1) {
      pageNum.value -= 1
    }
    loadArticles()
  } catch (e) {
    // 错误已由拦截器提示
  }
}

function formatDate(dateStr) {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleString('zh-CN')
}

onMounted(() => {
  loadCategories()
  loadArticles()
})
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
