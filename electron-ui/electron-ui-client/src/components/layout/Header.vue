<script setup lang="ts">
import { useRoute } from 'vue-router'
import { useDark } from '@vueuse/core'
import { signOut } from '@/api/AuthorizationApi.ts'

defineProps({
  audience: {
    type: String
  },
  username: {
    type: String
  }
})

const route = useRoute()
const dark = useDark()
const isMobile = window.matchMedia('(max-width: 768px)').matches
const active = route.fullPath.replace('/', '')
</script>

<template>
  <div class="header-container">
    <el-menu
      mode="horizontal"
      router
      :ellipsis="false"
      class="header-menu"
      :default-active="active"
      :class="{ collapse: isMobile }"
    >
      <div class="el-menu-item brand">
        <el-avatar>{{ audience?.substring(0, 1) }}</el-avatar>
        <span class="brand-info">{{ audience }}</span>
      </div>
      <el-menu-item index="mine">
        <el-icon>
          <i-mdi-account-file-text-outline />
        </el-icon>
        <span>个人中心</span>
      </el-menu-item>
    </el-menu>
    <el-menu mode="horizontal" :ellipsis="false" class="header-menu">
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
  </div>
</template>

<style scoped lang="scss">
.header-container {
  width: 100%;
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  border-bottom: 1px solid var(--el-menu-border-color);

  .header-menu {
    border-bottom: none;

    .brand {
      display: flex;
      flex-direction: row;
      align-items: center;
      padding: 10px 16px 10px 12px;
      box-sizing: border-box;

      .el-avatar {
        flex: 0 0 var(--el-avatar-size);
      }

      .brand-info {
        display: inline-block;
        margin-left: 8px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
        font-size: var(--el-font-size-large);
        transition: none;
      }
    }

    .el-switch {
      .el-icon {
        margin-left: 6px;
      }
    }

    & > .el-sub-menu.is-active {
      ::v-deep(.el-sub-menu__title) {
        border-bottom: none;
      }
    }

    &.collapse {
      transition: none;

      .brand {
        .brand-info {
          width: 0;
          height: 0;
          opacity: 0;
        }
      }
    }
  }
}

.divider {
  border-top: 1px var(--el-border-color) var(--el-border-style);
}
</style>
