export const environment = {
  production: false,
  apiBaseUrl: 'http://localhost:8080/api',
  adminServiceUrl: 'http://localhost:8081/api/v1',
  auditServiceUrl: 'http://localhost:8082/api/v1',
  documentServiceUrl: 'http://localhost:8083/api/v1',
  complianceServiceUrl: 'http://localhost:8084/api/v1',
  llmServiceUrl: 'http://localhost:8085/api/v1',
  azure: {
    clientId: 'YOUR_CLIENT_ID',
    authority: 'https://login.microsoftonline.com/YOUR_TENANT_ID',
    redirectUri: 'http://localhost:4200',
    apiScopes: ['api://YOUR_API_ID/.default']
  }
};
