import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient, HttpClientModule } from '@angular/common/http';
import { FormsModule } from '@angular/forms';

interface Document {
  id: string;
  name: string;
  classification: string;
  createdAt: string;
  updatedAt: string;
}

@Component({
  selector: 'app-document-list',
  standalone: true,
  imports: [CommonModule, HttpClientModule, FormsModule],
  template: `
    <div class="document-list-container" role="region" aria-label="Document list">
      <header class="page-header">
        <h1 id="page-title">Documents</h1>
        <button 
          type="button" 
          class="btn-primary"
          (click)="openUploadDialog()"
          aria-label="Upload new document">
          Upload Document
        </button>
      </header>

      <div class="filters" role="search" aria-label="Filter documents">
        <label for="classification-filter" class="sr-only">Filter by classification</label>
        <select 
          id="classification-filter"
          [(ngModel)]="selectedClassification"
          (change)="filterDocuments()"
          aria-label="Document classification filter">
          <option value="">All Classifications</option>
          <option value="PUBLIC">Public</option>
          <option value="INTERNAL">Internal</option>
          <option value="CONFIDENTIAL">Confidential</option>
          <option value="RESTRICTED">Restricted</option>
        </select>
      </div>

      <div *ngIf="loading" class="loading" role="status" aria-live="polite">
        <span class="sr-only">Loading documents</span>
        <span aria-hidden="true">Loading...</span>
      </div>

      <div *ngIf="error" class="error" role="alert" aria-live="assertive">
        <strong>Error:</strong> {{ error }}
      </div>

      <table *ngIf="!loading && !error && documents.length > 0" 
             class="document-table"
             role="table"
             aria-label="Documents table">
        <thead>
          <tr role="row">
            <th scope="col">Name</th>
            <th scope="col">Classification</th>
            <th scope="col">Created</th>
            <th scope="col">Actions</th>
          </tr>
        </thead>
        <tbody>
          <tr *ngFor="let doc of documents; trackBy: trackByDocId" role="row">
            <td>{{ doc.name }}</td>
            <td>
              <span class="badge badge-{{ doc.classification.toLowerCase() }}" 
                    [attr.aria-label]="'Classification: ' + doc.classification">
                {{ doc.classification }}
              </span>
            </td>
            <td>{{ doc.createdAt | date:'short' }}</td>
            <td>
              <button 
                type="button"
                class="btn-link"
                (click)="viewDocument(doc.id)"
                [attr.aria-label]="'View document ' + doc.name">
                View
              </button>
              <button 
                type="button"
                class="btn-link"
                (click)="downloadDocument(doc.id)"
                [attr.aria-label]="'Download document ' + doc.name">
                Download
              </button>
            </td>
          </tr>
        </tbody>
      </table>

      <div *ngIf="!loading && !error && documents.length === 0" 
           class="empty-state"
           role="status"
           aria-live="polite">
        <p>No documents found.</p>
      </div>
    </div>
  `,
  styles: [`
    .document-list-container {
      max-width: 1200px;
      margin: 0 auto;
    }
    .page-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 2rem;
    }
    h1 {
      font-size: 2rem;
      font-weight: 600;
      color: #1976d2;
    }
    .btn-primary {
      background-color: #1976d2;
      color: white;
      border: none;
      padding: 0.75rem 1.5rem;
      border-radius: 4px;
      cursor: pointer;
      font-size: 1rem;
      font-weight: 500;
    }
    .btn-primary:hover, .btn-primary:focus {
      background-color: #1565c0;
      outline: 2px solid #0d47a1;
      outline-offset: 2px;
    }
    .btn-link {
      background: none;
      border: none;
      color: #1976d2;
      text-decoration: underline;
      cursor: pointer;
      padding: 0.25rem 0.5rem;
      margin-right: 0.5rem;
    }
    .btn-link:hover, .btn-link:focus {
      color: #1565c0;
      outline: 2px solid #0d47a1;
      outline-offset: 2px;
    }
    .filters {
      margin-bottom: 1.5rem;
    }
    select {
      padding: 0.5rem;
      border: 1px solid #ccc;
      border-radius: 4px;
      font-size: 1rem;
      min-width: 200px;
    }
    select:focus {
      outline: 2px solid #1976d2;
      outline-offset: 2px;
    }
    .document-table {
      width: 100%;
      border-collapse: collapse;
      background: white;
      box-shadow: 0 2px 4px rgba(0,0,0,0.1);
    }
    .document-table th {
      background-color: #f5f5f5;
      padding: 1rem;
      text-align: left;
      font-weight: 600;
      border-bottom: 2px solid #ddd;
    }
    .document-table td {
      padding: 1rem;
      border-bottom: 1px solid #eee;
    }
    .badge {
      padding: 0.25rem 0.75rem;
      border-radius: 12px;
      font-size: 0.875rem;
      font-weight: 500;
    }
    .badge-public { background-color: #e3f2fd; color: #1976d2; }
    .badge-internal { background-color: #fff3e0; color: #f57c00; }
    .badge-confidential { background-color: #fce4ec; color: #c2185b; }
    .badge-restricted { background-color: #f3e5f5; color: #7b1fa2; }
    .loading, .error, .empty-state {
      padding: 2rem;
      text-align: center;
    }
    .error {
      background-color: #ffebee;
      color: #c62828;
      border: 1px solid #c62828;
      border-radius: 4px;
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
export class DocumentListComponent implements OnInit {
  documents: Document[] = [];
  loading = false;
  error: string | null = null;
  selectedClassification = '';

  constructor(private http: HttpClient) {}

  ngOnInit() {
    this.loadDocuments();
  }

  loadDocuments() {
    this.loading = true;
    this.error = null;
    
    const url = 'http://localhost:8083/api/v1/documents';
    const params = this.selectedClassification 
      ? { classification: this.selectedClassification }
      : {};

    this.http.get<{ content: Document[] }>(url, { params }).subscribe({
      next: (response) => {
        this.documents = response.content || [];
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Failed to load documents. Please try again.';
        this.loading = false;
        console.error('Error loading documents:', err);
      }
    });
  }

  filterDocuments() {
    this.loadDocuments();
  }

  openUploadDialog() {
    // TODO: Implement upload dialog
    alert('Upload dialog will be implemented');
  }

  viewDocument(id: string) {
    // TODO: Navigate to document detail
    console.log('View document:', id);
  }

  downloadDocument(id: string) {
    // TODO: Implement download
    console.log('Download document:', id);
  }

  trackByDocId(index: number, doc: Document): string {
    return doc.id;
  }
}
