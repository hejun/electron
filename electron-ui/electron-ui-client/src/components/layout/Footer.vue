<script setup lang="ts">
defineProps({
  copyright: {
    type: String
  }
})
</script>

<template>
  <div class="footer">{{ copyright }}</div>
</template>

<style scoped lang="scss">
.footer {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f8fafd;
  color: var(--el-text-color-regular)
}
</style>
