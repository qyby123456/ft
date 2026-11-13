# Verified source cache cleanup and recovery

The user explicitly authorizes removing only uploaded andindependently recovered,unused local duplicates. The local development workspace is temporary cache. This authorization supersedes earlier blanket no-deletion notes for verified duplicate source copies only;it does not authorize deleting unuploaded binaries orprotected evidence.

Current cleanup target is only `github-sync/repositories/ft/neoforge/ftboss` in the persistence sidecar. The sole C source workspace remains authoritative andis retained for current Boss work andfrozen native input references. No `.git` metadata,minimum checkpoints,source-cache recovery instructions,restore scripts orverification tools are deleted. Both formal instances andhistorical D remain untouched.

Before cleanup,publish this guide,the cleanup plan andguards on the existing safe branch. Independently fetch that exact commit from GitHub into the previously remote-cloned verification repository andverify every path,size,SHA256 andcommit. That repository uses only previously proven remote objects plusfresh authoritative increments;no local source object copy orGit alternates. Brand-new empty clone attempts failed andremain evidence. Verify all necessary LFS/Release objects separately before treating any such large artifact as removable;current raw archives,JARs,worlds,SDKs,failed attempts andformal receipts are excluded andretained.

The guarded source-cache tool verifies the plan,clean checkout,source originals andlatest remote proof. It checks target-referencing processes andWindows Restart Manager file-handle owners without shutting any application down,then uses native Git sparse checkout toremove exactly the recorded duplicate working files. It retains Git objects/configuration andtheminimum recovery files. The receipt records every deleted path/hash,branch/commit,logical bytes andobserved system free-space delta. Any failed orpartial check stops without blind retries.

Recover the complete sidecar working copy when needed:

```text
git -C <persistence-cache> sparse-checkout disable
```

A separate new recovery directory can be reconstructed from `https://github.com/qyby123456/ft.git`,branch `codex/neoforge-source-20261113`,project prefix `neoforge/ftboss`. Never overwrite the active source workspace orformal instances. Before a future source publish,restore the necessary sidecar files,copy only reviewed source bytes,scan the exact staged index,push andindependently verify again.

Stage28 remains NOT-INSTALLABLE andnot deployed. Basic Curios silence andvalid native death/respawn/pre-expiry logout are scoped accepted;new-JVM login andthefull lifecycle matrix remain unaccepted. Weapons/Session stay paused.
