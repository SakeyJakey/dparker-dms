import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ApiService } from '../../../core/services/api.service';
import { NotificationService } from '../../../core/services/notification.service';
import { Role, RoleCreateRequest, PageResponse } from '../../../core/models';

@Component({
  selector: 'app-role-list',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  template: `
    <div class="page-container" role="region" aria-label="Role management">
      <header class="page-header">
        <h1>Role Management</h1>
        <button (click)="showCreateForm = !showCreateForm" class="btn btn-primary">{{ showCreateForm ? 'Cancel' : '+ Add Role' }}</button>
      </header>
      <div *ngIf="showCreateForm" class="form-card">
        <h2>Create Role</h2>
        <form (ngSubmit)="createRole()">
          <div class="form-row">
            <div class="form-group"><label for="roleName">Role Name *</label><input id="roleName" [(ngModel)]="newRole.name" name="name" required></div>
            <div class="form-group"><label for="roleDesc">Description</label><input id="roleDesc" [(ngModel)]="newRole.description" name="description"></div>
          </div>
          <button type="submit" class="btn btn-primary" [disabled]="!newRole.name">Create Role</button>
        </form>
      </div>
      <div *ngIf="loading" class="loading" role="status">Loading roles...</div>
      <table *ngIf="!loading && roles.length > 0" class="data-table" role="table" aria-label="Roles">
        <thead><tr><th scope="col">Name</th><th scope="col">Description</th><th scope="col">Permissions</th><th scope="col">Created</th><th scope="col">Actions</th></tr></thead>
        <tbody>
          <tr *ngFor="let role of roles; trackBy: trackById">
            <td class="font-bold">{{ role.name }}</td>
            <td>{{ role.description || '—' }}</td>
            <td>{{ role.permissions?.length || 0 }}</td>
            <td>{{ role.createdAt | date:'mediumDate' }}</td>
            <td><button (click)="deleteRole(role)" class="btn btn-sm btn-danger" [attr.aria-label]="'Delete role ' + role.name">Delete</button></td>
          </tr>
        </tbody>
      </table>
      <div *ngIf="!loading && roles.length === 0" class="empty-state" role="status">No roles defined.</div>
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
    .btn { padding: 0.5rem 1rem; border: none; border-radius: 6px; font-weight: 500; cursor: pointer; font-size: 0.9rem; }
    .btn-primary { background: #1976d2; color: white; }
    .btn-sm { padding: 0.3rem 0.75rem; font-size: 0.8rem; }
    .btn-danger { background: #c62828; color: white; }
    .btn:focus { outline: 2px solid #1976d2; outline-offset: 2px; }
    .btn:disabled { opacity: 0.5; }
    .loading, .empty-state { padding: 3rem; text-align: center; color: #666; }
  `]
})
export class RoleListComponent implements OnInit {
  roles: Role[] = [];
  loading = false;
  showCreateForm = false;
  newRole: RoleCreateRequest = { name: '', description: '' };

  constructor(private api: ApiService, private notify: NotificationService) {}
  ngOnInit(): void { this.loadRoles(); }

  loadRoles(): void {
    this.loading = true;
    this.api.getRoles().subscribe({
      next: (r: PageResponse<Role>) => { this.roles = r.content; this.loading = false; },
      error: () => { this.loading = false; }
    });
  }

  createRole(): void {
    this.api.createRole(this.newRole).subscribe({
      next: () => { this.notify.success('Role created'); this.showCreateForm = false; this.newRole = { name: '', description: '' }; this.loadRoles(); },
      error: () => this.notify.error('Failed to create role')
    });
  }

  deleteRole(role: Role): void {
    if (!window.confirm(`Delete role "${role.name}"?`)) return;
    this.api.deleteRole(role.id).subscribe({ next: () => { this.notify.success('Role deleted'); this.loadRoles(); } });
  }

  trackById(_i: number, role: Role): string { return role.id; }
}
