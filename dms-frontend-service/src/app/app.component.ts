import { Component } from '@angular/core';
import { RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';
import { CommonModule } from '@angular/common';
import { NotificationService, Notification } from './core/services/notification.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive, CommonModule],
  template: `
    <div class="app-container" role="application" aria-label="Document Management System">
      <header role="banner" class="app-header">
        <nav role="navigation" aria-label="Main navigation" class="nav-container">
          <a routerLink="/" class="logo" aria-label="DMS Home">
            <span class="logo-icon" aria-hidden="true">📄</span>
            <span class="logo-text">DMS</span>
          </a>
          <ul class="nav-menu" role="menubar">
            <li role="none"><a routerLink="/documents" routerLinkActive="active" role="menuitem">Documents</a></li>
            <li role="none"><a routerLink="/llm" routerLinkActive="active" role="menuitem">AI Query</a></li>
            <li role="none"><a routerLink="/admin" routerLinkActive="active" role="menuitem">Admin</a></li>
            <li role="none"><a routerLink="/compliance" routerLinkActive="active" role="menuitem">Compliance</a></li>
          </ul>
        </nav>
      </header>

      <!-- Notification toast -->
      <div *ngIf="notification" class="notification" [class]="'notification-' + notification.type" role="alert" aria-live="assertive">
        {{ notification.message }}
        <button (click)="notification = null" aria-label="Dismiss notification" class="notification-close">&times;</button>
      </div>

      <main id="main-content" role="main" class="app-main">
        <router-outlet></router-outlet>
      </main>

      <footer role="contentinfo" class="app-footer">
        <p>&copy; 2026 Document Management System — davidparker-lv-bmth</p>
      </footer>
    </div>
  `,
  styles: [`
    :host { display: block; min-height: 100vh; }
    .app-container { min-height: 100vh; display: flex; flex-direction: column; background: #fafafa; }
    .app-header { background: linear-gradient(135deg, #1565c0 0%, #1976d2 100%); color: white; padding: 0 2rem; box-shadow: 0 2px 8px rgba(0,0,0,0.15); position: sticky; top: 0; z-index: 100; }
    .nav-container { display: flex; align-items: center; max-width: 1400px; margin: 0 auto; height: 64px; }
    .logo { display: flex; align-items: center; text-decoration: none; color: white; margin-right: 3rem; font-weight: 700; font-size: 1.25rem; }
    .logo-icon { margin-right: 0.5rem; font-size: 1.5rem; }
    .nav-menu { list-style: none; padding: 0; margin: 0; display: flex; gap: 0.5rem; }
    .nav-menu a { color: rgba(255,255,255,0.85); text-decoration: none; padding: 0.5rem 1rem; border-radius: 6px; font-weight: 500; transition: all 0.2s; }
    .nav-menu a:hover, .nav-menu a:focus { background: rgba(255,255,255,0.15); color: white; outline: 2px solid rgba(255,255,255,0.5); outline-offset: 2px; }
    .nav-menu a.active { background: rgba(255,255,255,0.2); color: white; }
    .app-main { flex: 1; padding: 2rem; max-width: 1400px; margin: 0 auto; width: 100%; box-sizing: border-box; }
    .app-footer { background: #f5f5f5; padding: 1rem 2rem; text-align: center; color: #666; border-top: 1px solid #e0e0e0; }
    .notification { position: fixed; top: 80px; right: 20px; padding: 1rem 2rem; border-radius: 8px; color: white; z-index: 200; display: flex; align-items: center; gap: 1rem; box-shadow: 0 4px 12px rgba(0,0,0,0.15); animation: slideIn 0.3s ease; }
    .notification-success { background: #2e7d32; }
    .notification-error { background: #c62828; }
    .notification-info { background: #1565c0; }
    .notification-warning { background: #ef6c00; }
    .notification-close { background: none; border: none; color: white; font-size: 1.25rem; cursor: pointer; padding: 0 0.25rem; }
    @keyframes slideIn { from { transform: translateX(100%); opacity: 0; } to { transform: translateX(0); opacity: 1; } }
  `]
})
export class AppComponent {
  title = 'DMS Frontend';
  notification: Notification | null = null;

  constructor(private notificationService: NotificationService) {
    this.notificationService.notification$.subscribe(n => {
      this.notification = n;
      if (n.duration) {
        setTimeout(() => this.notification = null, n.duration);
      }
    });
  }
}
