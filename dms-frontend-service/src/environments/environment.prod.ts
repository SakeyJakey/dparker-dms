export const environment = {
  production: true,
  apiBaseUrl: '/api',
  adminServiceUrl: '/api/v1',
  auditServiceUrl: '/api/v1',
  documentServiceUrl: '/api/v1',
  complianceServiceUrl: '/api/v1',
  llmServiceUrl: '/api/v1',
  azure: {
    clientId: '${AZURE_CLIENT_ID}',
    authority: 'https://login.microsoftonline.com/${AZURE_TENANT_ID}',
    redirectUri: '${REDIRECT_URI}',
    apiScopes: ['api://${AZURE_API_ID}/.default']
  }
};
