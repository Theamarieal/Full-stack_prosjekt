<template>
  <div id="app">
    <router-view />
  </div>
</template>

<script setup>
// no logic needed here, routing is handled by vue-router
</script>

<style>
/* global reset! just keeps pages from inheriting unwanted padding/margin */
*,
*::before,
*::after {
  box-sizing: border-box;
  margin: 0;
  padding: 0;
}

body {
  font-family: Arial, sans-serif;
  background-color: #f7f6f2;
  color: #2c2c2a;
}

#app {
  min-height: 100vh;
}
</style>
