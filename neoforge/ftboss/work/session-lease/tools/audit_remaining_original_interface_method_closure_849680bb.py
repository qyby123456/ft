import hashlib
import json
import struct
import zipfile
from pathlib import Path

work = Path(__file__).resolve().parents[1]
root = work.parents[1]
hashes = {}
archives = {}

def digest(path):
    path = Path(path)
    value = hashlib.sha256(path.read_bytes()).hexdigest()
    hashes[str(path)] = value
    return value

def load(path):
    digest(path)
    return json.loads(path.read_text(encoding="utf-8"))

parent_path = work / "reports/native-remaining-original-superclass-closure-static-audit-41d445cf.json"
parent = load(parent_path)
assert parent["failures"] == 0 and parent["completeDiskSuperclassClosureClasses"] == 46
for name, wanted in parent["inputHashes"].items():
    assert digest(name) == wanted, Path(name).name
delta = load(work / "reports/native-native-reference-id-source-delta-849680bb.json")
for name, wanted in delta["inputHashes"].items():
    assert digest(name) == wanted, Path(name).name
assert delta["changedClassNamesVersus41"] == ["net/farmingtales/ftboss/item/SwordCavityBuild.class"]
command = load(work / "build/remaining-original-implementations-static-qualified-41d445cf/javap-command.json")
roots = [Path(value) for value in command[command.index("-classpath") + 1].split(";")]
java_base = Path("C:/Program Files/Zulu/zulu-21/jmods/java.base.jmod")
roots.append(java_base)

def raw_class(name):
    relative = name.replace(".", "/") + ".class"
    for entry in roots:
        if entry.is_dir():
            path = entry / relative
            if path.is_file():
                return path.read_bytes(), path, relative
        elif entry.is_file():
            if entry not in archives:
                archives[entry] = zipfile.ZipFile(entry)
            member = "classes/" + relative if entry.suffix == ".jmod" else relative
            try:
                return archives[entry].read(member), entry, member
            except KeyError:
                pass
    raise AssertionError("Unresolved class/interface " + name)

def parse_class(data):
    assert data[:4] == b"\xca\xfe\xba\xbe"
    count = struct.unpack_from(">H", data, 8)[0]
    pool = [None] * count
    offset = 10
    index = 1
    while index < count:
        tag = data[offset]
        offset += 1
        if tag == 1:
            length = struct.unpack_from(">H", data, offset)[0]
            offset += 2
            pool[index] = (tag, data[offset:offset + length].decode("utf-8", errors="replace"))
            offset += length
        elif tag in (7, 8, 16, 19, 20):
            pool[index] = (tag, struct.unpack_from(">H", data, offset)[0])
            offset += 2
        elif tag in (9, 10, 11, 12, 17, 18):
            pool[index] = (tag, *struct.unpack_from(">HH", data, offset))
            offset += 4
        elif tag in (3, 4):
            offset += 4
        elif tag in (5, 6):
            offset += 8
            index += 1
        elif tag == 15:
            offset += 3
        else:
            raise AssertionError("Unknown constant pool tag " + str(tag))
        index += 1
    def class_name(position):
        return pool[pool[position][1]][1].replace("/", ".") if position else None
    flags, current, superclass, count = struct.unpack_from(">HHHH", data, offset)
    offset += 8
    interfaces = [class_name(struct.unpack_from(">H", data, offset + position * 2)[0]) for position in range(count)]
    offset += count * 2
    def members(position):
        total = struct.unpack_from(">H", data, position)[0]
        position += 2
        result = []
        for unused in range(total):
            access, name, descriptor, attributes = struct.unpack_from(">HHHH", data, position)
            position += 8
            record = {"name": pool[name][1], "descriptor": pool[descriptor][1], "accessFlags": access, "hasCode": False}
            for unused_attribute in range(attributes):
                attribute, length = struct.unpack_from(">HI", data, position)
                position += 6
                if pool[attribute][1] == "Code":
                    code_length = struct.unpack_from(">I", data, position + 4)[0]
                    code = data[position + 8:position + 8 + code_length]
                    assert len(code) == code_length
                    record.update({"hasCode": True, "originalCodeLength": code_length, "originalCodeSha256": hashlib.sha256(code).hexdigest()})
                position += length
            result.append(record)
        return result, position
    fields, offset = members(offset)
    methods, offset = members(offset)
    references = []
    for entry in pool:
        if entry and entry[0] in (10, 11):
            signature = pool[entry[2]]
            assert signature[0] == 12
            references.append({"owner": class_name(entry[1]), "name": pool[signature[1]][1], "descriptor": pool[signature[2]][1], "interfaceReference": entry[0] == 11})
    return {"class": class_name(current), "superclass": class_name(superclass), "interfaces": interfaces, "accessFlags": flags,
            "declaredMethods": methods, "constantPoolMethodReferencesNotAReachabilityProof": references}

records = {}
expected = {row["class"]: row for row in parent["classRows"]}
try:
    pending = list(expected)
    while pending:
        name = pending.pop()
        if name in records:
            continue
        data, container, member = raw_class(name)
        record = parse_class(data)
        assert record["class"] == name
        checksum = hashlib.sha256(data).hexdigest()
        if name in expected:
            assert checksum == expected[name]["classSha256"], name
        record.update({"classSha256": checksum, "container": str(container), "archiveEntry": member})
        records[name] = record
        if record["superclass"]:
            pending.append(record["superclass"])
        pending.extend(record["interfaces"])
    for container in sorted({record["container"] for record in records.values()}):
        digest(container)
finally:
    for archive in archives.values():
        archive.close()

callback_names = {"hurtEnemy", "mineBlock", "use", "useOn", "releaseUsing", "onUseTick", "inventoryTick", "finishUsingItem",
                  "canPerformAction", "initializeClient", "getUseAnimation", "getUseDuration", "getDefaultAttributeModifiers",
                  "getAttributeModifiers", "isValidRepairItem", "canContinueUsing", "overrideStackedOnOther", "overrideOtherStackedOnMe",
                  "getDamage", "getMaxDamage", "isDamageable", "isDamaged", "getCraftingRemainingItem", "getEnchantmentValue",
                  "getShareTag", "readShareTag", "shouldCauseReequipAnimation", "shouldCauseBlockBreakReset"}
concrete = [row for row in parent["classRows"] if row["originalConcreteIds"]]
resolutions = []
for original in concrete:
    chain = []
    current = original["class"]
    while current:
        assert current not in chain
        chain.append(current)
        current = records[current]["superclass"]
    reachable_interfaces = set()
    pending = [interface for current in chain for interface in records[current]["interfaces"]]
    while pending:
        interface = pending.pop()
        if interface not in reachable_interfaces:
            reachable_interfaces.add(interface)
            pending.extend(records[interface]["interfaces"])
    resolved = {}
    for current in chain:
        for method in records[current]["declaredMethods"]:
            if method["name"] in callback_names and not method["accessFlags"] & (0x0002 | 0x0008):
                key = method["name"] + method["descriptor"]
                resolved.setdefault(key, {"declaringClass": current, **method})
    defaults = [{"declaringInterface": interface, **method} for interface in sorted(reachable_interfaces)
                for method in records[interface]["declaredMethods"]
                if method["name"] in callback_names and method["hasCode"] and not method["accessFlags"] & (0x0002 | 0x0008)]
    resolutions.append({"class": original["class"], "ids": original["originalConcreteIds"], "originalSuperclassChain": chain,
                        "completeInheritedInterfaces": sorted(reachable_interfaces), "selectedMostSpecificSuperclassMethodsByExactDescriptor": list(resolved.values()),
                        "selectedInterfaceDefaultCandidatesNotClaimedResolved": defaults})
digest(Path(__file__))
digest(root / "WRITE_OWNER.claim")
output = work / "reports/native-remaining-original-interface-method-closure-static-audit-849680bb.json"
with output.open("x", encoding="utf-8") as stream:
    json.dump({"failures": 0, "registeredItems": 66, "concreteImplementations": len(concrete), "completeSuperclassAndInterfaceDiskClasses": len(records),
               "allDeclaredMethodsIncludingConstructorsAndSynthetic": sum(len(row["declaredMethods"]) for row in records.values()),
               "classRows": sorted(records.values(), key=lambda row: row["class"]), "originalCallbackResolutionRows": resolutions,
               "boundedReuseOf41DiskClassesUnchangedIn849": True, "originalZeroDurabilityIdsMustStayZero": parent["originalZeroDurabilityIdsMustStayZero"],
               "constantPoolReferencesAreNotExecutedCallGraphOrProviderCoverage": True,
               "interfaceDefaultCandidatesRequireRuntimeDispatchAndMixinAudit": True, "actualTransformedRuntimeOrCapabilityProviderAccepted": False,
               "actualSignedCombatOrPaidRepairAccepted": False, "arbitrarySubclassAdmissionAllowed": False,
               "productChanged": False, "authenticatedMultiplayerAccepted": False, "installable": False, "inputHashes": hashes},
              stream, ensure_ascii=False, indent=2)
print(json.dumps({"report": output.name, "classesAndInterfaces": len(records), "implementations": len(concrete), "hashes": len(hashes), "failures": 0}))
