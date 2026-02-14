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
