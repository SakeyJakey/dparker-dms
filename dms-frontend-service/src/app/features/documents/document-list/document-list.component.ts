import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../../core/services/api.service';
import { Document, PageResponse } from '../../../core/models';

@Component({
  selector: 'app-document-list',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  template: `
    <div class="page-container" role="region" aria-label="Document list">
      <header class="page-header">
        <h1>Documents</h1>
        <a routerLink="/documents/upload" class="btn btn-primary" aria-label="Upload new document">
          + Upload Document
        </a>
      </header>

      <div class="filters" role="search" aria-label="Filter documents">
        <label for="classification-filter">Classification:</label>
        <select id="classification-filter" [(ngModel)]="selectedClassification" (change)="loadDocuments()" aria-label="Filter by classification">
          <option value="">All</option>
          <option value="PUBLIC">Public</option>
          <option value="INTERNAL">Internal</option>
          <option value="CONFIDENTIAL">Confidential</option>
          <option value="RESTRICTED">Restricted</option>
          <option value="PCI">PCI</option>
        </select>
        <input type="text" [(ngModel)]="searchQuery" placeholder="Search documents..." aria-label="Search documents" class="search-input" (keyup.enter)="loadDocuments()">
      </div>

      <div *ngIf="loading" class="loading" role="status" aria-live="polite">Loading documents...</div>
      <div *ngIf="error" class="error-banner" role="alert" aria-live="assertive">{{ error }}</div>

      <table *ngIf="!loading && documents.length > 0" class="data-table" role="table" aria-label="Documents">
        <thead>
          <tr><th scope="col">Name</th><th scope="col">Classification</th><th scope="col">Version</th><th scope="col">Created</th><th scope="col">Actions</th></tr>
        </thead>
        <tbody>
          <tr *ngFor="let doc of documents; trackBy: trackById">
            <td><a [routerLink]="['/documents', doc.id]" class="link">{{ doc.name }}</a></td>
            <td><span class="badge" [class]="'badge-' + doc.classification.toLowerCase()">{{ doc.classification }}</span></td>
            <td>v{{ doc.version }}</td>
            <td>{{ doc.createdAt | date:'medium' }}</td>
            <td class="actions">
              <a [routerLink]="['/documents', doc.id]" class="btn btn-sm" [attr.aria-label]="'View ' + doc.name">View</a>
              <button (click)="downloadDoc(doc)" class="btn btn-sm btn-outline" [attr.aria-label]="'Download ' + doc.name">Download</button>
            </td>
          </tr>
        </tbody>
      </table>

      <div *ngIf="!loading && documents.length === 0" class="empty-state" role="status">
        <p>No documents found. <a routerLink="/documents/upload">Upload your first document</a>.</p>
      </div>

      <div *ngIf="totalPages > 1" class="pagination" role="navigation" aria-label="Pagination">
        <button (click)="goToPage(currentPage - 1)" [disabled]="currentPage === 0" class="btn btn-sm">Previous</button>
        <span class="page-info">Page {{ currentPage + 1 }} of {{ totalPages }}</span>
        <button (click)="goToPage(currentPage + 1)" [disabled]="currentPage >= totalPages - 1" class="btn btn-sm">Next</button>
      </div>
    </div>
  `,
  styles: [`
    .page-container { max-width: 1200px; margin: 0 auto; }
    .page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 1.5rem; }
    h1 { font-size: 1.75rem; font-weight: 600; color: #1565c0; margin: 0; }
    .filters { display: flex; gap: 1rem; align-items: center; margin-bottom: 1.5rem; flex-wrap: wrap; }
    .filters label { font-weight: 500; color: #555; }
    select, .search-input { padding: 0.5rem 0.75rem; border: 1px solid #ddd; border-radius: 6px; font-size: 0.9rem; }
    select:focus, .search-input:focus { outline: 2px solid #1976d2; outline-offset: 2px; border-color: #1976d2; }
    .search-input { min-width: 250px; }
    .data-table { width: 100%; border-collapse: collapse; background: white; border-radius: 8px; overflow: hidden; box-shadow: 0 1px 4px rgba(0,0,0,0.08); }
    .data-table th { background: #f5f5f5; padding: 0.75rem 1rem; text-align: left; font-weight: 600; font-size: 0.85rem; color: #555; border-bottom: 2px solid #e0e0e0; }
    .data-table td { padding: 0.75rem 1rem; border-bottom: 1px solid #f0f0f0; }
    .data-table tr:hover { background: #fafafa; }
    .badge { padding: 0.2rem 0.6rem; border-radius: 12px; font-size: 0.8rem; font-weight: 500; }
    .badge-public { background: #e3f2fd; color: #1565c0; }
    .badge-internal { background: #fff3e0; color: #ef6c00; }
    .badge-confidential { background: #fce4ec; color: #c2185b; }
    .badge-restricted { background: #f3e5f5; color: #7b1fa2; }
    .badge-pci { background: #ffebee; color: #b71c1c; }
    .actions { display: flex; gap: 0.5rem; }
    .link { color: #1976d2; text-decoration: none; font-weight: 500; }
    .link:hover { text-decoration: underline; }
    .btn { padding: 0.5rem 1rem; border: none; border-radius: 6px; font-weight: 500; cursor: pointer; text-decoration: none; display: inline-block; font-size: 0.9rem; }
    .btn-primary { background: #1976d2; color: white; }
    .btn-primary:hover { background: #1565c0; }
    .btn-sm { padding: 0.3rem 0.75rem; font-size: 0.8rem; }
    .btn-outline { background: white; border: 1px solid #ddd; color: #555; }
    .btn-outline:hover { background: #f5f5f5; }
    .btn:focus { outline: 2px solid #1976d2; outline-offset: 2px; }
    .btn:disabled { opacity: 0.5; cursor: not-allowed; }
    .loading, .empty-state { padding: 3rem; text-align: center; color: #666; }
    .error-banner { background: #ffebee; color: #c62828; padding: 1rem; border-radius: 6px; margin-bottom: 1rem; border-left: 4px solid #c62828; }
    .pagination { display: flex; justify-content: center; align-items: center; gap: 1rem; margin-top: 1.5rem; }
    .page-info { color: #666; font-size: 0.9rem; }
  `]
})
export class DocumentListComponent implements OnInit {
  documents: Document[] = [];
  loading = false;
  error: string | null = null;
  selectedClassification = '';
  searchQuery = '';
  currentPage = 0;
  totalPages = 0;
  readonly applicationId = 'default';

  constructor(private api: ApiService) {}

  ngOnInit(): void { this.loadDocuments(); }

  loadDocuments(): void {
    this.loading = true;
    this.error = null;
    this.api.getDocuments(this.applicationId, this.currentPage, 20, this.selectedClassification || undefined)
      .subscribe({
        next: (response: PageResponse<Document>) => {
          this.documents = response.content || [];
          this.totalPages = response.totalPages;
          this.loading = false;
        },
        error: () => {
          this.error = 'Failed to load documents. Please try again.';
          this.loading = false;
        }
      });
  }

  goToPage(page: number): void {
    this.currentPage = page;
    this.loadDocuments();
  }

  downloadDoc(doc: Document): void {
    this.api.downloadDocument(doc.id).subscribe({
      next: (blob: Blob) => {
        const url = window.URL.createObjectURL(blob);
        const a = window.document.createElement('a');
        a.href = url;
        a.download = doc.name;
        a.click();
        window.URL.revokeObjectURL(url);
      },
      error: () => this.error = 'Failed to download document.'
    });
  }

  trackById(_index: number, doc: Document): string { return doc.id; }
}
