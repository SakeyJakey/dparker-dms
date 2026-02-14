import { Routes } from '@angular/router';

export const routes: Routes = [
  { path: '', redirectTo: '/documents', pathMatch: 'full' },
  {
    path: 'documents',
    loadComponent: () => import('./features/documents/document-list/document-list.component').then(m => m.DocumentListComponent),
    data: { title: 'Documents' }
  },
  {
    path: 'documents/upload',
    loadComponent: () => import('./features/documents/document-upload/document-upload.component').then(m => m.DocumentUploadComponent),
    data: { title: 'Upload Document' }
  },
  {
    path: 'documents/:id',
    loadComponent: () => import('./features/documents/document-detail/document-detail.component').then(m => m.DocumentDetailComponent),
    data: { title: 'Document Detail' }
  },
  {
    path: 'admin',
    loadComponent: () => import('./features/admin/admin-dashboard/admin-dashboard.component').then(m => m.AdminDashboardComponent),
    data: { title: 'Administration' }
  },
  {
    path: 'admin/users',
    loadComponent: () => import('./features/admin/user-list/user-list.component').then(m => m.UserListComponent),
    data: { title: 'User Management' }
  },
  {
    path: 'admin/roles',
    loadComponent: () => import('./features/admin/role-list/role-list.component').then(m => m.RoleListComponent),
    data: { title: 'Role Management' }
  },
  {
    path: 'admin/applications',
    loadComponent: () => import('./features/admin/application-list/application-list.component').then(m => m.ApplicationListComponent),
    data: { title: 'Application Management' }
  },
  {
    path: 'admin/webhooks',
    loadComponent: () => import('./features/admin/webhook-list/webhook-list.component').then(m => m.WebhookListComponent),
    data: { title: 'Webhook Management' }
  },
  {
    path: 'admin/api-keys',
    loadComponent: () => import('./features/admin/api-key-list/api-key-list.component').then(m => m.ApiKeyListComponent),
    data: { title: 'API Key Management' }
  },
  {
    path: 'analytics',
    loadComponent: () => import('./features/analytics/analytics-dashboard.component').then(m => m.AnalyticsDashboardComponent),
    data: { title: 'Analytics' }
  },
  {
    path: 'compliance',
    loadComponent: () => import('./features/compliance/compliance-dashboard/compliance-dashboard.component').then(m => m.ComplianceDashboardComponent),
    data: { title: 'Compliance' }
  },
  {
    path: 'compliance/audit-logs',
    loadComponent: () => import('./features/compliance/audit-logs/audit-logs.component').then(m => m.AuditLogsComponent),
    data: { title: 'Audit Logs' }
  },
  {
    path: 'llm',
    loadComponent: () => import('./features/llm/llm-query/llm-query.component').then(m => m.LlmQueryComponent),
    data: { title: 'AI Document Query' }
  },
  { path: '**', redirectTo: '/documents' }
];
