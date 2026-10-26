<template>
  <div class="page-container">
    <el-row :gutter="32">
      <!-- 左侧主内容 -->
      <el-col :xs="24" :sm="24" :md="17">
        <!-- 编辑式刊头 -->
        <section class="masthead">
          <p class="masthead-eyebrow">技术 · 思考 · 记录</p>
          <h1 class="masthead-title">
            在代码与文字之间，<br />留下一些痕迹。
          </h1>
          <p class="masthead-lead">
            这里是一个关于后端工程、前端实践与日常思考的写作空间。
            不追热点，只写真正想清楚的事。
          </p>
          <div class="masthead-actions">
            <el-button type="primary" size="large" @click="$router.push('/articles')">
              开始阅读
              <el-icon class="el-icon--right"><ArrowRight /></el-icon>
            </el-button>
            <el-button v-if="!userStore.isLoggedIn" size="large" @click="$router.push('/register')">
              加入写作
            </el-button>
            <el-button v-else size="large" @click="$router.push('/article/new')">
              写一篇文章
            </el-button>
          </div>
        </section>

        <div class="masthead-rule"></div>

        <!-- 最新文章 -->
        <div class="section-head">
          <div>
            <h2 class="section-heading">最新文章</h2>
            <p class="section-sub">按发布时间排序</p>
          </div>
          <router-link to="/articles" class="more-link">
            查看全部
            <el-icon><ArrowRight /></el-icon>
          </router-link>
        </div>

        <div class="article-list">
          <div v-if="loading" class="feed-state">
            <el-icon class="is-loading" size="24"><Loading /></el-icon>
            <span>正在加载</span>
          </div>

          <div v-else-if="articles.length === 0" class="feed-state">
            <el-icon size="32"><Document /></el-icon>
            <span>还没有文章，来写下第一篇吧</span>
          </div>

          <ArticleCard v-for="article in articles" :key="article.id" :article="article" />
        </div>
      </el-col>

      <!-- 右侧边栏 -->
      <el-col :xs="24" :sm="24" :md="7">
        <AppSidebar />
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { articleApi } from '@/api/article'
import { useUserStore } from '@/store/user'
import AppSidebar from '@/components/AppSidebar.vue'
import ArticleCard from '@/components/ArticleCard.vue'
import { Loading, Document, ArrowRight } from '@element-plus/icons-vue'

const userStore = useUserStore()
const articles = ref([])
const loading = ref(false)

async function loadLatest() {
  loading.value = true
  try {
    const data = await articleApi.getArticleList({ pageNum: 1, pageSize: 5 })
    articles.value = data.list || []
  } catch (e) {
    articles.value = []
  } finally {
    loading.value = false
  }
}

onMounted(loadLatest)
</script>

<style scoped>
/* ---------- 刊头：纯排版，无渐变 ---------- */
.masthead {
  padding: 56px 0 40px;
}

.masthead-eyebrow {
  font-size: 12px;
  letter-spacing: 0.22em;
  color: var(--color-primary);
  font-weight: 500;
  margin: 0 0 20px;
  text-transform: uppercase;
}

.masthead-title {
  font-family: "Songti SC", "STSong", Georgia, "Times New Roman", serif;
  font-size: 42px;
  font-weight: 600;
  line-height: 1.4;
  color: var(--color-text-1);
  margin: 0 0 20px;
  letter-spacing: 0.01em;
}

.masthead-lead {
  font-size: 16px;
  line-height: 1.9;
  color: var(--color-text-3);
  margin: 0 0 32px;
  max-width: 560px;
}

.masthead-actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.masthead-rule {
  height: 1px;
  background: var(--color-border-1);
  margin-bottom: 40px;
}

/* ---------- 区块标题 ---------- */
.section-head {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  margin-bottom: 24px;
}

.section-heading {
  font-family: "Songti SC", "STSong", Georgia, serif;
  font-size: 24px;
  font-weight: 600;
  color: var(--color-text-1);
  margin: 0 0 4px;
  letter-spacing: 0.01em;
}

.section-sub {
  font-size: 13px;
  color: var(--color-text-4);
  margin: 0;
}

.more-link {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: var(--color-text-3);
  transition: color var(--transition);
}

.more-link:hover {
  color: var(--color-primary);
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

@media (max-width: 992px) {
  .masthead {
    padding: 32px 0 28px;
  }
  .masthead-title {
    font-size: 32px;
  }
  .masthead-lead {
    font-size: 15px;
  }
}

@media (max-width: 640px) {
  .masthead-title {
    font-size: 26px;
  }
  .masthead-title br {
    display: none;
  }
}
</style>
