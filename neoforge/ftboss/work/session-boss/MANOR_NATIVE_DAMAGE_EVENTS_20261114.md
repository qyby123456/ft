# Stage31 Boss native damage events — scoped acceptance

Candidate SHA256: e63617ebd89d5e78055fef649db3392304a678abcafa4d74996a025d93f79c6c. Local only, NOT-INSTALLABLE; no formal deployment. Stage30 is the exact base. Two existing JAR entries change (NativeManorFinalDamageInstaller and ManorFinalDamage), five classes are added, and all other original entries, including every resource and Sparrow helper, remain byte-identical.

## Real shared context and original behavior

The Boss-owned bridge opens one per-invocation DamageContainer on the actual entity damage stack and a private thread-local frame before original settlement. Native Incoming cancellation/amount/reduction modifiers operate on this same context; original BeforeMitigation and BeforeHealth remain in their original settlement ordering. Native Pre changes the real pre-health amount. Absorption modifiers affect original planned absorption and native Post reflects actual applied health/reductions. Original default invulnerability remains 10 ticks; the incoming listener's requested 27 is applied to the actual entity.

The Stage29 owned reduction wrapper is retained. Its temporary stack context reuses the same container, preserves caller depth and restores it under normal, nested and exceptional returns. Four throwing listener types rethrow the identical sentinel Throwable; nested actual player attacks use independent frames and restore the outer frame. Product code does not invoke private gameplay callbacks. The test reads private frame state solely to observe cleanup. No paused weapon sources or independent-event product are edited.

## Proof and actual runtime coverage

The candidate composer passes 349 checks, including exact reversible reconstruction of all 341 original Boss method graphs, strict original defense/Sparrow verification, four ASM method checks, install idempotence and two negative controls. The structural round-trip survives ClassWriter serialization.

The accepted offline actual integrated-client matrix (manor-native-damage-events-native-v2-bccaec83) completes all 17 scenarios and 2498 checks. It uses a real survival ServerPlayer's empty-hand attacks, actual registered Boss entities and 40 natural entity ticks per scenario. Plain raw incoming damage is 7 and original HP loss remains about 0.28. Incoming cancel/zero/small/NaN, Pre half/NaN, custom cancel/half, real armor/absorption modifiers, actual invulnerability ticks, throwing Incoming/Pre/custom/Post consumers and nested actual attacks pass. Cancellation/nonfinite rejection preserves HP and planned absorption as asserted; Post is not spuriously fired. Thread-local and actual stack are restored after every case.

A fresh serial original Sparrow regression (sparrow-native-events-regression-v1-45d4271b) passes 1005 checks: three actual positive attacks and three actual native Post callbacks, 22 natural ticks of original owned navigation, original 60-tick expiry, no-AI rejection and entity-leave cleanup. Both accepted clients save every dimension, exit 0, are reaped, and retain every frozen input hash. No FakePlayer, manual entity tick or private gameplay callback is used. Authentication and managed battle are not established by these fixtures.

The expanded read-only class agent captures all 12 selected definitions; transformer returns null and does not redefine/retransform. Six bridge/helper definitions match candidate entries exactly. The relevant actual EasterBossEntity, ManorKiting and ManorFinalDamage definitions match the accepted matrix. The loaded audit passes 344 checks and reconstructs all 341 original methods exactly, verifies actual Sparrow/defense/context, ASM and installation idempotence. LivingEntity is recorded but no cross-JVM byte-equality or broader reuse claim is made.

## Frozen failures and metadata

The first compile exposed package-private BeforeHealth construction; a Boss-owned factory in the existing event package resolved access without changing weapon source. The second builder exposed an incomplete test graph serializer; it was expanded to cover all original instruction types rather than narrowing the method audit. The original failing sources/output remain frozen in reports.

The first native matrix failed its absorption scenario because modern setAbsorptionAmount clamps to max absorption (zero by default). Only the fixture now sets the public actual MAX_ABSORPTION attribute and asserts admission; product JAR and original acceptance assertions were unchanged. The first V2 launch failed in BountifulFares flammability registration / Fastutil rehash before the observer/world ran. It was preserved, terminated normally, then retried with the same candidate, observer and all 28 dependency JARs in a fresh attempt. None of these failures is reclassified as accepted; failed worlds are not used as seeds.

Frozen build receipts record pre-native false flags. The unchanged historical Sparrow observer retains broad false labels and productChanged=false. Those are historical template labels; the actual counters and subsequent scoped closeout govern this candidate. Do not rewrite frozen evidence to change labels.

## Source archive and local resources

Only reviewed Java/Python, text/configuration and path/hash evidence may enter the existing Git branch. JARs, raw classes, worlds, model/texture/animation/music/audio data and nested resource payloads are excluded. Necessary resource originals remain local, and Git source recovery does not recover them.

After terminal/reap checks, 112 closed-runtime JAR paths are retained with exact bytes; 111 duplicates use read-only same-hash local hardlinks and one new local canonical content was retained. All worlds and runtime input hashes remain intact. Receipt free-space deltas are measurements, not exclusive reclaimed-byte attribution. Fresh runtime instances must independently copy writable inputs. Source worktree duplicates may be evicted only after independent remote fetch/blob recovery and handle checks, with exact expected-absence manifests.

Primary closeout: work/session-boss/reports/manor-native-events-v1-closeout-20261114.json. Loaded audit: work/session-boss/reports/manor-native-events-loaded-audit-v1-20261114/receipt.json.

## Remaining acceptance

Actual original sharing paths, authenticated summon/managed fights, every normal/impossible Boss phase and reward/death/cancel, multiplayer, dimension and persistence remain. Curios new-JVM acceptance requires the user present for Create Backup and Load; do not automatically retry the attended fixture. Full audiovisual/action fidelity and final cumulative installation/deployment gates also remain. Both formal JAR hashes were read-only rechecked unchanged; D is read-only, weapons and Session development/validation remain paused. Full Boss goal remains active.

## Verified source recovery and cache cleanup

Source commit 326ba107bcc13d82b3fed589904bb304ae941828 is pushed to the existing source branch. Independent remote fetch/blob recovery verifies all 50 selected files and the complete 4505-file tree passes resource exclusion. Exactly 20 duplicate Git worktree Java/Python files (190502 bytes) were evicted after handle checks and marked skip-worktree. Canonical compilation/runtime inputs, worlds and every excluded resource remain local. Exact expected absent paths and hashes are persisted in checkpoint-manor-native-events-cleanup-20261114.json and its cleanup-result evidence; the primary source recovery index is checkpoint-manor-native-events-20261114.json. Weapons, archive-storage and root README are unchanged.
