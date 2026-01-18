# DMS Documentation Repository

This repository contains all documentation, reference configurations, and development utilities for the Document Management System (DMS).

## Repository Structure

```
dms-docs/
├── README.md                          # This file
├── reference/                         # Reference configurations
│   ├── clusters/                      # Kubernetes cluster configurations
│   ├── flux-config/                  # Flux GitOps configurations
│   └── istio-config/                 # Istio service mesh configurations
├── scripts/                          # Development scripts and utilities
│   └── entrypoint.sh                # Frontend service entrypoint script
├── verify-requirements.sh            # Requirements verification script
└── [documentation files]             # All project documentation (.md files)
```

## Purpose

This repository serves as:
- **Central Documentation Hub**: All project documentation lives here
- **Reference Configurations**: Kubernetes, Flux, and Istio configs for reference
- **Development Utilities**: Scripts and tools for local development
- **Knowledge Base**: Architecture, integration, and user guides

## Documentation Files

All documentation files are stored in this repository with metadata headers indicating:
- Last update timestamp
- GitHub username of the updater

## Reference Configurations

The `reference/` folder contains:
- **clusters/**: Kubernetes cluster manifests and configurations
- **flux-config/**: Flux GitOps repository and kustomization configs
- **istio-config/**: Istio service mesh policies and configurations

These are reference configurations for production deployments and should not be modified directly in this repository.

## Development Scripts

The `scripts/` folder contains utility scripts for:
- Local development setup
- Service initialization
- Testing utilities
- Deployment helpers

## Usage

### Viewing Documentation

All documentation files are in Markdown format and can be viewed:
- Directly in GitHub
- Using any Markdown viewer
- In your IDE

### Using Reference Configurations

Reference configurations can be:
- Copied to service-specific repositories
- Used as templates for new deployments
- Referenced when setting up new environments

### Running Development Scripts

```bash
# Make scripts executable
chmod +x scripts/*.sh

# Run a script
./scripts/entrypoint.sh
```

## Contributing

When updating documentation:
1. Check if the document already exists
2. Update the existing document rather than creating duplicates
3. Add/update metadata header with your GitHub username and timestamp
4. Follow the documentation standards in `.cursorrules`

## Related Repositories

- `dms-admin-service` - Administration service
- `dms-audit-service` - Audit logging service
- `dms-compliance-service` - Compliance service
- `dms-document-service` - Document management service
- `dms-llm-service` - LLM/AI service
- `dms-frontend-service` - Angular frontend
- `dms-core-service` - Shared library
- `dms-e2e-tests` - End-to-end test suite

---

*Last Updated: 2025-01-18T00:00:00Z*  
*Updated By: davidparker-lv-bmth*
