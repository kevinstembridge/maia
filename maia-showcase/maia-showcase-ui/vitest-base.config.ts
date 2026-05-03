import {defineConfig} from 'vitest/config';

export default defineConfig({
    test: {
        environment: 'jsdom',
        globals: true,
        isolate: false,
    },
    optimizeDeps: {
        force: true,
        include: [
            '@angular/core',
            '@angular/core/testing',
            '@angular/core/primitives/di',
            '@angular/core/primitives/signals',
        ],
    },
});
