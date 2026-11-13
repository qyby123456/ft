# Independent integration evidence

Conclusion: exact-hash build and source-based Forge world checks passed. Actual normal Forge dedicated-server candidate JAR verification: PASS_SCOPED. This is not a fully accepted release; remaining safety limits still apply.

## Exact candidate
- Latest successful run: 20261106_132015_129205
- Required GameTests passed: 8 (independent assertions and owner fixtures are separately prefixed in log)
- JAR: D:\ft工程文件\小型群系类地牢_现场生成器_20261106\integration\staging\20261106_132015_129205\assembled\build\libs\ft-biome-dungeon-generator-forge-1.20.1-0.1.0-candidate.jar
- JAR SHA-256: 2648e54f72169f7722852469f3aeaec5861676fe41e61de0f59eb04e90ec200d
- Snapshot lock SHA-256: c2e212a3e911c57afe4c66124f54b6615567664fed7d34c91707fe68de4a9faf
- Verification lock SHA-256: 57335c0bb7d7681eb7d236276b4993aa1cccfbcb4c8a0be2d0be57ea59abdf3d
- World log: D:\ft工程文件\小型群系类地牢_现场生成器_20261106\integration\run\20261106_132015_129205\gametest.log
- World log SHA-256: 7578d5ecdd20935c239db246367ee4049cb5f386b6726ba4bfc2d19df8d35f27

## Actual installed candidate JAR
- Installer/server result: installed-server-result.json; current status: PASS_COMMAND_COMMIT
- Independent saved-chunk readback: PASS
- Installed clean interrupt/resume/idempotency and final independent chunks: PASS
- Installed injected player-block zero-write envelope comparison: PASS
- Actual installed run: D:\ft工程文件\小型群系类地牢_现场生成器_20261106\integration\run\20261106_132015_129205_installed_132725
- Actual installed normal-server process: 58952
- Recovery process IDs: 17648, 50616
- Interrupted saved cursor: 640; committed idempotent replay: [13:33:07] [Server thread/INFO] [minecraft/MinecraftServer]: ftd-1ebedc879bfde49738b0a0d3cc76e8240307e7869dec56e643e14fccdff59593 phase=COMMITTED cursor=109060 attempted=109060 active=false tickets=0 error=
- Final independent installed readback: [{"instanceId": "ftd-1ebedc879bfde49738b0a0d3cc76e8240307e7869dec56e643e14fccdff59593", "phase": "COMMITTED", "roomIds": ["s8_05", "s8_06", "s8_06"], "rotations": [1, 1, 0], "rawInteriorCells": 22646, "perimeterCells": 16580, "routeTargets": 4, "reachableFullAirCells": 1057, "journalSha256": "dae36add000a2a96b1905f6a91ce54a82668075dad92b7cae9f116dc03e71194"}, {"instanceId": "ftd-25d3accddebd6772fae2f4a026d8ff3bca671222d20364e3f3c83e54ccb068d8", "phase": "COMMITTED", "roomIds": ["s8_05", "s8_05"], "rotations": [1, 3], "rawInteriorCells": 12792, "perimeterCells": 8472, "routeTargets": 2, "reachableFullAirCells": 780, "journalSha256": "5118e0ed76560c05f8b50c97ba7f097b776579d2177faaf686704fc1141dfd43"}]
- Conflict full saved envelope cells compared: 58938
- Exact installed mods JAR is the candidate hash above; no source-test classes or extra mods loaded. Normal dedicated-server launch, not ForgeGradle userdev.
- Actual installer official SHA1/CRC evidence: logs/official-installer-sha1.json and installed-server-result.json. Prior HTTP403 remains historical, not current blocker.

## Actual assertions
- RUNTIME_WORLD_PASS occupied_preflight_zero_writes id=ftd-27b267f5f57b88af95de9982cb6849cb0dfbebf79388a06735f886ed087029f9 cells=40986
- INTEGRATION_ASSERT invalid_anchor PASS sentinel_only=true
- INTEGRATION_ASSERT independent_rotation_sampling PASS checked_cells=68868 seeds=32 layouts=32 templates=[s8_05, s8_06] rotations=[0, 1, 2, 3] world_placement=false
- INTEGRATION_ASSERT player_conflict PASS compared_cells=18522
- INTEGRATION_ASSERT room_readback_journal_reload PASS cells=18522 process_restart=false
- INTEGRATION_ASSERT all_rotations_world PASS rotations=4 templates=s8_05,s8_06 independent_raw_interior_cells=29042 unused_boundary_ports_excluded=true
- INTEGRATION_ASSERT mixed_world_route_shell PASS rooms=3 targets=8 reachable_cells=1062 shell_cells=18842 tickets_released=true templates=[s8_05, s8_06, s8_06]
- INTEGRATION_ASSERT room_readback_journal_reload PASS cells=126350 process_restart=false
- RUNTIME_WORLD_PASS real_command_build dry_run operator_only readback pause_revalidation committed_idempotency id=ftd-3d96c74464e06b03026f041d34f140e72741ac4c9e9af91260ad513b4d7dfd68 cells=41814 operationHash=3a60f51f57d64488dda74f8e8e853a5322bd7021f298c57f4114f8b03d49e096 physical_flat_return_route=PASS tickets_released=PASS; forced-kill/powerloss NOT_RUN

## Clean interrupted restart replay
- PASS_CLEAN_INTERRUPTED_RECOVERY
- RUNTIME_RESTART_PREPARED process=56976 ftd-b7b0417369ce46aa43fb96f7cf42b0d59de6956c6dceac4bbde2bf8992d56e13 phase=BUILDING cursor=128 attempted=128 active=false tickets=0 error= operationHash=449500e0bcaade0ea94fbc1f29db4206a578f8c6f1daf281100abe9089dbcb21; clean interrupted shutdown, not forced kill
- RUNTIME_RESTART_LOADED process=68800 ftd-b7b0417369ce46aa43fb96f7cf42b0d59de6956c6dceac4bbde2bf8992d56e13 phase=BUILDING cursor=128 attempted=128 active=false tickets=0 error=
- RUNTIME_RESTART_PASS process=68800 id=ftd-b7b0417369ce46aa43fb96f7cf42b0d59de6956c6dceac4bbde2bf8992d56e13 cells=58938 operationHash=449500e0bcaade0ea94fbc1f29db4206a578f8c6f1daf281100abe9089dbcb21; distinct process interrupted recovery, forced-kill/powerloss NOT_RUN

## Preserved failure and limits
- First suite ran 5 tests; two independent fixture assertions failed (Planner and runtime instance identity differs). Server startup succeeded; owner tests passing did not cancel those failures. Original failed log is retained.
- Fixed only integration fixture: runtime instance identity includes occupied reservations; use persisted runtime plan and compare independently computed rooms/links/seed, not an identity computed with empty occupied set.
- Installed normal Forge server attempt: PASS_COMMAND_COMMIT: 
- Forced kill/powerloss, empty-pool/corrupt-NBT in-world zero-write checks, native payload injections, internal DistanceManager ticket audit and world-border edges remain unverified.
- Geometric sampling across 32 seeds is not 32 world placement runs. Physical return-route PASS is scoped to the sampled mixed three-room layout, not every layout.

## Reproduction
Use the explicit Python/Java17/Gradle paths in README.md. integrate.py gametest --gradle <Gradle8.8> --java-home <Java17> --offline --timeout 900 creates a fresh exact-hash snapshot and world.
replay_restart.py --snapshot <snapshot> --gradle <Gradle8.8> --java-home <Java17> runs prepare/resume in distinct JVMs under integration/run.
installed_server.py --snapshot <snapshot> --java-home <Java17> --installer <verified local installer> --official-sha1 <live official digest> runs actual installer-based dedicated-server candidate execution; never touches the formal instance.
installed_recovery.py --snapshot <snapshot> --run <installed run> --java-home <Java17> and installed_conflict.py with the same arguments perform normal dedicated-process recovery/conflict checks. See README for fresh-fixture requirements.
