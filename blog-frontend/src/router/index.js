import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/store/user'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('@/views/Home.vue'),
    meta: { title: '首页' }
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录', guestOnly: true }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register.vue'),
    meta: { title: '注册', guestOnly: true }
  },
  {
    path: '/profile',
    name: 'Profile',
    component: () => import('@/views/Profile.vue'),
    meta: { title: '个人中心', requiresAuth: true }
  },
  {
    path: '/my/articles',
    name: 'MyArticles',
    component: () => import('@/views/MyArticles.vue'),
    meta: { title: '我的文章', requiresAuth: true }
  },
  {
    path: '/article/new',
    name: 'ArticleCreate',
    component: () => import('@/views/ArticleEdit.vue'),
    meta: { title: '写文章', requiresAuth: true }
  },
  {
    path: '/article/:id/edit',
    name: 'ArticleEdit',
    component: () => import('@/views/ArticleEdit.vue'),
    meta: { title: '编辑文章', requiresAuth: true }
  },
  {
    path: '/admin',
    component: () => import('@/views/Admin.vue'),
    redirect: '/admin/dashboard',
    // 权限声明放在父路由，子路由通过 to.meta 合并继承
    meta: { title: '管理后台', requiresAuth: true, requiresAdmin: true },
    children: [
      {
        path: 'dashboard',
        name: 'AdminDashboard',
        component: () => import('@/views/admin/AdminDashboard.vue'),
        meta: { title: '概览' }
      },
      {
        path: 'users',
        name: 'AdminUsers',
        component: () => import('@/views/admin/AdminUsers.vue'),
        meta: { title: '用户管理' }
      },
      {
        path: 'articles',
        name: 'AdminArticles',
        component: () => import('@/views/admin/AdminArticles.vue'),
        meta: { title: '文章管理' }
      },
      {
        path: 'comments',
        name: 'AdminComments',
        component: () => import('@/views/admin/AdminComments.vue'),
        meta: { title: '评论管理' }
      },
      {
        path: 'categories',
        name: 'AdminCategories',
        component: () => import('@/views/admin/AdminCategories.vue'),
        meta: { title: '分类管理' }
      },
      {
        path: 'tags',
        name: 'AdminTags',
        component: () => import('@/views/admin/AdminTags.vue'),
        meta: { title: '标签管理' }
      },
      {
        path: 'announcements',
        name: 'AdminAnnouncements',
        component: () => import('@/views/admin/AdminAnnouncements.vue'),
        meta: { title: '公告管理' }
      },
      {
        path: 'friend-links',
        name: 'AdminFriendLinks',
        component: () => import('@/views/admin/AdminFriendLinks.vue'),
        meta: { title: '友链管理' }
      }
    ]
  },
  {
    path: '/articles',
    name: 'ArticleList',
    component: () => import('@/views/ArticleList.vue'),
    meta: { title: '文章列表' }
  },
  {
    path: '/articles/:id',
    name: 'ArticleDetail',
    component: () => import('@/views/ArticleDetail.vue'),
    meta: { title: '文章详情' }
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/NotFound.vue'),
    meta: { title: '页面不存在' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 全局前置守卫：路由权限控制
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()

  // 设置页面标题
  document.title = to.meta.title ? `${to.meta.title} - 博客系统` : '博客系统'

  // 1. 需要管理员权限的页面
  if (to.meta.requiresAdmin) {
    if (!userStore.isLoggedIn) {
      next({ path: '/login', query: { redirect: to.fullPath } })
      return
    }
    if (userStore.userInfo.role !== 1) {
      next({ path: '/' })
      return
    }
    next()
    return
  }

  // 2. 需要登录的页面
  if (to.meta.requiresAuth) {
    if (!userStore.isLoggedIn) {
      next({ path: '/login', query: { redirect: to.fullPath } })
      return
    }
    next()
    return
  }

  // 3. 仅游客可访问的页面（登录、注册）
  if (to.meta.guestOnly && userStore.isLoggedIn) {
    next({ path: '/' })
    return
  }

  // 4. 其他页面直接放行
  next()
})

// 全局后置守卫：滚动到顶部
router.afterEach(() => {
  window.scrollTo(0, 0)
})

export default router
