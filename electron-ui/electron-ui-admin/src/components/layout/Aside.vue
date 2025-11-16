<script setup lang="ts">
import { useRoute } from 'vue-router'

const { collapse, audience } = defineProps({
  collapse: {
    type: Boolean,
    default: false
  },
  audience: {
    type: String
  }
})
const route = useRoute()

const active = route.fullPath.replace('/', '')
</script>

<template>
  <div class="sidebar-container" :class="{ collapse: collapse }">
    <div class="brand">
      <el-avatar>{{ audience?.substring(0, 1) }}</el-avatar>
      <span class="brand-info">{{ audience }}</span>
    </div>
    <el-scrollbar>
      <el-menu mode="vertical" router :collapse="collapse" :default-active="active" :collapse-transition="false">
      </el-menu>
    </el-scrollbar>
  </div>
</template>

<style scoped lang="scss">
.sidebar-container {
  width: 180px;
  height: 100%;
  display: flex;
  flex-direction: column;
  border-right: 1px solid var(--el-menu-border-color);
  transition: width var(--el-transition-duration) var(--el-transition-function-fast-bezier);

  .brand {
    display: flex;
    flex-direction: row;
    align-items: center;
    padding: 10px 0 10px 12px;
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

  .el-menu {
    border-right: none;
  }

  &.collapse {
    width: 64px;
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
</style>
