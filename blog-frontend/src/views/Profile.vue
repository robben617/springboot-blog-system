<template>
  <div class="page-container profile-page">
    <header class="profile-head">
      <p class="profile-eyebrow">Account</p>
      <h1 class="profile-title">个人中心</h1>
    </header>

    <el-row :gutter="24">
      <!-- 左侧：资料概览 -->
      <el-col :xs="24" :sm="24" :md="8">
        <div class="card profile-card">
          <div class="avatar-wrap">
            <el-avatar :size="92" :src="userStore.userInfo.avatar || ''" class="main-avatar">
              {{ initial }}
            </el-avatar>
            <el-upload
              class="avatar-uploader"
              :show-file-list="false"
              :before-upload="beforeAvatarUpload"
              :http-request="handleAvatarUpload"
              accept="image/*"
            >
              <div class="upload-hint">
                <el-icon size="15"><Camera /></el-icon>
              </div>
            </el-upload>
          </div>

          <h2 class="profile-name">{{ userStore.userInfo.nickname || userStore.userInfo.username }}</h2>
          <p class="profile-handle">@{{ userStore.userInfo.username }}</p>

          <span class="role-pill" :class="userStore.userInfo.role === 1 ? 'role-admin' : 'role-user'">
            {{ userStore.userInfo.role === 1 ? '管理员' : '写作者' }}
          </span>

          <div class="profile-stats">
            <div class="stat">
              <span class="stat-value">{{ stats.articleCount }}</span>
              <span class="stat-label">文章</span>
            </div>
            <div class="stat">
              <span class="stat-value">{{ stats.commentCount }}</span>
              <span class="stat-label">评论</span>
            </div>
            <div class="stat">
              <span class="stat-value">{{ stats.viewCount }}</span>
              <span class="stat-label">浏览</span>
            </div>
          </div>
        </div>

        <div class="card bio-card">
          <h3 class="bio-title">个人简介</h3>
          <p class="bio-text">{{ userStore.userInfo.bio || '这个人很懒，什么都没有留下。' }}</p>
          <div class="bio-meta">
            <span>草稿 {{ stats.draftCount }} 篇</span>
            <span class="bio-dot">·</span>
            <span>已发布 {{ stats.publishedCount }} 篇</span>
          </div>
        </div>
      </el-col>

      <!-- 右侧：编辑区 -->
      <el-col :xs="24" :sm="24" :md="16">
        <div class="card edit-card">
          <el-tabs v-model="activeTab" class="profile-tabs">
            <el-tab-pane label="基本资料" name="profile">
              <el-form
                ref="profileFormRef"
                :model="profileForm"
                :rules="profileRules"
                label-position="top"
                class="profile-form"
              >
                <el-form-item label="昵称" prop="nickname">
                  <el-input v-model="profileForm.nickname" placeholder="请输入昵称" maxlength="20" show-word-limit />
                </el-form-item>

                <el-form-item label="邮箱" prop="email">
                  <el-input v-model="profileForm.email" placeholder="请输入邮箱" />
                </el-form-item>

                <el-form-item label="简介" prop="bio">
                  <el-input
                    v-model="profileForm.bio"
                    type="textarea"
                    :rows="4"
                    placeholder="介绍一下你自己"
                    maxlength="200"
                    show-word-limit
                  />
                </el-form-item>

                <el-form-item>
                  <el-button type="primary" :loading="profileLoading" @click="handleSaveProfile">
                    保存修改
                  </el-button>
                  <el-button @click="resetProfileForm">重置</el-button>
                </el-form-item>
              </el-form>
            </el-tab-pane>

            <el-tab-pane label="修改密码" name="password">
              <el-form
                ref="passwordFormRef"
                :model="passwordForm"
                :rules="passwordRules"
                label-position="top"
                class="profile-form"
              >
                <el-form-item label="原密码" prop="oldPassword">
                  <el-input v-model="passwordForm.oldPassword" type="password" placeholder="请输入原密码" show-password />
                </el-form-item>

                <el-form-item label="新密码" prop="newPassword">
                  <el-input v-model="passwordForm.newPassword" type="password" placeholder="6-20 位，含字母和数字" show-password />
                </el-form-item>

                <el-form-item label="确认新密码" prop="confirmPassword">
                  <el-input v-model="passwordForm.confirmPassword" type="password" placeholder="请再次输入新密码" show-password />
                </el-form-item>

                <el-form-item>
                  <el-button type="primary" :loading="passwordLoading" @click="handleChangePassword">
                    确认修改
                  </el-button>
                  <el-button @click="resetPasswordForm">重置</el-button>
                </el-form-item>
              </el-form>
            </el-tab-pane>
          </el-tabs>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useUserStore } from '@/store/user'
import { userApi } from '@/api/user'
import { ElMessage } from 'element-plus'
import { Camera } from '@element-plus/icons-vue'

const userStore = useUserStore()

const activeTab = ref('profile')
const profileFormRef = ref(null)
const passwordFormRef = ref(null)
const profileLoading = ref(false)
const passwordLoading = ref(false)

const stats = reactive({
  articleCount: 0,
  publishedCount: 0,
  draftCount: 0,
  commentCount: 0,
  viewCount: 0
})

const initial = computed(
  () =>
    userStore.userInfo.nickname?.charAt(0) ||
    userStore.userInfo.username?.charAt(0) ||
    'U'
)

const profileForm = reactive({
  nickname: '',
  email: '',
  bio: ''
})

const profileRules = {
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' },
    { min: 2, max: 20, message: '昵称长度 2-20 位', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }
  ]
}

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const validateConfirmPassword = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请再次输入新密码'))
  } else if (value !== passwordForm.newPassword) {
    callback(new Error('两次输入的新密码不一致'))
  } else {
    callback()
  }
}

const passwordRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
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
  confirmPassword: [{ required: true, validator: validateConfirmPassword, trigger: 'blur' }]
}

// 头像上传前校验
function beforeAvatarUpload(file) {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isImage) {
    ElMessage.error('只能上传图片文件')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB')
    return false
  }
  return true
}

// 自定义头像上传
async function handleAvatarUpload({ file }) {
  const formData = new FormData()
  formData.append('file', file)

  try {
    await userStore.uploadAvatar(formData)
    ElMessage.success('头像更新成功')
  } catch (e) {
    ElMessage.error('头像上传失败')
  }
}

async function loadStats() {
  try {
    const data = await userApi.getStats()
    Object.assign(stats, data)
  } catch (e) {
    // 错误已由拦截器提示
  }
}

async function handleSaveProfile() {
  if (!profileFormRef.value) return
  try {
    await profileFormRef.value.validate()
  } catch {
    return
  }

  profileLoading.value = true
  try {
    await userStore.updateProfile(profileForm)
    ElMessage.success('资料更新成功')
  } finally {
    profileLoading.value = false
  }
}

function resetProfileForm() {
  loadUserInfo()
}

async function handleChangePassword() {
  if (!passwordFormRef.value) return
  try {
    await passwordFormRef.value.validate()
  } catch {
    return
  }

  passwordLoading.value = true
  try {
    await userStore.changePassword({
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword
    })
    ElMessage.success('密码修改成功')
    resetPasswordForm()
  } finally {
    passwordLoading.value = false
  }
}

function resetPasswordForm() {
  passwordForm.oldPassword = ''
  passwordForm.newPassword = ''
  passwordForm.confirmPassword = ''
}

function loadUserInfo() {
  const info = userStore.userInfo
  profileForm.nickname = info.nickname || ''
  profileForm.email = info.email || ''
  profileForm.bio = info.bio || ''
}

onMounted(() => {
  loadUserInfo()
  loadStats()
  userStore
    .fetchUserInfo()
    .then(() => {
      loadUserInfo()
    })
    .catch(() => {})
})
</script>

<style scoped>
.profile-page {
  max-width: 1040px;
}

/* ---------- 页头 ---------- */
.profile-head {
  padding: 8px 0 26px;
  margin-bottom: 24px;
  border-bottom: 1px solid var(--color-border-1);
}

.profile-eyebrow {
  font-size: 12px;
  letter-spacing: 0.22em;
  color: var(--color-primary);
  font-weight: 500;
  margin: 0 0 10px;
  text-transform: uppercase;
}

.profile-title {
  font-family: "Songti SC", "STSong", Georgia, serif;
  font-size: 32px;
  font-weight: 600;
  color: var(--color-text-1);
  margin: 0;
  letter-spacing: 0.01em;
}

/* ---------- 左栏 ---------- */
.profile-card {
  text-align: center;
  margin-bottom: 16px;
  padding: 32px 24px 24px;
}

.avatar-wrap {
  position: relative;
  display: inline-block;
  margin-bottom: 18px;
}

.main-avatar {
  border: 1px solid var(--color-border-1);
  background: var(--color-primary-light);
  color: var(--color-primary);
  font-size: 34px;
  font-weight: 600;
  font-family: "Songti SC", "STSong", Georgia, serif;
}

.avatar-uploader {
  position: absolute;
  right: -2px;
  bottom: -2px;
}

.upload-hint {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 30px;
  height: 30px;
  border-radius: 50%;
  background: var(--color-text-1);
  color: #fff;
  cursor: pointer;
  border: 2px solid var(--color-bg-card);
  transition: background var(--transition);
}

.upload-hint:hover {
  background: #000;
}

.profile-name {
  font-family: "Songti SC", "STSong", Georgia, serif;
  font-size: 21px;
  font-weight: 600;
  color: var(--color-text-1);
  margin: 0 0 4px;
}

.profile-handle {
  font-size: 13px;
  color: var(--color-text-4);
  margin: 0 0 14px;
}

.role-pill {
  display: inline-block;
  padding: 3px 12px;
  border-radius: 999px;
  font-size: 12px;
  letter-spacing: 0.04em;
}

.role-admin {
  background: var(--color-accent-light);
  color: var(--color-accent);
}

.role-user {
  background: var(--color-primary-soft);
  color: var(--color-primary);
}

.profile-stats {
  display: flex;
  justify-content: space-around;
  margin-top: 24px;
  padding-top: 20px;
  border-top: 1px solid var(--color-border-2);
}

.stat {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.stat-value {
  font-family: "Songti SC", "STSong", Georgia, serif;
  font-size: 21px;
  font-weight: 600;
  color: var(--color-text-1);
  font-variant-numeric: tabular-nums;
}

.stat-label {
  font-size: 11px;
  letter-spacing: 0.12em;
  color: var(--color-text-4);
}

/* 简介 */
.bio-card {
  padding: 22px 24px;
}

.bio-title {
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 0.16em;
  color: var(--color-text-4);
  text-transform: uppercase;
  margin: 0 0 12px;
}

.bio-text {
  font-size: 14px;
  line-height: 1.8;
  color: var(--color-text-3);
  margin: 0 0 12px;
  white-space: pre-wrap;
}

.bio-meta {
  font-size: 12px;
  color: var(--color-text-4);
}

.bio-dot {
  margin: 0 6px;
  color: var(--color-text-5);
}

/* ---------- 右栏 ---------- */
.edit-card {
  padding: 8px 28px 28px;
}

.profile-form {
  max-width: 520px;
  padding-top: 12px;
}

.profile-form :deep(.el-form-item__label) {
  font-size: 13px;
  color: var(--color-text-2);
  font-weight: 500;
  padding-bottom: 6px;
}

.profile-tabs :deep(.el-tabs__header) {
  margin-bottom: 8px;
}

.profile-tabs :deep(.el-tabs__item) {
  font-size: 14px;
  color: var(--color-text-3);
}

.profile-tabs :deep(.el-tabs__item.is-active) {
  color: var(--color-text-1);
  font-weight: 500;
}

.profile-tabs :deep(.el-tabs__active-bar) {
  background-color: var(--color-text-1);
}

@media (max-width: 992px) {
  .profile-head {
    padding-bottom: 20px;
  }
  .profile-title {
    font-size: 26px;
  }
  .profile-card {
    margin-bottom: 16px;
  }
  .edit-card {
    padding: 8px 20px 22px;
  }
}
</style>
