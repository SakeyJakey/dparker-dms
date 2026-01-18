import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-compliance-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <div class="compliance-dashboard" role="region" aria-label="Compliance dashboard">
      <header class="page-header">
        <h1 id="compliance-title">Compliance Dashboard</h1>
      </header>

      <nav class="compliance-nav" role="navigation" aria-label="Compliance sections">
        <ul role="list">
          <li>
            <a routerLink="/compliance/pci" role="link" aria-label="PCI-DSS compliance">
              <span aria-hidden="true">💳</span> PCI-DSS Compliance
            </a>
          </li>
          <li>
            <a routerLink="/compliance/gdpr" role="link" aria-label="GDPR compliance">
              <span aria-hidden="true">🔒</span> GDPR Compliance
            </a>
          </li>
          <li>
            <a routerLink="/compliance/iso27001" role="link" aria-label="ISO 27001 compliance">
              <span aria-hidden="true">🛡️</span> ISO 27001 Compliance
            </a>
          </li>
          <li>
            <a routerLink="/compliance/audit" role="link" aria-label="Audit logs">
              <span aria-hidden="true">📋</span> Audit Logs
            </a>
          </li>
        </ul>
      </nav>
    </div>
  `,
  styles: [`
    .compliance-dashboard {
      max-width: 1200px;
      margin: 0 auto;
    }
    .page-header h1 {
      font-size: 2rem;
      font-weight: 600;
      color: #1976d2;
      margin-bottom: 2rem;
    }
    .compliance-nav ul {
      list-style: none;
      padding: 0;
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
      gap: 1rem;
    }
    .compliance-nav a {
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
    .compliance-nav a:hover, .compliance-nav a:focus {
      border-color: #1976d2;
      outline: 2px solid #1976d2;
      outline-offset: 2px;
      background-color: #e3f2fd;
    }
  `]
})
export class ComplianceDashboardComponent {}
