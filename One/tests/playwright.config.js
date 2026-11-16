const { defineConfig } = require('@playwright/test');

module.exports = defineConfig({
  testDir: '.',
  timeout: 30000,
  use: {
    channel: 'msedge',
    headless: true,
    baseURL: process.env.TEST_URL || 'http://127.0.0.1:8788',
  },
});
