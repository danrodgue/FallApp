exports.config = {
  runner: 'local',
  specs: ['./tests/e2e/**/*.e2e.js'],
  exclude: [],
  maxInstances: 1,
  capabilities: [
    {
      browserName: 'chrome',
      'goog:chromeOptions': {
        args: ['--headless=new', '--disable-gpu', '--window-size=1366,768', '--no-sandbox']
      }
    }
  ],
  logLevel: 'error',
  bail: 0,
  baseUrl: 'http://127.0.0.1:4173',
  waitforTimeout: 10000,
  connectionRetryTimeout: 120000,
  connectionRetryCount: 2,
  services: ['chromedriver'],
  framework: 'mocha',
  reporters: ['spec'],
  mochaOpts: {
    ui: 'bdd',
    timeout: 60000
  }
};
