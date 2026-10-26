<template>
  <div id="app">
    <el-container class="app-container">
      <!-- 顶部导航栏 -->
      <header class="app-header">
        <div class="header-inner">
          <!-- 品牌标识 -->
          <div class="brand" @click="goHome">
            <span class="brand-mark">博</span>
            <span class="brand-name">博客系统</span>
          </div>

          <!-- 主导航 -->
          <nav class="main-nav">
            <router-link to="/" class="nav-link" :class="{ active: isHome }">首页</router-link>
            <router-link to="/articles" class="nav-link" :class="{ active: isArticles }">文章</router-link>
            <router-link
              v-if="isLoggedIn"
              to="/my/articles"
              class="nav-link"
              :class="{ active: isMyArticles }"
            >
              我的文章
            </router-link>
            <router-link
              v-if="isAdmin"
              to="/admin"
              class="nav-link"
              :class="{ active: isAdminPage }"
            >
              管理后台
            </router-link>
          </nav>

          <!-- 用户区 -->
          <div class="header-user">
            <template v-if="isLoggedIn">
              <el-dropdown trigger="click" @command="handleCommand">
                <button class="user-trigger" type="button">
                  <el-avatar :size="30" :src="userStore.userInfo.avatar || ''" class="user-avatar">
                    {{ avatarLetter }}
                  </el-avatar>
                  <span class="user-name">{{ displayName }}</span>
                  <el-icon class="caret"><ArrowDown /></el-icon>
                </button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="profile">
                      <el-icon><User /></el-icon>个人中心
                    </el-dropdown-item>
                    <el-dropdown-item command="myArticles">
                      <el-icon><Document /></el-icon>我的文章
                    </el-dropdown-item>
                    <el-dropdown-item command="write">
                      <el-icon><EditPen /></el-icon>写文章
                    </el-dropdown-item>
                    <el-dropdown-item v-if="isAdmin" command="admin" divided>
                      <el-icon><Setting /></el-icon>管理后台
                    </el-dropdown-item>
                    <el-dropdown-item command="logout" divided>
                      <el-icon><SwitchButton /></el-icon>退出登录
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </template>
            <template v-else>
              <router-link to="/login" class="login-link">登录</router-link>
              <router-link to="/register" class="register-btn">注册</router-link>
            </template>
          </div>
        </div>
      </header>

      <!-- 主内容区 -->
      <main class="app-main">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </main>

      <!-- 页脚 -->
      <footer class="app-footer">
        <div class="footer-inner">
          <div class="footer-brand">
            <span class="footer-mark">博</span>
            <span class="footer-name">博客系统</span>
          </div>
          <p class="footer-desc">记录技术，沉淀思考。</p>
          <p class="footer-copy">© 2026 Blog System</p>
        </div>
      </footer>
    </el-container>
  </div>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowDown, User, Document, EditPen, Setting, SwitchButton } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const isLoggedIn = computed(() => userStore.isLoggedIn)
const isAdmin = computed(() => userStore.userInfo?.role === 1)

const displayName = computed(
  () => userStore.userInfo?.nickname || userStore.userInfo?.username || '用户'
)
const avatarLetter = computed(() => displayName.value.charAt(0) || 'U')

const isHome = computed(() => route.path === '/')
const isArticles = computed(
  () => route.path === '/articles' || route.path.startsWith('/articles/')
)
const isMyArticles = computed(() => route.path === '/my/articles' || route.path.startsWith('/article/'))
const isAdminPage = computed(() => route.path.startsWith('/admin'))

function goHome() {
  router.push('/')
}

function handleCommand(command) {
  switch (command) {
    case 'profile':
      router.push('/profile')
      break
    case 'myArticles':
      router.push('/my/articles')
      break
    case 'write':
      router.push('/article/new')
      break
    case 'admin':
      router.push('/admin')
      break
    case 'logout':
      ElMessageBox.confirm('确定要退出登录吗？', '提示', {
        confirmButtonText: '退出',
        cancelButtonText: '取消',
        type: 'warning'
      })
        .then(async () => {
          await userStore.logout()
          ElMessage.success('已退出登录')
          router.push('/login')
        })
        .catch(() => {})
      break
  }
}

onMounted(() => {
  userStore.restoreFromStorage()
})
</script>

<style scoped>
.app-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: var(--color-bg-body);
}

/* ---------- 顶部导航：白底细线，安静克制 ---------- */
.app-header {
  position: sticky;
  top: 0;
  z-index: 100;
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: saturate(180%) blur(12px);
  -webkit-backdrop-filter: saturate(180%) blur(12px);
  border-bottom: 1px solid var(--color-border-2);
}

.header-inner {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
  height: 64px;
  display: flex;
  align-items: center;
  gap: 40px;
}

/* 品牌 */
.brand {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  flex-shrink: 0;
  user-select: none;
}

.brand-mark {
  width: 32px;
  height: 32px;
  border-radius: var(--radius-md);
  background: var(--color-text-1);
  color: #fff;
  font-size: 17px;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
  font-family: "Songti SC", "STSong", Georgia, serif;
  letter-spacing: 0;
}

.brand-name {
  font-size: 19px;
  font-weight: 600;
  color: var(--color-text-1);
  letter-spacing: 0.04em;
  font-family: "Songti SC", "STSong", Georgia, "Times New Roman", serif;
}

/* 导航链接 */
.main-nav {
  display: flex;
  align-items: center;
  gap: 28px;
  flex: 1;
  height: 100%;
}

.nav-link {
  position: relative;
  height: 64px;
  display: inline-flex;
  align-items: center;
  font-size: 14px;
  color: var(--color-text-3);
  letter-spacing: 0.02em;
  transition: color var(--transition);
}

.nav-link:hover {
  color: var(--color-text-1);
}

.nav-link.active {
  color: var(--color-text-1);
  font-weight: 500;
}

.nav-link.active::after {
  content: '';
  position: absolute;
  left: 0;
  right: 0;
  bottom: -1px;
  height: 2px;
  background: var(--color-text-1);
  border-radius: 1px;
}

/* 用户区 */
.header-user {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-shrink: 0;
}

.user-trigger {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 8px 4px 4px;
  border: none;
  background: transparent;
  border-radius: 999px;
  cursor: pointer;
  transition: background var(--transition);
  font-family: inherit;
}

.user-trigger:hover {
  background: var(--color-bg-soft);
}

.user-avatar {
  background: var(--color-primary-light);
  color: var(--color-primary);
  font-weight: 600;
  font-size: 13px;
}

.user-name {
  font-size: 14px;
  color: var(--color-text-2);
  max-width: 110px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.caret {
  font-size: 12px;
  color: var(--color-text-4);
}

.login-link {
  font-size: 14px;
  color: var(--color-text-2);
  padding: 6px 4px;
}

.login-link:hover {
  color: var(--color-text-1);
}

.register-btn {
  font-size: 14px;
  color: #fff;
  background: var(--color-text-1);
  padding: 8px 18px;
  border-radius: var(--radius-md);
  transition: background var(--transition);
}

.register-btn:hover {
  background: #000;
  color: #fff;
}

/* ---------- 主内容 ---------- */
.app-main {
  flex: 1;
  padding: 32px 0;
}

/* ---------- 页脚 ---------- */
.app-footer {
  border-top: 1px solid var(--color-border-2);
  background: #fff;
  padding: 32px 0;
}

.footer-inner {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
  text-align: center;
}

.footer-brand {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.footer-mark {
  width: 22px;
  height: 22px;
  border-radius: var(--radius-sm);
  background: var(--color-text-1);
  color: #fff;
  font-size: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-family: "Songti SC", "STSong", Georgia, serif;
}

.footer-name {
  font-size: 14px;
  font-weight: 600;
  color: var(--color-text-2);
  letter-spacing: 0.04em;
  font-family: "Songti SC", "STSong", Georgia, serif;
}

.footer-desc {
  font-size: 13px;
  color: var(--color-text-4);
  margin: 0 0 6px;
}

.footer-copy {
  font-size: 12px;
  color: var(--color-text-5);
  margin: 0;
}

/* ---------- 过渡 ---------- */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.22s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* ---------- 响应式 ---------- */
@media (max-width: 860px) {
  .header-inner {
    gap: 20px;
    padding: 0 16px;
  }
  .main-nav {
    gap: 18px;
  }
  .brand-name,
  .user-name {
    display: none;
  }
}

@media (max-width: 640px) {
  .nav-link {
    font-size: 13px;
  }
  .main-nav {
    gap: 14px;
  }
}
</style>
