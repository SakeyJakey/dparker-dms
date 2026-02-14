import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-confirm-dialog',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div *ngIf="visible" class="dialog-overlay" role="dialog" aria-modal="true" [attr.aria-label]="title">
      <div class="dialog-content">
        <h2>{{ title }}</h2>
        <p>{{ message }}</p>
        <div class="dialog-actions">
          <button (click)="onCancel()" class="btn btn-outline" type="button">Cancel</button>
          <button (click)="onConfirm()" class="btn" [class]="confirmClass" type="button" autofocus>{{ confirmText }}</button>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .dialog-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.5); display: flex; align-items: center; justify-content: center; z-index: 1000; animation: fadeIn 0.2s ease; }
    .dialog-content { background: white; border-radius: 12px; padding: 2rem; max-width: 420px; width: 90%; box-shadow: 0 8px 32px rgba(0,0,0,0.2); }
    h2 { margin: 0 0 0.75rem; font-size: 1.25rem; color: #333; }
    p { margin: 0 0 1.5rem; color: #666; line-height: 1.5; }
    .dialog-actions { display: flex; gap: 0.75rem; justify-content: flex-end; }
    .btn { padding: 0.5rem 1.25rem; border: none; border-radius: 6px; font-weight: 500; cursor: pointer; font-size: 0.9rem; }
    .btn-outline { background: white; border: 1px solid #ddd; color: #555; }
    .btn-danger { background: #c62828; color: white; }
    .btn-primary { background: #1976d2; color: white; }
    .btn:focus { outline: 2px solid #1976d2; outline-offset: 2px; }
    @keyframes fadeIn { from { opacity: 0; } to { opacity: 1; } }
  `]
})
export class ConfirmDialogComponent {
  @Input() visible = false;
  @Input() title = 'Confirm';
  @Input() message = 'Are you sure?';
  @Input() confirmText = 'Confirm';
  @Input() confirmClass = 'btn-danger';
  @Output() confirmed = new EventEmitter<void>();
  @Output() cancelled = new EventEmitter<void>();

  onConfirm(): void { this.confirmed.emit(); this.visible = false; }
  onCancel(): void { this.cancelled.emit(); this.visible = false; }
}
