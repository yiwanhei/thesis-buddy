<template>
  <router-view />
</template>

<script setup>
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
  -webkit-tap-highlight-color: transparent;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  background: #f5f5f5;
  max-width: 480px;
  margin: 0 auto;
  min-height: 100vh;
  position: relative;
}

#app {
  min-height: 100vh;
  background: #f5f5f5;
}

/* 移动端通用按钮 */
button {
  -webkit-tap-highlight-color: transparent;
  touch-action: manipulation;
}

input, select, textarea, button {
  font-size: 16px !important; /* 防止iOS自动缩放 */
}
</style>
