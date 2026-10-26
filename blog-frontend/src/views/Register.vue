<template>
  <div class="auth-page">
    <div class="auth-card">
      <header class="auth-head">
        <span class="auth-mark">博</span>
        <h1 class="auth-title">创建账号</h1>
        <p class="auth-desc">加入我们，开始记录你的想法</p>
      </header>

      <el-form
        ref="registerFormRef"
        :model="registerForm"
        :rules="registerRules"
        class="auth-form"
      >
        <el-form-item prop="username">
          <el-input
            v-model="registerForm.username"
            placeholder="用户名（3-20 位，字母 / 数字 / 下划线）"
            size="large"
            :prefix-icon="User"
          />
        </el-form-item>

        <el-form-item prop="email">
          <el-input
            v-model="registerForm.email"
            placeholder="邮箱"
            size="large"
            :prefix-icon="Message"
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="registerForm.password"
            type="password"
            placeholder="密码（6-20 位，须含字母和数字）"
            size="large"
            :prefix-icon="Lock"
            show-password
          />
        </el-form-item>

        <el-form-item prop="confirmPassword">
          <el-input
            v-model="registerForm.confirmPassword"
            type="password"
            placeholder="确认密码"
            size="large"
            :prefix-icon="Lock"
            show-password
            @keyup.enter="handleRegister"
          />
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            size="large"
            class="submit-btn"
            :loading="loading"
            @click="handleRegister"
          >
            注册
          </el-button>
        </el-form-item>
      </el-form>

      <div class="auth-foot">
        <span>已有账号？</span>
        <router-link to="/login" class="auth-link">直接登录</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { ElMessage } from 'element-plus'
import { User, Lock, Message } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

const registerFormRef = ref(null)
const loading = ref(false)

const registerForm = reactive({
  username: '',
  email: '',
  password: '',
  confirmPassword: ''
})

// 校验确认密码
const validateConfirmPassword = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== registerForm.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const registerRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度 3-20 位', trigger: 'blur' },
    {
      pattern: /^[a-zA-Z0-9_]+$/,
      message: '用户名仅允许字母、数字和下划线',
      trigger: 'blur'
    }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度 6-20 位', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (!value) return callback()
        const hasLetter = /[a-zA-Z]/.test(value)
        const hasDigit = /\d/.test(value)
        if (!hasLetter || !hasDigit) {
          callback(new Error('密码须同时包含字母和数字'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  confirmPassword: [
    { required: true, validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

async function handleRegister() {
  if (!registerFormRef.value) return

  try {
    await registerFormRef.value.validate()
  } catch {
    return
  }

  loading.value = true
  try {
    await userStore.register({
      username: registerForm.username,
      password: registerForm.password,
      email: registerForm.email
    })
    ElMessage.success('注册成功！请登录')
    router.push('/login')
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
  padding: 48px 20px;
  min-height: calc(100vh - 64px - 130px);
}

.auth-card {
  width: 100%;
  max-width: 420px;
  background: var(--color-bg-card);
  border: 1px solid var(--color-border-2);
  border-radius: var(--radius-xl);
  padding: 40px 40px 32px;
  box-shadow: var(--shadow-sm);
}

.auth-head {
  text-align: center;
  margin-bottom: 28px;
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
  margin-bottom: 18px;
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
  padding-top: 18px;
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
