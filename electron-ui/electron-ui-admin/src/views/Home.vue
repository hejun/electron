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

function obtainOffsetDay(offsetDay = 1): string {
  const now = new Date()
  now.setTime(now.getTime() + offsetDay * 24 * 60 * 60 * 1000)
  return (
    now.getFullYear() + '-' + (now.getMonth() + 1 + '').padStart(2, '0') + '-' + (now.getDate() + '').padStart(2, '0')
  )
}

function searchFlights(e: Event) {
  const element = e.target as HTMLInputElement
  element.disabled = true
  respRef.value = null
  cabinRef.value = null
  indexRef.value = 0
  fetch(`${BASE_URL}/flights/domestic/search`, {
    method: 'POST',
    credentials: 'include',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      segments: [
        {
          departureCityCode: 'XIY',
          arrivalCityCode: 'BJS',
          departureDate: obtainOffsetDay(1)
        }
      ],
      adultCount: 1
    })
  })
    .then(resp => resp.json())
    .then(resp => (respRef.value = resp))
    .then(() => (element.disabled = false))
}

const indexRef = ref(0)
const cabinRef = ref()

function searchPrices(e: Event) {
  if (!respRef.value) {
    respRef.value = { tips: '还未查询航班' }
    return
  }
  const element = e.target as HTMLInputElement
  element.disabled = true

  const segments = []
  for (let i = 0; i < respRef.value.flights[indexRef.value].flightSegments.length; i++) {
    segments.push({
      departureAirlineCode: respRef.value.flights[indexRef.value].flightSegments[i].departureAirlineCode,
      arrivalAirlineCode: respRef.value.flights[indexRef.value].flightSegments[i].arrivalAirlineCode,
      departureDate: respRef.value.flights[indexRef.value].flightSegments[i].departureDate,
      airline: respRef.value.flights[indexRef.value].flightSegments[i].airline,
      flightNo: respRef.value.flights[indexRef.value].flightSegments[i].flightNo
    })
  }
  cabinRef.value = null
  fetch(`${BASE_URL}/flights/domestic/prices`, {
    method: 'POST',
    credentials: 'include',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      segments: segments,
      adultCount: 1
    })
  })
    .then(resp => resp.json())
    .then(resp => (cabinRef.value = resp))
    .then(() => (element.disabled = false))
}
</script>

<template>
  <div>
    <button @click="login">Login</button>
    <button @click="logout">logout</button>
    <button @click="notifications">Notifications</button>
    <button @click="sendMessage">SendMessage</button>
    <button @click="searchFlights">SearchFlights</button>
    <input type="number" min="0" :max="respRef?.flights?.length - 1 || 0" v-model="indexRef" />
    <button @click="searchPrices">SearchPrices</button>
    <button @click="createOrder">CreateOrder</button>
  </div>
  <div class="result">
    <pre v-if="respRef">{{ JSON.stringify(respRef, null, '\t') }}</pre>
    <pre v-if="cabinRef">{{ JSON.stringify(cabinRef, null, '\t') }}</pre>
  </div>
  <RouterView />
</template>

<style scoped lang="scss">
.result {
  display: flex;

  pre {
    flex: 1;
  }
}
</style>
