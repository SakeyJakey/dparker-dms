# LV Authentication Patterns

This document describes the standard LV patterns for Azure AD authentication in microservices applications. These patterns are application-generic and can be applied to any Spring Boot application.

## Overview

This document covers:
- Azure AD authentication flow
- JWT token handling
- Role mapping from Azure AD groups
- Configuration via ConfigMaps and Secrets
- Integration with API Gateway and Frontend

## Authentication Flow

### High-Level Flow

```
1. User clicks "Login with Microsoft" in frontend
   ↓
2. Frontend redirects to: /api/auth/azure/login
   (via Istio/Ingress → API Gateway → auth-service)
   ↓
3. Auth-service (MSAL4J) redirects to Azure AD
   ↓
4. User authenticates with Azure AD
   ↓
5. Azure AD redirects to: /api/auth/azure/callback?code=...&state=...
   (via Istio/Ingress → API Gateway → auth-service)
   ↓
6. Auth-service exchanges code for tokens, maps AD groups to roles,
   creates/updates user and session, returns JWT to frontend
   ↓
7. Frontend uses JWT token for all further /api/* calls
   (Browser → Istio/Ingress → Frontend Nginx → API Gateway → backend services)
```

## Configuration

### Azure AD ConfigMap

**Location**: `clusters/<env>/releases/<app>/<env>/config-maps/<app>-azure-configmap.yaml`

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: <app>-azure-configmap
  namespace: <app>-<env>
data:
  # Authority (Azure AD tenant endpoint)
  azure.auth.authority: "https://login.microsoftonline.com/${AZURE_TENANT_ID}/v2.0"
  
  # Home page (origin only, no path)
  azure.auth.home-page: "https://<app>.<env>.lvad.lvfs.net"
  
  # Redirect endpoint (path only, no full URL)
  app.redirectEndpoint: "/api/auth/azure/callback"
  
  # Auth provider
  auth.provider: "azure"
  
  # Azure AD group → application role mapping
  # Use Azure AD group Object ID (GUID) or display name as key
  azure.ad.role-mapping.<group-object-id>: "ROLE_ADMINISTRATOR"
  azure.ad.role-mapping.<group-display-name>: "ROLE_USER"
```

**Critical Configuration Points**:

1. **Home Page**: Must be origin only (no path)
   - ✅ Correct: `https://<app>.<env>.lvad.lvfs.net`
   - ❌ Wrong: `https://<app>.<env>.lvad.lvfs.net/dashboard`

2. **Redirect URI**: Built as `homePage + redirectEndpoint`
   - Result: `https://<app>.<env>.lvad.lvfs.net/api/auth/azure/callback`
   - Must match Azure AD app registration exactly

3. **Role Mapping**: Use Azure AD group Object ID or display name
   - Object ID: GUID from Azure Portal → Azure AD → Groups → Object ID
   - Display Name: Group name (e.g., `SickPay-Policy-Administrators`)

### Azure AD Secrets

**Location**: Azure Key Vault or Kubernetes Secrets

```yaml
apiVersion: v1
kind: Secret
metadata:
  name: <app>-secrets
  namespace: <app>-<env>
type: Opaque
stringData:
  azure.client.id: "<azure-ad-app-client-id>"
  azure.client.secret: "<azure-ad-app-client-secret>"
  jwt.secret: "<shared-jwt-secret-for-app-tokens>"
```

**Environment Variables in Deployment**:

```yaml
env:
- name: AZURE_CLIENT_ID
  valueFrom:
    secretKeyRef:
      name: <app>-secrets
      key: azure.client.id
- name: AZURE_CLIENT_SECRET
  valueFrom:
    secretKeyRef:
      name: <app>-secrets
      key: azure.client.secret
- name: JWT_SECRET
  valueFrom:
    secretKeyRef:
      name: <app>-secrets
      key: jwt.secret
- name: AZURE_AUTH_AUTHORITY
  valueFrom:
    configMapKeyRef:
      name: <app>-azure-configmap
      key: azure.auth.authority
- name: AZURE_HOME_PAGE
  valueFrom:
    configMapKeyRef:
      name: <app>-azure-configmap
      key: azure.auth.home-page
- name: APP_REDIRECT_ENDPOINT
  valueFrom:
    configMapKeyRef:
      name: <app>-azure-configmap
      key: app.redirectEndpoint
```

## Spring Boot Configuration

### application.yml

```yaml
azure:
  auth:
    authority: ${AZURE_AUTH_AUTHORITY}
    home-page: ${AZURE_HOME_PAGE}
    client-id: ${AZURE_CLIENT_ID}
    client-secret: ${AZURE_CLIENT_SECRET}
    scopes: openid profile offline_access
    redirect-uri: ${AZURE_HOME_PAGE}${APP_REDIRECT_ENDPOINT}

app:
  redirect-endpoint: ${APP_REDIRECT_ENDPOINT:/api/auth/azure/callback}

jwt:
  secret: ${JWT_SECRET}
  expiration: 3600000  # 1 hour in milliseconds

azure:
  ad:
    role-mapping:
      # Map Azure AD groups to application roles
      # Keys can be Object IDs or display names
      <group-object-id>: ROLE_ADMINISTRATOR
      <group-display-name>: ROLE_USER
```

### Security Configuration

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Value("${azure.auth.authority}")
    private String authority;
    
    @Value("${azure.auth.client-id}")
    private String clientId;
    
    @Value("${azure.auth.client-secret}")
    private String clientSecret;
    
    @Value("${azure.auth.redirect-uri}")
    private String redirectUri;
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/azure/login").permitAll()
                .requestMatchers("/api/auth/azure/callback").permitAll()
                .requestMatchers("/actuator/health").permitAll()
                .anyRequest().authenticated()
            )
            .oauth2Login(oauth2 -> oauth2
                .authorizationEndpoint(auth -> auth
                    .baseUri("/api/auth/azure/login")
                )
                .redirectionEndpoint(redir -> redir
                    .baseUri("/api/auth/azure/callback")
                )
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .decoder(jwtDecoder())
                )
            );
        return http.build();
    }
    
    @Bean
    public JwtDecoder jwtDecoder() {
        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder
            .withJwkSetUri(authority + "/discovery/v2.0/keys")
            .build();
        return jwtDecoder;
    }
}
```

## MSAL4J Integration

### AuthHelper Pattern

Based on the LV pattern from `dparker-rti-now`, use an adapter pattern for framework-agnostic authentication:

```java
public class AuthHelper {
    
    public static void signIn(IdentityContextAdapter contextAdapter) 
            throws AuthException, IOException {
        authorize(contextAdapter);
    }
    
    private static void authorize(IdentityContextAdapter contextAdapter) 
            throws IOException, AuthException {
        final IdentityContextData context = contextAdapter.getContext();
        
        if (context.getAccount() != null) {
            // Try silent token acquisition first
            acquireTokenSilently(contextAdapter);
        } else {
            // No existing session, redirect to Azure AD
            redirectToAuthorizationEndpoint(contextAdapter);
        }
    }
    
    private static void redirectToAuthorizationEndpoint(
            IdentityContextAdapter contextAdapter) throws IOException {
        final IdentityContextData context = contextAdapter.getContext();
        
        // Generate CSRF protection values
        final String state = UUID.randomUUID().toString();
        final String nonce = UUID.randomUUID().toString();
        
        context.setStateAndNonce(state, nonce);
        contextAdapter.setContext(context);
        
        final ConfidentialClientApplication client = getConfidentialClientInstance();
        AuthorizationRequestUrlParameters parameters = 
            AuthorizationRequestUrlParameters
                .builder(redirectUri, Collections.singleton(scopes))
                .responseMode(ResponseMode.QUERY)
                .prompt(Prompt.SELECT_ACCOUNT)
                .state(state)
                .nonce(nonce)
                .build();
        
        final String authorizeUrl = 
            client.getAuthorizationRequestUrl(parameters).toString();
        contextAdapter.redirectUser(authorizeUrl);
    }
    
    public static void processAADCallback(IdentityContextAdapter contextAdapter) 
            throws AuthException {
        final IdentityContextData context = contextAdapter.getContext();
        
        // 1. Validate state (CSRF protection)
        validateState(contextAdapter);
        
        // 2. Check for error codes
        processErrorCodes(contextAdapter);
        
        // 3. Get authorization code
        final String authCode = contextAdapter.getParameter("code");
        if (authCode == null) {
            throw new AuthException("Auth code is not in request!");
        }
        
        // 4. Exchange code for token
        final AuthorizationCodeParameters authParams = 
            AuthorizationCodeParameters
                .builder(authCode, new URI(redirectUri))
                .scopes(Collections.singleton(scopes))
                .build();
        
        final ConfidentialClientApplication client = getConfidentialClientInstance();
        final IAuthenticationResult result = client.acquireToken(authParams).get();
        
        // 5. Parse and validate ID token
        context.setIdTokenClaims(result.idToken());
        validateNonce(context);
        
        // 6. Map Azure AD groups to application roles
        mapAzureADGroupsToRoles(context);
        
        // 7. Store authentication result
        context.setAuthResult(result, client.tokenCache().serialize());
    }
}
```

## Role Mapping

### Azure AD Group to Application Role

```java
@Component
public class AzureADRoleMapper {
    
    @Value("#{${azure.ad.role-mapping:{}}}")
    private Map<String, String> roleMapping;
    
    public List<String> mapGroupsToRoles(List<String> azureADGroups) {
        return azureADGroups.stream()
            .map(group -> roleMapping.get(group))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }
    
    public List<String> mapGroupIdsToRoles(List<String> azureADGroupIds) {
        return azureADGroupIds.stream()
            .map(groupId -> roleMapping.get(groupId))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }
}
```

**Configuration**:
- Keys in ConfigMap: `azure.ad.role-mapping.<group-id>` or `azure.ad.role-mapping.<group-name>`
- Values: Application roles (e.g., `ROLE_ADMINISTRATOR`, `ROLE_USER`)

## JWT Token Generation

### Application JWT Token

After Azure AD authentication, generate application-specific JWT:

```java
@Service
public class JwtTokenService {
    
    @Value("${jwt.secret}")
    private String jwtSecret;
    
    @Value("${jwt.expiration}")
    private long jwtExpiration;
    
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", userDetails.getUsername());
        claims.put("roles", userDetails.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.toList()));
        
        return Jwts.builder()
            .setClaims(claims)
            .setSubject(userDetails.getUsername())
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
            .signWith(SignatureAlgorithm.HS512, jwtSecret)
            .compact();
    }
}
```

## API Gateway Integration

### Authentication Endpoints

The API Gateway routes authentication requests:

```
/api/auth/azure/login      → auth-service (initiates Azure AD login)
/api/auth/azure/callback   → auth-service (handles Azure AD callback)
/api/auth/logout           → auth-service (handles logout)
```

### Token Validation

Backend services validate JWT tokens:

```java
@Component
public class JwtTokenValidator {
    
    @Value("${jwt.secret}")
    private String jwtSecret;
    
    public Claims validateToken(String token) {
        return Jwts.parser()
            .setSigningKey(jwtSecret)
            .parseClaimsJws(token)
            .getBody();
    }
}
```

## Istio Integration

### Request Authentication

Istio validates Azure AD tokens at the ingress gateway:

```yaml
apiVersion: security.istio.io/v1
kind: RequestAuthentication
metadata:
  name: default-<env>-internal
  namespace: istio-system
spec:
  selector:
    matchLabels:
      istio: ingressgateway-<env>-internal
  jwtRules:
    - issuer: "https://login.microsoftonline.com/${AZURE_TENANT_ID}/v2.0"
      jwksUri: "https://login.microsoftonline.com/${AZURE_TENANT_ID}/discovery/v2.0/keys"
      forwardOriginalToken: true
    - issuer: "<app>-auth-service"
      forwardOriginalToken: true
```

## Best Practices

1. **Home Page Configuration**: Always use origin only (no path)
2. **Redirect URI**: Build from `homePage + redirectEndpoint`
3. **Role Mapping**: Use Azure AD group Object IDs for reliability
4. **Secrets Management**: Store secrets in Key Vault, not ConfigMaps
5. **Token Validation**: Validate tokens in both Istio and application
6. **CSRF Protection**: Always validate state parameter
7. **Token Replay Protection**: Always validate nonce
8. **Session Management**: Store tokens server-side, not in browser
9. **HTTPS Only**: Always use HTTPS in production
10. **Token Expiration**: Set appropriate token expiration times

## Troubleshooting

### Redirect URI Mismatch

**Error**: `redirect_uri_mismatch` from Azure AD

**Solution**:
- Verify `azure.auth.home-page` is origin only (no path)
- Check redirect URI matches Azure AD app registration exactly
- Format: `https://<app>.<env>.lvad.lvfs.net/api/auth/azure/callback`

### Role Mapping Not Working

**Error**: User authenticated but no roles assigned

**Solution**:
- Verify Azure AD group Object IDs in ConfigMap
- Check group membership in Azure AD
- Verify role mapping keys match group IDs/names exactly
- Review application logs for mapping errors

### Token Validation Failures

**Error**: `Invalid token` or `Token expired`

**Solution**:
- Check JWT secret matches across services
- Verify token expiration time
- Check token format and claims
- Review Istio RequestAuthentication configuration

## References

- [Azure AD Authentication Guide](../reference/azure-ad-authentication-guide.md)
- [MSAL4J Documentation](https://github.com/AzureAD/microsoft-authentication-library-for-java)
- Reference implementations:
  - `/Users/davidparker/Documents/LV-Code/dparker-rti-now/docs/azure-ad-authentication-guide.md`
  - `/Users/davidparker/Documents/LV-Code/dparker-rti-now/clusters/dev/releases/rti/dev/config-maps/rti-azure-configmap.yaml`

## Version History

- **2026-01-XX**: Initial pattern documentation based on LV production implementations
