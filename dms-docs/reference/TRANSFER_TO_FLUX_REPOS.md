# Transfer Guide: Moving Configurations to Flux Repositories

This guide explains how to transfer the reference configurations from `dms-docs/reference/` to separate Flux GitOps repositories in GitHub.

## Overview

The configurations in `dms-docs/reference/` are templates that will be transferred to dedicated Flux repositories. These repositories will be managed separately and synced to Kubernetes clusters via Flux.

## Repository Structure Options

### Option 1: Single Repository with Branches

```
dms-flux-config/
├── main (or master)
│   └── clusters/
│       ├── dev/
│       ├── uat/
│       └── prod/
└── flux-config/
    ├── gitrepository.yaml
    └── kustomization-*.yaml
```

### Option 2: Separate Repositories per Environment (Recommended)

```
dms-flux-config-dev/
└── clusters/
    └── dev/

dms-flux-config-uat/
└── clusters/
    └── uat/

dms-flux-config-prod/
└── clusters/
    └── prod/
```

## Transfer Steps

### Step 1: Create Flux Repositories

1. Create new repositories in GitHub:
   - `dms-flux-config-dev` (or use branches in single repo)
   - `dms-flux-config-uat`
   - `dms-flux-config-prod`

2. Initialize with README and .gitignore

### Step 2: Copy Cluster Configurations

For each environment:

```bash
# Copy dev environment
cp -r dms-docs/reference/clusters/dev/* <flux-repo>/clusters/dev/

# Copy uat environment
cp -r dms-docs/reference/clusters/uat/* <flux-repo>/clusters/uat/

# Copy prod environment
cp -r dms-docs/reference/clusters/prod/* <flux-repo>/clusters/prod/
```

### Step 3: Update Flux Configuration

1. **Update GitRepository** (`flux-config/gitrepository.yaml`):

```yaml
apiVersion: source.toolkit.fluxcd.io/v1
kind: GitRepository
metadata:
  name: dms-config-dev  # Environment-specific name
  namespace: flux-system
spec:
  interval: 5m
  url: https://github.com/davidparker-lv-bmth/dms-flux-config-dev  # Actual repo URL
  ref:
    branch: main
  secretRef:
    name: dms-git-credentials
```

2. **Update Kustomization** (`flux-config/kustomization-dev.yaml`):

```yaml
apiVersion: kustomize.toolkit.fluxcd.io/v1
kind: Kustomization
metadata:
  name: dms-services-dev
  namespace: flux-system
spec:
  interval: 5m
  path: ./clusters/dev  # Path in the Flux repository
  prune: true
  sourceRef:
    kind: GitRepository
    name: dms-config-dev
  # ... rest of configuration
```

### Step 4: Commit and Push

```bash
cd <flux-repo>
git add .
git commit -m "Initial DMS configuration for <env>"
git push origin main
```

### Step 5: Apply Flux Configuration to Cluster

Apply the Flux configuration to your Kubernetes cluster:

```bash
# Apply GitRepository
kubectl apply -f flux-config/gitrepository.yaml

# Apply Kustomization
kubectl apply -f flux-config/kustomization-dev.yaml
```

### Step 6: Verify Flux Sync

```bash
# Check GitRepository status
kubectl get gitrepository -n flux-system

# Check Kustomization status
kubectl get kustomization -n flux-system

# Check sync logs
kubectl logs -n flux-system -l app=kustomize-controller
```

## Environment-Specific Considerations

### Development
- Repository: `dms-flux-config-dev`
- Branch: `main` or `dev`
- Access: Development team
- Auto-sync: Enabled (5 minute interval)

### UAT
- Repository: `dms-flux-config-uat`
- Branch: `main` or `uat`
- Access: QA and deployment team
- Auto-sync: Enabled (5 minute interval)

### Production
- Repository: `dms-flux-config-prod`
- Branch: `main` or `prod`
- Access: Restricted (production team only)
- Auto-sync: Enabled (5 minute interval)
- Approval: Consider requiring PR approval before merging

## Security Considerations

1. **Git Credentials**: Store as Kubernetes secrets
   ```bash
   kubectl create secret generic dms-git-credentials \
     --from-literal=username=<github-username> \
     --from-literal=password=<github-token> \
     -n flux-system
   ```

2. **Repository Access**: Use fine-grained access control
   - Dev: Development team
   - UAT: QA and deployment team
   - Prod: Production team only

3. **Secrets Management**: 
   - Never commit secrets to Flux repositories
   - Use Azure Key Vault or External Secrets Operator
   - Reference secrets from separate secret management system

## Post-Transfer Checklist

- [ ] Flux repositories created in GitHub
- [ ] Configurations copied to repositories
- [ ] GitRepository URLs updated
- [ ] Kustomization paths verified
- [ ] Git credentials configured
- [ ] Flux configurations applied to cluster
- [ ] Sync status verified
- [ ] Health checks passing
- [ ] Deployments successful
- [ ] Documentation updated

## Troubleshooting

### Flux Not Syncing

1. Check GitRepository status:
   ```bash
   kubectl describe gitrepository dms-config-dev -n flux-system
   ```

2. Verify Git credentials:
   ```bash
   kubectl get secret dms-git-credentials -n flux-system
   ```

3. Check Kustomization status:
   ```bash
   kubectl describe kustomization dms-services-dev -n flux-system
   ```

### Path Not Found

- Verify path in Kustomization matches repository structure
- Check that `clusters/<env>` directory exists in repository
- Ensure path is relative to repository root

### Resources Not Applied

- Check namespace exists before deploying resources
- Verify YAML syntax: `kubectl apply --dry-run=client -f <file>`
- Review Kustomization logs for errors

## References

- [Flux Documentation](https://fluxcd.io/docs/)
- [LV Flux Patterns](../patterns/LV-FLUX_PATTERNS.md)
- [Kubernetes Deployment Patterns](../patterns/LV-K8S_DEPLOYMENT_PATTERNS.md)

## Version History

- **2026-01-XX**: Initial transfer guide created
