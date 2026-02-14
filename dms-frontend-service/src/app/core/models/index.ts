export interface User {
  id: string;
  username: string;
  email: string;
  displayName: string;
  enabled: boolean;
  roles: Role[];
  createdAt: string;
  updatedAt: string;
}

export interface Role {
  id: string;
  name: string;
  description: string;
  permissions: Permission[];
  createdAt: string;
  updatedAt: string;
}

export interface Permission {
  id: string;
  name: string;
  description: string;
  resourceType: string;
  createdAt: string;
  updatedAt: string;
}

export interface RegisteredApplication {
  id: string;
  entraAppId: string;
  applicationName: string;
  storageContainerName: string;
  encryptionKeyName: string;
  status: 'ACTIVE' | 'INACTIVE' | 'SUSPENDED';
  createdAt: string;
  updatedAt: string;
}

export interface Document {
  id: string;
  applicationId: string;
  name: string;
  classification: 'PUBLIC' | 'INTERNAL' | 'CONFIDENTIAL' | 'RESTRICTED' | 'PCI';
  pciRelevant: boolean;
  gdprDataCategories: string[];
  retentionUntil: string;
  blobUrl: string;
  version: number;
  createdAt: string;
  updatedAt: string;
  createdBy: string;
  updatedBy: string;
}

export interface AuditEvent {
  id: string;
  eventId: string;
  eventType: string;
  eventCategory: string;
  timestamp: string;
  userId: string;
  username: string;
  applicationId: string;
  applicationName: string;
  resourceType: string;
  resourceId: string;
  resourceName: string;
  action: string;
  result: 'SUCCESS' | 'FAILURE' | 'PARTIAL';
  details: Record<string, unknown>;
  pciRelevant: boolean;
  gdprRelevant: boolean;
}

export interface LlmQueryRequest {
  query: string;
  filters?: {
    classifications?: string[];
    dateRange?: { start: string; end: string };
  };
  maxResults?: number;
  includeSummary?: boolean;
  applicationId?: string;
}

export interface LlmQueryResponse {
  correlationId: string;
  summary: string;
  results: Record<string, unknown>[];
  totalCount: number;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

export interface UserCreateRequest {
  username: string;
  email: string;
  displayName: string;
}

export interface RoleCreateRequest {
  name: string;
  description: string;
}

export interface ApplicationProvisionRequest {
  entraAppId: string;
  applicationName: string;
}

export interface ComplianceReport {
  status: string;
  period: string;
  message: string;
}

export interface DataExportResponse {
  exportPath: string;
  expiration: string;
}

export interface ErasureResponse {
  deletedCount: number;
  retainedCount: number;
}

// --- Workflow ---
export interface DocumentWorkflow {
  id: string;
  documentId: string;
  status: 'DRAFT' | 'REVIEW' | 'APPROVED' | 'REJECTED' | 'PUBLISHED' | 'ARCHIVED';
  previousStatus: string;
  assignedTo: string;
  comments: string;
  dueDate: string;
  createdAt: string;
}

export interface WorkflowTransitionRequest {
  targetStatus: string;
  assignedTo?: string;
  comments?: string;
  dueDate?: string;
}

// --- Bulk Operations ---
export interface BulkOperationRequest {
  documentIds: string[];
  action: 'DELETE' | 'CLASSIFY' | 'TAG' | 'ARCHIVE';
  targetClassification?: string;
  tags?: string[];
}

export interface BulkOperationResponse {
  totalRequested: number;
  successCount: number;
  failureCount: number;
  failedIds: string[];
  message: string;
}

// --- Search ---
export interface SearchRequest {
  query: string;
  applicationId?: string;
  classifications?: string[];
  dateFrom?: string;
  dateTo?: string;
  tags?: string[];
  page?: number;
  size?: number;
}

// --- Analytics ---
export interface DashboardAnalytics {
  totalDocuments: number;
  documentsThisMonth: number;
  documentsThisWeek: number;
  documentsByClassification: Record<string, number>;
  pciDocuments: number;
  gdprDocuments: number;
  storageUsedMb: number;
  activeUsers: number;
  totalQueries: number;
}

// --- Collaboration ---
export interface DocumentComment {
  id: string;
  documentId: string;
  userId: string;
  username: string;
  content: string;
  parentId: string | null;
  createdAt: string;
}

export interface DocumentShare {
  id: string;
  documentId: string;
  sharedWithUserId: string;
  sharedByUserId: string;
  permission: 'VIEW' | 'EDIT' | 'COMMENT';
  expiresAt: string | null;
  createdAt: string;
}

export interface DocumentFavorite {
  id: string;
  userId: string;
  documentId: string;
  createdAt: string;
}

// --- Templates ---
export interface DocumentTemplate {
  id: string;
  name: string;
  description: string;
  defaultClassification: string;
  contentTemplate: string;
  metadataSchema: string;
  applicationId: string;
  active: boolean;
  createdAt: string;
}

// --- Webhooks ---
export interface Webhook {
  id: string;
  name: string;
  url: string;
  eventTypes: string;
  applicationId: string;
  active: boolean;
  createdAt: string;
}

// --- API Keys ---
export interface ApiKey {
  id: string;
  name: string;
  keyPrefix: string;
  scopes: string;
  applicationId: string;
  active: boolean;
  expiresAt: string | null;
  lastUsedAt: string | null;
  createdAt: string;
}

export interface ApiKeyCreateResponse {
  id: string;
  name: string;
  key: string;
  prefix: string;
  scopes: string;
  message: string;
}
