// Setup global mocks para el entorno de test JSDOM

// Mock localStorage
const localStorageMock = (() => {
    let store = {};
    return {
        getItem: jest.fn(key => store[key] || null),
        setItem: jest.fn((key, value) => { store[key] = String(value); }),
        removeItem: jest.fn(key => { delete store[key]; }),
        clear: jest.fn(() => { store = {}; }),
        get length() { return Object.keys(store).length; },
        key: jest.fn(index => Object.keys(store)[index] || null)
    };
})();

Object.defineProperty(global, 'localStorage', { value: localStorageMock });

// Mock fetch global
global.fetch = jest.fn();

// Mock console.error para tests que verifican errores
const originalConsoleError = console.error;
beforeEach(() => {
    jest.clearAllMocks();
    localStorage.clear();
});

afterAll(() => {
    console.error = originalConsoleError;
});
