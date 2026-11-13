# Restore this checkpoint

1. Clone `https://github.com/qyby123456/ft.git` and check out the recorded commit on `main`.
2. Read `PROJECTS.md` and choose one independent project. Do not overlay dated variants onto each other.
3. If `backup-index/RELEASE_ASSETS.json` says required, download the named asset from release `ft-development-backup-20261113`; verify its SHA256 and extract at the repository root. Existing paths must not be overwritten unless their expected hash matches.
4. Verify each path and SHA256 in `backup-index/SOURCE_INDEX.jsonl.gz`. Entries with storage=git are Git objects; storage=github-release entries are restored from the release asset.
5. Check `backup-index/NOT_UPLOADED.json` before deleting or rebuilding anything. Restore required excluded dependencies from the original local archive, which remains authoritative for excluded content.
6. Use the original project build instructions and its matching Forge/NeoForge, Minecraft, Java, and Gradle versions. Generated files and test worlds are intentionally not part of this upload.

Cleanup is allowed only for individual original files whose exact bytes have been independently recovered from the remote Git commit or release. Excluded, changed, read-only, or unverified files must remain local.

## Portable helper scripts and notes

Files in backup-index/PORTABLE_SOURCES.json have personal profile directory names replaced with YOUR_USERNAME. Configure the placeholder before running those helpers. Their original hashes are recorded for provenance; the original files remain local and cannot be deleted based on the transformed upload.

The source index uses gzip compression. Decompress SOURCE_INDEX.jsonl.gz before parsing its JSONL records.
