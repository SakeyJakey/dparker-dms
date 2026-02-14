import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    redirectTo: '/documents',
    pathMatch: 'full'
  },
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
    data: { roles: ['DMS.Admin'], title: 'Administration' }
  },
  {
    path: 'admin/users',
    loadComponent: () => import('./features/admin/user-list/user-list.component').then(m => m.UserListComponent),
    data: { roles: ['DMS.Admin'], title: 'User Management' }
  },
  {
    path: 'admin/roles',
    loadComponent: () => import('./features/admin/role-list/role-list.component').then(m => m.RoleListComponent),
    data: { roles: ['DMS.Admin'], title: 'Role Management' }
  },
  {
    path: 'admin/applications',
    loadComponent: () => import('./features/admin/application-list/application-list.component').then(m => m.ApplicationListComponent),
    data: { roles: ['DMS.Admin'], title: 'Application Management' }
  },
  {
    path: 'compliance',
    loadComponent: () => import('./features/compliance/compliance-dashboard/compliance-dashboard.component').then(m => m.ComplianceDashboardComponent),
    data: { roles: ['DMS.Admin'], title: 'Compliance' }
  },
  {
    path: 'compliance/audit-logs',
    loadComponent: () => import('./features/compliance/audit-logs/audit-logs.component').then(m => m.AuditLogsComponent),
    data: { roles: ['DMS.Admin'], title: 'Audit Logs' }
  },
  {
    path: 'llm',
    loadComponent: () => import('./features/llm/llm-query/llm-query.component').then(m => m.LlmQueryComponent),
    data: { title: 'AI Document Query' }
  },
  {
    path: '**',
    redirectTo: '/documents'
  }
];
