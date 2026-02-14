import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../../core/services/api.service';
import { NotificationService } from '../../../core/services/notification.service';
import { Webhook, PageResponse } from '../../../core/models';

@Component({
  selector: 'app-webhook-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="page-container" role="region" aria-label="Webhook Management">
      <header class="page-header"><h1>🔗 Webhook Management</h1>
        <button (click)="showCreate=!showCreate" class="btn btn-primary">{{ showCreate ? 'Cancel' : '+ Add Webhook' }}</button></header>
      <div *ngIf="showCreate" class="form-card">
        <h2>Create Webhook</h2>
        <form (ngSubmit)="create()">
          <div class="form-row">
            <div class="form-group"><label for="whName">Name *</label><input id="whName" [(ngModel)]="newWh.name" name="name" required></div>
            <div class="form-group"><label for="whUrl">URL *</label><input id="whUrl" [(ngModel)]="newWh.url" name="url" required placeholder="https://..."></div>
            <div class="form-group"><label for="whEvents">Event Types *</label><input id="whEvents" [(ngModel)]="newWh.eventTypes" name="events" required placeholder="CREATE,UPDATE,DELETE"></div>
          </div>
          <button type="submit" class="btn btn-primary" [disabled]="!newWh.name || !newWh.url || !newWh.eventTypes">Create</button>
        </form>
      </div>
      <table *ngIf="webhooks.length > 0" class="data-table" role="table">
        <thead><tr><th>Name</th><th>URL</th><th>Events</th><th>Active</th><th>Actions</th></tr></thead>
        <tbody>
          <tr *ngFor="let wh of webhooks"><td class="font-bold">{{ wh.name }}</td><td class="mono">{{ wh.url }}</td><td>{{ wh.eventTypes }}</td>
            <td><span class="badge" [class.badge-active]="wh.active" [class.badge-inactive]="!wh.active">{{ wh.active ? 'Active' : 'Inactive' }}</span></td>
            <td class="actions"><button (click)="test(wh)" class="btn btn-sm btn-outline">Test</button><button (click)="remove(wh)" class="btn btn-sm btn-danger">Delete</button></td></tr>
        </tbody>
      </table>
      <div *ngIf="webhooks.length === 0 && !loading" class="empty-state">No webhooks configured.</div>
    </div>
  `,
  styles: [`
    .page-container { max-width: 1200px; margin: 0 auto; } .page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 1.5rem; }
    h1 { font-size: 1.75rem; font-weight: 600; color: #1565c0; margin: 0; } h2 { font-size: 1.25rem; margin: 0 0 1rem; }
    .form-card { background: white; padding: 1.5rem; border-radius: 8px; box-shadow: 0 1px 4px rgba(0,0,0,0.08); margin-bottom: 1.5rem; }
    .form-row { display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 1rem; margin-bottom: 1rem; }
    .form-group { display: flex; flex-direction: column; gap: 0.25rem; } .form-group label { font-weight: 500; font-size: 0.85rem; }
    .form-group input { padding: 0.5rem; border: 1px solid #ddd; border-radius: 6px; } .form-group input:focus { outline: 2px solid #1976d2; outline-offset: 2px; }
    .data-table { width: 100%; border-collapse: collapse; background: white; border-radius: 8px; overflow: hidden; box-shadow: 0 1px 4px rgba(0,0,0,0.08); }
    .data-table th { background: #f5f5f5; padding: 0.75rem; text-align: left; font-weight: 600; font-size: 0.85rem; color: #555; border-bottom: 2px solid #e0e0e0; }
    .data-table td { padding: 0.75rem; border-bottom: 1px solid #f0f0f0; } .font-bold { font-weight: 600; } .mono { font-family: monospace; font-size: 0.85rem; }
    .badge { padding: 0.2rem 0.6rem; border-radius: 12px; font-size: 0.8rem; font-weight: 500; } .badge-active { background: #e8f5e9; color: #2e7d32; } .badge-inactive { background: #ffebee; color: #c62828; }
    .actions { display: flex; gap: 0.5rem; }
    .btn { padding: 0.5rem 1rem; border: none; border-radius: 6px; font-weight: 500; cursor: pointer; font-size: 0.9rem; } .btn-primary { background: #1976d2; color: white; }
    .btn-sm { padding: 0.3rem 0.75rem; font-size: 0.8rem; } .btn-outline { background: white; border: 1px solid #ddd; color: #555; } .btn-danger { background: #c62828; color: white; }
    .btn:focus { outline: 2px solid #1976d2; outline-offset: 2px; } .btn:disabled { opacity: 0.5; } .empty-state { padding: 3rem; text-align: center; color: #666; }
  `]
})
export class WebhookListComponent implements OnInit {
  webhooks: Webhook[] = [];
  loading = false;
  showCreate = false;
  newWh: Partial<Webhook> = { name: '', url: '', eventTypes: '', active: true };

  constructor(private api: ApiService, private notify: NotificationService) {}
  ngOnInit(): void { this.load(); }

  load(): void { this.loading = true; this.api.getWebhooks().subscribe({ next: (r: PageResponse<Webhook>) => { this.webhooks = r.content; this.loading = false; }, error: () => { this.loading = false; } }); }
  create(): void { this.api.createWebhook(this.newWh).subscribe({ next: () => { this.notify.success('Webhook created'); this.showCreate = false; this.newWh = { name: '', url: '', eventTypes: '', active: true }; this.load(); } }); }
  remove(wh: Webhook): void { if (!confirm(`Delete webhook "${wh.name}"?`)) return; this.api.deleteWebhook(wh.id).subscribe({ next: () => { this.notify.success('Webhook deleted'); this.load(); } }); }
  test(wh: Webhook): void { this.api.testWebhook(wh.id).subscribe({ next: (r: string) => this.notify.info(r), error: () => this.notify.error('Webhook test failed') }); }
}
