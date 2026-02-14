import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  User, Role, Permission, RegisteredApplication, Document,
  AuditEvent, LlmQueryRequest, LlmQueryResponse, PageResponse,
  UserCreateRequest, RoleCreateRequest, ApplicationProvisionRequest,
  ComplianceReport, DataExportResponse, ErasureResponse
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
}
