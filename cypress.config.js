const { defineConfig } = require("cypress");

module.exports = defineConfig({
  // viewportWidth:400,
  e2e: {
    setupNodeEvents(on, config) {
      // implement node event listeners here
    },
    supportFile: false,
  },
});
