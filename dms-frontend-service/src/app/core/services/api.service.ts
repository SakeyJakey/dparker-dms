import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  User, Role, Permission, RegisteredApplication, Document,
  AuditEvent, LlmQueryRequest, LlmQueryResponse, PageResponse,
  UserCreateRequest, RoleCreateRequest, ApplicationProvisionRequest,
  ComplianceReport, DataExportResponse, ErasureResponse,
  DocumentWorkflow, WorkflowTransitionRequest, BulkOperationRequest,
  BulkOperationResponse, SearchRequest, DashboardAnalytics,
  DocumentComment, DocumentShare, DocumentFavorite, DocumentTemplate,
  Webhook, ApiKey, ApiKeyCreateResponse
} from '../models';

@Injectable({ providedIn: 'root' })
export class ApiService {
  private adminUrl = environment.adminServiceUrl;
  private auditUrl = environment.auditServiceUrl;
  private documentUrl = environment.documentServiceUrl;
  private complianceUrl = environment.complianceServiceUrl;
  private llmUrl = environment.llmServiceUrl;

  constructor(private http: HttpClient) {}

  // ---- Users ----
  getUsers(page = 0, size = 20): Observable<PageResponse<User>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<PageResponse<User>>(`${this.adminUrl}/admin/users`, { params });
  }

  getUser(id: string): Observable<User> {
    return this.http.get<User>(`${this.adminUrl}/admin/users/${id}`);
  }

  createUser(request: UserCreateRequest): Observable<User> {
    return this.http.post<User>(`${this.adminUrl}/admin/users`, request);
  }

  updateUser(id: string, request: Partial<User>): Observable<User> {
    return this.http.put<User>(`${this.adminUrl}/admin/users/${id}`, request);
  }

  deleteUser(id: string): Observable<void> {
    return this.http.delete<void>(`${this.adminUrl}/admin/users/${id}`);
  }

  enableUser(id: string): Observable<User> {
    return this.http.put<User>(`${this.adminUrl}/admin/users/${id}/enable`, {});
  }

  disableUser(id: string): Observable<User> {
    return this.http.put<User>(`${this.adminUrl}/admin/users/${id}/disable`, {});
  }

  assignRole(userId: string, roleId: string): Observable<User> {
    const params = new HttpParams().set('roleId', roleId);
    return this.http.post<User>(`${this.adminUrl}/admin/users/${userId}/roles`, null, { params });
  }

  removeRole(userId: string, roleId: string): Observable<User> {
    return this.http.delete<User>(`${this.adminUrl}/admin/users/${userId}/roles/${roleId}`);
  }

  // ---- Roles ----
  getRoles(page = 0, size = 20): Observable<PageResponse<Role>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<PageResponse<Role>>(`${this.adminUrl}/admin/roles`, { params });
  }

  getRole(id: string): Observable<Role> {
    return this.http.get<Role>(`${this.adminUrl}/admin/roles/${id}`);
  }

  createRole(request: RoleCreateRequest): Observable<Role> {
    return this.http.post<Role>(`${this.adminUrl}/admin/roles`, request);
  }

  deleteRole(id: string): Observable<void> {
    return this.http.delete<void>(`${this.adminUrl}/admin/roles/${id}`);
  }

  assignPermission(roleId: string, permissionId: string): Observable<Role> {
    const params = new HttpParams().set('permissionId', permissionId);
    return this.http.post<Role>(`${this.adminUrl}/admin/roles/${roleId}/permissions`, null, { params });
  }

  // ---- Permissions ----
  getPermissions(page = 0, size = 100): Observable<PageResponse<Permission>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<PageResponse<Permission>>(`${this.adminUrl}/admin/permissions`, { params });
  }

  // ---- Applications ----
  getApplications(page = 0, size = 20): Observable<PageResponse<RegisteredApplication>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<PageResponse<RegisteredApplication>>(`${this.adminUrl}/admin/applications`, { params });
  }

  provisionApplication(request: ApplicationProvisionRequest): Observable<RegisteredApplication> {
    return this.http.post<RegisteredApplication>(`${this.adminUrl}/admin/applications`, request);
  }

  deprovisionApplication(id: string): Observable<void> {
    return this.http.delete<void>(`${this.adminUrl}/admin/applications/${id}`);
  }

  // ---- Documents ----
  getDocuments(applicationId: string, page = 0, size = 20, classification?: string): Observable<PageResponse<Document>> {
    let params = new HttpParams().set('applicationId', applicationId).set('page', page).set('size', size);
    if (classification) {
      params = params.set('classification', classification);
    }
    return this.http.get<PageResponse<Document>>(`${this.documentUrl}/documents`, { params });
  }

  getDocument(id: string): Observable<Document> {
    return this.http.get<Document>(`${this.documentUrl}/documents/${id}`);
  }

  uploadDocument(applicationId: string, file: File, name: string, classification: string): Observable<Document> {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('name', name);
    formData.append('classification', classification);
    return this.http.post<Document>(`${this.documentUrl}/documents`, formData);
  }

  deleteDocument(id: string): Observable<void> {
    return this.http.delete<void>(`${this.documentUrl}/documents/${id}`);
  }

  downloadDocument(id: string): Observable<Blob> {
    return this.http.get(`${this.documentUrl}/documents/${id}/download`, { responseType: 'blob' });
  }

  // ---- Audit ----
  getAuditLogs(page = 0, size = 20, eventType?: string): Observable<PageResponse<AuditEvent>> {
    let params = new HttpParams().set('page', page).set('size', size);
    if (eventType) {
      params = params.set('eventType', eventType);
    }
    return this.http.get<PageResponse<AuditEvent>>(`${this.auditUrl}/audit/logs`, { params });
  }

  // ---- Compliance ----
  getPciReport(period = 'MONTHLY'): Observable<ComplianceReport> {
    const params = new HttpParams().set('period', period);
    return this.http.get<ComplianceReport>(`${this.complianceUrl}/compliance/pci/report`, { params });
  }

  getDataSubjectData(id: string): Observable<DataExportResponse> {
    return this.http.get<DataExportResponse>(`${this.complianceUrl}/compliance/gdpr/data-subject/${id}`);
  }

  requestErasure(id: string): Observable<ErasureResponse> {
    return this.http.delete<ErasureResponse>(`${this.complianceUrl}/compliance/gdpr/data-subject/${id}`);
  }

  getIso27001Controls(): Observable<ComplianceReport> {
    return this.http.get<ComplianceReport>(`${this.complianceUrl}/compliance/iso27001/controls`);
  }

  // ---- LLM ----
  executeQuery(request: LlmQueryRequest): Observable<LlmQueryResponse> {
    return this.http.post<LlmQueryResponse>(`${this.llmUrl}/llm/query`, request);
  }

  complianceCheck(request: LlmQueryRequest): Observable<LlmQueryResponse> {
    return this.http.post<LlmQueryResponse>(`${this.llmUrl}/llm/compliance-check`, request);
  }

  // ---- Workflow ----
  getWorkflow(documentId: string): Observable<DocumentWorkflow> {
    return this.http.get<DocumentWorkflow>(`${this.documentUrl}/documents/${documentId}/workflow`);
  }

  getWorkflowHistory(documentId: string): Observable<DocumentWorkflow[]> {
    return this.http.get<DocumentWorkflow[]>(`${this.documentUrl}/documents/${documentId}/workflow/history`);
  }

  transitionWorkflow(documentId: string, request: WorkflowTransitionRequest): Observable<DocumentWorkflow> {
    return this.http.post<DocumentWorkflow>(`${this.documentUrl}/documents/${documentId}/workflow/transition`, request);
  }

  submitForReview(documentId: string, reviewerId?: string, comments?: string): Observable<DocumentWorkflow> {
    let params = new HttpParams();
    if (reviewerId) params = params.set('reviewerId', reviewerId);
    if (comments) params = params.set('comments', comments);
    return this.http.post<DocumentWorkflow>(`${this.documentUrl}/documents/${documentId}/workflow/submit-for-review`, null, { params });
  }

  approveDocument(documentId: string, comments?: string): Observable<DocumentWorkflow> {
    let params = new HttpParams();
    if (comments) params = params.set('comments', comments);
    return this.http.post<DocumentWorkflow>(`${this.documentUrl}/documents/${documentId}/workflow/approve`, null, { params });
  }

  rejectDocument(documentId: string, comments?: string): Observable<DocumentWorkflow> {
    let params = new HttpParams();
    if (comments) params = params.set('comments', comments);
    return this.http.post<DocumentWorkflow>(`${this.documentUrl}/documents/${documentId}/workflow/reject`, null, { params });
  }

  // ---- Bulk Operations ----
  executeBulkOperation(request: BulkOperationRequest): Observable<BulkOperationResponse> {
    return this.http.post<BulkOperationResponse>(`${this.documentUrl}/documents/bulk/operation`, request);
  }

  // ---- Advanced Search ----
  searchDocuments(request: SearchRequest): Observable<PageResponse<Document>> {
    return this.http.post<PageResponse<Document>>(`${this.documentUrl}/documents/search`, request);
  }

  fulltextSearch(query: string, page = 0, size = 20): Observable<PageResponse<Document>> {
    const params = new HttpParams().set('query', query).set('page', page).set('size', size);
    return this.http.get<PageResponse<Document>>(`${this.documentUrl}/documents/search/fulltext`, { params });
  }

  // ---- Analytics ----
  getDashboardAnalytics(applicationId?: string): Observable<DashboardAnalytics> {
    let params = new HttpParams();
    if (applicationId) params = params.set('applicationId', applicationId);
    return this.http.get<DashboardAnalytics>(`${this.documentUrl}/documents/analytics/dashboard`, { params });
  }

  // ---- Document Preview ----
  getPreview(documentId: string): Observable<Blob> {
    return this.http.get(`${this.documentUrl}/documents/${documentId}/preview`, { responseType: 'blob' });
  }

  getPreviewInfo(documentId: string): Observable<Record<string, unknown>> {
    return this.http.get<Record<string, unknown>>(`${this.documentUrl}/documents/${documentId}/preview/info`);
  }

  // ---- Comments ----
  getComments(documentId: string): Observable<DocumentComment[]> {
    return this.http.get<DocumentComment[]>(`${this.documentUrl}/documents/${documentId}/comments`);
  }

  addComment(documentId: string, content: string, parentId?: string): Observable<DocumentComment> {
    return this.http.post<DocumentComment>(`${this.documentUrl}/documents/${documentId}/comments`, { content, parentId });
  }

  deleteComment(documentId: string, commentId: string): Observable<void> {
    return this.http.delete<void>(`${this.documentUrl}/documents/${documentId}/comments/${commentId}`);
  }

  // ---- Sharing ----
  getShares(documentId: string): Observable<DocumentShare[]> {
    return this.http.get<DocumentShare[]>(`${this.documentUrl}/documents/${documentId}/shares`);
  }

  shareDocument(documentId: string, userId: string, permission: string): Observable<DocumentShare> {
    return this.http.post<DocumentShare>(`${this.documentUrl}/documents/${documentId}/shares`, { userId, permission });
  }

  revokeShare(documentId: string, shareId: string): Observable<void> {
    return this.http.delete<void>(`${this.documentUrl}/documents/${documentId}/shares/${shareId}`);
  }

  // ---- Favorites ----
  getFavorites(userId: string): Observable<PageResponse<DocumentFavorite>> {
    const params = new HttpParams().set('userId', userId);
    return this.http.get<PageResponse<DocumentFavorite>>(`${this.documentUrl}/documents/favorites`, { params });
  }

  addFavorite(documentId: string, userId: string): Observable<DocumentFavorite> {
    const params = new HttpParams().set('userId', userId);
    return this.http.post<DocumentFavorite>(`${this.documentUrl}/documents/${documentId}/favorites`, null, { params });
  }

  removeFavorite(documentId: string, userId: string): Observable<void> {
    const params = new HttpParams().set('userId', userId);
    return this.http.delete<void>(`${this.documentUrl}/documents/${documentId}/favorites`, { params });
  }

  // ---- Templates ----
  getTemplates(): Observable<PageResponse<DocumentTemplate>> {
    return this.http.get<PageResponse<DocumentTemplate>>(`${this.documentUrl}/documents/templates`);
  }

  createTemplate(template: Partial<DocumentTemplate>): Observable<DocumentTemplate> {
    return this.http.post<DocumentTemplate>(`${this.documentUrl}/documents/templates`, template);
  }

  deleteTemplate(id: string): Observable<void> {
    return this.http.delete<void>(`${this.documentUrl}/documents/templates/${id}`);
  }

  // ---- Webhooks ----
  getWebhooks(): Observable<PageResponse<Webhook>> {
    return this.http.get<PageResponse<Webhook>>(`${this.adminUrl}/admin/webhooks`);
  }

  createWebhook(webhook: Partial<Webhook>): Observable<Webhook> {
    return this.http.post<Webhook>(`${this.adminUrl}/admin/webhooks`, webhook);
  }

  deleteWebhook(id: string): Observable<void> {
    return this.http.delete<void>(`${this.adminUrl}/admin/webhooks/${id}`);
  }

  testWebhook(id: string): Observable<string> {
    return this.http.post(`${this.adminUrl}/admin/webhooks/${id}/test`, null, { responseType: 'text' });
  }

  // ---- API Keys ----
  getApiKeys(): Observable<PageResponse<ApiKey>> {
    return this.http.get<PageResponse<ApiKey>>(`${this.adminUrl}/admin/api-keys`);
  }

  createApiKey(name: string, scopes: string, applicationId?: string): Observable<ApiKeyCreateResponse> {
    return this.http.post<ApiKeyCreateResponse>(`${this.adminUrl}/admin/api-keys`, { name, scopes, applicationId });
  }

  revokeApiKey(id: string): Observable<void> {
    return this.http.delete<void>(`${this.adminUrl}/admin/api-keys/${id}`);
  }

  // ---- Export ----
  exportUsersCsv(): Observable<Blob> {
    return this.http.get(`${this.adminUrl}/admin/export/users/csv`, { responseType: 'blob' });
  }

  exportAuditCsv(eventType?: string): Observable<Blob> {
    let params = new HttpParams();
    if (eventType) params = params.set('eventType', eventType);
    return this.http.get(`${this.adminUrl}/admin/export/audit/csv`, { params, responseType: 'blob' });
  }

  exportComplianceReport(format = 'csv'): Observable<Blob> {
    const params = new HttpParams().set('format', format);
    return this.http.get(`${this.adminUrl}/admin/export/compliance/report`, { params, responseType: 'blob' });
  }
}
