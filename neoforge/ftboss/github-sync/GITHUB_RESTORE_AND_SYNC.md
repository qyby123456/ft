# Current target update, 2026-11-13

Target: https://github.com/qyby123456/ft.git (existing public repository). main currently points to27d20d5e1d2eb960f4a64cde9bd21230efae766c andcontains one README. Preserve it. Source import uses only neoforge/ftboss on authorized local branch codex/neoforge-source-20261113. The persistence clone is github-sync/repositories/ft;the original sole C workspace remains the development authority.

Remote reading andIPv4 receive-pack dry-run now succeed using the existing Credential Manager account matching the directly supplied repository owner,selected only in local clone configuration. The first no-account-hint failure andtransient HTTPS failure are retained. No token was printed orcopied. Actual branch write/recovery is unproven until a real push. Git user.useConfigOnly author check still fails;explicit author name/email are required andmust not be invented. Existing public target does not authorize uploading private account/session/configuration data. No commit,push orremote repository/branch was created.

Use verify_github_source_snapshot_v3.py for actual Git-eligible source andexact staged blobs,including JSON. Its complete1900file manifest/findings/controls are exactly equal to prior V2;batch object reads preserve every gate andassert index-tree stability. All allowlisted metadata matches are AST-reviewed andbound to exact file SHA256,line andcategory. Any change invalidates review. Old failed scan outputs are retained. Git attributes now preserve raw source bytes;verify SHA256 ofevery staged blob against its source manifest. Initial extension-only scans below are historical,not current completeness proof.

Both formal instances remain read-only;Stage27 andall rollback/archive evidence stay unchanged. Full Boss migration is active;weapons/Session remain paused. Do not delete local evidence until remote recovery is actually verified.

## Initial preparation history

# GitHub source persistence: pending remote configuration

The current source workspace has no Git repository or configured remote. Git is installed; GitHub CLI is not on PATH. No existing GitHub repository or authorization has been verified. No repository has been created, no commit/push/upload has occurred, and no local development evidence may be deleted on the assumption of remote recovery.

The latest instruction prohibits further deployment and formal-instance overwrites. Stage27 was deployed and archived before that instruction. Its current SHA256 is 6563f431dca1871eb7870653b74550ba2f5c0f66c104eea90ce58a99ca30f7c0. Preserve this existing state. Full Boss migration remains active and NOT-INSTALLABLE as a completed product;weapon development/validation and lease/payment/Session work remain paused. Keep the existing sole writer.

## Required next input

Provide the intended existing GitHub repository URL. Then inspect its visibility,branches,permissions,LFS/Release policy and existing worktree without guessing credentials or creating a public repository. GitHub CLI absence alone does not prove missing Git authentication;remote access has not been tested because there is no target. Never print credentials or inspect private launcher/account stores.

## Source and evidence boundary

Track reviewed source,build configuration,scripts and necessary deterministic test fixtures. Review root and batch files before staging;do not run git add . indiscriminately. The first local scanner produces relative paths,hashes and finding categories only,never matched secret values. It is an initial scan and does not establish that every candidate is safe to publish. Resolve every finding and inspect the exact staged diff before the first commit.

Raw build/cache/log/tmp/runtime worlds,game dependencies,formal-instance data,read-only historical D data,raw account/session/payment evidence and unreviewed receipts are excluded from ordinary Git. JSON is excluded by default except reviewed resources,deterministic fixture directories and sanitized github-sync manifests. Necessary raw receipts/configuration must first be reviewed and exported safely;do not simply remove ignore rules. Preserve original frozen inputs locally with their SHA256;redacted exports must identify the original hash and their own hash and cannot be presented as byte-identical recovery.

Large archives and essential binary recovery artifacts remain local until the target repository's existing Git LFS or GitHub Release support,permissions and limits are verified. Never place oversized archives,JARs,Minecraft SDKs or game worlds into ordinary Git. Do not upload weapon snapshots or any archive before a separate contents/privacy review. Preserve original archives and all inputs still referenced by active work.

## Incremental remote verification

After remote configuration and secret review,commit/push in bounded batches:source/configuration;necessary test fixtures;sanitized receipts/manifests/restore documentation;reviewed essential binaries through the supported storage mechanism. Use configured author identity only;do not invent it or disclose authentication details. Do not force-push or overwrite existing remote history. Record pushed commit IDs and object hashes without tokens.

For each completed future feature:run focused checks,review secrets and staged diff,commit and push the source/hash/test-result/next-goal record,then verify remote retrieval in a separate fresh directory. For binary evidence,download the remote LFS/Release object and compare SHA256 and archive manifests. Only explicitly approved reproducible local intermediates whose exact remote recovery has passed may then be removed. Current deployment prohibition remains in force unless changed by a new direct instruction.

## Existing local safety closeout

Stage26 and Stage27 main and closeout archives have complete per-file readback/hash verification. Exact Stage25/Stage26 rollback JARs remain preserved. The closed build-JAR cache maintenance retained339paths and88unique JARs,merged62duplicate physical files and recovered about3.4GiB;every path/content SHA256 remains unchanged. This was local deduplication,not deletion based on unverified GitHub storage. No native game JVM or supervisor should remain;verify current process state before continuing.
