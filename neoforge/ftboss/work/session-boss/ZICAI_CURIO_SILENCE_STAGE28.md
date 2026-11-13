## Curios 无确认页依赖项逐批完成，2026-11-14

同一 C 工作区、同一唯一写入者已分别完成 V13 默认不保留背包的死亡/真实掉落/复活/自然拾取（784 检查）、V15 真实 Curios 界面与原容器网络装备拒绝及到期后装备（515 检查）、V16 双非装饰槽选择/刷新与装饰槽排除（1528 检查）、V17 真实停止后去重表的只读直接观测（140 检查）。每项单独归档、提交推送、独立远端恢复核验后才清理合格的重复源码缓存。V14 读取事件结果过早的严格失败保留，V15 只修正探针读取时序，未修改产品或放宽断言。已有跨维度、四类回退和 18 项控制直接引用，未重跑。

剩余生命周期门槛为新 JVM 真实登录与持久化：需先准备有人值守的隔离观察器，再由用户点击实验性页面的 Create Backup and Load。当前没有等待窗口；不得绕过、猜测点击、修改世界标志或调用私有确认回调。V17 仅用只读 Field.get 观测表内容，未写字段/Map 或调用私有玩法方法；停止前已为空，不能把空表结果单独归因于 onStopped.clear，原方法仍由冻结图证明支持。状态表见 github-sync/checkpoint-curios-lifecycle-matrix-20261114.json。完整生命周期和 Boss 仍未完成，NOT-INSTALLABLE、不部署，两个正式实例只读，武器和 Session 暂停。已归档公共依赖仅合并同字节只读硬链接，所有路径、失败世界和冻结输入哈希保留；新实例必须独立复制。

## Stage28 dimension lifecycle increment, 2026-11-14

V12 newfreshnativeworld passes998checks:activepending survives actualoverworld-to-Nether playertransition withoutresettingUntil,naturaloriginaloverworld-clockexpiry restores exactlyone component-preservingrelicinNether,actualclient sees suppressed/restoredstates,andnative return staysstable40ticks. OneactualpositiveBosshit/twoactualdimensionevents;candidateunchanged. Priorfourfallback/18ownercontrols arecitedwithout rerun. See github-sync/checkpoint-boss-curios-dimension-20261114.json andwork/session-boss/CURIO_DIMENSION_RECOVERY_20261114.md. NOT-INSTALLABLE,nodeployment,formalinstancesread-only,weapons/Sessionpaused,samesolewriter.

New-JVM login isstillunaccepted:priorV7/V8experimentalBackupConfirmScreenevidence remains. Currentcomputer-use requiresunavailable node_repl;noalternateautomation/inputguess/privatecallback/world-flagchange. Anattendedisolatedreloadobserver mustbeprepared;theusermustactuallyclick Create Backup and Load. No windowiscurrentlywaiting. Remainingnative lifecycle gapsareexplicitin github-sync/checkpoint-curios-lifecycle-matrix-20261114.json. Archiveonlynewnativeevidence againsttheimmutablebase;freshremoteverifybeforeguardedduplicate-cachecleanup. Preservealloriginals/rawartifacts.

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
