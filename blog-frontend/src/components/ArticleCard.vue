<template>
  <article class="article-card" @click="goDetail">
    <div class="card-body">
      <!-- 顶部信息：分类 + 日期 -->
      <div class="card-kicker">
        <span v-if="article.categoryName" class="kicker-category">
          {{ article.categoryName }}
        </span>
        <span v-else class="kicker-category kicker-muted">随笔</span>
        <span class="kicker-dot">·</span>
        <time class="kicker-date">{{ formatDate(article.createTime) }}</time>
      </div>

      <!-- 标题 -->
      <h2 class="card-title">{{ article.title }}</h2>

      <!-- 摘要 -->
      <p class="card-summary">{{ article.summary || '暂无摘要' }}</p>

      <!-- 底部元信息 -->
      <div class="card-meta">
        <span class="meta-author">
          <el-avatar :size="20" :src="article.authorAvatar || ''" class="author-avatar">
            {{ article.authorNickname?.charAt(0) || 'U' }}
          </el-avatar>
          {{ article.authorNickname || '匿名' }}
        </span>
        <span class="meta-sep">·</span>
        <span class="meta-views">{{ article.viewCount || 0 }} 阅读</span>
      </div>
    </div>

    <!-- 封面缩略图（有封面才显示） -->
    <div v-if="article.cover" class="card-cover">
      <img :src="article.cover" :alt="article.title" loading="lazy" />
    </div>
  </article>
</template>

<script setup>
import { useRouter } from 'vue-router'

const props = defineProps({
  article: {
    type: Object,
    required: true
  }
})

const router = useRouter()

function goDetail() {
  router.push(`/articles/${props.article.id}`)
}

function formatDate(dateStr) {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  return `${d.getFullYear()}年${d.getMonth() + 1}月${d.getDate()}日`
}
</script>

<style scoped>
.article-card {
  display: flex;
  gap: 28px;
  background: var(--color-bg-card);
  border: 1px solid var(--color-border-2);
  border-radius: var(--radius-lg);
  padding: 26px 28px;
  cursor: pointer;
  transition: border-color var(--transition), box-shadow var(--transition), transform var(--transition);
}

.article-card:hover {
  border-color: var(--color-border-1);
  box-shadow: var(--shadow-md);
  transform: translateY(-1px);
}

.card-body {
  flex: 1;
  min-width: 0;
}

/* 顶部信息行 */
.card-kicker {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
  font-size: 12px;
  letter-spacing: 0.06em;
}

.kicker-category {
  color: var(--color-primary);
  font-weight: 500;
  text-transform: uppercase;
}

.kicker-muted {
  color: var(--color-text-4);
}

.kicker-dot {
  color: var(--color-text-5);
}

.kicker-date {
  color: var(--color-text-4);
}

/* 标题：衬线体，强化阅读气质 */
.card-title {
  font-family: "Songti SC", "STSong", Georgia, "Times New Roman", serif;
  font-size: 22px;
  font-weight: 600;
  line-height: 1.45;
  color: var(--color-text-1);
  margin: 0 0 10px;
  letter-spacing: -0.01em;
  transition: color var(--transition);
}

.article-card:hover .card-title {
  color: var(--color-primary);
}

/* 摘要 */
.card-summary {
  font-size: 14px;
  line-height: 1.75;
  color: var(--color-text-3);
  margin: 0 0 16px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

/* 元信息 */
.card-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: var(--color-text-4);
}

.meta-author {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  color: var(--color-text-3);
}

.author-avatar {
  background: var(--color-primary-light);
  color: var(--color-primary);
  font-size: 10px;
  font-weight: 600;
  flex-shrink: 0;
}

.meta-sep {
  color: var(--color-text-5);
}

/* 封面 */
.card-cover {
  width: 168px;
  height: 112px;
  flex-shrink: 0;
  border-radius: var(--radius-md);
  overflow: hidden;
  background: var(--color-bg-soft);
}

.card-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.4s ease;
}

.article-card:hover .card-cover img {
  transform: scale(1.04);
}

@media (max-width: 640px) {
  .article-card {
    padding: 20px;
    gap: 16px;
  }
  .card-title {
    font-size: 19px;
  }
  .card-cover {
    width: 96px;
    height: 96px;
  }
}
</style>
