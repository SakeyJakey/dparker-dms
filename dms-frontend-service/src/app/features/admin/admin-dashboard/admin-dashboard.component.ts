import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { ApiService } from '../../../core/services/api.service';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <div class="page-container" role="region" aria-label="Administration dashboard">
      <h1>Administration Dashboard</h1>
      <div class="stat-cards">
        <div class="stat-card" role="article"><h3>Users</h3><p class="stat-value">{{ stats.totalUsers }}</p><a routerLink="/admin/users" class="stat-link">Manage →</a></div>
        <div class="stat-card" role="article"><h3>Roles</h3><p class="stat-value">{{ stats.totalRoles }}</p><a routerLink="/admin/roles" class="stat-link">Manage →</a></div>
        <div class="stat-card" role="article"><h3>Applications</h3><p class="stat-value">{{ stats.totalApplications }}</p><a routerLink="/admin/applications" class="stat-link">Manage →</a></div>
      </div>
      <nav class="admin-nav" role="navigation" aria-label="Admin sections">
        <ul>
          <li><a routerLink="/admin/users" aria-label="User management">👥 User Management</a></li>
          <li><a routerLink="/admin/roles" aria-label="Role management">🔐 Role Management</a></li>
          <li><a routerLink="/admin/applications" aria-label="Application management">📱 Application Management</a></li>
          <li><a routerLink="/admin/webhooks" aria-label="Webhook management">🔗 Webhook Management</a></li>
          <li><a routerLink="/admin/api-keys" aria-label="API key management">🔑 API Key Management</a></li>
          <li><a routerLink="/compliance/audit-logs" aria-label="Audit logs">📋 Audit Logs</a></li>
        </ul>
      </nav>
    </div>
  `,
  styles: [`
    .page-container { max-width: 1200px; margin: 0 auto; }
    h1 { font-size: 1.75rem; font-weight: 600; color: #1565c0; margin-bottom: 1.5rem; }
    .stat-cards { display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 1.5rem; margin-bottom: 2rem; }
    .stat-card { background: white; padding: 1.5rem; border-radius: 8px; box-shadow: 0 1px 4px rgba(0,0,0,0.08); }
    .stat-card h3 { margin: 0 0 0.5rem 0; font-size: 0.85rem; color: #888; font-weight: 600; text-transform: uppercase; }
    .stat-value { font-size: 2.5rem; font-weight: 700; color: #1565c0; margin: 0 0 0.5rem 0; }
    .stat-link { color: #1976d2; text-decoration: none; font-weight: 500; font-size: 0.9rem; }
    .stat-link:hover { text-decoration: underline; }
    .admin-nav ul { list-style: none; padding: 0; display: grid; grid-template-columns: repeat(auto-fit, minmax(250px, 1fr)); gap: 1rem; }
    .admin-nav a { display: block; padding: 1.25rem; background: white; border: 2px solid #e0e0e0; border-radius: 8px; text-decoration: none; color: #333; font-weight: 500; transition: all 0.2s; }
    .admin-nav a:hover, .admin-nav a:focus { border-color: #1976d2; background: #e3f2fd; outline: 2px solid #1976d2; outline-offset: 2px; }
  `]
})
export class AdminDashboardComponent implements OnInit {
  stats = { totalUsers: 0, totalRoles: 0, totalApplications: 0 };

  constructor(private api: ApiService) {}

  ngOnInit(): void { this.loadStats(); }

  loadStats(): void {
    this.api.getUsers(0, 1).subscribe({ next: (r) => this.stats.totalUsers = r.totalElements, error: () => {} });
    this.api.getRoles(0, 1).subscribe({ next: (r) => this.stats.totalRoles = r.totalElements, error: () => {} });
    this.api.getApplications(0, 1).subscribe({ next: (r) => this.stats.totalApplications = r.totalElements, error: () => {} });
  }
}
