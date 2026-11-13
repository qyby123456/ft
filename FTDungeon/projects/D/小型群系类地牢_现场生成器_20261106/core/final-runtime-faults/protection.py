import json
import shutil
import time
from pathlib import Path
import run_faults as shared

HOME = Path(__file__).resolve().parent


def main():
    progress = shared.integrate.load(HOME / "progress.json")
    assert progress["status"] != "RUNNING", "Do not overlap owned servers"
    lock = shared.integrate.load(HOME / "input-lock.json")
    source = HOME / "inputs/baseline"
    run = Path(progress["root"]) / "protection_piston"
    run.mkdir()
    shutil.copytree(source / "libraries", run / "libraries")
    shutil.copytree(source / "world", run / "world")
    shared.integrate.save(run / "copied-world-lock.json", {"files": [{"path": path.relative_to(run / "world").as_posix(), "sha256": shared.integrate.digest(path)} for path in sorted((run / "world").rglob("*")) if path.is_file()]})
    (run / "mods").mkdir()
    jar = Path(lock["jar"])
    shutil.copyfile(jar, run / "mods" / jar.name)
    for name in ("eula.txt", "server.properties"):
        shutil.copyfile(source / name, run / name)
    reader = shared.base.load_reader(shared.INTEGRATION / "staging/20261106_132015_129205")
    identity = lock["sourceInstanceId"]
    saved, journal = shared.record(reader, run / "world", identity)
    lower = json.loads(saved["bounds"])["min"]
    piston = (lower["x"] + 3, lower["y"] + 1, lower["z"] + 3)
    destination = (piston[0] + 1, piston[1], piston[2])
    power = (piston[0] - 1, piston[1], piston[2])
    result = {"status": "RUNNING", "processes": [], "jarSha256": lock["jarSha256"], "piston": piston, "protectedDestination": destination, "power": power}
    server = None
    try:
        server = shared.Server(run, "piston")
        server.send("forceload add " + str(piston[0] - 2) + " " + str(piston[2]) + " " + str(piston[0] + 2) + " " + str(piston[2]))
        time.sleep(2)
        for position, state in ((piston, "minecraft:piston[facing=east]"), (power, "minecraft:redstone_block")):
            offset = len(server.text())
            server.send("setblock " + " ".join(map(str, position)) + " " + state)
            server.wait(lambda text: "Changed the block" in text[offset:])
        deadline = time.monotonic() + 30
        while time.monotonic() < deadline:
            status = server.status(identity)
            if "ROLLBACK_REQUIRED" in status and "blocked piston" in status:
                break
            time.sleep(0.5)
        else:
            raise RuntimeError("Actual piston event did not quarantine: " + status)
        assert "active=false tickets=0" in status
        server.send("ftbiomedungeon_candidate build " + identity + " CONFIRM_ISOLATED_AIRSPACE")
        server.wait(lambda text: "Conflict quarantined" in text)
        server.stop()
        result["processes"].append(server.evidence)
        server = None
        native = shared.native(reader, run / "world")
        assert native.state(destination)[0] == "minecraft:air"
        assert dict(native.state(piston)[1]).get("extended") == "false"
        assert native.state(power)[0] == "minecraft:redstone_block"
        final, journal = shared.record(reader, run / "world", identity)
        assert final["commitChallenge"] != saved["commitChallenge"]
        assert not final["receiptHash"]
        compared = shared.compare_sources(reader, HOME / "inputs/snapshot", source / "world", run / "world", json.loads(saved["plan"]))
        assert not compared["changedSourceOrNativeCells"]
        result.update(status="PASS_NATIVE_PISTON_CANCEL_QUARANTINE_CHALLENGE_INVALIDATION_ZERO_SOURCE_CHANGE", finalStatus=status, challengeChanged=True, receiptEmpty=True, savedDestination=native.state(destination), fixture="Operator-inserted piston/redstone in below-room protected padding air; native event, not a player placement claim", **compared)
    except Exception as error:
        result.update(status="FAIL", error=repr(error))
    finally:
        if server is not None:
            server.stop()
            result["processes"].append(server.evidence)
        shared.integrate.save(run / "result.json", result)
        shared.integrate.save(HOME / "protection-result.json", result)
        print(result["status"], result.get("error", ""), flush=True)


if __name__ == "__main__":
    main()
