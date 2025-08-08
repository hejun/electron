<script setup lang="ts">
import { ref } from 'vue'

const BASE_URL = import.meta.env.VITE_BASE_URL ?? ''

function login() {
  window.location.href = `${BASE_URL}/oauth2/authorization/neutron?redirect_uri=${window.location.href}`
}

function logout() {
  window.location.href = `${BASE_URL}/logout`
}

const respRef = ref()

function notifications() {
  fetch(`${BASE_URL}/notifications/message`, { credentials: 'include' })
    .then(resp => resp.json())
    .then(resp => (respRef.value = JSON.stringify(resp)))
}

function sendMessage() {
  fetch(`${BASE_URL}/notifications/message`, {
    method: 'POST',
    credentials: 'include',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({})
  })
    .then(resp => resp.json())
    .then(resp => (respRef.value = JSON.stringify(resp)))
}

function createOrder() {
  fetch(`${BASE_URL}/trade/order`, {
    method: 'POST',
    credentials: 'include',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({})
  })
    .then(resp => resp.json())
    .then(resp => (respRef.value = JSON.stringify(resp)))
}

function obtainTomorrow(): string {
  const now = new Date()
  now.setTime(now.getTime() + 24 * 60 * 60 * 1000)
  return (
    now.getFullYear() + '-' + (now.getMonth() + 1 + '').padStart(2, '0') + '-' + (now.getDate() + '').padStart(2, '0')
  )
}

function searchFlights() {
  fetch(`${BASE_URL}/flights/domestic/search`, {
    method: 'POST',
    credentials: 'include',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      departureCityCode: 'XIY',
      arrivalCityCode: 'BJS',
      departureDate: obtainTomorrow()
    })
  })
    .then(resp => resp.json())
    .then(resp => (respRef.value = JSON.stringify(resp)))
}
</script>

<template>
  <div>
    <button @click="login">Login</button>
    <button @click="logout">logout</button>
    <button @click="notifications">Notifications</button>
    <button @click="sendMessage">SendMessage</button>
    <button @click="searchFlights">SearchFlights</button>
    <button @click="createOrder">CreateOrder</button>
  </div>
  <div>{{ respRef }}</div>
  <RouterView />
</template>
