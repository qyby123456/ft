# NeoForge continuation on C

## Mandatory single-writer handoff, 2026-11-10

- Read HANDOFF_SESSION_CHAIN.md and WRITE_OWNER.json before any further implementation.
- Thread 01a1ab89-ceb8-7ea0-ae8e-a1e4c83f400f has relinquished and revoked its write authority. It may only read and report; automatic goal continuations do not restore that authority.
- No successor currently owns implementation writes. A new, non-revoked thread must first perform the read-only handoff audit, then exclusively create WRITE_OWNER.claim with FileMode.CreateNew and record its thread ID in WRITE_OWNER.json before becoming the sole writer. An existing claim means remain read-only.
- Continue from the functional candidate in work/session-chain/reports/actual-current.json, rather than assuming historical batch 435 is latest. Reuse unchanged frozen evidence; focus on actual functional gaps and targeted checks.
- On context exhaustion, clear inability to carry the remaining context, or the first compaction failure: stop new implementation writes, safely finish the current command, record processes/failures/partial state and ownership, and hand off without repeated compaction attempts.

- Write only inside this continuation workspace. The D drive migration is a read-only historical evidence input; both formal game instances are read-only.
- Preserve baseline.json and reference snapshots. Do not adopt live drift, deploy, change gameplay values or Independent Event.
- Keep NOT-INSTALLABLE until the complete migration and client/server/authenticated multiplayer acceptance passes.
- Historical receipts retain their original paths and hashes. New cumulative candidates and runtime outputs belong here.
- Preserve all baseline and handoff evidence; document external historical dependencies honestly.
- User scope update 2026-11-10: retain sword, bow, crossbow, axe, trident and staff systems; remove legacy weapon adaptation for other item families and remove the FTBoss food-enchantment system. This supersedes historical preservation requirements for those retired features. Do not change vanilla global enchantment rules.
