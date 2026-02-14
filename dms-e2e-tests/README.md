# DMS E2E Tests

End-to-end tests for the Document Management System using Playwright.

## Prerequisites

- Node.js 20+
- npm 10+

## Setup

```bash
npm install
npx playwright install
```

## Running Tests

```bash
# Run all tests
npm test

# Run tests with browser visible
npm run test:headed

# Run specific test file
npx playwright test tests/documents.spec.ts

# View test report
npm run report
```

## Test Coverage

- **Navigation**: App routing, page loading, redirects
- **Documents**: List, upload, detail, search, filter
- **Admin**: Dashboard, user/role/application CRUD
- **Compliance**: PCI/GDPR/ISO dashboard, audit logs
- **LLM Query**: Query input, mode selection, results
- **Accessibility**: ARIA labels, heading hierarchy, keyboard navigation
