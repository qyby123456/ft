# Bow/crossbow, cavity pointer and blade-overlay patch

The canonical source project is incomplete relative to the installed runtime. Do not replace the complete mod with a normal whole-project build for this repair.

## Scope

- Transplant only FourWeaponUseGuard.blocksNativeSourceAction. Admit exact vanilla BowItem/CrossbowItem through existing readiness, broken-state and native-source paths; retain material bow/crossbow and trident checks. Other subclasses remain unchanged.
- Add client-only SwordCavityPointerEvents. It captures only in-bounds left/right presses on SwordCavityContainerScreen, then routes the matching drag/release through that screen exactly once before generic inventory gesture handlers. External overlays and other screens are not captured. The captured screen uses a weak reference; this is gesture state, not authorization state.
- Remove EventBusSubscriber and SubscribeEvent annotations from GourmetBladeStreamVisual and its Registration class. Models, textures, damage, skills, identifiers, NBT, configuration and packet schemas are unchanged. No external direct callers remain.
- Preserve every other production entry and every other guard method. The accepted delta is three modified class entries plus one added client subscriber.

## Reproduction

Initial baseline SHA256: 46de4a9cf798128d75175de5e266693518383fc89d0c4d29c6417cfae1637626. The deployment baseline is 71217ffccfb0fbf98fdbba158cf8ea4cd907ec4f49384819dc999d8184ee360c, preserving a concurrent inspector/prayer-staff/language update byte-for-byte. See evidence/REBASE.json in the work tree.

Use the existing Java 17/Forge mapping/dependency cache and the classpath file from _work/four-weapon-ui-20261019/classpath.txt. Put an immutable baseline.jar in a dedicated work directory beneath this project. Run:

    node tools/runtime_patches/bow-crossbow-pointer-vfx-20261020/build.cjs <work-directory>

The builder uses the exact Java snapshots in this directory. It maps the baseline, transplants one method, remaps only the changed payload, strips only the two old visual classes' event annotations, verifies exact method/entry delta and dependency closure, and runs both ZIP gates. It does not deploy. Do not run it against a work directory whose candidate is in use by Minecraft.

## Runtime acceptance and installation

The isolated evidence tree is _work/bow-crossbow-pointer-vfx-20261020. The probe uses Minecraft MouseHandler callbacks with pointer movement and intervening natural render/tick frames, not only direct screen calls; all ordinary installed screen-event subscribers remain active. Bow/crossbow tests use a real client/server network, natural charging ticks, both hands, ammunition conservation, spawned projectiles and durability checks. Unrelated test-only SDM shop-refresh isolation is inherited from the existing UI fixture and is not a production change.

Install only after acceptance, using the guarded deployment wrapper in that evidence tree. It must acquire all three deployment locks, verify the original production hash and tested candidate hash, refuse any running Minecraft process, preserve a full backup, call tools/deploy_jar.ps1 with the explicit current 1.5.2 TargetPath, and verify full installed readback. No Desktop, fix2, NeoForge or player-world writes are permitted.

Accepted and installed SHA256: 61736919709b781fc1459bde1459544a63f8afcd5a2ee2512c67f6c12c22896e. Final cold run client-candidate04 passes 18 UI cases, 90 normal pointer actions, 16 drag/right-click sequences, 28 real-network bow/crossbow cases and 288 rule/guard checks. The corrected baseline reproduces vanilla bow use rejection but does not reproduce the exact player-world Shift-only complaint in its first two fresh UI cases. Earlier signature-only mouse reflection failures were harness errors, not production evidence. See the work-tree REPORT.md and release/DEPLOYMENT.json for the precise acceptance and retained failures.

Docs impact is proposed in _work/docs-proposals/bow-crossbow-pointer-vfx.md; docs/** is not modified. Isolated regression cannot certify player-world frame-time behavior or every external inventory mod combination.
