import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-loading-spinner',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="spinner-container" [class.overlay]="overlay" role="status" aria-live="polite">
      <div class="spinner" aria-hidden="true"></div>
      <span class="spinner-text">{{ message }}</span>
    </div>
  `,
  styles: [`
    .spinner-container { display: flex; flex-direction: column; align-items: center; justify-content: center; padding: 2rem; gap: 1rem; }
    .spinner-container.overlay { position: fixed; inset: 0; background: rgba(255,255,255,0.8); z-index: 1000; }
    .spinner { width: 40px; height: 40px; border: 4px solid #e0e0e0; border-top: 4px solid #1976d2; border-radius: 50%; animation: spin 0.8s linear infinite; }
    .spinner-text { color: #666; font-size: 0.9rem; }
    @keyframes spin { to { transform: rotate(360deg); } }
  `]
})
export class LoadingSpinnerComponent {
  @Input() message = 'Loading...';
  @Input() overlay = false;
}
