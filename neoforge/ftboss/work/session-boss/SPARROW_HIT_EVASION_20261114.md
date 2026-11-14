# Stage30: original Sparrow hit evasion — 2026-11-14

Scoped acceptance is complete for the original confirmed-hit evasion helper, original state/rules and three original integration hooks. The cumulative product is still NOT-INSTALLABLE and is not deployed. Weapons and Session development/validation remain paused. Both formal game instances and historical D stay read-only.

## Product and original behavior

Candidate SHA256: `877309d1ff2332ab6d407cd1b456d39987109d17e8f6a8e98184abe55703c25b`.
Base Stage29 SHA256: `78caa46ef163c4f3ee9cc3d1a90d5ee58e1bbf4d791f213fca390986f987692f`.

The build maps the original SparrowHitEvasion and its State to NeoForge/Mojmap and copies the pure original SparrowHitEvasionRules class byte-for-byte. All 25 mapped original method graphs and 25 ASM method checks pass. The 57-member mapping proof contains only the used subset, with input hashes; complete official mapping files are not exported.

The hooks retain the original ordering: Sparrow.confirmedHit before ManorDefense.confirmedHit; original prepare around supportedBoss; original evadeSupport around ManorKiting. Existing defense and damage installers still verify the combined class. A narrow defense-verifier compatibility adjustment recognizes the exact verified Sparrow prefix; it does not remove the defense assertion. Two existing installer classes change, four classes are added, and all other Stage29 archive entries remain byte-identical.

No evasion algorithm was simplified. Original player/source eligibility, excluded damage tags, 60-tick reaction deadline, 8-tick path refresh, speed 1.25, ally range/scope, path-node safety, original scoring, owned-path cleanup and server-stop cleanup remain in the mapped original helper. Static port verification is not a claim that every branch has full live battle coverage.

## Accepted native scope

All runs use a real offline integrated client/server, actual registered entities and an actual survival ServerPlayer performing empty-hand attacks. They use fresh public world creation and natural ticks, no FakePlayer, fake authentication, manually invoked entity ticks or private gameplay callbacks. Read-only reflection observes original state fields; it does not modify them.

- `sparrow-hit-evasion-native-v4-ba1eddca`: 971 checks; three real positive-damage attacks. Normal eligible Sparrow creates state; no-AI Sparrow rejects it; a later eligible Sparrow verifies actual entity-leave cleanup. Owned original navigation runs for 22 natural ticks. The original 60-tick deadline expires naturally; all fixture state entries are cleared.
- `sparrow-hit-evasion-ally-native-v1-ad0360ee`: 772 checks; an actual distinct registered ally passes original ally guards and actual same-fight/summoner equality. Both identifiers are naturally null in this ordinary fixture; no managed fight or summoning authority was fabricated. Original owned navigation and controlled-kiting protection each run for 16 natural ticks. No-AI on the ally only holds its reference position; it is not excluded by the original ally predicate. This proves that ordinary original branch, not authenticated managed-fight acceptance.
- Both runs save completely, exit 0, reap the client and retain every frozen input hash. Candidate bytes are identical between them.

The 351-check final-definition audit preserves 339 actual EasterBossEntity methods and changes exactly hitFeedback/sparrowTick. It verifies the original defense verifier, exact Stage29 reduction-context method, idempotence and rejection of a corrupt hook. Its audited EasterBoss definition and four captured Boss helper definitions are byte-identical in both accepted runs. LivingEntity final bytes differ across JVMs and are explicitly outside this reused audit. No claim of six-class byte identity is made.

## Preserved failures and metadata correction

V1 bootstrap is a strict failure because the pre-existing defense verifier initially rejected the additional prefix. V2 and V3 are strict positive-navigation failures even though state admission and expiry worked. The original safe-path predicate rejects all seven public candidate paths at the first node for their exact grid/knockback position; the threshold is short by approximately 0.010963 blocks. The original threshold is unchanged. V4 uses a separate eligible starting position in a fresh world. The rejected worlds are retained and never used as seeds.

The first closeout's six-class equality assumption also failed before any closeout/control writes. Its failure is recorded. The corrected binding reuses the existing auditor only for the exact EasterBoss definition it actually audits.

The ally observer inherited `probe.noAllyFixture=true` and a broad `probe.sparrowEvasionPortAccepted=false` template label. These are stale metadata labels. The frozen source and report remain unmodified; the closeout explicitly corrects their meaning using the distinct actual ally UUID, original guards, owned path/control ticks and asserted scoped outer acceptance. No original failed trial is reclassified as passed.

## Storage and recovery

Only Java/Python source, text documentation/configuration and reviewed JSON/path/hash evidence are eligible for the existing source branch. Models, textures, animations, music/audio, raw class files, JARs, ZIPs, worlds and embedded binary resources must never be included. The full staged Git tree must pass the resource/secret guards before upload. Independent public fetch and per-blob size/SHA256 recovery must pass before duplicate source cache eviction.

Closed failed trials V1/V2/V3 were locally deduplicated without removing paths or bytes: 84 JAR paths retained, 82 read-only aliases, two new canonical local contents, about 504 MiB reclaimed at measurement. All failed worlds and frozen input hashes remain intact. This is local hardlink reuse and makes no remote resource-recovery claim. Accepted/runtime inputs and local-only resources stay available. New runtimes must copy independent writable files rather than link mutable mods.

Restore absent duplicate Git source paths with `work/session-boss/tools/restore_boss_git_blob_v1.py`, a verified checkpoint path/SHA256 and an explicit output path. Long Git paths require ls-tree object IDs plus cat-file, not a long revision:path argument. Canonical candidate build inputs remain local; source Git does not recover excluded resources.

## Remaining work

Native LivingDamage.Post compatibility remains unaccepted (both native trials observe zero callbacks). Authenticated summon/managed-fight entry, complete normal/impossible battles, materials/rewards integration, multiplayer, full original visual/action fidelity and persistence/restart/dimension gates remain. Attended Curios reload must wait for the user's explicit presence; do not automate its confirmation or reuse failed worlds. Keep the full Boss goal active and do not deploy under the current formal-write prohibition.

Primary receipt: `work/session-boss/reports/sparrow-hit-evasion-v2-closeout-20261114.json`.

## Verified source checkpoint and cache eviction

Feature commit `70ead9b29fdca72e924c8afc352058ebcafc50fc` is pushed on the existing source branch. Independent public fetch, object-ID blob reads, all 44 file byte/size/SHA256 comparisons and Git fsck pass. The full 4425-file Git tree passes resource exclusion and the staged source scan passes all 13 detector controls with no findings. Weapons/archive-storage/root README remain unchanged. After verification, exactly 24 duplicate Java/Python files (248040 bytes) were removed from the Git worktree, with skip-worktree recorded; canonical build/runtime inputs and every resource remain. Checkpoint manifests identify every expected absent path for on-demand restoration.
