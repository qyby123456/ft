import hashlib
import json
import sys
from collections import Counter
from pathlib import Path

ROOT = Path(__file__).resolve().parent
CATALOG = ROOT.parent
PROJECT = CATALOG.parent
sys.path.insert(0,str(CATALOG))
from freeze_catalog import digest,load,save
from nbtio import Structure,plain


def main():
    integration = PROJECT / "integration/v4-unified"
    issues = load(integration / "ISSUES.json")
    final = load(integration / "final-evidence.json")
    blocker = next(record for record in issues["blockers"] if record["id"]=="S8_07_FULL_WIDTH_INTERIOR_DOORWAY_NOT_CERTIFIED")
    room = next(record for record in load(CATALOG / "expansion-v3/seven-original-room-review.json") if record["id"]=="S8-07")
    source = CATALOG / "expansion-v3" / room["resource"]["path"]
    if digest(source)!=blocker["sourceSha256"]:
        raise ValueError("source hash mismatch")
    structure = Structure.read(source)
    cells = [{"position":[position_x,position_y,position_z],"state":plain(structure.get((position_x,position_y,position_z)).as_tag())} for position_x in range(9,16) for position_y in range(1,9) for position_z in range(0,7)]
    failures = blocker["failures"]
    per_case = dict(Counter(record["caseId"] for record in failures))
    lane_ranges = {}
    for case in sorted(per_case):
        lanes = sorted(set(round(record["originalContinuousEndpoints"][0][0],6) for record in failures if record["caseId"]==case))
        lane_ranges[case] = {"failedLaneCenters":lanes,"count":len(lanes)}
    # Original open door native AABBs independently verified against reported geometry.
    # Geometry here is static algebra, not an executed Minecraft simulation.
    collision_intervals = [[11,11.1875],[12.8125,13],[13,14]]
    lanes = [round(11.3+number*0.05,6) for number in range(49)]
    predicted = [center for center in lanes if any(center-0.3<high-1e-8 and center+0.3>low+1e-8 for low,high in collision_intervals)]
    observed = lane_ranges["s8_07_r0"]["failedLaneCenters"]
    comparison = {"predictedCollisionCenters":predicted,"observedFailedCenters":observed,"same":predicted==observed,"doorwayClearBodyCenterInterval":{"min":11.4875,"max":12.5125},"openDoorFreePhysicalGap":1.625,"requiredStripBodyEnvelope":[11,14],"declaredBoundaryWidth":3,"method":"interval overlap with width0.6 body; original native open-door side panels plus solid right jamb; not a new native world run"}
    save(ROOT / "geometry-review.json", {"sourceSha256":digest(source),"source":str(source),"unchangedOriginal":True,"integrationJarSha256":final["jarSha256"],"integrationIssueSha256":digest(integration / "ISSUES.json"),"integrationFinalEvidenceSha256":digest(integration / "final-evidence.json"),"failedCount":len(failures),"perCase":per_case,"failedLanes":lane_ranges,"originalStrip":blocker["originalStrip"],"intervalReproduction":comparison,"originalCells":cells,"independentConclusion":"Width3 is true at external boundary; the same width is physically false through adjacent open double doors and right jamb. The acceptance strip is a stronger full-interior-width contract, not merely the original boundary metadata; retain it under current authorization.","externalRouteConclusion":"Routing outside original room can connect entrances, but cannot change collision on these fixed original-coordinate straight-lane checks or supply the original doorway width. It cannot resolve this blocker without changing acceptance or original geometry."})
    v4 = CATALOG / "acceptance-candidate-v4"
    cases = load(v4 / "rotated-route-inputs.json")["cases"]
    checks = load(v4 / "path-check-matrix.json")["checks"]
    original_target_counts = {}
    for room_entry in load(v4 / "release-gate-candidates.json")["templates"]:
        route_entries = load(v4 / f"evidence/{room_entry['id']}/route-input-original.json")
        exact = [entry for entry in route_entries if entry.get("sha256")==room_entry["sourceSha256"]]
        if len(exact)!=1:
            raise ValueError("ambiguous original route")
        route = exact[0]
        original_target_counts[room_entry["id"]] = len(route["targets"])
        for case in [case for case in cases if case["templateId"]==room_entry["id"]]:
            if [(target["label"],target["sourcePosition"]) for target in case["targets"]] != [(target["label"],target["position"]) for target in route["targets"]]:
                raise ValueError("original target lost")
            expected = (len(case["ports"])+1)*len(route["targets"])+len(case["ports"])*(len(case["ports"])-1)
            if len([check for check in checks if check["caseId"]==case["caseId"]])!=expected:
                raise ValueError("route matrix incomplete")
    if len(cases)!=36 or len(checks)!=2200 or sum(original_target_counts.values())!=148:
        raise ValueError("unexpected matrix counts")
    save(ROOT / "target-audit.json", {"status":"PASS","cases":36,"directedChecksWithReturn":2200,"originalTargetsPerRotation":148,"allRotationsOriginalTargets":592,"originalTargetCounts":original_target_counts,"targetsRemoved":0,"integrationActual":{"attempted":final["isolatedFullMatrix"]["attempted"],"independentlyReplayed":final["isolatedFullMatrix"]["independentlyReplayed"],"originalTargetsRemoved":final["isolatedFullMatrix"]["originalTargetsRemoved"]},"noAcceptanceRelaxation":True})
    # Plan only: move the intact adjacent pair to a side branch and create a three-wide main arch.
    # No NBT is created and no source/candidate is altered before the coordinator reviews the tradeoff.
    edits = []
    def propose(position, after, reason):
        before = plain(structure.get(tuple(position)).as_tag())
        if before!=after:
            edits.append({"position":position,"before":before,"after":after,"reason":reason})
    for position_x in (11,12,13):
        for position_y in (4,5,6):
            for position_z in (4,5):
                propose([position_x,position_y,position_z],{"Name":"minecraft:air"},"full-width main arch at original requested strip, preserve floor Y3 and original stair rows")
    for old_x,new_x in ((11,8),(12,9)):
        for position_y in (4,5):
            propose([new_x,position_y,4],plain(structure.get((old_x,position_y,4)).as_tag()),"relocate original adjacent left/right leaves together to a functional two-block side portal")
    for position_x in (8,9):
        for position_y in (4,5,6):
            propose([position_x,position_y,5],{"Name":"minecraft:air"},"connect side double-door portal behind front wall without touching original central target route")
    save(ROOT / "repair-proposal.json", {"status":"PLAN_ONLY_REQUIRES_COORDINATOR_REVIEW","accepted":False,"originalSha256":digest(source),"derivedSha256":None,"derivedHashReason":"No derived NBT was generated. Exact changed cells are proposed; after approval serialize deterministically and independently freeze actual derived byte SHA, never reuse original acceptance.","candidateIdProposed":"s8_07_widearch_side_doublegate_v5","changeCount":len(edits),"edits":edits,"preserve":["all original source files and old candidate versions","all stairs and original ascending shape","all original containers, banners and BE payloads","original adjacent double-door left/right hinge pairing and open state as a relocated functional side portal","all 16 original S8-07 acceptance target coordinates/labels and all other room targets","original external north/south feet, widths and heights"],"tradeoffs":["The original two-leaf gate moves to X8/9; it remains adjacent/usable but is no longer the main central passage gate.","Main central gateway becomes a three-wide calcite-framed open arch; original closed double-door cannot seal a true three-wide full-height passage.","Some calcite/amethyst/deepslate front-wall material is carved; furniture/stairs are not deleted or flattened.","Requires visual review and new independent architecture/native-survival/route/interaction acceptance; no native success claim yet."],"whyNotMoveSingleLeavesToJambs":"Would leave two separated doors unable to close the central opening as an actual paired gate. Rejected as silently weakening double-door functionality.","runtimeConsumptionContract":{"newVersionRequired":True,"newTemplateIdRequired":True,"preserveOriginalIdHash":True,"acceptedUntilIndependentReview":False,"mustBind":["actual derived bytes SHA256","exact edits ledger SHA","runtime/core/JAR/manifest hashes","all original targets and four rotations","original full-width strip and three-headroom checks unchanged","new side-pair open/close behavior and return path","source payload inventory/banners unchanged","separate-process save/restart readback"],"noCrossThreadWritesOrMessages":True}})
    print("PASS target audit 36 cases / 2200 checks / 148 original targets per rotation")
    print("doorway failed per rotation:",per_case,"interval prediction matches:",comparison["same"])
    print("PLAN ONLY proposed edits="+str(len(edits))+"; derived NBT not generated; accepted=false")


if __name__ == "__main__":
    main()
