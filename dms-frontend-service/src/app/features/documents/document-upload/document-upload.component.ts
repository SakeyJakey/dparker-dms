import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { ApiService } from '../../../core/services/api.service';
import { NotificationService } from '../../../core/services/notification.service';

@Component({
  selector: 'app-document-upload',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  template: `
    <div class="page-container" role="region" aria-label="Upload document">
      <header class="page-header">
        <h1>Upload Document</h1>
        <a routerLink="/documents" class="btn btn-outline">← Back to Documents</a>
      </header>
      <form (ngSubmit)="upload()" class="form-card" #uploadForm="ngForm" aria-label="Document upload form">
        <div class="form-group">
          <label for="docName">Document Name *</label>
          <input id="docName" type="text" [(ngModel)]="name" name="name" required placeholder="Enter document name" aria-required="true">
        </div>
        <div class="form-group">
          <label for="classification">Classification *</label>
          <select id="classification" [(ngModel)]="classification" name="classification" required aria-required="true">
            <option value="">Select classification</option>
            <option value="PUBLIC">Public</option>
            <option value="INTERNAL">Internal</option>
            <option value="CONFIDENTIAL">Confidential</option>
            <option value="RESTRICTED">Restricted</option>
            <option value="PCI">PCI</option>
          </select>
        </div>
        <div class="form-group">
          <label for="file">File *</label>
          <div class="file-drop-zone" [class.drag-over]="isDragOver" (dragover)="onDragOver($event)" (dragleave)="isDragOver=false" (drop)="onDrop($event)" role="button" tabindex="0" aria-label="Drop file here or click to browse" (keyup.enter)="fileInput.click()">
            <input #fileInput id="file" type="file" (change)="onFileSelected($event)" class="file-input" aria-label="Select file">
            <p *ngIf="!selectedFile">Drag and drop a file here, or click to browse</p>
            <p *ngIf="selectedFile" class="selected-file">📎 {{ selectedFile.name }} ({{ formatFileSize(selectedFile.size) }})</p>
          </div>
        </div>
        <div class="form-actions">
          <button type="submit" class="btn btn-primary" [disabled]="uploading || !name || !classification || !selectedFile">
            {{ uploading ? 'Uploading...' : 'Upload Document' }}
          </button>
        </div>
        <div *ngIf="error" class="error-banner" role="alert">{{ error }}</div>
      </form>
    </div>
  `,
  styles: [`
    .page-container { max-width: 700px; margin: 0 auto; }
    .page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 1.5rem; }
    h1 { font-size: 1.75rem; font-weight: 600; color: #1565c0; margin: 0; }
    .form-card { background: white; border-radius: 8px; padding: 2rem; box-shadow: 0 1px 4px rgba(0,0,0,0.08); }
    .form-group { margin-bottom: 1.5rem; }
    .form-group label { display: block; font-weight: 500; margin-bottom: 0.5rem; color: #333; }
    .form-group input, .form-group select { width: 100%; padding: 0.6rem 0.75rem; border: 1px solid #ddd; border-radius: 6px; font-size: 0.95rem; box-sizing: border-box; }
    .form-group input:focus, .form-group select:focus { outline: 2px solid #1976d2; outline-offset: 2px; border-color: #1976d2; }
    .file-drop-zone { border: 2px dashed #ccc; border-radius: 8px; padding: 2rem; text-align: center; cursor: pointer; transition: all 0.2s; position: relative; }
    .file-drop-zone:hover, .drag-over { border-color: #1976d2; background: #e3f2fd; }
    .file-input { position: absolute; inset: 0; opacity: 0; cursor: pointer; }
    .selected-file { color: #1976d2; font-weight: 500; }
    .form-actions { margin-top: 1.5rem; }
    .btn { padding: 0.6rem 1.25rem; border: none; border-radius: 6px; font-weight: 500; cursor: pointer; text-decoration: none; display: inline-block; font-size: 0.95rem; }
    .btn-primary { background: #1976d2; color: white; }
    .btn-primary:hover { background: #1565c0; }
    .btn-primary:disabled { opacity: 0.5; cursor: not-allowed; }
    .btn-outline { background: white; border: 1px solid #ddd; color: #555; }
    .btn:focus { outline: 2px solid #1976d2; outline-offset: 2px; }
    .error-banner { background: #ffebee; color: #c62828; padding: 1rem; border-radius: 6px; margin-top: 1rem; border-left: 4px solid #c62828; }
  `]
})
export class DocumentUploadComponent {
  name = '';
  classification = '';
  selectedFile: File | null = null;
  uploading = false;
  isDragOver = false;
  error: string | null = null;

  constructor(private api: ApiService, private router: Router, private notify: NotificationService) {}

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      this.selectedFile = input.files[0];
    }
  }

  onDragOver(event: DragEvent): void { event.preventDefault(); this.isDragOver = true; }

  onDrop(event: DragEvent): void {
    event.preventDefault();
    this.isDragOver = false;
    if (event.dataTransfer?.files && event.dataTransfer.files.length > 0) {
      this.selectedFile = event.dataTransfer.files[0];
    }
  }

  upload(): void {
    if (!this.selectedFile || !this.name || !this.classification) return;
    this.uploading = true;
    this.error = null;
    this.api.uploadDocument('default', this.selectedFile, this.name, this.classification)
      .subscribe({
        next: () => {
          this.notify.success('Document uploaded successfully!');
          void this.router.navigate(['/documents']);
        },
        error: () => {
          this.error = 'Failed to upload document. Please try again.';
          this.uploading = false;
        }
      });
  }

  formatFileSize(bytes: number): string {
    if (bytes < 1024) return bytes + ' B';
    if (bytes < 1048576) return (bytes / 1024).toFixed(1) + ' KB';
    return (bytes / 1048576).toFixed(1) + ' MB';
  }
}
