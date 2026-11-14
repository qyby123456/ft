# Manor native damage-event compatibility baseline — 2026-11-14

The unchanged Stage30 Boss candidate has a verified native event compatibility gap. This is a diagnostic baseline, with 353 checks and a working vanilla positive control; no product change or native event compatibility acceptance is claimed. Stage30 Sparrow scoped acceptance stays valid. The full Boss product remains NOT-INSTALLABLE and undeployed; weapons/Session remain paused and formal instances/D remain read-only.

## Actual native observation

Fresh isolated offline integrated client/server, actual survival ServerPlayer and two real positive empty-hand attacks:

| Target | Health before/after | Original BeforeMitigation | Original BeforeHealth | Native Incoming | Native Pre | Native Post |
| --- | --- | --- | --- | --- | --- | --- |
| Actual registered variant-nine Easter Boss | 1024 / 1023.72 | 1 | 1 | 0 | 0 | 0 |
| Actual registered vanilla Cow | 10 / 3 | 0 | 0 | 1 | 1 | 1 |

The same registered native listeners observe both fixtures; recording is scoped to the synchronous actual server attack and actual target object. Both attacks have raw incoming amount 7.0 under this exact mod set. Boss BeforeHealth sees 0.27999994 before its real health write. Vanilla Pre sees health 10 and Post sees health 3, with amount 7. Every observed source refers to the actual player. The vanilla control has no AI solely to hold its position; the Boss naturally runs 40 entity ticks before the attack.

The client saves completely, exits 0, is reaped, captures final definitions and preserves all frozen input hashes. No fake player/authentication, private gameplay callbacks, manually invoked entity ticks, weapon gameplay, managed fight or authenticated summon is used.

## Original and public native API evidence

The original Forge ManorFinalDamage.settle calls ForgeHooks.onLivingHurt before reduction, then creates/posts the global Forge LivingDamageEvent after original reductions/absorption planning and reads its mutable amount before sharing and writing health. Its event-bus return controls cancellation.

The current mapped settlement instead calls NativeManorDamageEvents. Its before-mitigation helper posts only the existing custom BeforeMitigation phase; its before-health helper constructs/posts only custom BeforeHealth. The actually final-defined ManorFinalDamage class in this native baseline is byte-identical to the candidate class, and the read-only audit records exact original/current class hashes and call instructions. This static evidence and the live vanilla control corroborate the gap; absence of an event class name alone is not the proof.

The local official NeoForge 21.1.233 API exposes LivingIncomingDamageEvent (cancellation, mutable amount, reduction modifiers, invulnerability ticks), LivingDamageEvent.Pre (shared DamageContainer and mutable new damage) and Post (original/new damage, source and reductions after health application). The audit records the used public signatures and class hashes, without exporting SDK archives or assets.

## Required implementation boundary

A future Boss-only adapter needs one coherent per-invocation damage/reduction context. Native incoming modifiers and cancellation must affect actual settlement, not a disconnected notification container. Preserve original custom phase callbacks, amount ordering, absorption/sharing behavior and the Stage29 owned-stack exception/nesting guarantees. Native Pre must operate at the real pre-health phase; Post must represent actual completed health application and must not fire for rejected/canceled settlement. Correct original amount, reduction metadata and invulnerability semantics are required.

Verify amount modification, cancellation, zero/nonfinite rejection, reduction modifiers, native/custom observer ordering, nested damage, observer exceptions, caller stack restoration, no duplicate callbacks and unchanged Sparrow behavior. Do not alter paused weapon sources or the independent-event product, relax assertions, replace original rules or emit synthetic callbacks just to satisfy counters.

## Local storage and excluded resources

After all three relevant runtimes were terminal, 84 independent closed-runtime JAR copies were replaced with 84 existing read-only, same-hash local hardlinks. No new canonical content was created. All original paths, bytes, frozen input hashes and worlds remain. Measured additional free space was 648130560 bytes (about 618 MiB). New runtimes must make independent writable copies.

Models, textures, animations and music/audio are local-only. Raw JAR/class/world/binary resources and nested payloads are excluded from Git and Release uploads. Only reviewed source, text and path/hash evidence may be archived. Source recovery does not recover excluded resources.

Primary receipt: `work/session-boss/reports/manor-native-damage-event-baseline-closeout-20261114.json`.
