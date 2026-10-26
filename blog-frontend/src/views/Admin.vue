<template>
  <div class="admin-layout page-container">
    <!-- 侧边导航 -->
    <aside class="admin-side">
      <div class="side-head">
        <span class="side-mark">博</span>
        <div class="side-head-text">
          <span class="side-name">管理后台</span>
          <span class="side-role">Console</span>
        </div>
      </div>

      <nav class="side-nav">
        <p class="nav-group-label">内容</p>
        <router-link
          v-for="item in contentNav"
          :key="item.path"
          :to="item.path"
          class="side-link"
          :class="{ active: isActive(item.path) }"
        >
          <el-icon><component :is="item.icon" /></el-icon>
          <span>{{ item.label }}</span>
        </router-link>

        <p class="nav-group-label">站点</p>
        <router-link
          v-for="item in siteNav"
          :key="item.path"
          :to="item.path"
          class="side-link"
          :class="{ active: isActive(item.path) }"
        >
          <el-icon><component :is="item.icon" /></el-icon>
          <span>{{ item.label }}</span>
        </router-link>

        <p class="nav-group-label">系统</p>
        <router-link
          v-for="item in systemNav"
          :key="item.path"
          :to="item.path"
          class="side-link"
          :class="{ active: isActive(item.path) }"
        >
          <el-icon><component :is="item.icon" /></el-icon>
          <span>{{ item.label }}</span>
        </router-link>
      </nav>

      <div class="side-foot">
        <router-link to="/" class="back-site">
          <el-icon><Back /></el-icon>
          返回站点
        </router-link>
      </div>
    </aside>

    <!-- 内容区 -->
    <section class="admin-content">
      <router-view />
    </section>
  </div>
</template>

<script setup>
import { useRoute } from 'vue-router'
import {
  DataLine, User, Document, ChatDotRound, Collection, PriceTag, Bell, Link, Back
} from '@element-plus/icons-vue'

const route = useRoute()

const contentNav = [
  { path: '/admin/articles', label: '文章管理', icon: Document },
  { path: '/admin/comments', label: '评论管理', icon: ChatDotRound },
  { path: '/admin/categories', label: '分类管理', icon: Collection },
  { path: '/admin/tags', label: '标签管理', icon: PriceTag }
]

const siteNav = [
  { path: '/admin/announcements', label: '公告管理', icon: Bell },
  { path: '/admin/friend-links', label: '友链管理', icon: Link }
]

const systemNav = [
  { path: '/admin/dashboard', label: '站点概览', icon: DataLine },
  { path: '/admin/users', label: '用户管理', icon: User }
]

function isActive(path) {
  return route.path === path || route.path.startsWith(path + '/')
}
</script>

<style scoped>
.admin-layout {
  display: flex;
  gap: 28px;
  align-items: flex-start;
  max-width: 1280px;
}

/* ---------- 侧边栏 ---------- */
.admin-side {
  width: 208px;
  flex-shrink: 0;
  position: sticky;
  top: 88px;
  background: var(--color-bg-card);
  border: 1px solid var(--color-border-2);
  border-radius: var(--radius-lg);
  padding: 20px 14px 14px;
}

.side-head {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 0 6px 18px;
  margin-bottom: 8px;
  border-bottom: 1px solid var(--color-border-1);
}

.side-mark {
  width: 30px;
  height: 30px;
  border-radius: var(--radius-md);
  background: var(--color-text-1);
  color: #fff;
  font-size: 15px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-family: "Songti SC", "STSong", Georgia, serif;
  flex-shrink: 0;
}

.side-head-text {
  display: flex;
  flex-direction: column;
  line-height: 1.3;
}

.side-name {
  font-size: 14px;
  font-weight: 600;
  color: var(--color-text-1);
}

.side-role {
  font-size: 10px;
  letter-spacing: 0.16em;
  color: var(--color-text-4);
  text-transform: uppercase;
}

/* 导航 */
.side-nav {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.nav-group-label {
  font-size: 11px;
  letter-spacing: 0.16em;
  color: var(--color-text-5);
  text-transform: uppercase;
  margin: 16px 0 6px;
  padding-left: 10px;
}

.side-link {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 9px 10px;
  border-radius: var(--radius-md);
  font-size: 13.5px;
  color: var(--color-text-3);
  transition: all var(--transition);
}

.side-link:hover {
  background: var(--color-bg-soft);
  color: var(--color-text-1);
}

.side-link.active {
  background: var(--color-primary-soft);
  color: var(--color-primary);
  font-weight: 500;
}

.side-link .el-icon {
  font-size: 15px;
}

/* 底部 */
.side-foot {
  margin-top: 20px;
  padding-top: 14px;
  border-top: 1px solid var(--color-border-1);
}

.back-site {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 10px;
  font-size: 13px;
  color: var(--color-text-4);
  border-radius: var(--radius-md);
  transition: all var(--transition);
}

.back-site:hover {
  color: var(--color-text-1);
  background: var(--color-bg-soft);
}

/* ---------- 内容区 ---------- */
.admin-content {
  flex: 1;
  min-width: 0;
}

.admin-content :deep(.page-title) {
  font-family: "Songti SC", "STSong", Georgia, serif;
  font-size: 26px;
  margin-bottom: 18px;
}

.admin-content :deep(.page-header) {
  margin-bottom: 18px;
}

@media (max-width: 900px) {
  .admin-layout {
    flex-direction: column;
    gap: 16px;
  }
  .admin-side {
    width: 100%;
    position: static;
    padding: 14px;
  }
  .side-nav {
    flex-direction: row;
    flex-wrap: wrap;
    gap: 6px;
  }
  .nav-group-label {
    display: none;
  }
  .side-foot {
    display: none;
  }
}
</style>
