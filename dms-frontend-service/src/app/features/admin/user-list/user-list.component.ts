import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ApiService } from '../../../core/services/api.service';
import { NotificationService } from '../../../core/services/notification.service';
import { User, UserCreateRequest, PageResponse } from '../../../core/models';

@Component({
  selector: 'app-user-list',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  template: `
    <div class="page-container" role="region" aria-label="User management">
      <header class="page-header">
        <h1>User Management</h1>
        <button (click)="showCreateForm = !showCreateForm" class="btn btn-primary">{{ showCreateForm ? 'Cancel' : '+ Add User' }}</button>
      </header>

      <div *ngIf="showCreateForm" class="form-card" aria-label="Create user form">
        <h2>Create User</h2>
        <form (ngSubmit)="createUser()">
          <div class="form-row">
            <div class="form-group"><label for="username">Username *</label><input id="username" [(ngModel)]="newUser.username" name="username" required></div>
            <div class="form-group"><label for="email">Email *</label><input id="email" [(ngModel)]="newUser.email" name="email" type="email" required></div>
            <div class="form-group"><label for="displayName">Display Name</label><input id="displayName" [(ngModel)]="newUser.displayName" name="displayName"></div>
          </div>
          <button type="submit" class="btn btn-primary" [disabled]="!newUser.username || !newUser.email">Create User</button>
        </form>
      </div>

      <div *ngIf="loading" class="loading" role="status">Loading users...</div>
      <table *ngIf="!loading && users.length > 0" class="data-table" role="table" aria-label="Users">
        <thead><tr><th scope="col">Username</th><th scope="col">Email</th><th scope="col">Display Name</th><th scope="col">Status</th><th scope="col">Actions</th></tr></thead>
        <tbody>
          <tr *ngFor="let user of users; trackBy: trackById">
            <td>{{ user.username }}</td>
            <td>{{ user.email }}</td>
            <td>{{ user.displayName }}</td>
            <td><span class="badge" [class.badge-active]="user.enabled" [class.badge-inactive]="!user.enabled">{{ user.enabled ? 'Active' : 'Disabled' }}</span></td>
            <td class="actions">
              <button *ngIf="user.enabled" (click)="disableUser(user)" class="btn btn-sm btn-outline" [attr.aria-label]="'Disable ' + user.username">Disable</button>
              <button *ngIf="!user.enabled" (click)="enableUser(user)" class="btn btn-sm btn-outline" [attr.aria-label]="'Enable ' + user.username">Enable</button>
              <button (click)="deleteUser(user)" class="btn btn-sm btn-danger" [attr.aria-label]="'Delete ' + user.username">Delete</button>
            </td>
          </tr>
        </tbody>
      </table>
      <div *ngIf="!loading && users.length === 0" class="empty-state" role="status">No users found.</div>

      <div *ngIf="totalPages > 1" class="pagination"><button (click)="goToPage(currentPage-1)" [disabled]="currentPage===0" class="btn btn-sm">Previous</button><span>Page {{ currentPage + 1 }} / {{ totalPages }}</span><button (click)="goToPage(currentPage+1)" [disabled]="currentPage>=totalPages-1" class="btn btn-sm">Next</button></div>
    </div>
  `,
  styles: [`
    .page-container { max-width: 1200px; margin: 0 auto; }
    .page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 1.5rem; }
    h1 { font-size: 1.75rem; font-weight: 600; color: #1565c0; margin: 0; }
    h2 { font-size: 1.25rem; font-weight: 600; color: #333; margin: 0 0 1rem 0; }
    .form-card { background: white; padding: 1.5rem; border-radius: 8px; box-shadow: 0 1px 4px rgba(0,0,0,0.08); margin-bottom: 1.5rem; }
    .form-row { display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 1rem; margin-bottom: 1rem; }
    .form-group { display: flex; flex-direction: column; gap: 0.25rem; }
    .form-group label { font-weight: 500; font-size: 0.85rem; color: #555; }
    .form-group input { padding: 0.5rem; border: 1px solid #ddd; border-radius: 6px; font-size: 0.9rem; }
    .form-group input:focus { outline: 2px solid #1976d2; outline-offset: 2px; }
    .data-table { width: 100%; border-collapse: collapse; background: white; border-radius: 8px; overflow: hidden; box-shadow: 0 1px 4px rgba(0,0,0,0.08); }
    .data-table th { background: #f5f5f5; padding: 0.75rem 1rem; text-align: left; font-weight: 600; font-size: 0.85rem; color: #555; border-bottom: 2px solid #e0e0e0; }
    .data-table td { padding: 0.75rem 1rem; border-bottom: 1px solid #f0f0f0; }
    .badge { padding: 0.2rem 0.6rem; border-radius: 12px; font-size: 0.8rem; font-weight: 500; }
    .badge-active { background: #e8f5e9; color: #2e7d32; }
    .badge-inactive { background: #ffebee; color: #c62828; }
    .actions { display: flex; gap: 0.5rem; }
    .btn { padding: 0.5rem 1rem; border: none; border-radius: 6px; font-weight: 500; cursor: pointer; font-size: 0.9rem; }
    .btn-primary { background: #1976d2; color: white; }
    .btn-sm { padding: 0.3rem 0.75rem; font-size: 0.8rem; }
    .btn-outline { background: white; border: 1px solid #ddd; color: #555; }
    .btn-danger { background: #c62828; color: white; }
    .btn:focus { outline: 2px solid #1976d2; outline-offset: 2px; }
    .btn:disabled { opacity: 0.5; cursor: not-allowed; }
    .loading, .empty-state { padding: 3rem; text-align: center; color: #666; }
    .pagination { display: flex; justify-content: center; align-items: center; gap: 1rem; margin-top: 1.5rem; }
  `]
})
export class UserListComponent implements OnInit {
  users: User[] = [];
  loading = false;
  currentPage = 0;
  totalPages = 0;
  showCreateForm = false;
  newUser: UserCreateRequest = { username: '', email: '', displayName: '' };

  constructor(private api: ApiService, private notify: NotificationService) {}

  ngOnInit(): void { this.loadUsers(); }

  loadUsers(): void {
    this.loading = true;
    this.api.getUsers(this.currentPage).subscribe({
      next: (r: PageResponse<User>) => { this.users = r.content; this.totalPages = r.totalPages; this.loading = false; },
      error: () => { this.loading = false; }
    });
  }

  createUser(): void {
    this.api.createUser(this.newUser).subscribe({
      next: () => { this.notify.success('User created'); this.showCreateForm = false; this.newUser = { username: '', email: '', displayName: '' }; this.loadUsers(); },
      error: () => this.notify.error('Failed to create user')
    });
  }

  enableUser(user: User): void { this.api.enableUser(user.id).subscribe({ next: () => { this.notify.success('User enabled'); this.loadUsers(); } }); }
  disableUser(user: User): void { this.api.disableUser(user.id).subscribe({ next: () => { this.notify.success('User disabled'); this.loadUsers(); } }); }
  deleteUser(user: User): void {
    if (!window.confirm(`Delete user "${user.username}"?`)) return;
    this.api.deleteUser(user.id).subscribe({ next: () => { this.notify.success('User deleted'); this.loadUsers(); }, error: () => this.notify.error('Failed to delete user') });
  }

  goToPage(page: number): void { this.currentPage = page; this.loadUsers(); }
  trackById(_i: number, user: User): string { return user.id; }
}
