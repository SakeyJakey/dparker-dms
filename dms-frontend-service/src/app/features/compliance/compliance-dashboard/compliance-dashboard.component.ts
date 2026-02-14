import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../../core/services/api.service';
import { NotificationService } from '../../../core/services/notification.service';
import { ComplianceReport } from '../../../core/models';

@Component({
  selector: 'app-compliance-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  template: `
    <div class="page-container" role="region" aria-label="Compliance dashboard">
      <h1>Compliance Dashboard</h1>

      <div class="compliance-grid">
        <section class="compliance-card" aria-label="PCI-DSS compliance">
          <h2>💳 PCI-DSS</h2>
          <p class="status" [class.status-ok]="pciReport?.status === 'operational'">{{ pciReport?.status || 'Loading...' }}</p>
          <p>{{ pciReport?.message || '' }}</p>
          <div class="card-actions">
            <label for="pciPeriod">Report Period:</label>
            <select id="pciPeriod" [(ngModel)]="pciPeriod" (change)="loadPciReport()">
              <option value="MONTHLY">Monthly</option>
              <option value="QUARTERLY">Quarterly</option>
            </select>
          </div>
        </section>

        <section class="compliance-card" aria-label="GDPR compliance">
          <h2>🔒 GDPR</h2>
          <div class="form-inline">
            <label for="dataSubjectId">Data Subject ID:</label>
            <input id="dataSubjectId" [(ngModel)]="dataSubjectId" placeholder="Enter UUID" aria-label="Data subject ID">
          </div>
          <div class="card-actions">
            <button (click)="exportData()" class="btn btn-primary btn-sm" [disabled]="!dataSubjectId">Export Data</button>
            <button (click)="requestErasure()" class="btn btn-danger btn-sm" [disabled]="!dataSubjectId">Request Erasure</button>
          </div>
          <p *ngIf="gdprResult" class="result-text" role="status">{{ gdprResult }}</p>
        </section>

        <section class="compliance-card" aria-label="ISO 27001 compliance">
          <h2>🛡️ ISO 27001</h2>
          <p class="status" [class.status-ok]="isoControls?.status === 'operational'">{{ isoControls?.status || 'Loading...' }}</p>
          <p>{{ isoControls?.message || '' }}</p>
        </section>

        <section class="compliance-card" aria-label="Audit logs">
          <h2>📋 Audit Logs</h2>
          <p>View comprehensive audit trail of all system activities.</p>
          <a routerLink="/compliance/audit-logs" class="btn btn-primary btn-sm">View Audit Logs →</a>
        </section>
      </div>
    </div>
  `,
  styles: [`
    .page-container { max-width: 1200px; margin: 0 auto; }
    h1 { font-size: 1.75rem; font-weight: 600; color: #1565c0; margin-bottom: 1.5rem; }
    h2 { font-size: 1.25rem; margin: 0 0 0.75rem 0; }
    .compliance-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(300px, 1fr)); gap: 1.5rem; }
    .compliance-card { background: white; padding: 1.5rem; border-radius: 8px; box-shadow: 0 1px 4px rgba(0,0,0,0.08); }
    .status { font-weight: 600; font-size: 1.1rem; color: #555; }
    .status-ok { color: #2e7d32; }
    .form-inline { display: flex; gap: 0.5rem; align-items: center; flex-wrap: wrap; margin-bottom: 1rem; }
    .form-inline label { font-weight: 500; font-size: 0.85rem; }
    .form-inline input { padding: 0.4rem 0.6rem; border: 1px solid #ddd; border-radius: 6px; flex: 1; min-width: 200px; }
    .form-inline input:focus { outline: 2px solid #1976d2; outline-offset: 2px; }
    .card-actions { display: flex; gap: 0.75rem; align-items: center; flex-wrap: wrap; margin-top: 1rem; }
    .card-actions label { font-weight: 500; font-size: 0.85rem; }
    .card-actions select { padding: 0.4rem; border: 1px solid #ddd; border-radius: 6px; }
    .btn { padding: 0.5rem 1rem; border: none; border-radius: 6px; font-weight: 500; cursor: pointer; text-decoration: none; display: inline-block; font-size: 0.9rem; }
    .btn-primary { background: #1976d2; color: white; }
    .btn-danger { background: #c62828; color: white; }
    .btn-sm { padding: 0.4rem 0.75rem; font-size: 0.85rem; }
    .btn:focus { outline: 2px solid #1976d2; outline-offset: 2px; }
    .btn:disabled { opacity: 0.5; cursor: not-allowed; }
    .result-text { margin-top: 0.75rem; padding: 0.5rem; background: #f5f5f5; border-radius: 4px; font-size: 0.85rem; }
  `]
})
export class ComplianceDashboardComponent implements OnInit {
  pciReport: ComplianceReport | null = null;
  isoControls: ComplianceReport | null = null;
  pciPeriod = 'MONTHLY';
  dataSubjectId = '';
  gdprResult: string | null = null;

  constructor(private api: ApiService, private notify: NotificationService) {}

  ngOnInit(): void {
    this.loadPciReport();
    this.loadIsoControls();
  }

  loadPciReport(): void {
    this.api.getPciReport(this.pciPeriod).subscribe({
      next: (r: ComplianceReport) => this.pciReport = r,
      error: () => this.pciReport = { status: 'error', period: this.pciPeriod, message: 'Failed to load' }
    });
  }

  loadIsoControls(): void {
    this.api.getIso27001Controls().subscribe({
      next: (r: ComplianceReport) => this.isoControls = r,
      error: () => this.isoControls = { status: 'error', period: '', message: 'Failed to load' }
    });
  }

  exportData(): void {
    if (!this.dataSubjectId) return;
    this.api.getDataSubjectData(this.dataSubjectId).subscribe({
      next: (r) => { this.gdprResult = `Data exported to: ${r.exportPath}`; this.notify.success('Data exported successfully'); },
      error: () => this.notify.error('Failed to export data')
    });
  }

  requestErasure(): void {
    if (!this.dataSubjectId || !window.confirm('Are you sure? This will permanently delete all data for this data subject.')) return;
    this.api.requestErasure(this.dataSubjectId).subscribe({
      next: (r) => { this.gdprResult = `Deleted: ${r.deletedCount}, Retained: ${r.retainedCount}`; this.notify.success('Erasure request processed'); },
      error: () => this.notify.error('Failed to process erasure request')
    });
  }
}
