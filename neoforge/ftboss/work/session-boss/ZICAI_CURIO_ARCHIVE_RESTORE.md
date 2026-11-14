# Stage28 scoped evidence archive recovery

The local ZIP uses content-addressed blobs to avoid storing identical runtime dependency bytes eight times. No original files orfailed evidence are deleted. `ARCHIVE_MANIFEST.json` records every original workspace-relative path,byte count andSHA256;`blobs/<sha256>` stores each unique payload. Do not extract blob names directly into a game installation.

Verify without writing:

```text
python -B work/session-boss/tools/restore_boss_zicai_curio_archive_v1.py <archive.zip>
```

Restore every original relative path into a new empty directory:

```text
python -B work/session-boss/tools/restore_boss_zicai_curio_archive_v1.py <archive.zip> --output <new-empty-directory>
```

The tool rejects absolute/traversal paths,extra ZIP entries,inconsistent sizes,bad hashes andexisting output directories. It verifies each restored file. Never target a formal instance orthe active source workspace.

This is a local Boss evidence/recovery container,not an installable mod release. Full Boss migration,authenticated multiplayer/rewards,full AI battle andcomplete Curios lifecycle matrix remain unaccepted. Large binaries have no proven remote recovery. Published reviewed source andits verified Git history remain separate;external game SDK/assets/dependency inputs are not claimed to be fully bundled by this scoped archive. Preserve all original local inputs.
