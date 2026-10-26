<template>
  <div class="article-page">
    <div v-if="loading" class="state-box">
      <el-icon class="is-loading" size="24"><Loading /></el-icon>
      <span>正在加载</span>
    </div>

    <template v-else-if="article">
      <div class="article-shell">
        <!-- 文章头部 -->
        <header class="article-head">
          <div class="head-kicker">
            <span v-if="article.categoryName" class="head-category" @click="goCategory(article.categoryId)">
              {{ article.categoryName }}
            </span>
            <span class="head-dot" v-if="article.categoryName">·</span>
            <time class="head-date">{{ formatDate(article.createTime) }}</time>
          </div>

          <h1 class="head-title">{{ article.title }}</h1>

          <div class="head-meta">
            <span class="head-author">
              <el-avatar :size="26" :src="article.authorAvatar || ''" class="author-avatar">
                {{ article.authorNickname?.charAt(0) || 'U' }}
              </el-avatar>
              {{ article.authorNickname }}
            </span>
            <span class="meta-sep">·</span>
            <span class="meta-views">{{ article.viewCount }} 阅读</span>

            <button v-if="canEdit" type="button" class="edit-action" @click="goEdit">
              编辑文章
            </button>
          </div>
        </header>

        <!-- 封面 -->
        <figure v-if="article.cover" class="article-cover">
          <img :src="article.cover" :alt="article.title" />
        </figure>

        <!-- 正文 -->
        <div class="article-body">
          <div class="markdown-content" v-html="contentHtml"></div>

          <!-- 标签 -->
          <div v-if="article.tags && article.tags.length" class="article-tags">
            <button
              v-for="tag in article.tags"
              :key="tag.id"
              type="button"
              class="tag-chip"
              @click="goTag(tag.id)"
            >
              # {{ tag.name }}
            </button>
          </div>
        </div>

        <!-- 评论区 -->
        <section class="comment-section">
          <header class="comment-head">
            <h2 class="comment-title">评论</h2>
            <span class="comment-count">{{ commentTotal }}</span>
          </header>

          <!-- 发表评论 -->
          <div v-if="userStore.isLoggedIn" class="comment-editor">
            <div class="editor-row">
              <el-avatar :size="36" :src="userStore.userInfo.avatar || ''" class="author-avatar">
                {{ userStore.userInfo.nickname?.charAt(0) || 'U' }}
              </el-avatar>
              <div class="editor-main">
                <el-input
                  v-model="newComment"
                  type="textarea"
                  :rows="3"
                  :placeholder="replyingTo ? `回复 @${replyingTo.userNickname}…` : '写下你的想法…'"
                  maxlength="500"
                  show-word-limit
                />
                <div class="editor-actions">
                  <span v-if="replyingTo" class="replying-hint">
                    正在回复 {{ replyingTo.userNickname }}
                    <button type="button" class="cancel-reply" @click="cancelReply">取消</button>
                  </span>
                  <el-button type="primary" size="small" :loading="commentLoading" @click="submitComment">
                    发表
                  </el-button>
                </div>
              </div>
            </div>
          </div>

          <div v-else class="comment-login">
            <span>登录后即可参与讨论</span>
            <el-button type="primary" size="small" plain @click="$router.push('/login')">去登录</el-button>
          </div>

          <!-- 评论列表 -->
          <div class="comment-list">
            <div v-if="comments.length === 0" class="comment-empty">
              还没有评论，来说两句吧
            </div>

            <article v-for="comment in comments" :key="comment.id" class="comment-item">
              <el-avatar :size="38" :src="comment.userAvatar || ''" class="author-avatar">
                {{ comment.userNickname?.charAt(0) || 'U' }}
              </el-avatar>

              <div class="comment-main">
                <div class="comment-header">
                  <span class="comment-author">{{ comment.userNickname }}</span>
                  <span class="comment-time">{{ formatDate(comment.createTime) }}</span>
                </div>

                <p class="comment-text">{{ comment.content }}</p>

                <div class="comment-actions">
                  <button type="button" class="action-btn" @click="replyTo(comment)">
                    回复
                    <span v-if="comment.replies && comment.replies.length">({{ comment.replies.length }})</span>
                  </button>
                  <button
                    v-if="canDelete(comment)"
                    type="button"
                    class="action-btn action-danger"
                    @click="removeComment(comment)"
                  >
                    删除
                  </button>
                </div>

                <!-- 回复 -->
                <div v-if="comment.replies && comment.replies.length" class="reply-list">
                  <div v-for="reply in comment.replies" :key="reply.id" class="reply-item">
                    <div class="reply-line">
                      <span class="reply-author">{{ reply.userNickname }}</span>
                      <span class="reply-text">{{ reply.content }}</span>
                    </div>
                    <div class="reply-meta">
                      <span class="comment-time">{{ formatDate(reply.createTime) }}</span>
                      <button
                        v-if="canDelete(reply)"
                        type="button"
                        class="action-btn action-danger"
                        @click="removeComment(reply)"
                      >
                        删除
                      </button>
                    </div>
                  </div>
                </div>
              </div>
            </article>
          </div>
        </section>
      </div>
    </template>

    <div v-else class="state-box">
      <el-icon size="40"><DocumentRemove /></el-icon>
      <p>文章不存在或已被删除</p>
      <el-button type="primary" @click="$router.push('/')">返回首页</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { articleApi } from '@/api/article'
import { commonApi } from '@/api/common'
import { renderMarkdown } from '@/utils/markdown'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Loading, DocumentRemove } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const article = ref(null)
const comments = ref([])
const loading = ref(true)
const newComment = ref('')
const commentLoading = ref(false)
const replyingTo = ref(null)

const contentHtml = computed(() => renderMarkdown(article.value?.content || ''))

// 作者本人或管理员可进入编辑
const canEdit = computed(() => {
  if (!userStore.isLoggedIn || !article.value) return false
  if (userStore.isAdmin) return true
  return article.value.authorId === userStore.userId
})

// 评论总数 = 一级评论 + 其下全部回复
const commentTotal = computed(() =>
  comments.value.reduce((sum, c) => sum + 1 + (c.replies ? c.replies.length : 0), 0)
)

async function loadArticle() {
  loading.value = true
  article.value = null
  try {
    article.value = await articleApi.getArticleDetail(route.params.id)
    await loadComments()
  } catch (e) {
    article.value = null
  } finally {
    loading.value = false
  }
}

async function loadComments() {
  try {
    const data = await commonApi.getComments(route.params.id)
    comments.value = data || []
  } catch (e) {
    comments.value = []
  }
}

async function submitComment() {
  if (!newComment.value.trim()) {
    ElMessage.warning('请输入评论内容')
    return
  }

  commentLoading.value = true
  try {
    await commonApi.createComment({
      articleId: article.value.id,
      content: newComment.value.trim(),
      parentId: replyingTo.value?.id || null
    })
    ElMessage.success('评论发表成功')
    newComment.value = ''
    replyingTo.value = null
    await loadComments()
  } catch (e) {
    // 错误已在拦截器中处理
  } finally {
    commentLoading.value = false
  }
}

function replyTo(comment) {
  replyingTo.value = comment
  newComment.value = ''
}

function cancelReply() {
  replyingTo.value = null
  newComment.value = ''
}

// 管理员可删除任意评论；文章作者可删除自己文章下的评论（含他人评论）
function canDelete(comment) {
  if (!userStore.isLoggedIn) return false
  if (userStore.isAdmin) return true
  return !!article.value && article.value.authorId === userStore.userId
}

async function removeComment(comment) {
  try {
    await ElMessageBox.confirm('确定要删除这条评论吗？其下的回复也会一并删除。', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
  } catch (e) {
    return
  }
  try {
    await commonApi.deleteComment(comment.id)
    ElMessage.success('评论已删除')
    await loadComments()
  } catch (e) {
    // 错误已在拦截器中处理
  }
}

function goTag(id) {
  router.push({ path: '/articles', query: { tagId: id } })
}

function goEdit() {
  router.push(`/article/${article.value.id}/edit`)
}

function goCategory(id) {
  if (id === null || id === undefined) return
  router.push({ path: '/articles', query: { categoryId: id } })
}

function formatDate(dateStr) {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  return `${d.getFullYear()}年${d.getMonth() + 1}月${d.getDate()}日`
}

onMounted(loadArticle)
</script>

<style scoped>
.article-page {
  max-width: 780px;
  margin: 0 auto;
  padding: 0 24px 40px;
}

/* ---------- 头部 ---------- */
.article-head {
  padding: 32px 0 28px;
}

.head-kicker {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  letter-spacing: 0.1em;
  margin-bottom: 18px;
}

.head-category {
  color: var(--color-primary);
  font-weight: 500;
  cursor: pointer;
  text-transform: uppercase;
}

.head-category:hover {
  text-decoration: underline;
  text-underline-offset: 4px;
}

.head-dot {
  color: var(--color-text-5);
}

.head-date {
  color: var(--color-text-4);
}

.head-title {
  font-family: "Songti SC", "STSong", Georgia, "Times New Roman", serif;
  font-size: 38px;
  font-weight: 600;
  line-height: 1.4;
  color: var(--color-text-1);
  margin: 0 0 22px;
  letter-spacing: 0.01em;
}

.head-meta {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 13px;
  color: var(--color-text-4);
}

.head-author {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  color: var(--color-text-2);
}

.author-avatar {
  background: var(--color-primary-light);
  color: var(--color-primary);
  font-size: 12px;
  font-weight: 600;
  flex-shrink: 0;
}

.meta-sep {
  color: var(--color-text-5);
}

.edit-action {
  margin-left: auto;
  border: none;
  background: none;
  padding: 0;
  font-family: inherit;
  font-size: 13px;
  color: var(--color-text-4);
  cursor: pointer;
  transition: color var(--transition);
}

.edit-action:hover {
  color: var(--color-primary);
}

/* ---------- 封面 ---------- */
.article-cover {
  margin: 0 0 32px;
  border-radius: var(--radius-lg);
  overflow: hidden;
  background: var(--color-bg-soft);
}

.article-cover img {
  width: 100%;
  max-height: 380px;
  object-fit: cover;
}

/* ---------- 正文 ---------- */
.article-body {
  padding-bottom: 36px;
  border-bottom: 1px solid var(--color-border-1);
}

.markdown-content {
  font-size: 16.5px;
  line-height: 1.95;
  color: var(--color-text-2);
  letter-spacing: 0.01em;
  word-break: break-word;
}

.markdown-content :deep(h1),
.markdown-content :deep(h2),
.markdown-content :deep(h3),
.markdown-content :deep(h4) {
  font-family: "Songti SC", "STSong", Georgia, serif;
  color: var(--color-text-1);
  font-weight: 600;
  letter-spacing: 0.01em;
  margin: 2em 0 0.8em;
  line-height: 1.45;
}

.markdown-content :deep(h1) { font-size: 27px; }
.markdown-content :deep(h2) { font-size: 23px; }
.markdown-content :deep(h3) { font-size: 19px; }
.markdown-content :deep(h4) { font-size: 17px; }

.markdown-content :deep(p) {
  margin: 0 0 1.3em;
}

.markdown-content :deep(ul),
.markdown-content :deep(ol) {
  margin: 0 0 1.3em;
  padding-left: 1.4em;
}

.markdown-content :deep(li) {
  margin-bottom: 0.5em;
}

.markdown-content :deep(blockquote) {
  margin: 1.6em 0;
  padding: 4px 0 4px 20px;
  border-left: 3px solid var(--color-primary);
  color: var(--color-text-3);
  font-style: normal;
}

.markdown-content :deep(blockquote p:last-child) {
  margin-bottom: 0;
}

.markdown-content :deep(.md-code) {
  margin: 1.4em 0;
  padding: 18px 20px;
  background: #1d2026;
  color: #e6e8eb;
  border-radius: var(--radius-md);
  overflow-x: auto;
  font-size: 13.5px;
  line-height: 1.7;
  font-family: "JetBrains Mono", Consolas, Monaco, "Courier New", monospace;
}

.markdown-content :deep(.md-inline-code) {
  padding: 2px 6px;
  background: var(--color-bg-soft);
  border: 1px solid var(--color-border-2);
  border-radius: var(--radius-sm);
  color: #a8532f;
  font-size: 0.88em;
  font-family: "JetBrains Mono", Consolas, Monaco, monospace;
}

.markdown-content :deep(img) {
  max-width: 100%;
  border-radius: var(--radius-md);
  margin: 1.4em 0;
}

.markdown-content :deep(a) {
  color: var(--color-primary);
  border-bottom: 1px solid var(--color-primary-light);
  transition: border-color var(--transition);
}

.markdown-content :deep(a:hover) {
  border-bottom-color: var(--color-primary);
}

.markdown-content :deep(hr) {
  border: none;
  border-top: 1px solid var(--color-border-1);
  margin: 2.4em 0;
}

.markdown-content :deep(strong) {
  font-weight: 600;
  color: var(--color-text-1);
}

/* ---------- 标签 ---------- */
.article-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 36px;
}

.tag-chip {
  padding: 5px 13px;
  border: 1px solid var(--color-border-1);
  border-radius: 999px;
  background: transparent;
  color: var(--color-text-3);
  font-size: 12.5px;
  font-family: inherit;
  cursor: pointer;
  transition: all var(--transition);
}

.tag-chip:hover {
  border-color: var(--color-primary);
  color: var(--color-primary);
  background: var(--color-primary-soft);
}

/* ---------- 评论 ---------- */
.comment-section {
  padding-top: 40px;
}

.comment-head {
  display: flex;
  align-items: baseline;
  gap: 10px;
  margin-bottom: 24px;
}

.comment-title {
  font-family: "Songti SC", "STSong", Georgia, serif;
  font-size: 21px;
  font-weight: 600;
  color: var(--color-text-1);
  margin: 0;
}

.comment-count {
  font-size: 13px;
  color: var(--color-text-4);
  font-variant-numeric: tabular-nums;
}

.comment-editor {
  margin-bottom: 32px;
}

.editor-row {
  display: flex;
  gap: 12px;
}

.editor-main {
  flex: 1;
  min-width: 0;
}

.editor-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 10px;
}

.replying-hint {
  font-size: 12px;
  color: var(--color-text-4);
}

.cancel-reply {
  border: none;
  background: none;
  padding: 0 0 0 8px;
  color: var(--color-primary);
  font-size: 12px;
  font-family: inherit;
  cursor: pointer;
  text-decoration: underline;
  text-underline-offset: 3px;
}

.comment-login {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 16px 20px;
  margin-bottom: 28px;
  background: var(--color-bg-card);
  border: 1px solid var(--color-border-2);
  border-radius: var(--radius-lg);
  font-size: 14px;
  color: var(--color-text-3);
}

.comment-list {
  display: flex;
  flex-direction: column;
}

.comment-empty {
  padding: 32px 0;
  text-align: center;
  font-size: 14px;
  color: var(--color-text-4);
}

.comment-item {
  display: flex;
  gap: 14px;
  padding: 22px 0;
  border-top: 1px solid var(--color-border-2);
}

.comment-main {
  flex: 1;
  min-width: 0;
}

.comment-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
}

.comment-author {
  font-size: 14px;
  font-weight: 500;
  color: var(--color-text-1);
}

.comment-time {
  font-size: 12px;
  color: var(--color-text-4);
}

.comment-text {
  font-size: 15px;
  line-height: 1.8;
  color: var(--color-text-2);
  margin: 0 0 10px;
  white-space: pre-wrap;
  word-break: break-word;
}

.comment-actions {
  display: flex;
  gap: 16px;
}

.action-btn {
  border: none;
  background: none;
  padding: 0;
  font-size: 12.5px;
  font-family: inherit;
  color: var(--color-text-4);
  cursor: pointer;
  transition: color var(--transition);
}

.action-btn:hover {
  color: var(--color-primary);
}

.action-danger:hover {
  color: #c0392b;
}

/* 回复 */
.reply-list {
  margin-top: 14px;
  padding-left: 14px;
  border-left: 2px solid var(--color-border-2);
}

.reply-item {
  padding: 8px 0;
}

.reply-item + .reply-item {
  border-top: 1px dashed var(--color-border-2);
}

.reply-line {
  font-size: 14px;
  line-height: 1.75;
  color: var(--color-text-2);
}

.reply-author {
  font-weight: 500;
  color: var(--color-text-1);
  margin-right: 6px;
}

.reply-text {
  color: var(--color-text-3);
}

.reply-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 4px;
}

/* ---------- 状态 ---------- */
.state-box {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 14px;
  padding: 120px 20px;
  color: var(--color-text-4);
  font-size: 14px;
}

.state-box p {
  margin: 0;
}

@media (max-width: 640px) {
  .article-page {
    padding: 0 16px 32px;
  }
  .head-title {
    font-size: 27px;
  }
  .markdown-content {
    font-size: 16px;
  }
}
</style>
