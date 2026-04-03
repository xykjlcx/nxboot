# Security Policy

## Supported Versions

| Version | Supported          |
|---------|--------------------|
| 0.9.x   | :white_check_mark: |
| < 0.9   | :x:                |

## Reporting a Vulnerability

If you discover a security vulnerability in NxBoot, please report it responsibly.

**Do NOT open a public GitHub issue for security vulnerabilities.**

Instead, please send an email to the maintainers with:

1. Description of the vulnerability
2. Steps to reproduce
3. Potential impact
4. Suggested fix (if any)

We will acknowledge receipt within 48 hours and provide a timeline for the fix.

## Security Design

NxBoot follows these security principles:

- **No unsafe defaults**: JWT secret and CORS origins must be explicitly configured in non-dev environments
- **Fail-fast**: Missing critical configuration causes startup failure, not silent fallback
- **Defense in depth**: Frontend permission checks + backend `@PreAuthorize` + database-level RBAC
- **Scoped delegation**: Non-admin users can only assign permissions they themselves hold
