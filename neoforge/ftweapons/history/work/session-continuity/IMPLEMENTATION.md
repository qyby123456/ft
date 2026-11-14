# Native Session continuity work, 2026-11-10

This is implementation work inside the C continuation workspace. The canonical cumulative receipt remains `work/session-chain/reports/actual-current.json` until an explicitly recorded promotion. All candidates remain NOT-INSTALLABLE. No formal instance was deployed or overwritten.

## Functional changes

The transactional proposal contains four effective source replacements under `proposal-transactional/net/farmingtales/ftboss/item`:

- `FoodWeaponOriginalSequence.Session.close` advances field expectations after the existing original-return and observation checks. Observation failures still revoke the Session and do not replace the original native exception.
- `OwnerFieldExpectedAuthority.advance` requires the exact Session close caller, the bound original preparation, matching generation and original persistent roots, the independently expected prior state, and the actual player/carrier fields matching the independently expected next state. It advances expected fields, rather than adopting an observed image as authority.
- `OwnerNativeSemanticAuthority.advance` likewise checks the current weapon input, generation, lease, independently expected prior/next weapon values and physical component storage. It obtains all new storage identities and rechecks them before committing the expected/root/components update.
- `NativeItemComponentObservation.nativeWearStorage` admits only the precise added authority method descriptor, retaining the existing caller restriction for all other callers.

Class graph size remains 1381, effective source count 716, registered guard count 208. No new product observer, guard class or proof layer was added. The two expected-value fields become mutable only within the existing thread and authority locking rules.

## Attempts that must not be treated as acceptance

- Original candidate repeated fixture: `work/session-chain/runtime/reports/session-continuity.json`, runtime `focused-storage308-5ca5689e`. First hits complete, repeated native wear fails because the weapon input expectation was not advanced. The probe fails; the server exits 0 using stdin stop and saves fully.
- Initial implementation: `reports/candidate.json`, graph `candidate-d56054d3`; runtime `focused-storage308-cb323931`, report `session-continuity-candidate.json`. `nativeWearStorage` denies the new caller. A partially advanced in-memory weapon expectation is possible in this failed prototype because it assigned expected before reading the storage identity. The process exited with the actual native effects saved; this candidate was never promoted.
- Corrected caller and close exception handling: `reports/candidate-corrected.json`, graph `candidate-35ac0b20`; runtime `focused-storage308-ca99b6e1`, report `session-continuity-corrected.json`. All three first-hit Sessions actually complete and all repeated native wear expectations match. Repeated Sessions fail after the fixture serializes live player timer NBT with `toString`. The existing `getAllKeys` alias hook exposes a mutable Set and retires the field domain. This run is not repeated-Session acceptance.
- Early compile-only directories `candidate-c7f7e199` and `candidate-9ba427ba` were never runtime candidates or promoted. Their working proposal paths have later edits; do not treat those paths as frozen source snapshots.

The active-scope fixture instead reads the actual raw player fields into an immutable value and materializes a detached NBT copy for serialization. This is observation only; it does not write an application authority field or forge a signature, generation, lease or preparation. Before/after domain-retirement fields explicitly detect contamination.

The four active-scope faults are test-only mixins under a separate fixture mod: wrong key, wrong receiver, alias write/restore, and an original native exception whose identity must survive. A passing positive control is required; installing an incompatible fault mixin and causing global coverage rejection is not acceptance.

## Process and failure notes

Initial two apply-patch wrapper invocations failed before changing source. The direct Codex apply-patch executable succeeded. The Windows Store Python alias was unusable; subsequent commands use the bundled Python executable.

An auxiliary jcmd process 55508 became stuck attaching to already-exited server 40868. Only that diagnostic helper was stopped; no game Java process was forcibly stopped. Its exec terminal was consumed with code 1. All failed runtime probes retain their native effects and logs, and are not described as globally rolled back.

## Historical read-only boundary incident

The first bundled Python import automatically wrote two Python 3.12 bytecode caches under the D historical helper directories. This violates the requested read-only boundary. Exact paths, hashes and timestamps are in `reports/historical-cache-boundary-incident.json`. Historical source/evidence files were not edited, and no further cleanup write or deletion was performed on D. Every subsequent Python invocation must use `python.exe -B script.py`; imports must not produce more bytecode caches in historical directories.

## Still required

Final active-scope runtime `focused-storage308-0d9d3f9b` exited natively with code 0, stop and complete save, but the probe fails. The test Mixin prevents the positive control from installing coverage; its four injected rejection results therefore cannot count as active-scope acceptance. Raw observation/materialization also still changes a live player domain from not retired to retired. No candidate was promoted. The previous cumulative receipt remains byte-identical. See `reports/handoff-status.json` and the updated root handoff; all work processes have ended.

Full repair/payment and non-null lease continuation, active provider internal-mutation revocation, ten external recipe gaps, old protocol/directory/translation removal, remaining six-family variants, real client and dedicated-server acceptance, authenticated multiplayer/combat, four actual GL views, and instance-level closure remain outstanding. Positive fake-player bow cases cannot satisfy those gates. Vanilla global enchantments and Independent Event remain unchanged.
