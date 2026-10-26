<template>
  <div class="admin-dashboard">
    <h2 class="page-title">站点概览</h2>

    <!-- 指标 -->
    <div class="metric-grid">
      <div class="metric-card">
        <span class="metric-label">文章总数</span>
        <span class="metric-value">{{ stats.articleCount }}</span>
        <span class="metric-note">已发布 {{ stats.publishedCount }} · 草稿 {{ stats.draftCount }}</span>
      </div>
      <div class="metric-card">
        <span class="metric-label">注册用户</span>
        <span class="metric-value">{{ stats.userCount }}</span>
        <span class="metric-note">不含已删除账号</span>
      </div>
      <div class="metric-card">
        <span class="metric-label">评论总数</span>
        <span class="metric-value">{{ stats.commentCount }}</span>
        <span class="metric-note">含回复</span>
      </div>
      <div class="metric-card">
        <span class="metric-label">总浏览量</span>
        <span class="metric-value">{{ stats.totalViews }}</span>
        <span class="metric-note">全站累计</span>
      </div>
    </div>

    <!-- 最近文章 -->
    <div class="panel">
      <div class="panel-head">
        <h3 class="panel-title">最近文章</h3>
        <router-link to="/admin/articles" class="panel-more">
          查看全部
          <el-icon><ArrowRight /></el-icon>
        </router-link>
      </div>

      <el-table :data="recentArticles" v-loading="loading">
        <el-table-column prop="id" label="ID" width="64" />
        <el-table-column prop="title" label="标题" min-width="220" show-overflow-tooltip />
        <el-table-column prop="authorNickname" label="作者" width="110" />
        <el-table-column prop="categoryName" label="分类" width="110">
          <template #default="{ row }">{{ row.categoryName || '未分类' }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="88">
          <template #default="{ row }">
            <span class="status-pill" :class="row.status === 1 ? 'pill-live' : 'pill-draft'">
              {{ row.status === 1 ? '已发布' : '草稿' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="viewCount" label="浏览" width="76" />
        <el-table-column prop="createTime" label="创建时间" width="160">
          <template #default="{ row }">{{ formatDate(row.createTime) }}</template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { adminApi } from '@/api/admin'
import { ArrowRight } from '@element-plus/icons-vue'

const loading = ref(false)
const recentArticles = ref([])
const stats = reactive({
  articleCount: 0,
  publishedCount: 0,
  draftCount: 0,
  userCount: 0,
  commentCount: 0,
  totalViews: 0
})

async function loadStats() {
  try {
    const data = await adminApi.getStats()
    Object.assign(stats, data)
  } catch (e) {
    // 错误已由拦截器提示
  }
}

async function loadRecentArticles() {
  loading.value = true
  try {
    const data = await adminApi.getArticles({ pageNum: 1, pageSize: 5 })
    recentArticles.value = data.list || []
  } catch (e) {
    recentArticles.value = []
  } finally {
    loading.value = false
  }
}

function formatDate(dateStr) {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

onMounted(() => {
  loadStats()
  loadRecentArticles()
})
</script>

<style scoped>
/* ---------- 指标卡 ---------- */
.metric-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 14px;
  margin-bottom: 20px;
}

.metric-card {
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding: 20px 22px;
  background: var(--color-bg-card);
  border: 1px solid var(--color-border-2);
  border-radius: var(--radius-lg);
  transition: border-color var(--transition), box-shadow var(--transition);
}

.metric-card:hover {
  border-color: var(--color-border-1);
  box-shadow: var(--shadow-sm);
}

.metric-label {
  font-size: 12px;
  letter-spacing: 0.1em;
  color: var(--color-text-4);
  text-transform: uppercase;
}

.metric-value {
  font-family: "Songti SC", "STSong", Georgia, serif;
  font-size: 32px;
  font-weight: 600;
  color: var(--color-text-1);
  line-height: 1.2;
  font-variant-numeric: tabular-nums;
}

.metric-note {
  font-size: 12px;
  color: var(--color-text-4);
}

/* ---------- 面板 ---------- */
.panel {
  background: var(--color-bg-card);
  border: 1px solid var(--color-border-2);
  border-radius: var(--radius-lg);
  padding: 22px 24px 12px;
}

.panel-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.panel-title {
  font-family: "Songti SC", "STSong", Georgia, serif;
  font-size: 17px;
  font-weight: 600;
  color: var(--color-text-1);
  margin: 0;
}

.panel-more {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: var(--color-text-3);
}

.panel-more:hover {
  color: var(--color-primary);
}

/* 状态胶囊 */
.status-pill {
  display: inline-block;
  padding: 2px 9px;
  border-radius: 999px;
  font-size: 12px;
  line-height: 1.6;
}

.pill-live {
  background: var(--color-primary-soft);
  color: var(--color-primary);
}

.pill-draft {
  background: var(--color-accent-light);
  color: var(--color-accent);
}

@media (max-width: 1000px) {
  .metric-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 560px) {
  .metric-grid {
    grid-template-columns: 1fr;
  }
}
</style>
