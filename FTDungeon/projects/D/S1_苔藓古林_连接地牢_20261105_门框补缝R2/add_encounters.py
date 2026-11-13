import hashlib
import importlib.util
import json
import math
import msvcrt
import shutil
import sys
import zipfile
from collections import Counter
from pathlib import Path

sys.dont_write_bytecode = True
BASE = Path(__file__).resolve().parent
ROOT = BASE.with_name("S1_苔藓古林_连接地牢_20261105_战斗R3")
INSTANCE = Path(r"C:\Users\YOUR_USERNAME\AppData\Roaming\.minecraft\versions\FarmingTales_ForgeⅡ v1.5.2")
WORLD = INSTANCE / "saves/FT_S1_Mossy_Connected_20261105"
sys.path.insert(0, str(INSTANCE / "output/dungeon-expansion-20261101/tools"))
from nbtio import AIR, Tag, Structure, integer, plain, read_nbt, string, write_nbt

OBJECTIVE = "s1_enc"
NS = "s1_encounters"
PACK = "s1-encounters-r3.zip"
MOBS = "FT地牢怪物-Forge-1.20.1-0.20.1.jar"
ROOMS = {
    2: ("泉室巡逻", [("warrior", "regular"), ("archer", "regular")]),
    3: ("水景守卫", [("warrior", "regular"), ("mage", "regular")]),
    4: ("苔殿禁卫", [("warrior", "elite"), ("warrior", "regular"), ("archer", "regular")]),
    5: ("长廊伏兵", [("warrior", "regular"), ("archer", "regular"), ("warrior", "regular")]),
    6: ("折廊术士", [("mage", "elite"), ("warrior", "regular")]),
    8: ("庭院猎手", [("archer", "regular"), ("warrior", "regular"), ("mage", "regular")]),
    9: ("双守卫精英", [("warrior", "elite"), ("archer", "elite")]),
    1: ("盘根王座", [("king", "boss")]),
}


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def save(path, value):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


def function(name, lines):
    path = ROOT / f"datapack/data/{NS}/functions/{name}.mcfunction"
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text("\n".join(lines) + "\n", encoding="utf-8")


def box(module):
    pos_x, pos_y, pos_z = module["origin"]
    size_x, size_y, size_z = module["size"]
    return f"x={pos_x},y={pos_y-60},z={pos_z},dx={size_x-1},dy={size_y-1},dz={size_z-1}"


def build():
    ROOT.mkdir(exist_ok=True)
    assert not (ROOT / "deployment-report.json").exists()
    structure = Structure.read(BASE / "mossy-forest-connected.nbt")
    layout = json.loads((BASE / "layout.json").read_text(encoding="utf-8"))
    routes = json.loads((BASE / "qa/route-input.json").read_text(encoding="utf-8"))[0]
    module_map = {entry["image"]: entry for entry in layout["modules"]}
    with zipfile.ZipFile(INSTANCE / "mods" / MOBS) as archive:
        names = json.loads(archive.read("assets/ftdungeonmobs/lang/zh_cn.json"))
        for variant in ("warrior", "archer", "mage", "king"):
            assert f"entity.ftdungeonmobs.skeleton_{variant}" in names
        assert "net/farmingtales/ftdungeonmobs/DungeonDifficultyApi.class" in archive.namelist()
    plan = []
    for number, (title, units) in ROOMS.items():
        module = module_map[number]
        center_x = module["origin"][0] + module["size"][0] / 2
        center_z = module["origin"][2] + module["size"][2] / 2
        candidates = set()
        for entry in routes["targets"]:
            if not entry["label"].startswith(f"S1-{number:02}-"):
                continue
            pos_x, pos_y, pos_z = entry["position"]
            if all(structure.get((pos_x, pos_y + rise, pos_z)) == AIR for rise in range(4)):
                below = structure.get((pos_x, pos_y - 1, pos_z)).name
                if any(token in below for token in ("bricks", "planks", "polished", "chiseled", "stone", "log")) and not any(token in below for token in ("slab", "stairs", "wall", "button", "pressure")):
                    if module["origin"][0] + 2 <= pos_x < module["origin"][0] + module["size"][0] - 2 and module["origin"][2] + 2 <= pos_z < module["origin"][2] + module["size"][2] - 2:
                        candidates.add((pos_x, pos_y, pos_z))
        selected = []
        for unit_index, (variant, kind) in enumerate(units):
            available = [position for position in candidates if all(math.dist(position, other) >= 3 for other in selected)]
            assert available, (number, unit_index)
            position = min(available, key=lambda point: ((point[0]-center_x)**2 + (point[2]-center_z)**2, point))
            selected.append(position)
        plan.append({"room": number, "title": title, "box": box(module), "module": module, "units": [{"id": f"ftdungeonmobs:skeleton_{variant}", "kind": kind, "local": position, "world": [position[0]+0.5, position[1]-60, position[2]+0.5], "width": 1.0 if variant == "king" else 0.7, "height": {"warrior":1.95,"archer":2.05,"mage":2.1,"king":3.25}[variant]} for (variant, kind), position in zip(units, selected)]})
    save(ROOT / "encounters.json", {"source": str(BASE / "mossy-forest-connected.nbt"), "source_sha256": digest(BASE / "mossy-forest-connected.nbt"), "world": str(WORLD), "origin": [0,-60,0], "rooms": plan, "counts": Counter(unit["kind"] for room in plan for unit in room["units"]), "mod": str(INSTANCE / "mods" / MOBS), "mod_sha256": digest(INSTANCE / "mods" / MOBS)})
    save(ROOT / "datapack/pack.mcmeta", {"pack": {"pack_format":15, "description":"S1 mossy dungeon: finite FT skeleton encounters, elites and skeleton king"}})
    for event in ("load", "tick"):
        save(ROOT / f"datapack/data/minecraft/tags/functions/{event}.json", {"values":[f"{NS}:{event}"]})
    load = [f"scoreboard objectives add {OBJECTIVE} dummy", f"scoreboard players add #enabled {OBJECTIVE} 0", f"execute unless data storage {NS}:state installed run scoreboard players set #enabled {OBJECTIVE} 1", f"data modify storage {NS}:state installed set value 1b", f"scoreboard players add #clock {OBJECTIVE} 0", f'bossbar add {NS}:king {{"text":"盘根骷髅王","color":"dark_green"}}', f"bossbar set {NS}:king color green", f"bossbar set {NS}:king visible false"]
    load += [f"scoreboard players add #r{room['room']} {OBJECTIVE} 0" for room in plan]
    function("load", load)
    function("tick", [f"scoreboard players add #clock {OBJECTIVE} 1", f"execute if score #clock {OBJECTIVE} matches 20.. in minecraft:overworld run function {NS}:pulse", f"execute if score #clock {OBJECTIVE} matches 20.. run scoreboard players set #clock {OBJECTIVE} 0"])
    pulse = [f"bossbar set {NS}:king visible false", f"bossbar set {NS}:king players"]
    participant = "@a[gamemode=!creative,gamemode=!spectator,"
    for room in plan:
        number = room["room"]
        viewers = participant + room["box"] + "]"
        pulse.append(f"execute if score #enabled {OBJECTIVE} matches 1 if entity {viewers} run function {NS}:room_{number:02}/pulse")
        units = room["units"]
        spawn = [f"scoreboard players set #spawn_ok {OBJECTIVE} 0"]
        for unit_index, unit in enumerate(units):
            pos_x, pos_y, pos_z = unit["world"]
            tag = f"s1_r{number}_u{unit_index}"
            kind = unit["kind"]
            elite = kind == "elite"
            boss = kind == "boss"
            multiplier = 1.5 if boss else 2.0 if elite else 1.0
            attack = 1.3 if boss else 1.25 if elite else 1.0
            armor = 3 if boss else 2 if elite else 0
            display = "盘根骷髅王" if boss else ("苔林精英·" if elite else "苔林·") + names["entity." + unit["id"].replace(":", ".")]
            tags = ["s1_encounter", f"s1_room_{number}", tag, f"s1_{kind}"]
            nbt = '{Tags:' + json.dumps(tags) + ',PersistenceRequired:1b,CanPickUpLoot:0b,CustomName:' + json.dumps(json.dumps({"text":display,"color":"dark_red" if boss else "gold" if elite else "gray"},ensure_ascii=False),ensure_ascii=False) + ',CustomNameVisible:' + ('1b' if elite or boss else '0b') + f',FTDungeonDifficulty:{{Tier:1,Automatic:0b,Overrides:{{HealthMultiplier:{multiplier}d,AttackMultiplier:{attack}d,ArmorBonus:{armor}.0d,MagicArmorBonus:0.0d,SpellPowerMultiplier:{attack}d}}}}' + '}'
            spawn += [f"execute store success score #one {OBJECTIVE} run summon {unit['id']} {pos_x} {pos_y} {pos_z} {nbt}", f"scoreboard players operation #spawn_ok {OBJECTIVE} += #one {OBJECTIVE}", f"execute as @e[tag={tag},limit=1] store result entity @s Health float 1 run attribute @s minecraft:generic.max_health get 1"]
        spawn += [f"execute if score #spawn_ok {OBJECTIVE} matches {len(units)} run scoreboard players set #r{number} {OBJECTIVE} 1", f"execute unless score #spawn_ok {OBJECTIVE} matches {len(units)} run kill @e[tag=s1_room_{number}]", f"execute unless score #spawn_ok {OBJECTIVE} matches {len(units)} run scoreboard players set #r{number} {OBJECTIVE} -1", f'execute unless score #spawn_ok {OBJECTIVE} matches {len(units)} run tellraw @a {{"text":"S1 刷怪失败，已停止该点；请检查 FT 地牢怪物模组和日志，再运行 reset。","color":"red"}}', f'execute if score #r{number} {OBJECTIVE} matches 1 run tellraw {viewers} {{"text":"{room["title"]} · 战斗开始","color":"gold"}}']
        function(f"room_{number:02}/spawn", spawn)
        loaded = " ".join(f"if loaded {int(unit['world'][0])} {unit['world'][1]} {int(unit['world'][2])}" for unit in units)
        room_pulse = [f"execute if score #r{number} {OBJECTIVE} matches 0 {loaded} run function {NS}:room_{number:02}/spawn"]
        for unit_index, unit in enumerate(units):
            pos_x, pos_y, pos_z = unit["world"]
            room_pulse.append(f"execute as @e[tag=s1_r{number}_u{unit_index}] unless entity @s[{room['box']}] run tp @s {pos_x} {pos_y} {pos_z}")
        room_pulse += [f"execute if score #r{number} {OBJECTIVE} matches 1 {loaded} unless entity @e[tag=s1_room_{number}] run function {NS}:room_{number:02}/clear"]
        if number == 1:
            room_pulse.append(f"execute if score #r1 {OBJECTIVE} matches 1 if entity @e[tag=s1_boss,limit=1] run function {NS}:bossbar")
        function(f"room_{number:02}/pulse", room_pulse)
        clear = [f"scoreboard players set #r{number} {OBJECTIVE} 2", f'tellraw {viewers} {{"text":"{room["title"]} · 已清理","color":"green"}}']
        if number == 1:
            clear += [f"bossbar set {NS}:king visible false", f"bossbar set {NS}:king players", f'tellraw {viewers} {{"text":"盘根骷髅王已击败！本次王座战完成，重进房间不会重复刷王。","color":"gold"}}']
        function(f"room_{number:02}/clear", clear)
    function("pulse", pulse)
    boss_viewers = participant + "x=42,y=-56,z=14,distance=..40]"
    function("bossbar", [f"bossbar set {NS}:king players {boss_viewers}", f"execute store result bossbar {NS}:king max run attribute @e[tag=s1_boss,limit=1] minecraft:generic.max_health get 1", f"execute store result bossbar {NS}:king value run data get entity @e[tag=s1_boss,limit=1] Health 1", f"bossbar set {NS}:king visible true"])
    function("pause", [f"scoreboard players set #enabled {OBJECTIVE} 0", f"bossbar set {NS}:king visible false", f"bossbar set {NS}:king players", 'tellraw @a {"text":"S1 新遭遇已暂停；现有怪物仍会攻击，未被清除。","color":"yellow"}'])
    function("resume", [f"scoreboard players set #enabled {OBJECTIVE} 1", 'tellraw @a {"text":"S1 遭遇已启用，仅生存/冒险玩家进入房间时触发。","color":"green"}'])
    loaded_all = " ".join(f"if loaded {int(unit['world'][0])} {unit['world'][1]} {int(unit['world'][2])}" for room in plan for unit in room["units"])
    function("reset", [f"scoreboard players set #reset_ready {OBJECTIVE} 0", f"execute in minecraft:overworld {loaded_all} run scoreboard players set #reset_ready {OBJECTIVE} 1", f"execute in minecraft:overworld if score #reset_ready {OBJECTIVE} matches 1 run function {NS}:reset_loaded", f'execute unless score #reset_ready {OBJECTIVE} matches 1 run tellraw @a {{"text":"重置需加载整个地牢。先在入口使用 forceload add 0 0 144 172，等待加载后再 reset；结束后只移除本次新增的强加载。","color":"yellow"}}'])
    function("reset_loaded", [f"kill @e[tag=s1_encounter]"] + [f"scoreboard players set #r{room['room']} {OBJECTIVE} 0" for room in plan] + [f"bossbar set {NS}:king visible false", f"bossbar set {NS}:king players", 'tellraw @a {"text":"S1 已重置：清除本包标记怪物，8 处遭遇点恢复待触发。未清理原生 Boss 自行召唤的无标签援军。","color":"yellow"}'])
    readme = """# 苔藓古林：战斗版 R3

8 处有限房间遭遇：13 个普通怪、4 个精英、1 个盘根骷髅王。入口和走廊保持安全；没有放置无限产怪的笼式刷怪器。
使用已安装 FT 地牢怪物 0.20.1 的 skeleton_warrior / skeleton_archer / skeleton_mage / skeleton_king，保留原生攻击和法术。
锁定模组 Tier 1；普通怪属性不额外加成，精英血量×2、攻击/法术×1.25、护甲+2；Boss 血量×1.5、攻击/法术×1.3、护甲+3。最终值仍受模组每怪配置影响。
初次进入存档为普通难度；不自动切换玩家模式。创造/旁观不会触发，测试请 /gamemode survival 或 /gamemode adventure。
每房间一次，怪物不自然消失；已清理不会因离开/重进或 reload 重刷。失败状态为 -1，不无限重试。全队阵亡不会自动复位，幸存怪仍保留。
房间内每秒检查一次；本包标记单位越界则拉回各自刷怪点，避免跑到其他房间。不会强制加载区块。
Boss 血条只在王座战附近生存/冒险玩家可见；刷新精度 1 秒。Boss 原生援军不包含在 18 个初始单位上限里。

管理员命令：
- /function s1_encounters:pause：暂停新增遭遇（现有怪仍攻击）。
- /function s1_encounters:resume：恢复触发。
- /function s1_encounters:reset：须先加载所有刷怪点，清除本包标签单位并重置，原生无标签援军不删除。
本包绑定此存档和固定原点 (0,-60,0)，不是可任意搬移的刷怪模板。旧结构数据包仍为建筑版。
仅部署新的战斗数据包和 level.dat 的难度/数据包启用项；不改区块、玩家、现有容器、模组和全局配置。安装前完整备份。
静态校验不等于 Forge 实战验收；未验证实际召唤、法术平衡、击杀掉落或多玩家网络行为。
"""
    (ROOT / "README.md").write_text(readme, encoding="utf-8")
    with zipfile.ZipFile(ROOT / PACK, "w", zipfile.ZIP_DEFLATED) as archive:
        for path in sorted((ROOT / "datapack").rglob("*")):
            if path.is_file():
                archive.write(path, path.relative_to(ROOT / "datapack").as_posix())
    print(json.dumps({"rooms":len(plan),"counts":Counter(unit["kind"] for room in plan for unit in room["units"])},ensure_ascii=False))


def install():
    qa = json.loads((ROOT / "qa/encounter-audit.json").read_text(encoding="utf-8"))
    assert qa["passed"] and qa["spawn_count"] == 18
    with (WORLD / "session.lock").open("r+b") as lock:
        msvcrt.locking(lock.fileno(), msvcrt.LK_NBLCK, 1)
        try:
            target = WORLD / "datapacks" / PACK
            assert not target.exists()
            spec = importlib.util.spec_from_file_location("junctions", BASE.parent / "S1_苔藓古林_连接地牢_20261105/seal_junctions.py")
            junctions = importlib.util.module_from_spec(spec)
            spec.loader.exec_module(junctions)
            block_at, _ = junctions.read_saved_world(WORLD)
            plan = json.loads((ROOT / "encounters.json").read_text(encoding="utf-8"))
            assert digest(Path(plan["mod"])) == plan["mod_sha256"]
            for room in plan["rooms"]:
                for unit in room["units"]:
                    pos_x, pos_y, pos_z = unit["world"]
                    for rise in range(math.ceil(unit["height"])):
                        assert block_at((int(pos_x), pos_y+rise, int(pos_z))) == AIR
                    assert block_at((int(pos_x), pos_y-1, int(pos_z))).name not in ("minecraft:air","minecraft:water","minecraft:lava")
            backup = ROOT / "world-before-combat-verified.zip"
            with zipfile.ZipFile(backup, "x", zipfile.ZIP_DEFLATED) as archive:
                for path in sorted(WORLD.rglob("*")):
                    if path.is_file() and path.name != "session.lock":
                        archive.write(path, path.relative_to(WORLD).as_posix())
            with zipfile.ZipFile(backup) as archive:
                assert archive.testzip() is None
                before = {name:hashlib.sha256(archive.read(name)).hexdigest() for name in archive.namelist()}
            _, level = read_nbt(WORLD / "level.dat")
            data = level.value["Data"].value
            old_difficulty = data["Difficulty"].value
            data["Difficulty"] = Tag(1,2)
            enabled = data["DataPacks"].value["Enabled"].value
            entry = "file/" + PACK
            assert entry not in [value.value for value in enabled]
            enabled.append(string(entry))
            data["DataPacks"].value["Disabled"].value = [value for value in data["DataPacks"].value["Disabled"].value if value.value != entry]
            write_nbt(ROOT / "level-combat.dat", level)
            shutil.copy2(ROOT / PACK, target)
            shutil.copy2(ROOT / "level-combat.dat", WORLD / "level.dat")
            assert digest(target) == digest(ROOT / PACK)
            _, after_level = read_nbt(WORLD / "level.dat")
            assert plain(after_level) == plain(level)
            for relative, expected in before.items():
                if relative != "level.dat":
                    assert digest(WORLD / relative) == expected, relative
            save(ROOT / "deployment-report.json", {"installed":True,"world":str(WORLD),"backup":str(backup),"pack":str(target),"pack_sha256":digest(target),"changed_existing_files":["level.dat"],"added_files":["datapacks/"+PACK],"regions_players_containers_unchanged":True,"previous_difficulty":old_difficulty,"difficulty":2,"spawn_count":18,"room_encounters":8,"client_runtime_tested":False,"static_audit":qa})
            save(ROOT / "SHA256.json", {path.relative_to(ROOT).as_posix():digest(path) for path in sorted(ROOT.rglob("*")) if path.is_file() and path.name != "SHA256.json"})
            print("DEPLOYED 8 encounters / 13 regular / 4 elites / 1 boss; world backed up; regions unchanged")
        finally:
            lock.seek(0)
            msvcrt.locking(lock.fileno(), msvcrt.LK_UNLCK, 1)


if __name__ == "__main__":
    build() if sys.argv[1] == "build" else install()
