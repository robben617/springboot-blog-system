<template>
  <div class="not-found">
    <div class="nf-inner">
      <p class="nf-code">404</p>
      <h1 class="nf-title">页面不存在</h1>
      <p class="nf-desc">你访问的页面可能已被移除，或者地址有误。</p>
      <div class="nf-actions">
        <el-button type="primary" @click="$router.push('/')">
          <el-icon><House /></el-icon>
          返回首页
        </el-button>
        <el-button @click="$router.back()">
          <el-icon><ArrowLeft /></el-icon>
          返回上一页
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { House, ArrowLeft } from '@element-plus/icons-vue'
</script>

<style scoped>
.not-found {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: calc(100vh - 64px - 130px);
  padding: 40px 20px;
}

.nf-inner {
  text-align: center;
}

.nf-code {
  font-family: "Songti SC", "STSong", Georgia, serif;
  font-size: 88px;
  font-weight: 600;
  line-height: 1;
  color: var(--color-text-5);
  margin: 0 0 20px;
  letter-spacing: 0.04em;
}

.nf-title {
  font-family: "Songti SC", "STSong", Georgia, serif;
  font-size: 26px;
  font-weight: 600;
  color: var(--color-text-1);
  margin: 0 0 10px;
}

.nf-desc {
  font-size: 14px;
  color: var(--color-text-4);
  margin: 0 0 28px;
}

.nf-actions {
  display: flex;
  gap: 12px;
  justify-content: center;
}
</style>
