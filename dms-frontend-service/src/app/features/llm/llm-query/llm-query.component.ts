import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../../core/services/api.service';
import { LlmQueryRequest, LlmQueryResponse } from '../../../core/models';

@Component({
  selector: 'app-llm-query',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="page-container" role="region" aria-label="AI Document Query">
      <h1>🤖 AI Document Query</h1>
      <p class="subtitle">Ask questions about your documents using natural language. The AI will search and summarize relevant results.</p>

      <form (ngSubmit)="executeQuery()" class="query-form" aria-label="Query form">
        <div class="query-input-group">
          <label for="queryInput" class="sr-only">Enter your query</label>
          <textarea id="queryInput" [(ngModel)]="query" name="query" placeholder="e.g., Find all documents related to financial compliance" rows="3" aria-label="Query input" required></textarea>
          <button type="submit" class="btn btn-primary" [disabled]="!query.trim() || loading" aria-label="Execute query">
            {{ loading ? 'Searching...' : 'Search' }}
          </button>
        </div>

        <div class="query-options">
          <label>Mode:</label>
          <label class="radio-label"><input type="radio" [(ngModel)]="mode" name="mode" value="query"> General Query</label>
          <label class="radio-label"><input type="radio" [(ngModel)]="mode" name="mode" value="compliance"> Compliance Check</label>
        </div>
      </form>

      <div *ngIf="error" class="error-banner" role="alert">{{ error }}</div>

      <div *ngIf="response" class="results-section" role="region" aria-label="Query results">
        <div class="summary-card">
          <h2>Summary</h2>
          <p>{{ response.summary }}</p>
          <p class="meta">Correlation ID: <span class="mono">{{ response.correlationId }}</span> | Results: {{ response.totalCount || response.results.length }}</p>
        </div>

        <div *ngIf="response.results.length > 0" class="results-list">
          <h3>Matching Documents</h3>
          <div *ngFor="let result of response.results; let i = index" class="result-card" role="article">
            <h4>Result {{ i + 1 }}</h4>
            <pre class="result-data">{{ result | json }}</pre>
          </div>
        </div>

        <div *ngIf="response.results.length === 0" class="empty-results" role="status">
          <p>No matching documents found. Try rephrasing your query.</p>
        </div>
      </div>

      <div *ngIf="queryHistory.length > 0" class="history-section">
        <h3>Recent Queries</h3>
        <ul class="history-list">
          <li *ngFor="let h of queryHistory" class="history-item">
            <button (click)="query = h; executeQuery()" class="history-btn" [attr.aria-label]="'Rerun query: ' + h">{{ h }}</button>
          </li>
        </ul>
      </div>
    </div>
  `,
  styles: [`
    .page-container { max-width: 900px; margin: 0 auto; }
    h1 { font-size: 1.75rem; font-weight: 600; color: #1565c0; margin-bottom: 0.5rem; }
    .subtitle { color: #666; margin-bottom: 1.5rem; }
    .query-form { margin-bottom: 1.5rem; }
    .query-input-group { display: flex; gap: 1rem; align-items: flex-end; }
    textarea { flex: 1; padding: 0.75rem; border: 2px solid #ddd; border-radius: 8px; font-size: 1rem; font-family: inherit; resize: vertical; }
    textarea:focus { outline: none; border-color: #1976d2; box-shadow: 0 0 0 3px rgba(25,118,210,0.1); }
    .query-options { display: flex; gap: 1.5rem; align-items: center; margin-top: 0.75rem; }
    .query-options label:first-child { font-weight: 500; color: #555; }
    .radio-label { display: flex; align-items: center; gap: 0.3rem; cursor: pointer; }
    .btn { padding: 0.75rem 1.5rem; border: none; border-radius: 6px; font-weight: 600; cursor: pointer; font-size: 1rem; }
    .btn-primary { background: #1976d2; color: white; }
    .btn-primary:hover { background: #1565c0; }
    .btn:disabled { opacity: 0.5; cursor: not-allowed; }
    .btn:focus { outline: 2px solid #1976d2; outline-offset: 2px; }
    .results-section { margin-top: 1.5rem; }
    .summary-card { background: white; padding: 1.5rem; border-radius: 8px; box-shadow: 0 1px 4px rgba(0,0,0,0.08); margin-bottom: 1.5rem; border-left: 4px solid #1976d2; }
    .summary-card h2 { margin: 0 0 0.75rem 0; font-size: 1.1rem; color: #1565c0; }
    .meta { font-size: 0.8rem; color: #888; margin-top: 0.75rem; }
    .mono { font-family: monospace; }
    .results-list h3 { font-size: 1.1rem; color: #333; margin-bottom: 1rem; }
    .result-card { background: white; padding: 1rem; border-radius: 8px; box-shadow: 0 1px 4px rgba(0,0,0,0.08); margin-bottom: 0.75rem; }
    .result-card h4 { margin: 0 0 0.5rem 0; font-size: 0.9rem; color: #555; }
    .result-data { background: #f5f5f5; padding: 0.75rem; border-radius: 4px; font-size: 0.8rem; overflow-x: auto; max-height: 200px; }
    .empty-results { padding: 2rem; text-align: center; color: #666; background: white; border-radius: 8px; }
    .error-banner { background: #ffebee; color: #c62828; padding: 1rem; border-radius: 6px; margin-bottom: 1rem; border-left: 4px solid #c62828; }
    .history-section { margin-top: 2rem; }
    .history-section h3 { font-size: 1rem; color: #555; margin-bottom: 0.75rem; }
    .history-list { list-style: none; padding: 0; display: flex; flex-wrap: wrap; gap: 0.5rem; }
    .history-btn { background: #e3f2fd; color: #1565c0; border: none; padding: 0.3rem 0.75rem; border-radius: 16px; cursor: pointer; font-size: 0.85rem; }
    .history-btn:hover { background: #bbdefb; }
    .history-btn:focus { outline: 2px solid #1976d2; outline-offset: 2px; }
    .sr-only { position: absolute; width: 1px; height: 1px; padding: 0; margin: -1px; overflow: hidden; clip: rect(0,0,0,0); white-space: nowrap; border-width: 0; }
  `]
})
export class LlmQueryComponent {
  query = '';
  mode: 'query' | 'compliance' = 'query';
  loading = false;
  error: string | null = null;
  response: LlmQueryResponse | null = null;
  queryHistory: string[] = [];

  constructor(private api: ApiService) {}

  executeQuery(): void {
    if (!this.query.trim()) return;
    this.loading = true;
    this.error = null;
    this.response = null;

    const request: LlmQueryRequest = { query: this.query, includeSummary: true };
    const call = this.mode === 'compliance'
      ? this.api.complianceCheck(request)
      : this.api.executeQuery(request);

    call.subscribe({
      next: (r: LlmQueryResponse) => {
        this.response = r;
        this.loading = false;
        if (!this.queryHistory.includes(this.query)) {
          this.queryHistory.unshift(this.query);
          if (this.queryHistory.length > 10) this.queryHistory.pop();
        }
      },
      error: () => {
        this.error = 'Failed to execute query. The AI service may be unavailable.';
        this.loading = false;
      }
    });
  }
}
