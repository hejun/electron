<script setup lang="ts">
import { ref } from 'vue'

const BASE_URL = import.meta.env.VITE_BASE_URL ?? ''

function login() {
  window.location.href = `${BASE_URL}/oauth2/authorization/neutron?redirect_uri=${window.location.href}`
}

const respRef = ref()

function notifications() {
  fetch(`${BASE_URL}/notifications/message`, { credentials: 'include' })
    .then(resp => resp.json())
    .then(resp => (respRef.value = JSON.stringify(resp)))
}
</script>

<template>
  <div>
    <button @click="login">Login</button>
    <button @click="notifications">Notifications</button>
  </div>
  <div>{{ respRef }}</div>
  <RouterView />
</template>
