<script setup lang="ts">
import { onMounted, ref } from 'vue'
import Header from '@/components/layout/Header.vue'
import { findUserinfo } from '@/api/AuthorizationApi.ts'
import { findTenantByIssuer } from '@/api/Tenant.ts'

const collapse = ref(false)
const currentAudience = ref<string | undefined>()
const currentUser = ref<string | undefined>()
const currentCopyright = ref<string | undefined>()

findUserinfo()
  .then(user => {
    currentAudience.value = user.aud_name
    currentUser.value = user.sub
    return user.iss
  })
  .then(iss => {
    if (iss) {
      findTenantByIssuer(iss).then(res => (currentCopyright.value = res.copyright))
    }
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
    <el-header class="header">
      <Header :audience="currentAudience" :username="currentUser" />
    </el-header>
    <el-main>
      <el-scrollbar view-class="main">
        <router-view />
      </el-scrollbar>
    </el-main>
    <el-footer>
      <Footer :copyright="currentCopyright" />
    </el-footer>
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
  .el-footer {
    padding: 0;
  }
}
</style>
