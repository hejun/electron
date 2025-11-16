<script setup lang="ts">
import { useDark } from '@vueuse/core'
import { signOut } from '@/api/AuthorizationApi.ts'

const { collapse } = defineProps({
  collapse: {
    type: Boolean
  },
  username: {
    type: String
  }
})

const emitCollapse = defineEmits(['collapse'])
const toggleCollapse = () => emitCollapse('collapse')

const dark = useDark()
</script>

<template>
  <el-menu mode="horizontal" :ellipsis="false" class="header-menu">
    <el-tooltip :content="collapse ? '点击展开' : '点击收起'">
      <el-button text @click="toggleCollapse" class="el-menu-item">
        <template #icon>
          <el-icon v-if="collapse">
            <i-mdi-align-horizontal-left />
          </el-icon>
          <el-icon v-else>
            <i-mdi-align-horizontal-right />
          </el-icon>
        </template>
      </el-button>
    </el-tooltip>
    <el-menu-item index="theme">
      <el-tooltip content="切换主题">
        <el-switch v-model="dark">
          <template #active-action>
            <el-icon>
              <i-mdi-moon-and-stars />
            </el-icon>
          </template>
          <template #inactive-action>
            <el-icon>
              <i-mdi-white-balance-sunny />
            </el-icon>
          </template>
        </el-switch>
      </el-tooltip>
    </el-menu-item>
    <el-sub-menu index="head-menu">
      <template #title>
        <el-icon>
          <i-mdi-account-outline />
        </el-icon>
        <span>{{ username ?? '请登录' }}</span>
      </template>
      <el-menu-item index="profile">
        <el-icon>
          <i-mdi-account-outline />
        </el-icon>
        <span>个人信息</span>
      </el-menu-item>
      <el-menu-item index="updatePassword">
        <el-icon>
          <i-mdi-password-reset />
        </el-icon>
        <span>修改密码</span>
      </el-menu-item>
      <el-menu-item index="signOut" class="divider" @click="signOut">
        <el-icon>
          <i-mdi-logout-variant />
        </el-icon>
        <span>退出登录</span>
      </el-menu-item>
    </el-sub-menu>
  </el-menu>
</template>

<style scoped lang="scss">
.header-menu {
  & > .el-menu-item {
    border-bottom: none;

    &:nth-child(1) {
      margin-right: auto;
    }
  }

  & > .el-sub-menu.is-active {
    ::v-deep(.el-sub-menu__title) {
      border-bottom: none;
    }
  }

  .el-switch {
    .el-icon {
      margin-left: 6px;
    }
  }
}

.divider {
  border-top: 1px var(--el-border-color) var(--el-border-style);
}
</style>
