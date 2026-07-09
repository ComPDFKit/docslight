# Security Checklist

Run this checklist before publishing a release, building distribution artifacts, or sharing examples publicly.

## Release Checks

- Confirm no `.env` files or copied `.env` contents are committed.
- Confirm no private keys, certificates, signing keys, or SSH keys are committed.
- Confirm no real API tokens, bearer tokens, service credentials, or passwords are present.
- Confirm no internal IP addresses, private endpoints, database URLs, or bucket names are present.
- Confirm no model weights, customer files, or generated customer data are included.
- Confirm README and examples use safe placeholder values such as `your-api-key` and public localhost examples only.
- Run `git status --short` and review every tracked and untracked file before committing.
- Inspect build artifacts and source distributions to confirm they do not contain customer data, local caches, or secrets.
- Verify examples do not hard-code internal ComPDF endpoints or environment-specific paths.
- Verify documentation does not reveal unreleased infrastructure details.
