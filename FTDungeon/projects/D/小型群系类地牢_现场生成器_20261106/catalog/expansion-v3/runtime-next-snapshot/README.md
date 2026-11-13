# runtime/next — v2 BE capability candidate, not complete-biome release

This independent project was copied ONLY from hash-verified runtime v1 build inputs and the ready core sources. Baseline runtime/ready.json, JAR and frozen resources are unchanged; see baseline-lock.json. All builds/worlds in this directory. No formal instance, mods/config/saves or other component changed.

## Implemented production code

- Real native chest/barrel/banner BE support. Implicit chest states create native default BEs; explicit original Items/Patterns/name/lock payloads validated, preserved and compared through native saveWithoutMetadata normalization. Target BE coordinates are derived from real core unit-cube placement; original x/y/z are removed, never loaded at source coordinates. Only block-state carries facing/rotation in these three supported BE types; allowed payload fields have no spatial coordinates to rotate.
- Strict original NBT policy: ID must match block type. Vanilla registered items only, unique slots0–26, legal counts, no nested item tag/loot table/commands/caps/unknown fields. Banner pattern/color validated. Literal text names only; no click/hover commands. Paired/double chest states rejected pending native adjacency semantics tests. No furnace/spawner/sign/lectern or arbitrary mod BE claim.
- Full native serialized payload enters assembled operation digest, so hashes cover content as well as state. Source furniture cannot be silently overwritten by connector/sealing operations. Original full room NBT remains unchanged. Templates with fluids/fire/random-ticking vegetation/unsafe blocks still fail closed.
- V2 journal uses FTD2 magic, schema2 and separate world/ft_biome_dungeon_generator_v2 directory. Requests use runtime-v2-be1-core-v1; world marker is ALLOW_ISOLATED_TEST_WORLD_V2 with content `ft_biome_dungeon_generator v2 isolated test only`. Never mix with v1 data/operation hashes.
- Persist both attempted `ownedThrough` and completed `completedThrough` before/after batch. Exact native BE contents permit idempotent replay. A pristine default BE may be filled only in attempted-but-not-completed interval (crash between setBlock and BE.load). A changed/emptied completed inventory or banner mismatches and quarantines rather than refilling/overwriting. An ambiguous externally inserted exact default during an attempted write cannot be distinguished; no ACID claim.
- Preflight still requires entire target air/no existing BE/entity/fluid. Original preimage is therefore air, not a promise of restoration for existing player containers. BE template bytes and canonical operations regenerate from frozen resources plus hashes; journal is not a general preexisting-BE backup. No destructive rollback command.

Production entry points still load only accepted packaged baseline manifest; no `accepted=false` promotion or test-mode command is added. Package-private CatalogFactory supports exact frozen catalog injection only from separate GameTest source set; CandidateFixtures is NOT included in release JAR. The experimental JAR must never be loaded alongside v1 (same mod ID) or deployed to a formal instance. Additional mods remain refused by production gate.

## Read-only expansion contract

Consumed catalog/expansion-v2 ready/candidate-manifest and S8 source requirements read-only, hash-bound by freeze-fixtures.py. Seven candidate rooms remain accepted=false. Five BE-bearing original full rooms S8-02,03,04,07,09 are copied byte-identically ONLY to game-test resources. They include true entrance07, main hall04 and treasury09, not generic corridors replacing them. Real core already understands entrance/terminal roles; role test asserts original07 is root,04 is present and09 is final. Capability tests do not grant independent acceptance to catalog candidates.

## Reproduce (cwd this directory)

```powershell
$python='C:\Users\YOUR_USERNAME\.cache\codex-runtimes\codex-primary-runtime\dependencies\python\python.exe'
& $python -B freeze-fixtures.py
.\BUILD.ps1 -Task runGameTestServer -Offline -GradleOptions @('-PpayloadTest=true','-PgameTestDir=run/payload-v2-fresh')
.\BUILD.ps1 -Task runGameTestServer -Offline -GradleOptions @('-PbeRestartStage=prepare','-PgameTestDir=run/be-restart-v2')
.\BUILD.ps1 -Task runGameTestServer -Offline -GradleOptions @('-PbeRestartStage=resume','-PgameTestDir=run/be-restart-v2')
.\BUILD.ps1 -Task runtimeSelfTest -Offline
.\BUILD.ps1 -Task build -Offline
```

Never reuse role/fixture worlds for a new initial build: retain evidence and choose another local directory. Restart prepare/resume deliberately share their own isolated world. Test reports/logs determine PASS; zero tests or Gradle exit alone is not sufficient. Tests load production classes from source-run, not an installed/reobfuscated release JAR. Forced kill/powerloss not certified.

## Exact remaining full-generator blockers

Full nine-room S8 and eight-biome generator remain incomplete. S8-01 north interface contains original stairs one block inward (not a level full-aperture connector). S8-08 contains9 original lava blocks and28 vine blocks, still rejected: containment, propagation/order/native ticking and attachment checks need isolated world evidence; not solved by deleting lava/vines or disabling global gamerules. Candidate real entrance has ports y2/width3 and y4/width5; main hall has y2 andy4 widths5: preserve these actual original coordinates, not a unified guessed doorway height. Seven-candidate complete-role assembly also needs independent catalog acceptance, whole-room routing/access verification and more seeds; three-role capability pass does not certify all nine.

Other groups include many fluid/random/falling blocks and additional BE types. No automatic blanket vanilla allowlist or simplified replacement furniture. Paired containers, container interactions with players, disk corruption/forced kill, full rollback of preexisting block entities, combat/loot progression and all eight biomes remain unverified/unsupported. Exact executed results, hashes and constraints will be in this directory's own ready/progress/ISSUES, never in baseline ready.

## Executed capability evidence

- payload-world-final.log: actual Forge reports2 required tests passed. Five full original rooms x4 rotations =237000 original cells including96 native BEs. Native canSurvive and delayed full readback pass; this is not blanket random-tick/neighbor-propagation proof for every far-away fixture chunk.
- Same log: original S8-07入口 + S8-04主大厅 + S8-09宝库 assembled by real core and real runtime,146150 cells/22BEs. Mid-build BE-bearing journal reload passed; committed inventory mutation was detected and quarantined with no overwrite. The ERROR-level quarantine line is the expected fault-injection result, not silently hidden test failure.
- be-restart-prepare.log / be-restart-resume.log: actual process41500 saved interrupted cursor50432 and4 native BEs; distinct process7776 recovered exactly that instance/operation hash and4BEs, explicitly resumed and fully verified143175 cells/22BEs. Clean interrupted server save/restart, not force kill/power failure.
- Baseline41 frozen files and ready hash verified unchanged. Experimental release JAR excludes all unaccepted fixture resources/test injection classes; production packaged manifest still only accepted baseline pool until independent expansion acceptance.
