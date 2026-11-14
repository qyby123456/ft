# Weapon-switch alias hotfix

The global NBT alias hook calls OwnerRepairContinuation.identityAlias for every returned value. Its original condition walks the Java call stack even when the value cannot be the int[] identity array this method handles. Test the value type first. All eligible array callers still pass the same origin, active-read, binding, array-identity and contents checks.

No authorization result, weapon view, mutable NBT or registry snapshot is cached. No persisted keys, repair/payment rules, networking, configuration, balance or visual assets change.

## Source and production boundaries

The main Java tree is an older partial source snapshot and does not contain OwnerRepairContinuation or its full maintenance dependencies. Do not import that entire subsystem into the partial tree or replace the production class with a stale full build. The maintenance-source correction is retained in source.patch. The corrected complete file, its Java 17 compile result, baseline, exact production overlay, verifier and private-world evidence are under:

D:/ft工程文件/FT-SugarQueen-Boss-Mod/_work/weapon-switch-alias-hotfix-20261019

PatchIdentityAlias moves only the four caller-check instructions after the existing int[] type check and cast. It asserts the original production instruction shape and preserves stack-map frames, exception handlers, the rest of the method and all other methods. The JDK jar tool must apply the overlay; do not use ZipArchive Update. VerifyIdentityAlias checks that every other JAR entry and all other methods are unchanged.

## Validation scope

The actual production guard classes run with -Xverify:all in AliasBenchmark. The original and candidate each reject eight untrusted input types. Seven samples of 50,000 non-array alias calls give medians of 69,437,700 ns and 3,345,200 ns respectively on this machine. This approximately 95% reduction applies only to that alias path, not total frame time or player FPS.

The task workspace also contains an isolated, synthetic-identity Forge 1.20.1 native-weapon regression run. Check its current report and deployment receipt rather than treating this README as proof of a later run or installation.

Docs impact: none. This is an implementation-only short-circuit optimization. Nothing under docs/ is written.
