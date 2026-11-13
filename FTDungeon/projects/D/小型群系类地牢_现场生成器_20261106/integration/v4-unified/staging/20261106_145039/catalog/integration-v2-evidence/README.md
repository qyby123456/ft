# V2 independent capability review

Conclusion: SCOPED_CAPABILITY_PASS. This is NOT acceptance of a complete biome generator.

- Experimental JAR SHA256: f7d73680b20d390a6f08a090fea66b66000deb33bc0a6556ad282340035c6188
- Snapshot: D:\ft工程文件\小型群系类地牢_现场生成器_20261106\integration\v2\staging\20261106_134313
- Snapshot-lock SHA256: 48e35602c9b59f7d155e1efe058ffdbf387f7e06f8d89a012d42f92a817bb4b8
- V1 seal: UNCHANGED

## Source-based real Forge world checks
- payload: required passed=3; log=D:\ft工程文件\小型群系类地牢_现场生成器_20261106\integration\v2\run\20261106_134313\payload.log; SHA256=e0ebc47a32b73c02ab48b3cc56abfa79590d16e8bd1d79a718338e4357258ebb
  - INDEPENDENT_V2_PASS originalCells=237000 originalNativeBE=96 originalRawFields=184 completedContainerMutationsRefused=44 rotations=4 types=[minecraft:chest, minecraft:barrel, minecraft:banner] accepted=false productionPromotion=false
  - PAYLOAD_V2_WORLD_PASS originalRooms=5 rotations=4 completeCells=237000 preservedBE=96 dynamicFurniture=doors/slabs/lanterns/banner/chest/barrel/pots replay=defaults-before-complete-only conflict=modified-inventory-rejected; full assembled biome/fluid NOT_RUN
  - ROLE_BE_V2_WORLD_PASS actualEntrance=s8_07 mainHall=s8_04 terminal=s8_09 rooms=3 cells=111150 BE=22 journalReload=PASS payloadReadback=PASS inventoryConflict=QUARANTINED_ZERO_OVERWRITE baselinePromotion=NONE completeNineRooms=NO
- be-restart-prepare: required passed=1; log=D:\ft工程文件\小型群系类地牢_现场生成器_20261106\integration\v2\run\20261106_134313\be-restart-prepare.log; SHA256=d7b878727a76059de11c2869d45aaf6d7a7df96bbf8601a8e6fbdeeb007075cf
  - BE_V2_RESTART_PREPARED process=82680 id=ftd-9d56e21b4f62f247a06e4c0418597d056308464f5d536c973fe8de1a21dd1c8b placedBE=4 operationHash=d7765737dc0f900fa833d4c832c55a944110725af5f84d7e4810c4ec66bf2305 ftd-9d56e21b4f62f247a06e4c0418597d056308464f5d536c973fe8de1a21dd1c8b phase=BUILDING cursor=47744 attempted=47744 active=false tickets=0 error=
- be-restart-resume: required passed=1; log=D:\ft工程文件\小型群系类地牢_现场生成器_20261106\integration\v2\run\20261106_134313\be-restart-resume.log; SHA256=b6b301a3112dc86e37cd711a94bea1697d42db796d4caac8e10ef5898afde26d
  - BE_V2_RESTART_LOADED process=40276 id=ftd-9d56e21b4f62f247a06e4c0418597d056308464f5d536c973fe8de1a21dd1c8b persistedBE=4 ftd-9d56e21b4f62f247a06e4c0418597d056308464f5d536c973fe8de1a21dd1c8b phase=BUILDING cursor=47744 attempted=47744 active=false tickets=0 error=
  - BE_V2_RESTART_PASS process=40276 id=ftd-9d56e21b4f62f247a06e4c0418597d056308464f5d536c973fe8de1a21dd1c8b finalBE=22 cells=144900 operationHash=d7765737dc0f900fa833d4c832c55a944110725af5f84d7e4810c4ec66bf2305; clean-interrupted separate process, force-kill/powerloss NOT_RUN

## Independent saved native BE readback
- Status: PASS
- BE counts: {'minecraft:barrel': 8, 'minecraft:banner': 13, 'minecraft:chest': 1}
- Raw fields compared: 44
- Compared directly against frozen original NBT, not merely regenerated runtime operation payloads. Region and journal hashes are in native-be-readback.json.

## Actual installed experimental JAR
- Status: PASS_BASELINE_POOL_ONLY
- Actual template IDs used: ['s8_05', 's8_05']
- Independent saved interior blocks checked: 12792
- Production pool is only s8_05/s8_06. Candidate BE rooms are not packaged; normal command generation of those five originals and installed-JAR BE restart remain NOT_RUN_NOT_PACKAGED.

## Limits
- Five original BE rooms remain unaccepted, test-only; installed JAR contains only s8_05/s8_06
- Original Items/Patterns in current fixtures are empty; no rich nonempty-original-item/pattern preservation claim
- Clean interrupted source-process BE recovery, not force kill/powerloss; installed original-five BE recovery NOT_RUN_NOT_PACKAGED
- Full nine-room biome, lava/vines and protected player worlds not accepted

## Reproduction
- run_v2.py freezes both current readiness inventories into a new staging/run, then runs independent payload test alongside two owner fixtures and distinct-JVM BE prepare/resume.
- install_experimental.py --snapshot <v2 staging snapshot> uses a new normal server directory and exact experimental JAR, copying only known Forge libraries, never reusing any v1 world.
- read_native_be.py --snapshot <snapshot> --world <v2 run/be-restart-world/world> --mode complete --output <integration/v2/native-be-readback.json> compares saved native BE data independently.
- finalize.py rechecks the complete sealed v1 inventory and emits precise readiness, limitations and file hashes.
