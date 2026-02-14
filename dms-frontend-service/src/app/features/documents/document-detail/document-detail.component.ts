import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { ApiService } from '../../../core/services/api.service';
import { Document } from '../../../core/models';

@Component({
  selector: 'app-document-detail',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <div class="page-container" role="region" aria-label="Document detail">
      <header class="page-header">
        <a routerLink="/documents" class="btn btn-outline">← Back to Documents</a>
      </header>
      <div *ngIf="loading" class="loading" role="status">Loading document...</div>
      <div *ngIf="error" class="error-banner" role="alert">{{ error }}</div>
      <div *ngIf="document" class="detail-card">
        <h1>{{ document.name }}</h1>
        <div class="meta-grid">
          <div class="meta-item"><span class="meta-label">Classification</span><span class="badge" [class]="'badge-' + document.classification.toLowerCase()">{{ document.classification }}</span></div>
          <div class="meta-item"><span class="meta-label">Version</span><span>v{{ document.version }}</span></div>
          <div class="meta-item"><span class="meta-label">Created</span><span>{{ document.createdAt | date:'medium' }}</span></div>
          <div class="meta-item"><span class="meta-label">Updated</span><span>{{ document.updatedAt | date:'medium' }}</span></div>
          <div class="meta-item"><span class="meta-label">PCI Relevant</span><span>{{ document.pciRelevant ? 'Yes' : 'No' }}</span></div>
          <div class="meta-item"><span class="meta-label">Application ID</span><span>{{ document.applicationId }}</span></div>
        </div>
        <div class="actions-bar">
          <button (click)="download()" class="btn btn-primary" aria-label="Download document">Download</button>
          <button (click)="deleteDoc()" class="btn btn-danger" aria-label="Delete document">Delete</button>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .page-container { max-width: 800px; margin: 0 auto; }
    .page-header { margin-bottom: 1.5rem; }
    h1 { font-size: 1.75rem; font-weight: 600; color: #1565c0; margin: 0 0 1.5rem 0; }
    .detail-card { background: white; border-radius: 8px; padding: 2rem; box-shadow: 0 1px 4px rgba(0,0,0,0.08); }
    .meta-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(250px, 1fr)); gap: 1.25rem; margin-bottom: 2rem; }
    .meta-item { display: flex; flex-direction: column; gap: 0.25rem; }
    .meta-label { font-size: 0.8rem; font-weight: 600; color: #888; text-transform: uppercase; }
    .badge { padding: 0.2rem 0.6rem; border-radius: 12px; font-size: 0.8rem; font-weight: 500; display: inline-block; width: fit-content; }
    .badge-public { background: #e3f2fd; color: #1565c0; }
    .badge-internal { background: #fff3e0; color: #ef6c00; }
    .badge-confidential { background: #fce4ec; color: #c2185b; }
    .badge-restricted { background: #f3e5f5; color: #7b1fa2; }
    .badge-pci { background: #ffebee; color: #b71c1c; }
    .actions-bar { display: flex; gap: 1rem; padding-top: 1.5rem; border-top: 1px solid #eee; }
    .btn { padding: 0.6rem 1.25rem; border: none; border-radius: 6px; font-weight: 500; cursor: pointer; text-decoration: none; font-size: 0.95rem; }
    .btn-primary { background: #1976d2; color: white; }
    .btn-danger { background: #c62828; color: white; }
    .btn-outline { background: white; border: 1px solid #ddd; color: #555; display: inline-block; }
    .btn:focus { outline: 2px solid #1976d2; outline-offset: 2px; }
    .loading { padding: 3rem; text-align: center; color: #666; }
    .error-banner { background: #ffebee; color: #c62828; padding: 1rem; border-radius: 6px; margin-bottom: 1rem; border-left: 4px solid #c62828; }
  `]
})
export class DocumentDetailComponent implements OnInit {
  document: Document | null = null;
  loading = false;
  error: string | null = null;

  constructor(private route: ActivatedRoute, private api: ApiService) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) { this.loadDocument(id); }
  }

  loadDocument(id: string): void {
    this.loading = true;
    this.api.getDocument(id).subscribe({
      next: (doc: Document) => { this.document = doc; this.loading = false; },
      error: () => { this.error = 'Failed to load document.'; this.loading = false; }
    });
  }

  download(): void {
    if (!this.document) return;
    this.api.downloadDocument(this.document.id).subscribe({
      next: (blob: Blob) => {
        const url = window.URL.createObjectURL(blob);
        const a = window.document.createElement('a');
        a.href = url;
        a.download = this.document!.name;
        a.click();
        window.URL.revokeObjectURL(url);
      },
      error: () => this.error = 'Failed to download document.'
    });
  }

  deleteDoc(): void {
    if (!this.document || !window.confirm('Are you sure you want to delete this document?')) return;
    this.api.deleteDocument(this.document.id).subscribe({
      next: () => window.history.back(),
      error: () => this.error = 'Failed to delete document.'
    });
  }
}
