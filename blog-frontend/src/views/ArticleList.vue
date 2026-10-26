<template>
  <div class="page-container">
    <el-row :gutter="32">
      <!-- 左侧主内容 -->
      <el-col :xs="24" :sm="24" :md="17">
        <!-- 页头 -->
        <header class="archive-head">
          <p class="archive-eyebrow">Archive</p>
          <div class="archive-title-row">
            <h1 class="archive-title">文章归档</h1>
            <span v-if="total > 0" class="archive-count">共 {{ total }} 篇</span>
          </div>
        </header>

        <!-- 搜索 -->
        <div class="search-bar">
          <el-input
            v-model="keywordInput"
            placeholder="搜索标题或正文…"
            size="large"
            clearable
            @keyup.enter="applySearch"
            @clear="applySearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
            <template #append>
              <el-button @click="applySearch">搜索</el-button>
            </template>
          </el-input>
        </div>

        <!-- 筛选 -->
        <div class="filter-block">
          <div class="filter-row">
            <span class="filter-label">分类</span>
            <div class="filter-options">
              <button
                type="button"
                class="chip"
                :class="{ active: currentCategoryId === null }"
                @click="selectCategory(null)"
              >
                全部
              </button>
              <button
                v-for="cat in categories"
                :key="cat.id"
                type="button"
                class="chip"
                :class="{ active: currentCategoryId === cat.id }"
                @click="selectCategory(cat.id)"
              >
                {{ cat.name }}
              </button>
            </div>
          </div>

          <div class="filter-row">
            <span class="filter-label">标签</span>
            <div class="filter-options">
              <button
                type="button"
                class="chip"
                :class="{ active: currentTagId === null }"
                @click="selectTag(null)"
              >
                全部
              </button>
              <button
                v-for="tag in tags"
                :key="tag.id"
                type="button"
                class="chip"
                :class="{ active: currentTagId === tag.id }"
                @click="selectTag(tag.id)"
              >
                {{ tag.name }}
              </button>
            </div>
          </div>
        </div>

        <!-- 当前筛选 -->
        <div v-if="filterText" class="filter-active">
          <el-icon><Filter /></el-icon>
          <span class="filter-active-text">{{ filterText }}</span>
          <button type="button" class="filter-clear" @click="clearFilters">清除</button>
        </div>

        <!-- 文章列表 -->
        <div class="article-list">
          <div v-if="loading" class="feed-state">
            <el-icon class="is-loading" size="24"><Loading /></el-icon>
            <span>正在加载</span>
          </div>

          <div v-else-if="articles.length === 0" class="feed-state">
            <el-icon size="32"><Document /></el-icon>
            <span>没有找到符合条件的文章</span>
            <button type="button" class="filter-clear" @click="clearFilters">清除筛选条件</button>
          </div>

          <ArticleCard v-for="article in articles" :key="article.id" :article="article" />
        </div>

        <!-- 分页 -->
        <el-pagination
          v-if="total > 0"
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[5, 10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          background
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </el-col>

      <!-- 右侧边栏 -->
      <el-col :xs="24" :sm="24" :md="7">
        <AppSidebar />
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { articleApi } from '@/api/article'
import { commonApi } from '@/api/common'
import AppSidebar from '@/components/AppSidebar.vue'
import ArticleCard from '@/components/ArticleCard.vue'
import { Search, Filter, Loading, Document } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()

const articles = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const loading = ref(false)
const keywordInput = ref('')
const categories = ref([])
const tags = ref([])

// 筛选条件统一由路由 query 驱动，保证链接可分享、刷新后状态不丢失
const currentCategoryId = computed(() =>
  route.query.categoryId ? Number(route.query.categoryId) : null
)
const currentTagId = computed(() =>
  route.query.tagId ? Number(route.query.tagId) : null
)
const currentKeyword = computed(() => route.query.keyword || '')

const filterText = computed(() => {
  if (currentCategoryId.value !== null) {
    const cat = categories.value.find((c) => c.id === currentCategoryId.value)
    return `分类 · ${cat ? cat.name : currentCategoryId.value}`
  }
  if (currentTagId.value !== null) {
    const tag = tags.value.find((t) => t.id === currentTagId.value)
    return `标签 · ${tag ? tag.name : currentTagId.value}`
  }
  if (currentKeyword.value) {
    return `搜索 · ${currentKeyword.value}`
  }
  return ''
})

async function loadArticles() {
  loading.value = true
  try {
    let data
    if (currentCategoryId.value !== null) {
      data = await commonApi.getCategoryArticles(currentCategoryId.value, {
        pageNum: pageNum.value,
        pageSize: pageSize.value
      })
    } else if (currentTagId.value !== null) {
      data = await commonApi.getTagArticles(currentTagId.value, {
        pageNum: pageNum.value,
        pageSize: pageSize.value
      })
    } else {
      data = await articleApi.getArticleList({
        pageNum: pageNum.value,
        pageSize: pageSize.value,
        keyword: currentKeyword.value || undefined
      })
    }
    articles.value = data.list || []
    total.value = data.total || 0
  } catch (e) {
    articles.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

async function loadFilters() {
  const [cats, tgs] = await Promise.all([
    commonApi.getCategories().catch(() => []),
    commonApi.getTags().catch(() => [])
  ])
  categories.value = cats || []
  tags.value = tgs || []
}

function go(query) {
  router.push({ path: '/articles', query }).catch(() => {})
}

// 分类与标签筛选在后端是两套独立接口，这里保持互斥：选一个自动清掉另一个
function selectCategory(id) {
  go(id === null ? {} : { categoryId: id })
}

function selectTag(id) {
  go(id === null ? {} : { tagId: id })
}

function applySearch() {
  const kw = keywordInput.value.trim()
  go(kw ? { keyword: kw } : {})
}

function clearFilters() {
  keywordInput.value = ''
  go({})
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

watch(
  () => route.query,
  () => {
    keywordInput.value = currentKeyword.value
    pageNum.value = 1
    loadArticles()
  },
  { immediate: true }
)

onMounted(loadFilters)
</script>

<style scoped>
/* ---------- 页头 ---------- */
.archive-head {
  padding: 8px 0 24px;
}

.archive-eyebrow {
  font-size: 12px;
  letter-spacing: 0.22em;
  color: var(--color-primary);
  font-weight: 500;
  margin: 0 0 12px;
  text-transform: uppercase;
}

.archive-title-row {
  display: flex;
  align-items: baseline;
  gap: 12px;
}

.archive-title {
  font-family: "Songti SC", "STSong", Georgia, serif;
  font-size: 34px;
  font-weight: 600;
  color: var(--color-text-1);
  margin: 0;
  letter-spacing: 0.01em;
}

.archive-count {
  font-size: 13px;
  color: var(--color-text-4);
}

/* ---------- 搜索 ---------- */
.search-bar {
  margin-bottom: 20px;
}

.search-bar :deep(.el-input-group__append) {
  background: var(--color-bg-soft);
  border-left: 1px solid var(--color-border-1);
  box-shadow: none;
}

.search-bar :deep(.el-input-group__append .el-button) {
  color: var(--color-text-2);
  padding: 0 20px;
}

/* ---------- 筛选 ---------- */
.filter-block {
  padding: 18px 20px;
  background: var(--color-bg-card);
  border: 1px solid var(--color-border-2);
  border-radius: var(--radius-lg);
  margin-bottom: 20px;
}

.filter-row {
  display: flex;
  align-items: flex-start;
  gap: 16px;
}

.filter-row + .filter-row {
  margin-top: 14px;
}

.filter-label {
  flex-shrink: 0;
  width: 32px;
  font-size: 12px;
  letter-spacing: 0.12em;
  color: var(--color-text-4);
  line-height: 28px;
}

.filter-options {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.chip {
  padding: 5px 12px;
  border: 1px solid transparent;
  border-radius: 999px;
  background: transparent;
  color: var(--color-text-3);
  font-size: 13px;
  font-family: inherit;
  cursor: pointer;
  transition: all var(--transition);
  line-height: 1.5;
}

.chip:hover {
  background: var(--color-bg-soft);
  color: var(--color-text-1);
}

.chip.active {
  background: var(--color-text-1);
  border-color: var(--color-text-1);
  color: #fff;
  font-weight: 500;
}

/* ---------- 当前筛选 ---------- */
.filter-active {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;
  margin-bottom: 18px;
  background: var(--color-primary-soft);
  border: 1px solid var(--color-primary-light);
  border-radius: var(--radius-md);
  color: var(--color-primary);
  font-size: 13px;
}

.filter-active-text {
  flex: 1;
}

.filter-clear {
  border: none;
  background: none;
  padding: 0;
  color: var(--color-primary);
  font-size: 13px;
  font-family: inherit;
  cursor: pointer;
  text-decoration: underline;
  text-underline-offset: 3px;
}

.filter-clear:hover {
  color: var(--color-primary-hover);
}

/* ---------- 列表 ---------- */
.article-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

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

@media (max-width: 640px) {
  .archive-title {
    font-size: 26px;
  }
  .filter-row {
    flex-direction: column;
    gap: 8px;
  }
  .filter-label {
    line-height: 1;
    width: auto;
  }
}
</style>
