const { defineConfig } = require("cypress");

module.exports = defineConfig({
  video: false,
  screenshotOnRunFailure: false,
  // viewportWidth:400,
  e2e: {
    setupNodeEvents(on, config) {
      // implement node event listeners here
    },
    supportFile: false,
  },
});
