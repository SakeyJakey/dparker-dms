import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../../core/services/api.service';
import { NotificationService } from '../../../core/services/notification.service';
import { ApiKey, ApiKeyCreateResponse, PageResponse } from '../../../core/models';

@Component({
  selector: 'app-api-key-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="page-container" role="region" aria-label="API Key Management">
      <header class="page-header"><h1>🔑 API Key Management</h1>
        <button (click)="showCreate=!showCreate" class="btn btn-primary">{{ showCreate ? 'Cancel' : '+ Create API Key' }}</button></header>
      <div *ngIf="showCreate" class="form-card">
        <h2>Create API Key</h2>
        <form (ngSubmit)="create()">
          <div class="form-row">
            <div class="form-group"><label for="keyName">Name *</label><input id="keyName" [(ngModel)]="newKeyName" name="name" required placeholder="My Agent Key"></div>
            <div class="form-group"><label for="keyScopes">Scopes</label><input id="keyScopes" [(ngModel)]="newKeyScopes" name="scopes" placeholder="read,write"></div>
          </div>
          <button type="submit" class="btn btn-primary" [disabled]="!newKeyName">Create Key</button>
        </form>
      </div>
      <div *ngIf="createdKey" class="key-display" role="alert">
        <h3>⚠️ New API Key Created — Copy It Now!</h3>
        <p class="key-value mono">{{ createdKey.key }}</p>
        <p class="key-warning">This key will not be shown again. Store it securely.</p>
        <button (click)="copyKey()" class="btn btn-outline btn-sm">Copy to Clipboard</button>
      </div>
      <table *ngIf="apiKeys.length > 0" class="data-table" role="table">
        <thead><tr><th>Name</th><th>Prefix</th><th>Scopes</th><th>Status</th><th>Last Used</th><th>Actions</th></tr></thead>
        <tbody>
          <tr *ngFor="let key of apiKeys"><td class="font-bold">{{ key.name }}</td><td class="mono">{{ key.keyPrefix }}...</td><td>{{ key.scopes }}</td>
            <td><span class="badge" [class.badge-active]="key.active" [class.badge-inactive]="!key.active">{{ key.active ? 'Active' : 'Revoked' }}</span></td>
            <td>{{ key.lastUsedAt || 'Never' }}</td>
            <td><button *ngIf="key.active" (click)="revoke(key)" class="btn btn-sm btn-danger">Revoke</button></td></tr>
        </tbody>
      </table>
      <div *ngIf="apiKeys.length === 0 && !loading" class="empty-state">No API keys created.</div>
    </div>
  `,
  styles: [`
    .page-container { max-width: 1200px; margin: 0 auto; } .page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 1.5rem; }
    h1 { font-size: 1.75rem; font-weight: 600; color: #1565c0; margin: 0; } h2 { font-size: 1.25rem; margin: 0 0 1rem; } h3 { margin: 0 0 0.75rem; color: #ef6c00; }
    .form-card { background: white; padding: 1.5rem; border-radius: 8px; box-shadow: 0 1px 4px rgba(0,0,0,0.08); margin-bottom: 1.5rem; }
    .form-row { display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 1rem; margin-bottom: 1rem; }
    .form-group { display: flex; flex-direction: column; gap: 0.25rem; } .form-group label { font-weight: 500; font-size: 0.85rem; }
    .form-group input { padding: 0.5rem; border: 1px solid #ddd; border-radius: 6px; } .form-group input:focus { outline: 2px solid #1976d2; outline-offset: 2px; }
    .key-display { background: #fff3e0; border: 2px solid #ef6c00; padding: 1.5rem; border-radius: 8px; margin-bottom: 1.5rem; }
    .key-value { background: #333; color: #4caf50; padding: 0.75rem 1rem; border-radius: 4px; font-size: 0.85rem; word-break: break-all; margin: 0.5rem 0; }
    .key-warning { color: #c62828; font-weight: 500; font-size: 0.85rem; }
    .data-table { width: 100%; border-collapse: collapse; background: white; border-radius: 8px; overflow: hidden; box-shadow: 0 1px 4px rgba(0,0,0,0.08); }
    .data-table th { background: #f5f5f5; padding: 0.75rem; text-align: left; font-weight: 600; font-size: 0.85rem; color: #555; border-bottom: 2px solid #e0e0e0; }
    .data-table td { padding: 0.75rem; border-bottom: 1px solid #f0f0f0; } .font-bold { font-weight: 600; } .mono { font-family: monospace; }
    .badge { padding: 0.2rem 0.6rem; border-radius: 12px; font-size: 0.8rem; font-weight: 500; } .badge-active { background: #e8f5e9; color: #2e7d32; } .badge-inactive { background: #ffebee; color: #c62828; }
    .btn { padding: 0.5rem 1rem; border: none; border-radius: 6px; font-weight: 500; cursor: pointer; font-size: 0.9rem; } .btn-primary { background: #1976d2; color: white; }
    .btn-sm { padding: 0.3rem 0.75rem; font-size: 0.8rem; } .btn-outline { background: white; border: 1px solid #ddd; color: #555; } .btn-danger { background: #c62828; color: white; }
    .btn:focus { outline: 2px solid #1976d2; outline-offset: 2px; } .btn:disabled { opacity: 0.5; } .empty-state { padding: 3rem; text-align: center; color: #666; }
  `]
})
export class ApiKeyListComponent implements OnInit {
  apiKeys: ApiKey[] = [];
  loading = false;
  showCreate = false;
  newKeyName = '';
  newKeyScopes = 'read';
  createdKey: ApiKeyCreateResponse | null = null;

  constructor(private api: ApiService, private notify: NotificationService) {}
  ngOnInit(): void { this.load(); }

  load(): void { this.loading = true; this.api.getApiKeys().subscribe({ next: (r: PageResponse<ApiKey>) => { this.apiKeys = r.content; this.loading = false; }, error: () => { this.loading = false; } }); }
  create(): void { this.api.createApiKey(this.newKeyName, this.newKeyScopes).subscribe({ next: (r: ApiKeyCreateResponse) => { this.createdKey = r; this.notify.success('API key created'); this.showCreate = false; this.newKeyName = ''; this.load(); }, error: () => this.notify.error('Failed to create key') }); }
  revoke(key: ApiKey): void { if (!confirm(`Revoke API key "${key.name}"?`)) return; this.api.revokeApiKey(key.id).subscribe({ next: () => { this.notify.success('Key revoked'); this.load(); } }); }
  copyKey(): void { if (this.createdKey) { navigator.clipboard.writeText(this.createdKey.key); this.notify.success('Key copied to clipboard'); } }
}
