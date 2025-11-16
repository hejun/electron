<script setup lang="ts">
import { onMounted, ref } from 'vue'
import Aside from '@/components/layout/Aside.vue'
import Header from '@/components/layout/Header.vue'
import { findUserinfo } from '@/api/AuthorizationApi.ts'

const collapse = ref(false)
const currentAudience = ref<string | undefined>()
const currentUser = ref<string | undefined>()

findUserinfo().then(user => {
  currentAudience.value = user.aud_name
  currentUser.value = user.sub
})

onMounted(() => {
  // 小屏幕默认缩起菜单
  const isMobile = window.matchMedia('(max-width: 768px)').matches
  if (isMobile) {
    collapse.value = true
  }
})
</script>

<template>
  <el-container class="layout">
    <Aside :collapse="collapse" :audience="currentAudience" />
    <el-container>
      <el-header class="header">
        <Header :username="currentUser" :collapse="collapse" @collapse="collapse = !collapse" />
      </el-header>
      <el-main>
        <el-scrollbar view-class="main">
          <router-view />
        </el-scrollbar>
      </el-main>
    </el-container>
  </el-container>
</template>

<style scoped lang="scss">
.layout {
  height: 100%;

  .header {
    padding: 0;
  }

  .el-main {
    padding: 0;

    ::v-deep(.main) {
      height: 100%;
      padding: var(--el-main-padding);
      box-sizing: border-box;
    }
  }
}
</style>
