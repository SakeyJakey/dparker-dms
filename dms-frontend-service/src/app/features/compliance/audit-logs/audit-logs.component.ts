import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ApiService } from '../../../core/services/api.service';
import { AuditEvent, PageResponse } from '../../../core/models';

@Component({
  selector: 'app-audit-logs',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  template: `
    <div class="page-container" role="region" aria-label="Audit logs">
      <header class="page-header">
        <h1>Audit Logs</h1>
        <a routerLink="/compliance" class="btn btn-outline">← Back to Compliance</a>
      </header>
      <div class="filters">
        <label for="eventTypeFilter">Event Type:</label>
        <select id="eventTypeFilter" [(ngModel)]="selectedEventType" (change)="loadLogs()">
          <option value="">All Events</option>
          <option value="CREATE">Create</option>
          <option value="UPDATE">Update</option>
          <option value="DELETE">Delete</option>
          <option value="VIEW">View</option>
          <option value="DOWNLOAD">Download</option>
          <option value="LOGIN">Login</option>
          <option value="LOGOUT">Logout</option>
          <option value="LLM_QUERY_INITIATED">LLM Query</option>
        </select>
      </div>
      <div *ngIf="loading" class="loading" role="status">Loading audit logs...</div>
      <table *ngIf="!loading && logs.length > 0" class="data-table" role="table" aria-label="Audit events">
        <thead><tr><th scope="col">Timestamp</th><th scope="col">Type</th><th scope="col">Category</th><th scope="col">Action</th><th scope="col">Result</th><th scope="col">Resource</th></tr></thead>
        <tbody>
          <tr *ngFor="let log of logs; trackBy: trackById">
            <td class="mono">{{ log.timestamp | date:'medium' }}</td>
            <td><span class="badge">{{ log.eventType }}</span></td>
            <td>{{ log.eventCategory }}</td>
            <td>{{ log.action }}</td>
            <td><span class="result-badge" [class]="'result-' + log.result.toLowerCase()">{{ log.result }}</span></td>
            <td>{{ log.resourceType ? (log.resourceType + ': ' + (log.resourceName || log.resourceId || '—')) : '—' }}</td>
          </tr>
        </tbody>
      </table>
      <div *ngIf="!loading && logs.length === 0" class="empty-state" role="status">No audit logs found.</div>
      <div *ngIf="totalPages > 1" class="pagination">
        <button (click)="goToPage(currentPage-1)" [disabled]="currentPage===0" class="btn btn-sm">Previous</button>
        <span>Page {{ currentPage + 1 }} / {{ totalPages }}</span>
        <button (click)="goToPage(currentPage+1)" [disabled]="currentPage>=totalPages-1" class="btn btn-sm">Next</button>
      </div>
    </div>
  `,
  styles: [`
    .page-container { max-width: 1400px; margin: 0 auto; }
    .page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 1.5rem; }
    h1 { font-size: 1.75rem; font-weight: 600; color: #1565c0; margin: 0; }
    .filters { display: flex; gap: 1rem; align-items: center; margin-bottom: 1.5rem; }
    .filters label { font-weight: 500; color: #555; }
    select { padding: 0.5rem; border: 1px solid #ddd; border-radius: 6px; }
    select:focus { outline: 2px solid #1976d2; outline-offset: 2px; }
    .data-table { width: 100%; border-collapse: collapse; background: white; border-radius: 8px; overflow: hidden; box-shadow: 0 1px 4px rgba(0,0,0,0.08); font-size: 0.9rem; }
    .data-table th { background: #f5f5f5; padding: 0.6rem 0.75rem; text-align: left; font-weight: 600; font-size: 0.8rem; color: #555; border-bottom: 2px solid #e0e0e0; }
    .data-table td { padding: 0.6rem 0.75rem; border-bottom: 1px solid #f0f0f0; }
    .mono { font-family: monospace; font-size: 0.8rem; }
    .badge { padding: 0.15rem 0.5rem; border-radius: 10px; font-size: 0.75rem; font-weight: 500; background: #e3f2fd; color: #1565c0; }
    .result-badge { padding: 0.15rem 0.5rem; border-radius: 10px; font-size: 0.75rem; font-weight: 500; }
    .result-success { background: #e8f5e9; color: #2e7d32; }
    .result-failure { background: #ffebee; color: #c62828; }
    .result-partial { background: #fff3e0; color: #ef6c00; }
    .btn { padding: 0.5rem 1rem; border: none; border-radius: 6px; font-weight: 500; cursor: pointer; text-decoration: none; display: inline-block; font-size: 0.9rem; }
    .btn-outline { background: white; border: 1px solid #ddd; color: #555; }
    .btn-sm { padding: 0.3rem 0.75rem; font-size: 0.8rem; }
    .btn:focus { outline: 2px solid #1976d2; outline-offset: 2px; }
    .btn:disabled { opacity: 0.5; }
    .loading, .empty-state { padding: 3rem; text-align: center; color: #666; }
    .pagination { display: flex; justify-content: center; align-items: center; gap: 1rem; margin-top: 1.5rem; }
  `]
})
export class AuditLogsComponent implements OnInit {
  logs: AuditEvent[] = [];
  loading = false;
  selectedEventType = '';
  currentPage = 0;
  totalPages = 0;

  constructor(private api: ApiService) {}
  ngOnInit(): void { this.loadLogs(); }

  loadLogs(): void {
    this.loading = true;
    this.api.getAuditLogs(this.currentPage, 50, this.selectedEventType || undefined).subscribe({
      next: (r: PageResponse<AuditEvent>) => { this.logs = r.content; this.totalPages = r.totalPages; this.loading = false; },
      error: () => { this.loading = false; }
    });
  }

  goToPage(page: number): void { this.currentPage = page; this.loadLogs(); }
  trackById(_i: number, log: AuditEvent): string { return log.id; }
}
