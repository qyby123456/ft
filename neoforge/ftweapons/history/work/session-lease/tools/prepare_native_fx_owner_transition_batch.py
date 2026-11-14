import hashlib
import json
import sys
from pathlib import Path

work = Path(__file__).resolve().parents[1]
root = work.parents[1]
tag, mode = sys.argv[1:]
assert mode in ["positive", "controls"] and len(tag) == 8

def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()
def load(path):
    return json.loads(path.read_text(encoding="utf-8"))
parent_path = work / ("reports/native-exact-melee-defect-fx-diagnostic-preparation-5fdaea2d.json" if mode == "positive" else "reports/native-exact-melee-configured-controls-preparation-849680bb.json")
parent = load(parent_path)
for name, expected in parent["inputHashes"].items():
    assert digest(Path(name)) == expected, Path(name).name
binding = work / ("reports/native-repair-ranged-use-candidate-" + tag + ".json")
candidate = load(binding)
assert candidate["classCount"] == 1381 and candidate["effectiveSourceCount"] == 716 and candidate["guardCount"] == 208
source = Path(parent["newObserver"]).read_text(encoding="utf-8")
if mode == "positive":
    start = source.index('                List<Map<String, Object>> publications = new ArrayList<>(); row.put("publications", publications);')
    end = source.index('                ((List<ServerPlayer>)read(server.getPlayerList(), "players")).remove(player);', start)
    replacement = r'''                if (damageEvents.size() != 2 || target.getHealth() >= healthBefore || actual.getDamageValue() != wearBefore + 1)
                    throw new AssertionError("Original actual defect damage or original one wear did not return");
                for (Map<String, Object> damage : damageEvents) {
                    if (!Boolean.TRUE.equals(damage.get("resolvedSnapshotValid")) || !Boolean.TRUE.equals(damage.get("repairEpochPresent"))
                        || !Boolean.TRUE.equals(damage.get("serverSignaturePresent")) || !Boolean.TRUE.equals(damage.get("diagnostic_ownerDomainPresentAtActualDamagePhase"))
                        || !Boolean.FALSE.equals(damage.get("diagnostic_ownerDomainRetiredAtActualDamagePhase"))
                        || !damage.get("diagnostic_originalFxCooldownAtActualDamagePhase").equals(damage.get("diagnostic_actualDamageTick")))
                        throw new AssertionError("Original FX write did not preserve exact live owner domain at the actual signed damage phase");
                }
                List<Map<String, Object>> publications = new ArrayList<>(); row.put("publications", publications);
                for (Object action : actions) {
                    Object publication = read(action, "publication");
                    Object preparation = publication == null ? null : read(publication, "ownerReservePreparation");
                    Map<String, Object> observed = new LinkedHashMap<>(); publications.add(observed);
                    observed.put("publicationPresent", publication != null); observed.put("preparationPresent", preparation != null);
                    if (preparation != null) {
                        observed.put("nativeBeforePresent", read(preparation, "nativeBefore") != null);
                        observed.put("remainingPreparedUnsupported", read(preparation, "unsupported"));
                        Object session = read(preparation, "session");
                        if (session != null) {
                            var complete = session.getClass().getDeclaredMethod("traceComplete"); complete.setAccessible(true);
                            observed.put("actualTraceComplete", complete.invoke(session));
                            observed.put("actualTraceRevoked", read(session, "traceRevoked"));
                        }
                    }
                }
                if (publications.isEmpty() || publications.stream().anyMatch(value -> !Boolean.TRUE.equals(value.get("nativeBeforePresent"))))
                    throw new AssertionError("Actual FX successor did not reach the real owner-field reservation");
                row.put("originalFxCooldownOwnerTransitionAtBothActualSignedDamagePhases", true);
                row.put("wholeNineteenSignedSessionAccepted", false);
                row.put("originalPostCombatSignedSaveOrRestartAccepted", false);
                target.discard();
'''
    source = source[:start] + replacement + source[end:]
    source = source.replace("singleDefectDiagnosticUnexpectedlyCompletedOriginalSession", "singleOriginalFxCooldownOwnerTransitionScopeOnly")
else:
    anchor = "for (int sample = 0; sample < 19; sample++) {"
    assert source.count(anchor) == 1
    source = source.replace(anchor, "for (int sample : new int[]{13}) {")
    anchor = "                CompoundTag before = net.farmingtales.ftboss.item.NativeFoundationItemData.image(actual);"
    assert source.count(anchor) == 1
    replacement = r'''                Object originalDomain = ((net.farmingtales.ftboss.item.OwnerFieldEntityAccess)player).ftboss$ownerFieldDomain();
                CompoundTag persistent = (CompoundTag)read(player, "persistentData");
                if (originalDomain == null || Boolean.TRUE.equals(read(originalDomain, "retired")) || persistent == null)
                    throw new AssertionError("External FX control requires genuine original live owner domain");
                RuntimeException directFailure = new RuntimeException("foreign-fx-callback-identity");
                int[] directCalls = new int[]{0};
                try {
                    net.farmingtales.ftboss.item.OwnerFieldExpectedAuthority.nativeFxCooldown(persistent, "ftboss:weapon_fx_hit", server.overworld().getGameTime(),
                        arguments -> { directCalls[0]++; throw directFailure; });
                    throw new AssertionError("Foreign FX exception swallowed");
                } catch (RuntimeException returned) {
                    if (returned != directFailure || directCalls[0] != 1 || Boolean.TRUE.equals(read(originalDomain, "retired"))
                        || ((net.farmingtales.ftboss.item.OwnerFieldEntityAccess)player).ftboss$ownerFieldDomain() != originalDomain
                        || Boolean.TRUE.equals(read(actualGeneration, "retired"))) throw new AssertionError("Foreign FX callback acquired a transition or changed exception identity");
                }
                persistent.putLong("ftboss:weapon_fx_hit", server.overworld().getGameTime());
                if (!Boolean.TRUE.equals(read(originalDomain, "retired"))
                    || ((net.farmingtales.ftboss.item.OwnerFieldEntityAccess)player).ftboss$ownerFieldDomain() != originalDomain)
                    throw new AssertionError("External same-key FX mutation received a successor or retained owner-field authority");
                report.put("foreignFxExceptionIdentityExactlyOnceAndNoOwnerDomainReplacement", true);
                report.put("externalSameFxKeyMutationRetiresOriginalDomainAndNeverReissuesSuccessor", true);
                CompoundTag before = net.farmingtales.ftboss.item.NativeFoundationItemData.image(actual);'''
    source = source.replace(anchor, replacement)
new_source = work / ("tests/native-fx-owner-" + mode + "-" + tag + "/PositiveWeapon436.java")
new_launcher = work / ("tools/run_native_fx_owner_" + mode + "_" + tag + ".py")
preparation = work / ("reports/native-fx-owner-" + mode + "-preparation-" + tag + ".json")
output = work / ("reports/native-repair-runtime-fx-owner-" + mode + "-" + tag + ".json")
launcher = Path(parent["newLauncher"]).read_text(encoding="utf-8")
launcher = launcher.replace(Path(parent["newObserver"]).relative_to(work).as_posix(), new_source.relative_to(work).as_posix())
launcher = launcher.replace(parent_path.relative_to(work).as_posix(), preparation.relative_to(work).as_posix())
old_tag = "5fdaea2d" if mode == "positive" else "849680bb"
old_runtime = "reports/native-repair-runtime-exact-melee-defect-fx-diagnostic-5fdaea2d.json" if mode == "positive" else "reports/native-repair-runtime-exact-melee-configured-controls-849680bb.json"
launcher = launcher.replace(old_runtime, output.relative_to(work).as_posix())
old_binding = work / ("reports/native-repair-ranged-use-candidate-" + old_tag + ".json")
launcher = launcher.replace(old_binding.relative_to(work).as_posix(), binding.relative_to(work).as_posix())
launcher = launcher.replace(digest(old_binding), digest(binding))
helper = work / "tools/prepare_native_frost_ingress_runtime.py"
namespace = {"__file__": str(helper)}
exec(compile(helper.read_text(encoding="utf-8").split("candidate = work /")[0], str(helper), "exec"), namespace)
namespace["add"](new_source.relative_to(root).as_posix(), source)
namespace["add"](new_launcher.relative_to(root).as_posix(), launcher)
paths = [Path(__file__), parent_path, binding, Path(parent["newObserver"]), Path(parent["newLauncher"]), new_source, new_launcher, helper]
with preparation.open("x", encoding="utf-8") as stream:
    json.dump({"newObserver": str(new_source), "newLauncher": str(new_launcher), "frozenProduct": tag, "onlyActualSample": 13,
               "boundedOriginalFxOwnerTransitionOnly": True, "wholeNineteenSignedOrRestartAccepted": False,
               "authorityFieldsWritten": False, "installable": False,
               "inputHashes": {str(path): digest(path) for path in paths}}, stream, ensure_ascii=False, indent=2)
print(json.dumps({"prepared": preparation.name, "mode": mode, "boundedFxOnly": True}))
