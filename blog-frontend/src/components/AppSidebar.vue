<template>
  <aside class="app-sidebar">
    <!-- 关于 -->
    <section class="side-block">
      <h3 class="side-title">关于本站</h3>
      <p class="side-text">
        基于 Spring Boot 与 Vue 构建的写作空间，记录工程实践与阅读所得。
      </p>
    </section>

    <!-- 公告 -->
    <section class="side-block">
      <h3 class="side-title">公告</h3>
      <ul class="notice-list">
        <li
          v-for="item in announcements"
          :key="item.id"
          class="notice-item"
          role="button"
          tabindex="0"
          :aria-label="`查看公告：${item.title}`"
          @click="openAnnouncement(item)"
          @keyup.enter="openAnnouncement(item)"
        >
          <span class="notice-dot"></span>
          <div class="notice-main">
            <p class="notice-title">{{ item.title }}</p>
            <time class="notice-time">{{ formatDate(item.createTime) }}</time>
          </div>
          <el-icon class="notice-arrow"><ArrowRight /></el-icon>
        </li>
        <li v-if="announcements.length === 0" class="side-empty">暂无公告</li>
      </ul>
    </section>

    <!-- 分类 -->
    <section class="side-block">
      <h3 class="side-title">分类</h3>
      <ul class="category-list">
        <li
          v-for="cat in categories"
          :key="cat.id"
          class="category-item"
          @click="goCategory(cat.id)"
        >
          <span class="category-name">{{ cat.name }}</span>
          <span class="category-count">{{ cat.articleCount || 0 }}</span>
        </li>
        <li v-if="categories.length === 0" class="side-empty">暂无分类</li>
      </ul>
    </section>

    <!-- 标签 -->
    <section class="side-block">
      <h3 class="side-title">标签</h3>
      <div class="tag-cloud">
        <button
          v-for="tag in tags"
          :key="tag.id"
          type="button"
          class="tag-chip"
          @click="goTag(tag.id)"
        >
          {{ tag.name }}
        </button>
        <span v-if="tags.length === 0" class="side-empty">暂无标签</span>
      </div>
    </section>

    <!-- 友链 -->
    <section class="side-block">
      <h3 class="side-title">友情链接</h3>
      <ul class="friend-list">
        <li v-for="link in friendLinks" :key="link.id">
          <a :href="link.url" target="_blank" rel="noopener" class="friend-item">
            {{ link.name }}
            <el-icon class="friend-arrow"><TopRight /></el-icon>
          </a>
        </li>
        <li v-if="friendLinks.length === 0" class="side-empty">暂无友链</li>
      </ul>
    </section>

    <!-- 公告详情 -->
    <el-dialog
      v-model="announcementVisible"
      width="520px"
      align-center
      class="announcement-dialog"
      :show-close="true"
    >
      <template #header>
        <div class="dialog-head">
          <span class="dialog-label">公告</span>
          <h3 class="dialog-title">{{ activeAnnouncement?.title }}</h3>
        </div>
      </template>
      <div class="dialog-body">
        <time class="dialog-time">{{ formatFullDate(activeAnnouncement?.createTime) }}</time>
        <p class="dialog-content">{{ activeAnnouncement?.content }}</p>
      </div>
    </el-dialog>
  </aside>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { commonApi } from '@/api/common'
import { TopRight, ArrowRight } from '@element-plus/icons-vue'

const router = useRouter()

const categories = ref([])
const tags = ref([])
const announcements = ref([])
const friendLinks = ref([])

// 公告详情弹窗
const announcementVisible = ref(false)
const activeAnnouncement = ref(null)

function openAnnouncement(item) {
  activeAnnouncement.value = item
  announcementVisible.value = true
}

async function loadSidebar() {
  const [cats, tgs, anns, links] = await Promise.all([
    commonApi.getCategories().catch(() => []),
    commonApi.getTags().catch(() => []),
    commonApi.getLatestAnnouncements(5).catch(() => []),
    commonApi.getFriendLinks().catch(() => [])
  ])
  categories.value = cats || []
  tags.value = tgs || []
  announcements.value = anns || []
  friendLinks.value = links || []
}

function goCategory(id) {
  router.push({ path: '/articles', query: { categoryId: id } })
}

function goTag(id) {
  router.push({ path: '/articles', query: { tagId: id } })
}

function formatDate(dateStr) {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  return `${d.getMonth() + 1}月${d.getDate()}日`
}

function formatFullDate(dateStr) {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  return `${d.getFullYear()}年${d.getMonth() + 1}月${d.getDate()}日`
}

onMounted(loadSidebar)
</script>

<style scoped>
.app-sidebar {
  position: sticky;
  top: 96px;
}

.side-block {
  margin-bottom: 36px;
}

/* 小标题：字距拉开，像杂志栏目名 */
.side-title {
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 0.18em;
  color: var(--color-text-4);
  text-transform: uppercase;
  margin: 0 0 16px;
  padding-bottom: 10px;
  border-bottom: 1px solid var(--color-border-1);
}

.side-text {
  font-size: 14px;
  line-height: 1.85;
  color: var(--color-text-3);
  margin: 0;
}

/* 公告 */
.notice-list,
.category-list,
.friend-list {
  list-style: none;
  margin: 0;
  padding: 0;
}

.notice-item {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: 9px 8px;
  margin: 0 -8px;
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: background var(--transition);
}

.notice-item:hover {
  background: var(--color-bg-soft);
}

.notice-item:focus-visible {
  outline: 2px solid var(--color-primary);
  outline-offset: 1px;
}

.notice-item:hover .notice-title {
  color: var(--color-primary);
}

.notice-arrow {
  font-size: 11px;
  color: var(--color-text-5);
  opacity: 0;
  transform: translateX(-3px);
  transition: all var(--transition);
  margin-top: 6px;
  flex-shrink: 0;
}

.notice-item:hover .notice-arrow {
  opacity: 1;
  transform: translateX(0);
}

.notice-dot {
  width: 4px;
  height: 4px;
  border-radius: 50%;
  background: var(--color-accent);
  margin-top: 9px;
  flex-shrink: 0;
}

.notice-main {
  min-width: 0;
}

.notice-title {
  font-size: 14px;
  color: var(--color-text-2);
  line-height: 1.5;
  margin: 0 0 3px;
}

.notice-time {
  font-size: 12px;
  color: var(--color-text-4);
}

/* 分类 */
.category-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 9px 0;
  cursor: pointer;
  color: var(--color-text-2);
  font-size: 14px;
  transition: color var(--transition);
}

.category-item:hover {
  color: var(--color-primary);
}

.category-count {
  font-size: 12px;
  color: var(--color-text-4);
  font-variant-numeric: tabular-nums;
}

/* 标签 */
.tag-cloud {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tag-chip {
  padding: 4px 11px;
  border: 1px solid var(--color-border-1);
  border-radius: 999px;
  background: transparent;
  color: var(--color-text-3);
  font-size: 12px;
  font-family: inherit;
  cursor: pointer;
  transition: all var(--transition);
}

.tag-chip:hover {
  border-color: var(--color-primary);
  color: var(--color-primary);
  background: var(--color-primary-soft);
}

/* 友链 */
.friend-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 0;
  font-size: 14px;
  color: var(--color-text-3);
  transition: color var(--transition);
}

.friend-item:hover {
  color: var(--color-primary);
}

.friend-arrow {
  font-size: 12px;
  opacity: 0;
  transform: translate(-3px, 3px);
  transition: all var(--transition);
}

.friend-item:hover .friend-arrow {
  opacity: 1;
  transform: translate(0, 0);
}

.side-empty {
  font-size: 13px;
  color: var(--color-text-5);
  padding: 4px 0;
  list-style: none;
}

@media (max-width: 992px) {
  .app-sidebar {
    position: static;
    margin-top: 40px;
  }
}
</style>
