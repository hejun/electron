<script setup lang="ts">
import { onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import useAuthStore from '@/store/AuthStore'
import { enc } from 'crypto-js'
import { obtainToken } from '@/api/AuthorizationApi'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const { error, error_description, code, state } = route.query

const oAuthObtainToken = () => {
  const codeVerify = authStore.authenticator ? authStore.authenticator!.codeVerify : authStore.authenticator

  if (!code || !codeVerify) {
    // eslint-disable-next-line no-alert
    alert(`Missing code or verify code`)
    return
  }

  authStore.destroyAuthentication()

  obtainToken(code as string, codeVerify)
    .then(res => authStore.updateToken(res))
    .then(() => {
      const url = state ? decodeURIComponent(enc.Utf8.stringify(enc.Base64.parse(state as string))) : '/'
      router.replace(url)
    })
}

onMounted(() => oAuthObtainToken())
</script>

<template>
  <div v-if="error">
    <p>{{ error }}</p>
    <p>{{ error_description }}</p>
  </div>
  <div v-else>请稍后...</div>
</template>
