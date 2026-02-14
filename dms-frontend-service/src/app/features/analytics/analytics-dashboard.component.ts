import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ApiService } from '../../core/services/api.service';
import { DashboardAnalytics } from '../../core/models';

@Component({
  selector: 'app-analytics-dashboard',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="page-container" role="region" aria-label="Analytics Dashboard">
      <h1>📊 Analytics Dashboard</h1>
      <div *ngIf="loading" class="loading" role="status">Loading analytics...</div>
      <div *ngIf="analytics" class="analytics-grid">
        <div class="stat-card"><h3>Total Documents</h3><p class="stat-value">{{ analytics.totalDocuments }}</p></div>
        <div class="stat-card"><h3>This Month</h3><p class="stat-value">{{ analytics.documentsThisMonth }}</p></div>
        <div class="stat-card"><h3>PCI Documents</h3><p class="stat-value">{{ analytics.pciDocuments }}</p></div>
        <div class="stat-card"><h3>Total Queries</h3><p class="stat-value">{{ analytics.totalQueries }}</p></div>
      </div>
      <div *ngIf="analytics" class="classification-section">
        <h2>Documents by Classification</h2>
        <div class="bar-chart">
          <div *ngFor="let entry of classificationEntries" class="bar-row">
            <span class="bar-label">{{ entry[0] }}</span>
            <div class="bar-track"><div class="bar-fill" [style.width.%]="getBarWidth(entry[1])"></div></div>
            <span class="bar-value">{{ entry[1] }}</span>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .page-container { max-width: 1200px; margin: 0 auto; }
    h1 { font-size: 1.75rem; font-weight: 600; color: #1565c0; margin-bottom: 1.5rem; }
    h2 { font-size: 1.25rem; color: #333; margin: 2rem 0 1rem; }
    .analytics-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 1.5rem; }
    .stat-card { background: white; padding: 1.5rem; border-radius: 8px; box-shadow: 0 1px 4px rgba(0,0,0,0.08); }
    .stat-card h3 { margin: 0 0 0.5rem; font-size: 0.85rem; color: #888; text-transform: uppercase; font-weight: 600; }
    .stat-value { font-size: 2.5rem; font-weight: 700; color: #1565c0; margin: 0; }
    .bar-chart { background: white; padding: 1.5rem; border-radius: 8px; box-shadow: 0 1px 4px rgba(0,0,0,0.08); }
    .bar-row { display: flex; align-items: center; gap: 1rem; margin-bottom: 0.75rem; }
    .bar-label { width: 120px; font-weight: 500; font-size: 0.9rem; }
    .bar-track { flex: 1; height: 24px; background: #f0f0f0; border-radius: 12px; overflow: hidden; }
    .bar-fill { height: 100%; background: linear-gradient(90deg, #1976d2, #42a5f5); border-radius: 12px; transition: width 0.5s; }
    .bar-value { width: 40px; text-align: right; font-weight: 600; color: #555; }
    .loading { padding: 3rem; text-align: center; color: #666; }
  `]
})
export class AnalyticsDashboardComponent implements OnInit {
  analytics: DashboardAnalytics | null = null;
  loading = false;
  classificationEntries: [string, number][] = [];
  maxCount = 1;

  constructor(private api: ApiService) {}

  ngOnInit(): void {
    this.loading = true;
    this.api.getDashboardAnalytics().subscribe({
      next: (data: DashboardAnalytics) => {
        this.analytics = data;
        this.classificationEntries = Object.entries(data.documentsByClassification || {});
        this.maxCount = Math.max(1, ...this.classificationEntries.map(e => e[1]));
        this.loading = false;
      },
      error: () => { this.loading = false; }
    });
  }

  getBarWidth(count: number): number {
    return (count / this.maxCount) * 100;
  }
}
