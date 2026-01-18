# Reference Configurations

This directory contains reference Kubernetes and Flux configurations that are managed in separate Flux GitOps repositories in GitHub.

## Directory Structure

```
reference/
├── clusters/          # Kubernetes manifests for dev, uat, prod environments
└── flux-config/       # Flux GitOps configuration files
```

## Purpose

These configurations serve as:
- **Reference templates** for setting up Flux repositories
- **Documentation** of the standard LV patterns and structure
- **Examples** for implementing similar applications
- **Transfer materials** for moving to actual Flux repositories

## Usage

### For Flux Repository Setup

1. **Copy configurations** from this directory to your Flux repository
2. **Update GitRepository references** in `flux-config/gitrepository.yaml`
3. **Customize** environment-specific values (URLs, namespaces, etc.)
4. **Commit and push** to trigger Flux sync

### For New Applications

1. **Review** the structure and patterns
2. **Copy** relevant configurations
3. **Rename** application-specific resources
4. **Update** service URLs, namespaces, and labels
5. **Follow** the LV patterns documented in `dms-docs/patterns/`

## Clusters Directory

Contains Kubernetes manifests organized by environment:

```
clusters/
├── dev/
│   ├── manifests/istio-configs/
│   ├── namespaces/
│   └── releases/dms/dev/
│       ├── config-maps/
│       ├── istio-configs/
│       └── service-accounts/
├── uat/
│   └── [same structure]
└── prod/
    └── [same structure]
```

### Key Files

- **Namespaces**: Environment-specific namespace definitions
- **ConfigMaps**: Service URLs and configuration
- **Istio Configs**: VirtualServices, DestinationRules, PeerAuthentication
- **Service Accounts**: Service identities for mTLS

## Flux Config Directory

Contains Flux GitOps configuration:

```
flux-config/
├── gitrepository.yaml        # Git source definition
├── kustomization-dev.yaml     # Dev environment sync
├── kustomization-uat.yaml     # UAT environment sync
└── kustomization-prod.yaml    # Prod environment sync
```

**Note**: These configurations are reference templates. When transferring to actual Flux repositories:
- Update `gitrepository.yaml` with the actual repository URL
- Each environment may have its own repository or branch
- Paths in kustomization files should point to the `clusters/<env>` directory in the Flux repository

### Setup Steps

1. **Create GitRepository** resource pointing to your Flux repo
2. **Create Kustomization** resources for each environment
3. **Configure** path to point to `clusters/<env>` directory
4. **Set** health checks for deployments
5. **Apply** to cluster via `kubectl apply` or Flux CLI

## Transfer to Flux Repository

When ready to transfer to actual Flux repositories:

1. **Create separate repositories** for each environment (or use branches)
2. **Copy** `clusters/<env>` to the repository
3. **Update** `flux-config/gitrepository.yaml` with actual repo URL
4. **Update** `flux-config/kustomization-*.yaml` paths if needed
5. **Apply** Flux configurations to the cluster
6. **Verify** Flux sync status

## Environment-Specific Notes

### Development (dev)
- Namespace: `dms-dev` and `dms-dev-frontend`
- Host: `dms.dev.lvad.lvfs.net`
- Replicas: 2 (can be reduced to 1 for cost optimization)

### UAT
- Namespace: `dms-uat` and `dms-uat-frontend`
- Host: `dms.uat.lvad.lvfs.net`
- Replicas: 2 (production-like)

### Production (prod)
- Namespace: `dms-prod` and `dms-prod-frontend`
- Host: `dms.prod.lvad.lvfs.net`
- Replicas: 2+ (high availability)

## Related Documentation

- [LV Patterns](../patterns/) - Application-generic patterns
- [Architecture](../ARCHITECTURE.md) - System architecture
- [Infrastructure Requirements](../INFRASTRUCTURE_REQUIREMENTS.md) - Infrastructure setup

## Version History

- **2026-01-XX**: Initial reference configurations created
- **2026-01-XX**: Moved to dms-docs/reference for Flux repository management
