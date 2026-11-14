from pathlib import Path

ROOT = Path(__file__).resolve().parents[3]
WORK = ROOT / 'work/session-boss'
helper = WORK / 'tools/prepare_boss_blood_tide_stage10.py'
namespace = {'__file__': str(helper)}
text = helper.read_text(encoding='utf-8')
exec(compile(text[:text.index('probe=(WORK/')], str(helper), 'exec'), namespace)
create = namespace['create']

def replace(text, before, after):
    assert text.count(before) == 1, before
    return text.replace(before, after)

probe = (WORK / 'tests/summon-fx-v7/BossSummonFxRuntimeProbeV7.java').read_text(encoding='utf-8')
probe = probe.replace('BossSummonFxRuntimeProbeV7', 'BossSummonFxRuntimeProbeV8')
probe = replace(probe, 'private static ServerStartedEvent startEvent;', 'private static ServerStartedEvent startEvent;\n    private static int warmup;\n    private static int priorOwnerTicks;\n    private static int naturalOwnerTicks;')
probe = replace(probe, 'startEvent=event;', 'startEvent=event;\n        level=event.getServer().overworld();\n        for(int chunkX=-2;chunkX<=2;chunkX++) for(int chunkZ=-2;chunkZ<=2;chunkZ++) level.setChunkForced(chunkX,chunkZ,true);')
probe = replace(probe, 'start=level.getGameTime();', 'start=level.getGameTime();priorOwnerTicks=owner.tickCount;')
probe = replace(probe, 'if (!setup) {setup=true;prepare();return;}', 'if (!setup) {if(++warmup<60) return;setup=true;prepare();return;}')
probe = replace(probe, 'for (Entity entity:entities) if (entity instanceof MeatMountainEntity butcher && butcher.isAlive()) butcher.tick();', 'require(owner.tickCount==priorOwnerTicks+1,"NativeSchedulerAdvancesActualOwnerExactlyOnce"+callbacks);\n            priorOwnerTicks=owner.tickCount;naturalOwnerTicks++;')
probe = replace(probe, 'report.put("directedRegisteredEntityTicksPerNativeCallback",true);report.put("naturalEntitySchedulingAccepted",false);', 'report.put("directedRegisteredEntityTicksPerNativeCallback",false);report.put("naturalEntitySchedulingAccepted",report.get("failures").equals(0) && naturalOwnerTicks>=66);report.put("naturalOwnerTicks",naturalOwnerTicks);report.put("forcedChunkWarmupTicks",warmup);')
probe = replace(probe, 'for(Entity entity:entities) entity.discard();', 'for(Entity entity:entities) entity.discard();\n            for(int chunkX=-2;chunkX<=2;chunkX++) for(int chunkZ=-2;chunkZ<=2;chunkZ++) level.setChunkForced(chunkX,chunkZ,false);')
create(WORK / 'tests/summon-fx-v8/BossSummonFxRuntimeProbeV8.java', probe)
runner = (WORK / 'tools/run_boss_summon_fx_probe_v7.py').read_text(encoding='utf-8')
runner = runner.replace('summon-fx-v7', 'summon-fx-v8').replace('BossSummonFxRuntimeProbeV7', 'BossSummonFxRuntimeProbeV8')
runner = replace(runner, 'original_jar = Path(built["jar"])', '''owner = load(ROOT / "WRITE_OWNER.json")
assert owner["noNewWeaponRuns"] is True and owner["weaponValidationPaused"] is True
assert load(ROOT / "WRITE_OWNER.claim")["threadId"] == owner["activeWriterThreadId"]
query = "Get-CimInstance Win32_Process | Where-Object { $_.Name -in @('java.exe','javaw.exe') -and $_.CommandLine -match 'session-boss|session-lease' } | Select-Object -ExpandProperty ProcessId"
assert not subprocess.check_output(["powershell", "-NoProfile", "-Command", query], encoding="utf-8-sig").strip()
original_jar = Path(built["jar"])''')
runner = replace(runner, '"naturalBossAiLoopAcceptedInThisRun": False,', '"naturalBossAiLoopAcceptedInThisRun": False, "naturalEntitySchedulingAccepted": probe.get("naturalEntitySchedulingAccepted", False),')
runner = replace(runner, 'Native server callbacks driving directed actual registered public-clone butcher entity ticks, actual native entity admission and original summonWave with original summon FX queue, particle budget, phase/tick/cap/expiry and failure/nonliving controls. No authenticated player ingress, client rendering or complete-battle acceptance', 'Natural native entity scheduler in forced chunks advances actual public-clone butcher without observer entity tick calls. Original summonWave, pulse phases, budget, cap, expiry and negative controls retained. Fixture static level/UUID setup remains directed; no authenticated player ingress, active combat AI, client rendering or complete-battle acceptance')
runner += '\nif arguments.mode == "candidate":\n    assert probe["naturalEntitySchedulingAccepted"] is True and probe["naturalOwnerTicks"] >= 66\n'
create(WORK / 'tools/run_boss_summon_fx_probe_v8.py', runner)
print('Prepared natural scheduler observer v8; no product code or weapon changes')
