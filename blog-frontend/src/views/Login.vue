<template>
  <div class="auth-page">
    <div class="auth-card">
      <header class="auth-head">
        <span class="auth-mark">博</span>
        <h1 class="auth-title">欢迎回来</h1>
        <p class="auth-desc">登录以继续你的写作与阅读</p>
      </header>

      <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="loginRules"
        class="auth-form"
        @keyup.enter="handleLogin"
      >
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="用户名"
            size="large"
            :prefix-icon="User"
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="密码"
            size="large"
            :prefix-icon="Lock"
            show-password
          />
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            size="large"
            class="submit-btn"
            :loading="loading"
            @click="handleLogin"
          >
            登录
          </el-button>
        </el-form-item>
      </el-form>

      <div class="auth-foot">
        <span>还没有账号？</span>
        <router-link to="/register" class="auth-link">创建账号</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const loginFormRef = ref(null)
const loading = ref(false)

const loginForm = reactive({
  username: '',
  password: ''
})

const loginRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度 3-20 位', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度 6-20 位', trigger: 'blur' }
  ]
}

async function handleLogin() {
  if (!loginFormRef.value) return

  try {
    await loginFormRef.value.validate()
  } catch {
    return
  }

  loading.value = true
  try {
    await userStore.login(loginForm)
    ElMessage.success('登录成功')
    const redirect = route.query.redirect || '/'
    router.push(redirect)
  } catch (e) {
    // 错误已在 axios 拦截器中提示
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-page {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 56px 20px;
  min-height: calc(100vh - 64px - 130px);
}

.auth-card {
  width: 100%;
  max-width: 400px;
  background: var(--color-bg-card);
  border: 1px solid var(--color-border-2);
  border-radius: var(--radius-xl);
  padding: 44px 40px 36px;
  box-shadow: var(--shadow-sm);
}

.auth-head {
  text-align: center;
  margin-bottom: 32px;
}

.auth-mark {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  border-radius: var(--radius-md);
  background: var(--color-text-1);
  color: #fff;
  font-size: 20px;
  font-family: "Songti SC", "STSong", Georgia, serif;
  margin-bottom: 18px;
}

.auth-title {
  font-family: "Songti SC", "STSong", Georgia, serif;
  font-size: 26px;
  font-weight: 600;
  color: var(--color-text-1);
  margin: 0 0 8px;
  letter-spacing: 0.01em;
}

.auth-desc {
  font-size: 13.5px;
  color: var(--color-text-4);
  margin: 0;
}

.auth-form :deep(.el-form-item) {
  margin-bottom: 20px;
}

.submit-btn {
  width: 100%;
  height: 44px;
  font-size: 15px;
  letter-spacing: 0.06em;
  margin-top: 4px;
}

.auth-foot {
  text-align: center;
  font-size: 13.5px;
  color: var(--color-text-4);
  padding-top: 20px;
  border-top: 1px solid var(--color-border-2);
}

.auth-link {
  color: var(--color-primary);
  margin-left: 6px;
  font-weight: 500;
}

.auth-link:hover {
  text-decoration: underline;
  text-underline-offset: 3px;
}
</style>
