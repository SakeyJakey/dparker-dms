import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { ApiService } from './api.service';

describe('ApiService', () => {
  let service: ApiService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        ApiService,
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    });
    service = TestBed.inject(ApiService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should get users', () => {
    const mockResponse = { content: [], totalElements: 0, totalPages: 0, size: 20, number: 0 };
    service.getUsers(0, 20).subscribe(result => {
      expect(result.content).toEqual([]);
      expect(result.totalElements).toBe(0);
    });
    const req = httpMock.expectOne(r => r.url.includes('/admin/users'));
    expect(req.request.method).toBe('GET');
    req.flush(mockResponse);
  });

  it('should create user', () => {
    const mockUser = { id: '1', username: 'test', email: 'test@test.com', displayName: 'Test' };
    service.createUser({ username: 'test', email: 'test@test.com', displayName: 'Test' }).subscribe(result => {
      expect(result.username).toBe('test');
    });
    const req = httpMock.expectOne(r => r.url.includes('/admin/users'));
    expect(req.request.method).toBe('POST');
    req.flush(mockUser);
  });

  it('should delete user', () => {
    service.deleteUser('123').subscribe();
    const req = httpMock.expectOne(r => r.url.includes('/admin/users/123'));
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });

  it('should get roles', () => {
    const mockResponse = { content: [], totalElements: 0, totalPages: 0, size: 20, number: 0 };
    service.getRoles().subscribe(result => {
      expect(result.content).toEqual([]);
    });
    const req = httpMock.expectOne(r => r.url.includes('/admin/roles'));
    expect(req.request.method).toBe('GET');
    req.flush(mockResponse);
  });

  it('should get documents', () => {
    const mockResponse = { content: [], totalElements: 0, totalPages: 0, size: 20, number: 0 };
    service.getDocuments('app-1').subscribe(result => {
      expect(result.content).toEqual([]);
    });
    const req = httpMock.expectOne(r => r.url.includes('/documents'));
    expect(req.request.method).toBe('GET');
    req.flush(mockResponse);
  });

  it('should execute LLM query', () => {
    const mockResponse = { correlationId: 'uuid', summary: 'Found 0', results: [], totalCount: 0 };
    service.executeQuery({ query: 'test' }).subscribe(result => {
      expect(result.summary).toBe('Found 0');
    });
    const req = httpMock.expectOne(r => r.url.includes('/llm/query'));
    expect(req.request.method).toBe('POST');
    req.flush(mockResponse);
  });

  it('should get audit logs', () => {
    const mockResponse = { content: [], totalElements: 0, totalPages: 0, size: 20, number: 0 };
    service.getAuditLogs().subscribe(result => {
      expect(result.content).toEqual([]);
    });
    const req = httpMock.expectOne(r => r.url.includes('/audit/logs'));
    expect(req.request.method).toBe('GET');
    req.flush(mockResponse);
  });

  it('should get PCI report', () => {
    const mockResponse = { status: 'operational', period: 'MONTHLY', message: 'OK' };
    service.getPciReport().subscribe(result => {
      expect(result.status).toBe('operational');
    });
    const req = httpMock.expectOne(r => r.url.includes('/compliance/pci/report'));
    expect(req.request.method).toBe('GET');
    req.flush(mockResponse);
  });

  it('should get dashboard analytics', () => {
    const mockResponse = { totalDocuments: 10, documentsThisMonth: 5, pciDocuments: 2, totalQueries: 100 };
    service.getDashboardAnalytics().subscribe(result => {
      expect(result.totalDocuments).toBe(10);
    });
    const req = httpMock.expectOne(r => r.url.includes('/documents/analytics/dashboard'));
    expect(req.request.method).toBe('GET');
    req.flush(mockResponse);
  });

  it('should get workflow', () => {
    const mockResponse = { id: '1', documentId: '2', status: 'DRAFT' };
    service.getWorkflow('doc-1').subscribe(result => {
      expect(result.status).toBe('DRAFT');
    });
    const req = httpMock.expectOne(r => r.url.includes('/documents/doc-1/workflow'));
    expect(req.request.method).toBe('GET');
    req.flush(mockResponse);
  });

  it('should create API key', () => {
    const mockResponse = { id: '1', name: 'test', key: 'dms_abc123', prefix: 'dms_abc1', scopes: 'read', message: 'OK' };
    service.createApiKey('test', 'read').subscribe(result => {
      expect(result.key).toContain('dms_');
    });
    const req = httpMock.expectOne(r => r.url.includes('/admin/api-keys'));
    expect(req.request.method).toBe('POST');
    req.flush(mockResponse);
  });

  it('should fulltext search', () => {
    const mockResponse = { content: [], totalElements: 0, totalPages: 0, size: 20, number: 0 };
    service.fulltextSearch('compliance').subscribe(result => {
      expect(result.content).toEqual([]);
    });
    const req = httpMock.expectOne(r => r.url.includes('/documents/search/fulltext'));
    expect(req.request.method).toBe('GET');
    req.flush(mockResponse);
  });
});
