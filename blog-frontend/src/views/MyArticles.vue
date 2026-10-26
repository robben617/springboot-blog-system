<template>
  <div class="page-container my-articles">
    <!-- 页头 -->
    <header class="workspace-head">
      <div>
        <p class="workspace-eyebrow">Workspace</p>
        <h1 class="workspace-title">我的文章</h1>
        <p class="workspace-sub">管理你的草稿与已发布内容</p>
      </div>
      <el-button type="primary" @click="handleCreate">
        <el-icon><EditPen /></el-icon>
        写文章
      </el-button>
    </header>

    <!-- 状态筛选 -->
    <div class="status-tabs">
      <button
        v-for="tab in tabs"
        :key="tab.value"
        type="button"
        class="status-tab"
        :class="{ active: statusFilter === tab.value }"
        @click="changeStatus(tab.value)"
      >
        {{ tab.label }}
      </button>
    </div>

    <!-- 列表 -->
    <div v-if="loading" class="feed-state">
      <el-icon class="is-loading" size="24"><Loading /></el-icon>
      <span>正在加载</span>
    </div>

    <div v-else-if="articles.length === 0" class="feed-state empty">
      <el-icon size="34"><Document /></el-icon>
      <p class="empty-title">这里还没有文章</p>
      <p class="empty-desc">写下第一篇，开始记录你的想法</p>
      <el-button type="primary" @click="handleCreate">开始写作</el-button>
    </div>

    <div v-else class="article-rows">
      <article v-for="article in articles" :key="article.id" class="article-row">
        <div class="row-main">
          <div class="row-kicker">
            <span
              class="status-dot"
              :class="article.status === 1 ? 'dot-published' : 'dot-draft'"
            ></span>
            <span class="status-text">{{ article.status === 1 ? '已发布' : '草稿' }}</span>
            <span class="row-dot">·</span>
            <span class="row-category">{{ article.categoryName || '未分类' }}</span>
            <span class="row-dot">·</span>
            <time class="row-date">{{ formatDate(article.updateTime || article.createTime) }}</time>
          </div>

          <h2 class="row-title" @click="goDetail(article.id)">{{ article.title }}</h2>

          <p v-if="article.summary" class="row-summary">{{ article.summary }}</p>

          <div class="row-meta">
            <span>{{ article.viewCount || 0 }} 阅读</span>
          </div>
        </div>

        <div class="row-actions">
          <el-button link type="primary" size="small" @click="handleEdit(article.id)">编辑</el-button>
          <el-button
            v-if="article.status === 1"
            link
            size="small"
            @click="handleDraft(article.id)"
          >
            转为草稿
          </el-button>
          <el-button v-else link type="success" size="small" @click="handlePublish(article.id)">
            发布
          </el-button>
          <el-button link type="danger" size="small" @click="handleDelete(article.id)">删除</el-button>
        </div>
      </article>
    </div>

    <el-pagination
      v-if="total > 0"
      v-model:current-page="pageNum"
      v-model:page-size="pageSize"
      :total="total"
      :page-sizes="[5, 10, 20]"
      layout="total, sizes, prev, pager, next"
      background
      @size-change="handleSizeChange"
      @current-change="handlePageChange"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { articleApi } from '@/api/article'
import { ElMessage, ElMessageBox } from 'element-plus'
import { EditPen, Document, Loading } from '@element-plus/icons-vue'

const router = useRouter()

const articles = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const loading = ref(false)
const statusFilter = ref('all')

const tabs = [
  { label: '全部', value: 'all' },
  { label: '已发布', value: '1' },
  { label: '草稿', value: '0' }
]

async function loadArticles() {
  loading.value = true
  try {
    const data = await articleApi.getMyArticles({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      status: statusFilter.value === 'all' ? undefined : Number(statusFilter.value)
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

function goDetail(id) {
  router.push(`/articles/${id}`)
}

function handleCreate() {
  router.push('/article/new')
}

function handleEdit(id) {
  router.push(`/article/${id}/edit`)
}

async function handlePublish(id) {
  try {
    await articleApi.publishArticle(id)
    ElMessage.success('文章已发布')
    loadArticles()
  } catch (e) {}
}

async function handleDraft(id) {
  try {
    await articleApi.draftArticle(id)
    ElMessage.success('已转为草稿')
    loadArticles()
  } catch (e) {}
}

async function handleDelete(id) {
  try {
    await ElMessageBox.confirm('删除后将无法恢复，确定要删除这篇文章吗？', '删除文章', {
      type: 'warning',
      confirmButtonText: '确定删除',
      cancelButtonText: '取消'
    })
  } catch {
    return
  }
  try {
    await articleApi.deleteArticle(id)
    ElMessage.success('文章已删除')
    if (articles.value.length === 1 && pageNum.value > 1) {
      pageNum.value -= 1
    }
    loadArticles()
  } catch (e) {}
}

function changeStatus(value) {
  statusFilter.value = value
  pageNum.value = 1
  loadArticles()
}

function handlePageChange(page) {
  pageNum.value = page
  loadArticles()
}

function handleSizeChange(size) {
  pageSize.value = size
  pageNum.value = 1
  loadArticles()
}

function formatDate(dateStr) {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  return `${d.getFullYear()}年${d.getMonth() + 1}月${d.getDate()}日`
}

onMounted(loadArticles)
</script>

<style scoped>
.my-articles {
  max-width: 960px;
}

/* ---------- 页头 ---------- */
.workspace-head {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 20px;
  padding: 8px 0 28px;
  margin-bottom: 8px;
  border-bottom: 1px solid var(--color-border-1);
}

.workspace-eyebrow {
  font-size: 12px;
  letter-spacing: 0.22em;
  color: var(--color-primary);
  font-weight: 500;
  margin: 0 0 10px;
  text-transform: uppercase;
}

.workspace-title {
  font-family: "Songti SC", "STSong", Georgia, serif;
  font-size: 34px;
  font-weight: 600;
  color: var(--color-text-1);
  margin: 0 0 6px;
  letter-spacing: 0.01em;
}

.workspace-sub {
  font-size: 13px;
  color: var(--color-text-4);
  margin: 0;
}

/* ---------- 状态筛选 ---------- */
.status-tabs {
  display: flex;
  gap: 4px;
  padding: 20px 0;
}

.status-tab {
  padding: 7px 16px;
  border: none;
  border-radius: 999px;
  background: transparent;
  color: var(--color-text-3);
  font-size: 13.5px;
  font-family: inherit;
  cursor: pointer;
  transition: all var(--transition);
}

.status-tab:hover {
  background: var(--color-bg-card);
  color: var(--color-text-1);
}

.status-tab.active {
  background: var(--color-text-1);
  color: #fff;
  font-weight: 500;
}

/* ---------- 列表 ---------- */
.article-rows {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.article-row {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 24px;
  padding: 22px 24px;
  background: var(--color-bg-card);
  border: 1px solid var(--color-border-2);
  border-radius: var(--radius-lg);
  transition: border-color var(--transition), box-shadow var(--transition);
}

.article-row:hover {
  border-color: var(--color-border-1);
  box-shadow: var(--shadow-sm);
}

.row-main {
  flex: 1;
  min-width: 0;
}

.row-kicker {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: var(--color-text-4);
  margin-bottom: 9px;
}

.status-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  flex-shrink: 0;
}

.dot-published {
  background: var(--color-primary);
}

.dot-draft {
  background: var(--color-accent);
}

.status-text {
  color: var(--color-text-3);
  font-weight: 500;
}

.row-dot {
  color: var(--color-text-5);
}

.row-title {
  font-family: "Songti SC", "STSong", Georgia, serif;
  font-size: 19px;
  font-weight: 600;
  line-height: 1.5;
  color: var(--color-text-1);
  margin: 0 0 8px;
  cursor: pointer;
  transition: color var(--transition);
}

.row-title:hover {
  color: var(--color-primary);
}

.row-summary {
  font-size: 13.5px;
  line-height: 1.75;
  color: var(--color-text-3);
  margin: 0 0 10px;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.row-meta {
  font-size: 12px;
  color: var(--color-text-4);
}

.row-actions {
  display: flex;
  align-items: center;
  gap: 4px;
  flex-shrink: 0;
  padding-top: 22px;
}

/* ---------- 状态 ---------- */
.feed-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 72px 20px;
  color: var(--color-text-4);
  font-size: 14px;
  background: var(--color-bg-card);
  border: 1px solid var(--color-border-2);
  border-radius: var(--radius-lg);
}

.empty-title {
  font-family: "Songti SC", "STSong", Georgia, serif;
  font-size: 18px;
  color: var(--color-text-2);
  margin: 4px 0 0;
}

.empty-desc {
  font-size: 13px;
  color: var(--color-text-4);
  margin: 0 0 10px;
}

@media (max-width: 720px) {
  .workspace-title {
    font-size: 26px;
  }
  .article-row {
    flex-direction: column;
    gap: 12px;
    padding: 18px;
  }
  .row-actions {
    padding-top: 0;
  }
}
</style>
