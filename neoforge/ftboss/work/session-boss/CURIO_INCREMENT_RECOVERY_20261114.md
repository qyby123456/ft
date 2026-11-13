# Curios native increment recovery, 2026-11-14

Status: NOT-INSTALLABLE; candidate unchanged andnot deployed. Both formal instances are read-only. Weapons andSession development remain paused.

## Source recovery

Fetch branch `codex/neoforge-source-20261113` of `https://github.com/qyby123456/ft.git`. Source lives under `neoforge/ftboss`;the root repository README is the original repository file. Use the checkpoint in `github-sync/checkpoint-boss-curios-fallback-owner-20261114.json`. Compare every tracked path,size andSHA256 to the independently fetched commit before removing any redundant cache. The historical import manifest belongs to its immutable import commit,not to a generated current workspace file. Canonical C source andfrozen native inputs stay intact.

## Local evidence recovery

Keep BOTH archives in `work/session-boss/archives/`:

- `boss-core-stage28-zicai-curio-scoped-evidence-v3-20261113.zip`, SHA256 `aca9e96def7887e0cf52637152df8922174326d5970376918d42b08bee864929`.
- `boss-core-stage28-curio-fallback-owner-increment-v1-20261114.zip`, SHA256 `5fd51db889f0ce447dad1bda6c8230857d83bbdf331b031d878b280d255e4480`.

Run `python -B work/session-boss/tools/restore_boss_zicai_curio_archive_v2.py <increment.zip> --base <base.zip>` for full referenced-payload validation. Add `--output <new-empty-directory>` to reconstruct all5804increment paths. The increment reuses immutable base blobs;it is not standalone. Complete hash readback andsmall physical restore controls passed;full evidence physical restore was not performed. Raw evidence archives,JARs andworlds have no independently proven remote recovery andmust not be deleted on the basis of ordinary Git source publication.

## Scoped acceptance

V9 retains one strict client-sync failure;only its passed missing-index case is reused. V10 passes the remaining identifier-removal,occupied-slot andfull-inventory cases withlossless exactly-once restoration andactual native client item sync. Slot-type map removal onthe client is not claimed. V11 passes18original source-precedence/projectile-depth/eligibility controls;creative andspectator controls both receive actual final-positive damage withoutsilence. Projectile ownership uses actual registered entities andpublic damage sources;natural flight/collision acceptance is not claimed. No product class/resource changed. New-JVM experimental confirmation andthe full Boss/lifecycle matrix remain incomplete.

## Duplicate-cache removal

`github-sync/checkpoint-curios-cache-cleanup-plan-20261114.json` lists onlysixnewduplicate native test/runner files in the persistence sidecar. The earlier1910pruned paths remain absent. Apply `work/session-boss/tools/prune_github_verified_source_cache_v2.py <plan> <fresh-independent-remote-proof> --apply --output <new-work/session-boss/reports-directory>` only after the plan andrestore notes are remote andfreshly hash-verified. The guard checks exactcommit,source originals,workspace containment,process references,file handles,andminimum recovery files. Preserve `.git`,all canonical source,all archives/failures/formal receipts andhistorical D. Record exactpath/hash,commit andlogical/observed freed bytes. No new thread orwriter is authorized.
