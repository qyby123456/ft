# FT archive storage

Source and this index are stored in Git. Large immutable recovery archives are uploaded as content-addressed assets of the same repository draft release ft-cache-archive-20261114. Draft assets require repository write access and are intentionally not a public playable release. No deployment or installability is claimed. Each remoteVerified row records an independent streamed full download SHA256 and byte count.

Use GitHub release asset API IDs with authenticated Accept: application/octet-stream requests. Do not print or save credentials or signed redirect URLs. Verify bytes and SHA256 before using a downloaded file. The original relative path is the key in INDEX.json. CAS increments still require their original hash-bound base archive. Native failed worlds remain failed evidence, never restart seeds.

Local archive copies are evictable caches after remote artifact verification plus independent Git index recovery verification. The active Stage28 base may remain cached while needed. Preserve the current Boss candidate, required compile/runtime inputs and formal instances.
