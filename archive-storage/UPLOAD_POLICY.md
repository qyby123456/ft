# Source-only upload policy

The user prohibits uploading all model resource files and music files. This includes models, textures, animations and audio inside JARs, archives and content-addressed blobs. This overrides the earlier archive offload/cache eviction workflow.

Only source code, approved text configuration, documentation and path/hash metadata may be uploaded. Model-related Java logic is source code; exported geometry, textures, animation data and music are excluded. Binary archives and opaque payloads are rejected until a separate complete payload audit proves compliance. An extension rename is not sufficient.

All attachments from the old ft-cache-archive-20261114 draft release were withdrawn after verifying a local original. No JAR cache eviction was performed. Historical indexes and cleanup plans are evidence only and must not authorize deletion. INDEX.json has no currently downloadable archives; withdrawnArchives records prior attachments and their withdrawal status.

Model and audio resources remain local. The source-only Git checkpoint cannot recover those resources. Do not delete their only local copy using this source checkpoint as a recovery claim.

Run maintenance/verify_source_only_upload.py against the exact Git tree before push. Repack source/evidence separately when another segment is ready. Never resume the frozen legacy upload or cache eviction tools.
