import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <div class="admin-dashboard" role="region" aria-label="Administration dashboard">
      <header class="page-header">
        <h1 id="admin-title">Administration Dashboard</h1>
      </header>

      <nav class="admin-nav" role="navigation" aria-label="Administration sections">
        <ul role="list">
          <li>
            <a routerLink="/admin/users" role="link" aria-label="User management">
              <span aria-hidden="true">👥</span> User Management
            </a>
          </li>
          <li>
            <a routerLink="/admin/roles" role="link" aria-label="Role management">
              <span aria-hidden="true">🔐</span> Role Management
            </a>
          </li>
          <li>
            <a routerLink="/admin/permissions" role="link" aria-label="Permission management">
              <span aria-hidden="true">🔑</span> Permission Management
            </a>
          </li>
          <li>
            <a routerLink="/admin/applications" role="link" aria-label="Application management">
              <span aria-hidden="true">📱</span> Application Management
            </a>
          </li>
        </ul>
      </nav>

      <section class="dashboard-stats" role="region" aria-label="System statistics">
        <h2 class="sr-only">System Statistics</h2>
        <div class="stat-cards">
          <div class="stat-card" role="article" aria-label="Total users">
            <h3>Total Users</h3>
            <p class="stat-value" aria-live="polite">{{ stats.totalUsers }}</p>
          </div>
          <div class="stat-card" role="article" aria-label="Total roles">
            <h3>Total Roles</h3>
            <p class="stat-value" aria-live="polite">{{ stats.totalRoles }}</p>
          </div>
          <div class="stat-card" role="article" aria-label="Total applications">
            <h3>Total Applications</h3>
            <p class="stat-value" aria-live="polite">{{ stats.totalApplications }}</p>
          </div>
        </div>
      </section>
    </div>
  `,
  styles: [`
    .admin-dashboard {
      max-width: 1200px;
      margin: 0 auto;
    }
    .page-header h1 {
      font-size: 2rem;
      font-weight: 600;
      color: #1976d2;
      margin-bottom: 2rem;
    }
    .admin-nav ul {
      list-style: none;
      padding: 0;
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
      gap: 1rem;
      margin-bottom: 2rem;
    }
    .admin-nav a {
      display: block;
      padding: 1.5rem;
      background: white;
      border: 2px solid #e0e0e0;
      border-radius: 8px;
      text-decoration: none;
      color: #333;
      font-weight: 500;
      transition: all 0.2s;
    }
    .admin-nav a:hover, .admin-nav a:focus {
      border-color: #1976d2;
      outline: 2px solid #1976d2;
      outline-offset: 2px;
      background-color: #e3f2fd;
    }
    .stat-cards {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
      gap: 1.5rem;
    }
    .stat-card {
      background: white;
      padding: 1.5rem;
      border-radius: 8px;
      box-shadow: 0 2px 4px rgba(0,0,0,0.1);
    }
    .stat-card h3 {
      margin: 0 0 0.5rem 0;
      font-size: 0.875rem;
      color: #666;
      font-weight: 500;
    }
    .stat-value {
      font-size: 2rem;
      font-weight: 600;
      color: #1976d2;
      margin: 0;
    }
    .sr-only {
      position: absolute;
      width: 1px;
      height: 1px;
      padding: 0;
      margin: -1px;
      overflow: hidden;
      clip: rect(0, 0, 0, 0);
      white-space: nowrap;
      border-width: 0;
    }
  `]
})
export class AdminDashboardComponent implements OnInit {
  stats = {
    totalUsers: 0,
    totalRoles: 0,
    totalApplications: 0
  };

  constructor(private http: HttpClient) {}

  ngOnInit() {
    this.loadStats();
  }

  loadStats() {
    // TODO: Load actual stats from API
    this.stats = {
      totalUsers: 0,
      totalRoles: 0,
      totalApplications: 0
    };
  }
}
