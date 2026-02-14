---
Last Updated: 2026-02-14T15:30:00Z
Updated By: davidparker-lv-bmth
---

# DMS User Guide

## Table of Contents

1. [Introduction](#introduction)
2. [Getting Started](#getting-started)
3. [Authentication](#authentication)
4. [Document Management](#document-management)
5. [Admin Functions](#admin-functions)
6. [Compliance Features](#compliance-features)
7. [AI-Powered Document Queries](#ai-powered-document-queries)
8. [Troubleshooting](#troubleshooting)

---

## Introduction

The Document Management System (DMS) is an enterprise-grade platform for storing, organizing, retrieving, and managing documents across your organization. The system provides:

- **Secure Document Storage**: Multi-application isolated storage with role-based access control
- **Document Versioning**: Track document history and changes
- **AI-Powered Search**: Natural language queries to find documents
- **Compliance**: PCI-DSS, GDPR, and ISO 27001 compliance features
- **Audit Trail**: Complete audit logging for all operations

---

## Getting Started

### Accessing the System

1. Open your web browser and navigate to the DMS URL provided by your administrator
2. You will be redirected to the login page if not already authenticated

### System Requirements

- Modern web browser (Chrome, Firefox, Edge, Safari)
- JavaScript enabled
- Internet connection
- Valid user account

---

## Authentication

### Logging In

1. Navigate to the DMS login page
2. Enter your email address in the **Email** field
3. Enter your password in the **Password** field
4. Click **Login** or press Enter

### Azure AD Authentication

If your organization uses Azure AD (Microsoft Entra ID):
1. Click **Sign in with Microsoft**
2. You will be redirected to Microsoft's login page
3. Enter your organizational email and password
4. Complete multi-factor authentication if required
5. You will be automatically redirected back to DMS

### Logging Out

1. Click your profile icon in the top-right corner
2. Select **Logout** from the dropdown menu

### Password Reset

If you forget your password:
1. Click **Forgot Password** on the login page
2. Enter your email address
3. Check your email for password reset instructions
4. Follow the link to create a new password

---

## Document Management

### Viewing Documents

1. Click **Documents** in the main navigation menu
2. You will see a list of documents you have access to
3. Use the **Classification** filter to filter documents by:
   - **Public**: Available to all users
   - **Internal**: Internal use only
   - **Confidential**: Restricted access
   - **Restricted**: Highly restricted access

### Uploading Documents

1. Navigate to the **Documents** page
2. Click the **Upload Document** button
3. In the upload dialog:
   - Click **Choose File** and select the document from your computer
   - Enter a **Document Name** (required)
   - Select a **Classification** from the dropdown (required)
   - Optionally add tags or description
4. Click **Upload**
5. Wait for the upload to complete - you'll see a success message

**Supported File Types**: PDF, DOCX, XLSX, PPTX, TXT, images (JPG, PNG, GIF)

**File Size Limit**: Maximum 100 MB per file

### Downloading Documents

1. Find the document in the document list
2. Click the **Download** button next to the document
3. The file will download to your default download folder

### Viewing Document Details

1. Click **View** next to a document in the list
2. The document detail page shows:
   - Document name and metadata
   - Classification and tags
   - Creation and modification dates
   - Version history
   - Access permissions

### Updating Document Metadata

1. Open the document detail page
2. Click **Edit** button
3. Modify the document name, classification, or tags
4. Click **Save** to apply changes

### Deleting Documents

1. Open the document detail page
2. Click **Delete** button
3. Confirm the deletion in the dialog
4. **Note**: Deletion requires appropriate permissions

### Document Versioning

The system automatically creates versions when documents are updated:

1. Open a document's detail page
2. Click the **Versions** tab
3. View all versions with timestamps and change descriptions
4. Click a version to view or download that specific version

---

## Admin Functions

> **Note**: Admin functions are only available to users with administrator roles.

### User Management

#### Viewing Users

1. Navigate to **Admin** → **Users**
2. View the list of all users in the system
3. Use search and filters to find specific users

#### Creating a User

1. Click **Create User** button
2. Fill in the form:
   - **Email**: User's email address (required)
   - **First Name**: User's first name (required)
   - **Last Name**: User's last name (required)
   - **Password**: Initial password (required)
   - **Roles**: Select one or more roles
3. Click **Create**

#### Editing a User

1. Find the user in the list
2. Click **Edit** next to the user
3. Modify user details
4. Click **Save**

#### Assigning Roles to Users

1. Open a user's detail page
2. Click **Assign Role**
3. Select a role from the dropdown
4. Click **Assign**

#### Enabling/Disabling Users

1. Open a user's detail page
2. Click **Enable** or **Disable** button
3. Disabled users cannot log in

### Role Management

#### Viewing Roles

1. Navigate to **Admin** → **Roles**
2. View all roles defined in the system

#### Creating a Role

1. Click **Create Role** button
2. Enter:
   - **Name**: Role name (required)
   - **Description**: Role description
   - **Permissions**: Select permissions to assign
3. Click **Create**

#### Assigning Permissions to Roles

1. Open a role's detail page
2. Click **Assign Permission**
3. Select permissions from the list
4. Click **Assign**

### Permission Management

#### Viewing Permissions

1. Navigate to **Admin** → **Permissions**
2. View all available permissions

#### Creating a Permission

1. Click **Create Permission** button
2. Enter:
   - **Name**: Permission identifier (e.g., `documents.read`)
   - **Description**: What this permission allows
3. Click **Create**

### Application Management

#### Viewing Applications

1. Navigate to **Admin** → **Applications**
2. View all registered applications

#### Provisioning a New Application

1. Click **Provision Application** button
2. Enter:
   - **Application Name**: Name of the application
   - **Description**: Application description
   - **Contact Email**: Administrator email
3. Click **Provision**
4. The system will create isolated storage for the application

#### Deprovisioning an Application

1. Find the application in the list
2. Click **Deprovision**
3. Confirm the action
4. **Warning**: This will remove all application data

---

## Compliance Features

> **Note**: Compliance features require administrator access.

### PCI-DSS Compliance

#### Viewing PCI Compliance Report

1. Navigate to **Compliance** → **PCI Report**
2. View compliance status and controls
3. Reports can be generated for:
   - Daily
   - Weekly
   - Monthly
   - Quarterly

#### Exporting PCI Reports

1. Select the report period
2. Click **Export Report**
3. Choose format (PDF or CSV)
4. Download the report

### GDPR Compliance

#### Data Subject Rights

The system supports GDPR data subject rights:

**Right to Access**
1. Navigate to **Compliance** → **GDPR**
2. Enter the data subject ID
3. Click **Export Data**
4. Download the exported data package

**Right to Erasure**
1. Navigate to **Compliance** → **GDPR**
2. Enter the data subject ID
3. Click **Request Erasure**
4. Confirm the erasure request
5. The system will process the request and remove all personal data

**Data Portability**
1. Export data using the "Export Data" feature
2. Data is provided in a machine-readable format (JSON)

### ISO 27001 Controls

#### Viewing ISO 27001 Controls

1. Navigate to **Compliance** → **ISO 27001**
2. View the status of all security controls
3. Each control shows:
   - Control ID and name
   - Implementation status
   - Last audit date
   - Compliance evidence

---

## AI-Powered Document Queries

### Using Natural Language Queries

1. Navigate to **LLM Query** in the main menu
2. Enter your question in natural language, for example:
   - "Find all documents related to compliance"
   - "What documents contain sensitive customer information?"
   - "Show me documents about security policies"
3. Click **Query** or press Enter
4. View the results with:
   - Relevant documents
   - Answer summary
   - Confidence scores
   - Source citations

### Query Tips

- Be specific: "documents about Q4 financial reports" is better than "financial documents"
- Use keywords: Include important terms related to your search
- Ask questions: The system understands natural language questions

### Compliance Queries

1. Navigate to **LLM Query**
2. Select **Compliance Check** mode
3. Enter your compliance-related question
4. The system will provide compliance-focused answers with relevant controls

---

## Troubleshooting

### Common Issues

#### Cannot Log In

- Verify your email and password are correct
- Check if your account is enabled (contact administrator)
- Clear browser cache and cookies
- Try a different browser

#### Document Upload Fails

- Check file size (must be under 100 MB)
- Verify file type is supported
- Check your internet connection
- Ensure you have upload permissions

#### Cannot Access Document

- Verify you have the required permissions
- Check if the document classification allows your access level
- Contact your administrator if access is needed

#### Query Returns No Results

- Try different keywords
- Check if documents exist with the requested classification
- Verify your permissions allow access to relevant documents

### Getting Help

- **User Support**: Contact your system administrator
- **Documentation**: Refer to this user guide
- **Technical Issues**: Submit a support ticket through your organization's help desk

### Keyboard Shortcuts

- **Ctrl/Cmd + K**: Quick search
- **Ctrl/Cmd + U**: Upload document
- **Esc**: Close dialogs/modals
- **Tab**: Navigate between form fields

### Accessibility

The DMS is designed to be accessible:
- Full keyboard navigation support
- Screen reader compatible
- High contrast mode available
- WCAG 2.1 Level AA compliant

To enable accessibility features:
1. Click your profile icon
2. Select **Settings**
3. Enable **Accessibility Mode**

---

## Best Practices

### Document Organization

- Use descriptive document names
- Apply appropriate classifications
- Add tags for better searchability
- Keep documents in appropriate folders

### Security

- Never share your login credentials
- Log out when finished, especially on shared computers
- Report suspicious activity to administrators
- Follow your organization's data handling policies

### Performance

- Upload documents during off-peak hours when possible
- Use filters to narrow document lists
- Clear browser cache regularly
- Close unused browser tabs

---

## Appendix

### Document Classifications

- **Public**: Information that can be freely shared
- **Internal**: For internal organizational use only
- **Confidential**: Restricted to authorized personnel
- **Restricted**: Highly sensitive, requires special clearance

### Supported File Formats

- **Documents**: PDF, DOCX, DOC, RTF, TXT
- **Spreadsheets**: XLSX, XLS, CSV
- **Presentations**: PPTX, PPT
- **Images**: JPG, JPEG, PNG, GIF, BMP
- **Archives**: ZIP, RAR (extracted automatically)

### Browser Compatibility

- Chrome 90+
- Firefox 88+
- Edge 90+
- Safari 14+

---

*Last Updated: 2024*
