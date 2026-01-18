import { Routes } from '@angular/router';
import { MsalGuard } from '@azure/msal-angular';

export const routes: Routes = [
  {
    path: '',
    redirectTo: '/documents',
    pathMatch: 'full'
  },
  {
    path: 'documents',
    loadComponent: () => import('./features/documents/document-list/document-list.component').then(m => m.DocumentListComponent),
    canActivate: [MsalGuard],
    data: { title: 'Documents' }
  },
  {
    path: 'admin',
    loadComponent: () => import('./features/admin/admin-dashboard/admin-dashboard.component').then(m => m.AdminDashboardComponent),
    canActivate: [MsalGuard],
    data: { roles: ['DMS.Admin'], title: 'Administration' }
  },
  {
    path: 'compliance',
    loadComponent: () => import('./features/compliance/compliance-dashboard/compliance-dashboard.component').then(m => m.ComplianceDashboardComponent),
    canActivate: [MsalGuard],
    data: { roles: ['DMS.Admin'], title: 'Compliance' }
  }
];
