import { Component } from '@angular/core';
import { RouterOutlet, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, RouterLink, CommonModule],
  template: `
    <div class="app-container" role="application" aria-label="Document Management System">
      <header role="banner" class="app-header">
        <nav role="navigation" aria-label="Main navigation">
          <h1 class="sr-only">Document Management System</h1>
          <a routerLink="/" class="logo" aria-label="DMS Home">
            <span aria-hidden="true">DMS</span>
          </a>
          <ul class="nav-menu" role="menubar">
            <li role="none">
              <a routerLink="/documents" role="menuitem" aria-label="Documents">Documents</a>
            </li>
            <li role="none">
              <a routerLink="/admin" role="menuitem" aria-label="Administration">Admin</a>
            </li>
            <li role="none">
              <a routerLink="/compliance" role="menuitem" aria-label="Compliance">Compliance</a>
            </li>
          </ul>
        </nav>
      </header>
      <main role="main" class="app-main">
        <router-outlet></router-outlet>
      </main>
      <footer role="contentinfo" class="app-footer">
        <p>&copy; 2024 Document Management System. All rights reserved.</p>
      </footer>
    </div>
  `,
  styles: [`
    .app-container {
      min-height: 100vh;
      display: flex;
      flex-direction: column;
    }
    .app-header {
      background-color: #1976d2;
      color: white;
      padding: 1rem 2rem;
    }
    .app-main {
      flex: 1;
      padding: 2rem;
    }
    .app-footer {
      background-color: #f5f5f5;
      padding: 1rem 2rem;
      text-align: center;
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
export class AppComponent {
  title = 'DMS Frontend';
}
