import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent
CATALOG = ROOT.parent
PROJECT = CATALOG.parent
sys.path.insert(0, str(CATALOG))
from freeze_catalog import INDEX, digest, load, save
from nbtio import Structure, plain


def copy_locked(path, relative):
    import shutil
    path = Path(path)
    expected = digest(path)
    destination = ROOT / relative
    destination.parent.mkdir(parents=True, exist_ok=True)
    shutil.copyfile(path, destination)
    if digest(destination) != expected or digest(path) != expected:
        raise ValueError("snapshot copy changed")
    return {"source":str(path),"path":relative,"sha256":expected}


def rotate(values, turns):
    position_x, position_y, position_z = values
    return [(position_x,position_y,position_z),(-position_z,position_y,position_x),(-position_x,position_y,-position_z),(position_z,position_y,-position_x)][turns]


def transform(values, turns, origin):
    return [coordinate+offset for coordinate,offset in zip(rotate(values,turns),origin)]


def main():
    seals = {}
    for directory in (CATALOG,CATALOG / "expansion-v2",CATALOG / "expansion-v3"):
        for record in load(directory / "SHA256SUMS.json")["files"]:
            if digest(directory / record["path"]) != record["sha256"]:
                raise ValueError("prior inventory changed")
        for name in ("ready.json","progress.json","SHA256SUMS.json"):
            seals[str((directory/name).relative_to(CATALOG))] = digest(directory/name)
    integration = PROJECT / "integration/v2"
    integration_ready = load(integration / "ready.json")
    verification = []
    for path, expected in integration_ready["files"].items():
        if digest(integration / path) != expected:
            raise ValueError("independent integration evidence changed: " + path)
        verification.append({"path":path,"sha256":expected})
    integration_evidence = []
    for path in ("ready.json","final-evidence.json","installed-result.json","native-be-readback.json","README.md"):
        integration_evidence.append(copy_locked(integration/path,"integration-v2-evidence/"+path))
    final = load(integration / "final-evidence.json")
    for number, suite in enumerate(final["suites"]):
        integration_evidence.append(copy_locked(suite["log"],f"integration-v2-evidence/suite-{number}.log"))
    save(ROOT / "evidence-layers.json", {"architecture":"Independent base-room acceptance from original index, source hash bound; not runtime promotion", "independentV2Capability":{"artifactSha256":final["artifactSha256"],"status":final["status"],"scope":"Five BE original rooms x four rotations, 237000 cells / 96 native BE / 184 original fields; 44 committed container mutation refusals; role assembly and clean separate-process BE recovery, saved native 22 BE readback", "notCovered":["complete room routes/return paths and live player interactions","installed-JAR original BE rooms: NOT_RUN_NOT_PACKAGED","force kill/power loss","nine-room original whole biome","nonempty-original rich item/pattern payloads"],"verifiedFileCount":len(verification),"verifiedFiles":verification,"snapshots":integration_evidence},"remainingStages":["independent native route matrix and interactions","test-only installed candidate artifact containing original room bytes","parent explicit bounded experimental command-pool authorization"],"noPromotion":True})
    rooms, cases, path_cases = [], [], []
    for item in load(INDEX)["items"]:
        if item["group"] != 8 or item["image"] >= 10:
            continue
        identifier = item["id"].lower().replace("-","_")
        original = Path(item["candidate"])
        if digest(original) != item["sha256"]:
            raise ValueError("source changed")
        room_copy = copy_locked(original,f"candidate-resources/data/biomedungeon/structures/{identifier}.nbt")
        evidence_directory = Path(item["independent_evidence"]).parent
        route_path = evidence_directory / "route-input.json"
        route_entries = load(route_path)
        matches = [entry for entry in route_entries if entry.get("sha256")==item["sha256"]]
        if len(matches) != 1:
            raise ValueError("must select exactly one route input by original SHA, not list index: " + identifier)
        route = matches[0]
        metadata_path = original.with_suffix(".json")
        metadata = load(metadata_path)
        if metadata["sha256"] != item["sha256"]:
            raise ValueError("source metadata mismatch")
        evidence = [copy_locked(route_path,f"evidence/{identifier}/route-input-original.json"),copy_locked(metadata_path,f"evidence/{identifier}/metadata-original.json"),copy_locked(item["independent_evidence"],f"evidence/{identifier}/file-audit.json")]
        for number, path in enumerate(item["acceptance_records"]):
            evidence.append(copy_locked(path,f"evidence/{identifier}/acceptance-{number}"+Path(path).suffix))
        ports = metadata.get("connections",metadata.get("ports"))
        structure = Structure.read(original)
        interactions = [{"localBlock":list(position),"state":plain(block.as_tag()),"requiredAction":"open UI, compare empty original/default inventory, close; test mutation quarantine only in separate sacrificial instance"} for position,block in structure.blocks.items() if block.name in {"minecraft:chest","minecraft:barrel"}]
        doors = [{"localBlock":list(position),"state":plain(block.as_tag()),"requiredAction":"original open-state path, close/open interaction in sacrificial fixture, restore original and repeat return route"} for position,block in structure.blocks.items() if block.name.endswith("_door") and block.get("half")=="lower"]
        gates = ["SOURCE_HASH_AND_ARCHITECTURAL_EVIDENCE", "INDEPENDENT_V2_NATIVE_PAYLOAD_CAPABILITY", "ALL_ORIGINAL_TARGETS_4_ROTATIONS_NATIVE_ROUTES_AND_RETURN", "ROLE_AND_BE_DOOR_INTERACTIONS", "INSTALLED_CANDIDATE_ARTIFACT_ORIGINAL_ROOM_GENERATION_AND_SAVED_READBACK", "PARENT_EXPLICIT_SCOPED_AUTHORIZATION"]
        additional = []
        if item["image"] in {1,7}:
            additional.append("NATIVE_STEP_UP_BODY_SWEEP_AND_LOWER_UPPER_PATH")
        if item["image"]==8:
            additional.append("INDEPENDENT_ORIGINAL_LAVA_VINE_NATIVE_TICKS_CONTAINMENT_AND_RECOVERY")
        rooms.append({"id":identifier,"sourceId":item["id"],"sourceSha256":item["sha256"],"role":{1:"altar_hall",2:"library_storage",3:"small_treasure",4:"main_hall",5:"straight_passage",6:"corner_passage",7:"entrance",8:"garden",9:"treasury"}[item["image"]],"resource":room_copy,"evidence":evidence,"accepted":False,"currentBaselineMember":item["image"] in {5,6},"independentV2PayloadCapabilityCovered":item["image"] in {2,3,4,7,9},"originalTargetCount":len(route["targets"]),"routeSelection":"unique original SHA match, preserving duplicates/labels/order and fractional coordinates", "requiredGates":gates+additional,"pendingRouteChecks":"NOT_RUN_BY_CATALOG","pendingInstalledOriginalRoomEvidence":"NOT_RUN_NOT_PACKAGED_IN_V2","parentAuthorization":"NOT_GRANTED_BY_THIS_DOCUMENT","interactions":interactions,"doorInteractions":doors})
        for turns in range(4):
            origin = [0,96,0]
            transformed_targets = [{**target,"targetId":f"original-{number:03}","sourceIndex":number,"sourcePosition":target["position"],"position":transform(target["position"],turns,origin)} for number,target in enumerate(route["targets"])]
            transformed_ports = [{"portIndex":number,"original":port,"worldFeet":transform(port.get("position",port.get("feet")),turns,origin),"worldFacing":["north","east","south","west"][(["north","east","south","west"].index(port.get("facing",port.get("side")))+turns)%4]} for number,port in enumerate(ports)]
            staircase_cells = [{"local":list(position),"worldBlock":transform(position,turns,origin),"sourceState":plain(block.as_tag()),"rotationNote":"rotate native state via Minecraft Rotation; coordinate transform alone is not world validation"} for position,block in structure.blocks.items() if block.name.endswith("_stairs")]
            case_id = identifier + "_r" + str(turns)
            case = {"caseId":case_id,"templateId":identifier,"sourceSha256":item["sha256"],"candidate":room_copy["path"],"quarterTurns":turns,"origin":origin,"transform":"R about local zero then translate; no AABB normalization; source feet coordinates not silently shifted by 0.5", "runIsolation":"cases reuse same anchor: fresh fixture/reset per case; do not place simultaneously", "start":transform(route["start"],turns,origin),"sourceStart":route["start"],"swimming":route.get("swimming",False),"targets":transformed_targets,"ports":transformed_ports,"stairBlockProbes":staircase_cells,"requiredDirections":"each declared port to every original target and reverse; original start to every target and reverse; all declared port pairs both ways", "nativeBody":"survival player width0.6 height1.8 stepHeight0.6; native VoxelShapes, supports and current door collision; no air-only voxel BFS; actual fluid status must be checked", "geometryStatus":"INPUT_ONLY_NOT_EXECUTED"}
            cases.append(case)
            for start_index, start in [("original-start",case["start"])]+[("port-"+str(port["portIndex"]),port["worldFeet"]) for port in transformed_ports]:
                for target in transformed_targets:
                    path_cases.append({"caseId":case_id,"from":start_index,"to":target["targetId"],"start":start,"end":target["position"],"mustReturn":True,"status":"NOT_RUN"})
            for first in transformed_ports:
                for second in transformed_ports:
                    if first["portIndex"] != second["portIndex"]:
                        path_cases.append({"caseId":case_id,"from":"port-"+str(first["portIndex"]),"to":"port-"+str(second["portIndex"]),"start":first["worldFeet"],"end":second["worldFeet"],"mustReturn":True,"status":"NOT_RUN"})
    save(ROOT / "release-gate-candidates.json", {"schemaVersion":1,"scope":"Proposed gates only; zero new promotions. Existing accepted baseline is unchanged.","templates":rooms,"gateRules":{"experimentalPool":"all required gates explicitly PASS for exact source hashes/artifact/core/manifest, parent signature required","testOnlyArtifact":"independent tester may assemble accepted=false test fixtures under explicit test authorization; this is not command-pool release","partialPass":"do not infer whole-biome approval from five-BE capability, three-role assembly or two corridors","unsupported":"01 and08 remain blocked until their additional native geometry/fluid gates pass"}})
    save(ROOT / "rotated-route-inputs.json", {"schemaVersion":1,"cases":cases,"expectedCases":36,"preserveTargets":True,"worldTests":"NOT_RUN_BY_CATALOG"})
    save(ROOT / "path-check-matrix.json", {"checks":path_cases,"successRule":"all checks pass with real returned paths and swept native body collision; no target exclusions, key-role removal, stair erasure or teleport shortcuts", "cases":36})
    save(ROOT / "prior-baseline-lock.json",seals)
    print("PASS integration/v2 verified files="+str(len(verification)))
    print("PASS nine original rooms; 36 rotations; preserved source targets per rotation="+str(sum(room["originalTargetCount"] for room in rooms)))
    print("PASS directed/return route check inputs="+str(len(path_cases))+"; new promotions=0")


if __name__ == "__main__":
    main()
