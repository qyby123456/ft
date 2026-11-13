## Stage28 native Curios increment, 2026-11-14

Four scoped fallback cases now pass with actual integrated-player/server/client item synchronization:missing slot index,missing slot identifier after natural empty-slot sync,occupied slot without overwrite,andfull inventory retaining pending until capacity is freed. V9 whole-run failure remains preserved;only its already-passed missing-index case is accepted. V10 runs only remaining three cases. V11 passes18owner/projectile/eligibility controls,including actual final-positive creative andspectator exemptions and21registered projectiles;natural projectile collisions are not claimed. Native checks V9=1059(withone preservedfailure),V10=1145,V11=2000. Product candidate unchanged.

Incremental local archive has5804paths andrequires the hash-bound original Stage28 base;all paths,size andpayload hashes verified. No full physical restore orlarge-artifact remote recovery is claimed. See github-sync/checkpoint-boss-curios-fallback-owner-20261114.json andCURIO_INCREMENT_RECOVERY_20261114.md. Continue actual supported new-JVM confirmation/remaining lifecycle work without guessed experimental-screen input. NOT-INSTALLABLE,no deployment,formal instances read-only,weapons/Session paused,same sole writer. Clean only independently remote-verified duplicate source-cache files under the published plan;retain canonical frozen inputs andall raw artifacts.

# Stage28 Zicai Curios silence: scoped local candidate

Status: NOT-INSTALLABLE,not deployed,full Boss migration incomplete. Both formal instances remain read-only;Stage27 remains the deployed baseline. Weapon development/validation andSession work remain paused;the separate weapon recovery JAR is not an installable addon.

## Original contract and narrow port

The candidate adds only `ZicaiCurioSilence`,its `Choice` and`ZicaiCurioSilenceRules` from the frozen original reference. All1451existing classes andall resources remain byte-identical to Stage27. Candidate SHA256: `c45cc10ffb7b572d876e869b692a75496c88c337434ed7140225df298b7bb65e`.

The original21helper graphs,eight subscriber priorities andpure rules/Choice bytes are preserved,withbounded NeoForge event,Optional,TriState andregistry-aware ItemStack codec adapters. Build proof verifies36methods,four meaningful negative controls anddouble-patch rejection. It does not establish a full gameplay acceptance matrix.

## Native evidence

| Version | Checks | Failures | Scoped outcome |
| --- | ---: | ---: | --- |
| V1 | 65 | 1 | Immediate-login positive-damage fixture rejected;retained failure |
| V2 | 404 | 0 | Unchanged Stage27 effective baseline:two original gaps |
| V3 | 1099 | 0 | Basic native suppression,deduplication,refresh,expiry andclient sync accepted |
| V4 | 311 | 1 | Invalid gold-nugget head-slot respawn strict failure retained |
| V5 | 316 | 0 | Unchanged Stage27 invalid-slot control reproduces count2 |
| V6 | 589 | 0 | Valid original boss_relic death,respawn andpre-expiry logout accepted |
| V7 | 0 | 1 | New-JVM reload bounded failure;no integrated server |
| V8 | 0 | 1 | Public screen diagnosis:experimental-world BackupConfirmScreen;no integrated server |

V2 proves an actual effective baseline using an actual registered eligible NoAI Boss andfinal positive damage through the public native damage pipeline. Raw missing classes alone were not treated as a functional gap. V3 verifies original100tick suppression,same-tick deduplication,natural refresh andexpiry,equip-event queries andactual local client equipped/suppressed/restored synchronization.

V4's invalid vanilla gold nugget fails the actual head-slot validator. The unchanged Stage27 V5 control starts withone matching item andalso ends withtwo after actual native death/respawn;V4 remains a strict failure andno failed world becomes a seed. This known invalid-fixture behavior is not repaired orclaimed accepted by the Boss port.

V6 instead requires the original registered `frost_crown_charm` to pass the real `boss_relic` validator andCanEquip event. It verifies exactly one matching item across all actual/cosmetic Curios slots,player inventory andpending records,including native death,real client respawn packet andactual pre-expiry logout. The logout occurs at gameTime277 before pendingUntil370,clears pending andpreserves exactly one item withoriginal components.

V7 andV8 copy only the accepted normal-saved V6 world,verifying every copied/source world file hash. Both stop normally withunchanged inputs. V8 proves that public world opening reaches `BackupConfirmScreen`,title `Worlds using Experimental Settings are not supported`,with`Create Backup and Load`,`I know what I'm doing!` and`Cancel`. No integrated server orplayer starts;new-JVM persistence is therefore unaccepted. No warning is automatically accepted,world data rewritten,private callback invoked orfailed world reused. The required computer-use node_repl tool is not exposed in this run.

All native tests use actual vanilla integrated local login,not authenticated multiplayer. No synthetic/FakePlayer,manual entity/effect tick,private gameplay bridge,full AI combat,actual equip-action UI orframebuffer acceptance is claimed.

## Local archive and source recovery

The current scoped archive is `work/session-boss/archives/boss-core-stage28-zicai-curio-scoped-evidence-v3-20261113.zip`. Its receipt is under `work/session-boss/releases/boss-core-stage28-zicai-curio-silence-v3-20261113/archive-receipt.json`. The archive stores content-addressed payloads andevery original path/hash;see `ZICAI_CURIO_ARCHIVE_RESTORE.md` andthe fail-closed restore tool. All original inputs,failed attempts andpartial V2 archive-prewrite evidence remain local.

Reviewed source andsanitized checkpoint are intended for the existing `codex/neoforge-source-20261113` branch under `neoforge/ftboss`;original main/README remain unchanged. Raw worlds,archives,JARs,runtime logs andprivate configuration are excluded fromordinary Git. Source recovery does not establish remote binary recovery. Delete no local originals.

## Remaining acceptance

Continue actual slot-removal andoccupied-slot inventory fallback in a fresh public native world,then original eligibility/owner/projectile resolution controls. Separately complete the observed experimental-world confirmation through supported native/UI input andactual new-JVM login. Preserve the accepted V6 seed;never promote failed reload worlds.

The complete original Curios lifecycle matrix,authenticated multiplayer/rewards andfull Boss battle remain pending. No further formal deployment is authorized.
