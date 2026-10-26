<template>
  <div class="editor-page page-container">
    <!-- 顶部工具栏 -->
    <div class="editor-topbar">
      <button type="button" class="back-btn" @click="goBack">
        <el-icon><ArrowLeft /></el-icon>
        <span>返回</span>
      </button>

      <div class="topbar-center">
        <span class="topbar-status" :class="isEdit ? 'status-edit' : 'status-new'">
          {{ isEdit ? '编辑文章' : '新文章' }}
        </span>
      </div>

      <div class="topbar-actions">
        <span class="word-count">{{ wordCount }} 字</span>
        <el-button :loading="savingDraft" @click="save(0)">存草稿</el-button>
        <el-button type="primary" :loading="publishing" @click="save(1)">
          {{ isEdit ? '保存并发布' : '发布' }}
        </el-button>
      </div>
    </div>

    <div v-loading="loading" class="editor-body">
      <!-- 主编辑区 -->
      <div class="editor-main">
        <div class="editor-paper">
          <el-input
            v-model="form.title"
            placeholder="无标题文章"
            maxlength="200"
            class="title-input"
          />

          <div class="mode-bar">
            <div class="mode-switch">
              <button
                type="button"
                class="mode-btn"
                :class="{ active: mode === 'edit' }"
                @click="mode = 'edit'"
              >
                编辑
              </button>
              <button
                type="button"
                class="mode-btn"
                :class="{ active: mode === 'preview' }"
                @click="mode = 'preview'"
              >
                预览
              </button>
            </div>
            <span class="mode-hint">支持 Markdown</span>
          </div>

          <el-input
            v-if="mode === 'edit'"
            v-model="form.content"
            type="textarea"
            :rows="22"
            placeholder="# 从这里开始写&#10;&#10;支持 Markdown 语法：标题、**加粗**、- 列表、> 引用、``` 代码块 ```、[链接](url)"
            class="content-input"
            @input="onContentInput"
          />

          <div v-else class="preview-pane md-preview" v-html="previewHtml"></div>
        </div>
      </div>

      <!-- 侧边设置 -->
      <aside class="editor-side">
        <div class="side-card">
          <h3 class="side-title">发布设置</h3>

          <div class="field">
            <label class="field-label">分类</label>
            <el-select v-model="form.categoryId" placeholder="选择分类" clearable class="field-control">
              <el-option v-for="cat in categories" :key="cat.id" :label="cat.name" :value="cat.id" />
            </el-select>
          </div>

          <div class="field">
            <label class="field-label">标签</label>
            <el-select
              v-model="form.tagIds"
              multiple
              collapse-tags
              collapse-tags-tooltip
              placeholder="可多选"
              class="field-control"
            >
              <el-option v-for="tag in tags" :key="tag.id" :label="tag.name" :value="tag.id" />
            </el-select>
          </div>

          <div class="field">
            <label class="field-label">封面图</label>
            <div v-if="form.cover" class="cover-preview">
              <img :src="form.cover" alt="封面预览" />
              <button type="button" class="cover-remove" @click="form.cover = ''">移除</button>
            </div>
            <el-upload
              v-else
              class="cover-uploader"
              :show-file-list="false"
              :before-upload="beforeCoverUpload"
              :http-request="handleCoverUpload"
              accept="image/*"
            >
              <div class="cover-drop">
                <el-icon><Picture /></el-icon>
                <span>上传封面图片</span>
                <small>jpg / png / gif / webp，不超过 5MB</small>
              </div>
            </el-upload>
            <el-input
              v-model="form.cover"
              placeholder="或直接填写图片 URL"
              clearable
              class="field-control cover-url-input"
            />
          </div>

          <div class="field">
            <div class="field-head">
              <label class="field-label">摘要</label>
              <button
                v-if="summaryTouched || summaryOutOfSync"
                type="button"
                class="field-link"
                @click="resetAutoSummary"
              >
                恢复自动
              </button>
              <span v-else class="field-badge">自动跟随正文</span>
            </div>
            <el-input
              v-model="form.summary"
              type="textarea"
              :rows="4"
              maxlength="500"
              show-word-limit
              placeholder="留空则由正文自动生成"
              class="field-control"
              @input="onSummaryInput"
            />
          </div>
        </div>

        <div class="side-card tips-card">
          <h3 class="side-title">写作提示</h3>
          <ul class="tips-list">
            <li>标题简洁，一眼能看懂主题</li>
            <li>开头三句话说明你要解决什么问题</li>
            <li>代码块注明语言，方便阅读</li>
            <li>存草稿可以随时继续编辑</li>
          </ul>
        </div>
      </aside>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { articleApi } from '@/api/article'
import { commonApi } from '@/api/common'
import { renderMarkdown, countWords } from '@/utils/markdown'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Picture } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()

const isEdit = computed(() => !!route.params.id)

const form = reactive({
  title: '',
  content: '',
  categoryId: null,
  tagIds: [],
  cover: '',
  summary: ''
})

const categories = ref([])
const tags = ref([])
const mode = ref('edit')
const loading = ref(false)
const savingDraft = ref(false)
const publishing = ref(false)

const wordCount = computed(() => countWords(form.content))
const previewHtml = computed(() => renderMarkdown(form.content))

// ---------- 摘要：默认跟随正文，手动改写后停止自动同步 ----------
const AUTO_SUMMARY_LEN = 150
const summaryTouched = ref(false)
// 载入时若摘要与正文自动摘要不一致（如历史文章改过正文），提示可一键重建
const summaryOutOfSync = ref(false)

function buildAutoSummary(text) {
  return String(text || '')
    .replace(/```[\s\S]*?```/g, ' ')
    .replace(/[#*`_~>\-]/g, ' ')
    .replace(/\s+/g, ' ')
    .trim()
    .slice(0, AUTO_SUMMARY_LEN)
}

function onContentInput() {
  if (!summaryTouched.value) {
    form.summary = buildAutoSummary(form.content)
  }
}

function onSummaryInput() {
  summaryTouched.value = true
}

function resetAutoSummary() {
  summaryTouched.value = false
  summaryOutOfSync.value = false
  form.summary = buildAutoSummary(form.content)
}

async function loadOptions() {
  const [catRes, tagRes] = await Promise.all([
    commonApi.getCategories().catch(() => []),
    commonApi.getTags().catch(() => [])
  ])
  categories.value = catRes || []
  tags.value = tagRes || []
}

async function loadArticle(id) {
  loading.value = true
  try {
    const data = await articleApi.getArticleForEdit(id)
    form.title = data.title || ''
    form.content = data.content || ''
    form.categoryId = data.categoryId ?? null
    form.tagIds = (data.tags || []).map((t) => t.id)
    form.cover = data.cover || ''
    form.summary = data.summary || ''
    // 进入编辑后，摘要默认继续跟随正文；手动改写后才停止同步
    summaryTouched.value = false
    summaryOutOfSync.value =
      !!form.summary.trim() && form.summary.trim() !== buildAutoSummary(form.content)
  } catch (e) {
    ElMessage.error('加载文章失败')
    router.push('/my/articles')
  } finally {
    loading.value = false
  }
}

// 封面上传前校验
function beforeCoverUpload(file) {
  if (!file.type.startsWith('image/')) {
    ElMessage.error('只能上传图片文件')
    return false
  }
  if (file.size / 1024 / 1024 > 5) {
    ElMessage.error('图片大小不能超过 5MB')
    return false
  }
  return true
}

async function handleCoverUpload({ file }) {
  const formData = new FormData()
  formData.append('file', file)
  try {
    form.cover = await articleApi.uploadCover(formData)
    ElMessage.success('封面上传成功')
  } catch (e) {
    // 错误已由拦截器提示
  }
}

function buildPayload(status) {
  const content = form.content.trim()
  let summary = form.summary.trim()
  if (!summary) {
    summary = content.replace(/[#*`_~>\-]/g, '').replace(/\s+/g, ' ').slice(0, 150)
  }
  return {
    title: form.title.trim(),
    content,
    categoryId: form.categoryId || null,
    tagIds: form.tagIds,
    cover: form.cover.trim() || null,
    summary,
    status
  }
}

function validate() {
  if (!form.title.trim()) {
    ElMessage.warning('请填写文章标题')
    return false
  }
  if (!form.content.trim()) {
    ElMessage.warning('请填写文章正文')
    return false
  }
  return true
}

async function save(status) {
  if (!validate()) return

  if (status === 0) savingDraft.value = true
  else publishing.value = true

  try {
    const payload = buildPayload(status)
    if (isEdit.value) {
      await articleApi.updateArticle(route.params.id, payload)
      ElMessage.success(status === 1 ? '文章已保存并发布' : '已保存为草稿')
    } else {
      await articleApi.createArticle(payload)
      ElMessage.success(status === 1 ? '文章已发布' : '草稿已保存')
    }
    router.push('/my/articles')
  } catch (e) {
    // 错误已由拦截器提示
  } finally {
    savingDraft.value = false
    publishing.value = false
  }
}

function goBack() {
  router.push('/my/articles')
}

onMounted(async () => {
  await loadOptions()
  if (isEdit.value) {
    loadArticle(route.params.id)
  }
})
</script>

<style scoped>
.editor-page {
  max-width: 1180px;
}

/* ---------- 顶部工具栏 ---------- */
.editor-topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 4px 0 18px;
  margin-bottom: 22px;
  border-bottom: 1px solid var(--color-border-1);
}

.back-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  border: none;
  background: none;
  padding: 6px 2px;
  color: var(--color-text-3);
  font-size: 13px;
  font-family: inherit;
  cursor: pointer;
  transition: color var(--transition);
}

.back-btn:hover {
  color: var(--color-text-1);
}

.topbar-center {
  flex: 1;
  text-align: center;
}

.topbar-status {
  font-size: 12px;
  letter-spacing: 0.12em;
  padding: 4px 10px;
  border-radius: 999px;
  text-transform: uppercase;
}

.status-new {
  background: var(--color-primary-soft);
  color: var(--color-primary);
}

.status-edit {
  background: var(--color-accent-light);
  color: var(--color-accent);
}

.topbar-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.word-count {
  font-size: 12px;
  color: var(--color-text-4);
  font-variant-numeric: tabular-nums;
  margin-right: 4px;
}

/* ---------- 布局 ---------- */
.editor-body {
  display: flex;
  gap: 24px;
  align-items: flex-start;
}

.editor-main {
  flex: 1;
  min-width: 0;
}

/* 稿纸：白底、宽松留白 */
.editor-paper {
  background: var(--color-bg-card);
  border: 1px solid var(--color-border-2);
  border-radius: var(--radius-lg);
  padding: 32px 36px 28px;
}

.title-input :deep(.el-input__wrapper) {
  box-shadow: none !important;
  padding: 0;
  background: transparent;
}

.title-input :deep(.el-input__inner) {
  font-family: "Songti SC", "STSong", Georgia, serif;
  font-size: 30px;
  font-weight: 600;
  color: var(--color-text-1);
  height: 52px;
  letter-spacing: 0.01em;
}

.title-input :deep(.el-input__inner::placeholder) {
  color: var(--color-text-5);
  font-weight: 500;
}

/* 模式切换 */
.mode-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 0;
  margin-bottom: 12px;
  border-top: 1px solid var(--color-border-2);
  border-bottom: 1px solid var(--color-border-2);
}

.mode-switch {
  display: flex;
  gap: 2px;
  padding: 3px;
  background: var(--color-bg-soft);
  border-radius: var(--radius-md);
}

.mode-btn {
  padding: 5px 16px;
  border: none;
  border-radius: var(--radius-sm);
  background: transparent;
  color: var(--color-text-3);
  font-size: 13px;
  font-family: inherit;
  cursor: pointer;
  transition: all var(--transition);
}

.mode-btn.active {
  background: #fff;
  color: var(--color-text-1);
  font-weight: 500;
  box-shadow: var(--shadow-sm);
}

.mode-hint {
  font-size: 12px;
  color: var(--color-text-4);
}

/* 正文编辑 */
.content-input :deep(.el-textarea__inner) {
  box-shadow: none !important;
  padding: 0;
  font-family: "JetBrains Mono", Consolas, Monaco, "Courier New", monospace;
  font-size: 14.5px;
  line-height: 1.9;
  color: var(--color-text-2);
  background: transparent;
  resize: vertical;
}

.content-input :deep(.el-textarea__inner::placeholder) {
  color: var(--color-text-5);
}

/* 预览 */
.preview-pane {
  min-height: 540px;
  padding: 8px 0;
}

.md-preview {
  font-size: 16px;
  line-height: 1.9;
  color: var(--color-text-2);
  word-break: break-word;
}

.md-preview :deep(h1),
.md-preview :deep(h2),
.md-preview :deep(h3),
.md-preview :deep(h4) {
  font-family: "Songti SC", "STSong", Georgia, serif;
  margin: 1.8em 0 0.7em;
  color: var(--color-text-1);
  font-weight: 600;
  line-height: 1.45;
}

.md-preview :deep(h1) { font-size: 26px; }
.md-preview :deep(h2) { font-size: 22px; }
.md-preview :deep(h3) { font-size: 18px; }

.md-preview :deep(p) {
  margin: 0 0 1.2em;
}

.md-preview :deep(ul),
.md-preview :deep(ol) {
  margin: 0 0 1.2em;
  padding-left: 1.4em;
}

.md-preview :deep(li) {
  margin-bottom: 0.4em;
}

.md-preview :deep(blockquote) {
  margin: 1.4em 0;
  padding: 4px 0 4px 18px;
  border-left: 3px solid var(--color-primary);
  color: var(--color-text-3);
}

.md-preview :deep(.md-code) {
  margin: 1.2em 0;
  padding: 16px 18px;
  background: #1d2026;
  color: #e6e8eb;
  border-radius: var(--radius-md);
  overflow-x: auto;
  font-size: 13px;
  line-height: 1.7;
  font-family: "JetBrains Mono", Consolas, Monaco, monospace;
}

.md-preview :deep(.md-inline-code) {
  padding: 2px 6px;
  background: var(--color-bg-soft);
  border: 1px solid var(--color-border-2);
  border-radius: var(--radius-sm);
  color: #a8532f;
  font-size: 0.88em;
}

.md-preview :deep(img) {
  max-width: 100%;
  border-radius: var(--radius-md);
}

.md-preview :deep(a) {
  color: var(--color-primary);
}

.md-preview :deep(hr) {
  border: none;
  border-top: 1px solid var(--color-border-1);
  margin: 2em 0;
}

/* ---------- 侧边 ---------- */
.editor-side {
  width: 300px;
  flex-shrink: 0;
}

.side-card {
  background: var(--color-bg-card);
  border: 1px solid var(--color-border-2);
  border-radius: var(--radius-lg);
  padding: 22px;
  margin-bottom: 16px;
}

.side-title {
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 0.16em;
  color: var(--color-text-4);
  text-transform: uppercase;
  margin: 0 0 18px;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--color-border-1);
}

.field + .field {
  margin-top: 18px;
}

.field-label {
  display: block;
  font-size: 13px;
  color: var(--color-text-2);
  margin-bottom: 7px;
  font-weight: 500;
}

.field-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 7px;
}

.field-head .field-label {
  margin-bottom: 0;
}

.field-badge {
  font-size: 11px;
  color: var(--color-primary);
  background: var(--color-primary-soft);
  padding: 1px 8px;
  border-radius: 999px;
}

.field-link {
  border: none;
  background: none;
  padding: 0;
  font-family: inherit;
  font-size: 11px;
  color: var(--color-text-4);
  cursor: pointer;
  text-decoration: underline;
  text-underline-offset: 3px;
  transition: color var(--transition);
}

.field-link:hover {
  color: var(--color-primary);
}

.field-control {
  width: 100%;
}

/* 封面上传 */
.cover-uploader {
  width: 100%;
}

.cover-drop {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
  padding: 22px 12px;
  border: 1px dashed var(--color-border-1);
  border-radius: var(--radius-md);
  color: var(--color-text-4);
  font-size: 13px;
  cursor: pointer;
  transition: all var(--transition);
}

.cover-drop:hover {
  border-color: var(--color-primary);
  color: var(--color-primary);
  background: var(--color-primary-soft);
}

.cover-drop .el-icon {
  font-size: 20px;
}

.cover-drop small {
  font-size: 11px;
  color: var(--color-text-5);
}

.cover-preview {
  position: relative;
  border-radius: var(--radius-md);
  overflow: hidden;
  background: var(--color-bg-soft);
}

.cover-preview img {
  width: 100%;
  height: 120px;
  object-fit: cover;
}

.cover-remove {
  position: absolute;
  top: 8px;
  right: 8px;
  padding: 3px 10px;
  border: none;
  border-radius: 999px;
  background: rgba(0, 0, 0, 0.6);
  color: #fff;
  font-size: 11px;
  font-family: inherit;
  cursor: pointer;
  transition: background var(--transition);
}

.cover-remove:hover {
  background: rgba(0, 0, 0, 0.8);
}

.cover-url-input {
  margin-top: 8px;
}

.tips-list {
  list-style: none;
  margin: 0;
  padding: 0;
}

.tips-list li {
  position: relative;
  padding-left: 16px;
  font-size: 13px;
  line-height: 1.7;
  color: var(--color-text-3);
  margin-bottom: 8px;
}

.tips-list li::before {
  content: '';
  position: absolute;
  left: 0;
  top: 9px;
  width: 4px;
  height: 4px;
  border-radius: 50%;
  background: var(--color-primary);
  opacity: 0.6;
}

@media (max-width: 980px) {
  .editor-body {
    flex-direction: column;
  }
  .editor-side {
    width: 100%;
  }
  .editor-paper {
    padding: 24px 20px;
  }
  .title-input :deep(.el-input__inner) {
    font-size: 24px;
  }
  .topbar-center {
    display: none;
  }
}
</style>
