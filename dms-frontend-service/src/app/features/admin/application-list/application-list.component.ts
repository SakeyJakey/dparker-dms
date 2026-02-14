import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../../core/services/api.service';
import { NotificationService } from '../../../core/services/notification.service';
import { RegisteredApplication, ApplicationProvisionRequest, PageResponse } from '../../../core/models';

@Component({
  selector: 'app-application-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="page-container" role="region" aria-label="Application management">
      <header class="page-header">
        <h1>Application Management</h1>
        <button (click)="showCreateForm = !showCreateForm" class="btn btn-primary">{{ showCreateForm ? 'Cancel' : '+ Provision Application' }}</button>
      </header>
      <div *ngIf="showCreateForm" class="form-card">
        <h2>Provision Application</h2>
        <form (ngSubmit)="provision()">
          <div class="form-row">
            <div class="form-group"><label for="entraId">Entra App ID *</label><input id="entraId" [(ngModel)]="newApp.entraAppId" name="entraAppId" required></div>
            <div class="form-group"><label for="appName">Application Name *</label><input id="appName" [(ngModel)]="newApp.applicationName" name="applicationName" required></div>
          </div>
          <button type="submit" class="btn btn-primary" [disabled]="!newApp.entraAppId || !newApp.applicationName">Provision</button>
        </form>
      </div>
      <div *ngIf="loading" class="loading" role="status">Loading applications...</div>
      <table *ngIf="!loading && apps.length > 0" class="data-table" role="table" aria-label="Applications">
        <thead><tr><th scope="col">Name</th><th scope="col">Entra App ID</th><th scope="col">Status</th><th scope="col">Storage</th><th scope="col">Actions</th></tr></thead>
        <tbody>
          <tr *ngFor="let app of apps; trackBy: trackById">
            <td class="font-bold">{{ app.applicationName }}</td>
            <td class="mono">{{ app.entraAppId }}</td>
            <td><span class="badge" [class]="'badge-' + app.status.toLowerCase()">{{ app.status }}</span></td>
            <td class="mono">{{ app.storageContainerName }}</td>
            <td><button (click)="deprovision(app)" class="btn btn-sm btn-danger" [attr.aria-label]="'Deprovision ' + app.applicationName">Deprovision</button></td>
          </tr>
        </tbody>
      </table>
      <div *ngIf="!loading && apps.length === 0" class="empty-state" role="status">No applications provisioned.</div>
    </div>
  `,
  styles: [`
    .page-container { max-width: 1200px; margin: 0 auto; }
    .page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 1.5rem; }
    h1 { font-size: 1.75rem; font-weight: 600; color: #1565c0; margin: 0; }
    h2 { font-size: 1.25rem; margin: 0 0 1rem 0; }
    .form-card { background: white; padding: 1.5rem; border-radius: 8px; box-shadow: 0 1px 4px rgba(0,0,0,0.08); margin-bottom: 1.5rem; }
    .form-row { display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 1rem; margin-bottom: 1rem; }
    .form-group { display: flex; flex-direction: column; gap: 0.25rem; }
    .form-group label { font-weight: 500; font-size: 0.85rem; color: #555; }
    .form-group input { padding: 0.5rem; border: 1px solid #ddd; border-radius: 6px; }
    .form-group input:focus { outline: 2px solid #1976d2; outline-offset: 2px; }
    .data-table { width: 100%; border-collapse: collapse; background: white; border-radius: 8px; overflow: hidden; box-shadow: 0 1px 4px rgba(0,0,0,0.08); }
    .data-table th { background: #f5f5f5; padding: 0.75rem 1rem; text-align: left; font-weight: 600; font-size: 0.85rem; color: #555; border-bottom: 2px solid #e0e0e0; }
    .data-table td { padding: 0.75rem 1rem; border-bottom: 1px solid #f0f0f0; }
    .font-bold { font-weight: 600; }
    .mono { font-family: monospace; font-size: 0.85rem; }
    .badge { padding: 0.2rem 0.6rem; border-radius: 12px; font-size: 0.8rem; font-weight: 500; }
    .badge-active { background: #e8f5e9; color: #2e7d32; }
    .badge-inactive { background: #fff3e0; color: #ef6c00; }
    .badge-suspended { background: #ffebee; color: #c62828; }
    .btn { padding: 0.5rem 1rem; border: none; border-radius: 6px; font-weight: 500; cursor: pointer; font-size: 0.9rem; }
    .btn-primary { background: #1976d2; color: white; }
    .btn-sm { padding: 0.3rem 0.75rem; font-size: 0.8rem; }
    .btn-danger { background: #c62828; color: white; }
    .btn:focus { outline: 2px solid #1976d2; outline-offset: 2px; }
    .btn:disabled { opacity: 0.5; }
    .loading, .empty-state { padding: 3rem; text-align: center; color: #666; }
  `]
})
export class ApplicationListComponent implements OnInit {
  apps: RegisteredApplication[] = [];
  loading = false;
  showCreateForm = false;
  newApp: ApplicationProvisionRequest = { entraAppId: '', applicationName: '' };

  constructor(private api: ApiService, private notify: NotificationService) {}
  ngOnInit(): void { this.loadApps(); }

  loadApps(): void {
    this.loading = true;
    this.api.getApplications().subscribe({
      next: (r: PageResponse<RegisteredApplication>) => { this.apps = r.content; this.loading = false; },
      error: () => { this.loading = false; }
    });
  }

  provision(): void {
    this.api.provisionApplication(this.newApp).subscribe({
      next: () => { this.notify.success('Application provisioned'); this.showCreateForm = false; this.newApp = { entraAppId: '', applicationName: '' }; this.loadApps(); },
      error: () => this.notify.error('Failed to provision application')
    });
  }

  deprovision(app: RegisteredApplication): void {
    if (!window.confirm(`Deprovision "${app.applicationName}"? This will remove all application data.`)) return;
    this.api.deprovisionApplication(app.id).subscribe({
      next: () => { this.notify.success('Application deprovisioned'); this.loadApps(); },
      error: () => this.notify.error('Failed to deprovision application')
    });
  }

  trackById(_i: number, app: RegisteredApplication): string { return app.id; }
}
