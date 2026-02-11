module.exports = {
    testEnvironment: 'jsdom',
    testMatch: ['**/js/__tests__/**/*.test.js'],
    collectCoverageFrom: [
        'js/**/*.js',
        '!js/__tests__/**',
        '!js/config.js'
    ],
    coverageDirectory: 'coverage',
    coverageReporters: ['text', 'lcov', 'html'],
    setupFiles: ['./js/__tests__/setup.js'],
    verbose: true
};
