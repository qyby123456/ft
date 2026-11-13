from __future__ import annotations

import json
import sys
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
GAME = Path(r"C:\Users\YOUR_USERNAME\AppData\Roaming\.minecraft\versions\FarmingTales_ForgeⅡ v1.5.2")
LEGACY = GAME / "output" / "dungeon-expansion-20261101" / "tools"
REFERENCE_ROOT = Path(r"D:\ft工程文件\image2.5\dungeon-design-batch2-20261101")
INCOMING = Path(r"D:\ft工程文件\FT地牢_Image25接入_20261101\incoming")
sys.path.insert(0, str(LEGACY))

from nbtio import AIR, Structure, Tag, compound, integer, plain, read_nbt, state, string


SPECS = {
    "L3": {"name": "熔岩锻造大厅", "size": (23, 14, 23), "interior": (11, 1, 5), "stem": "L3-molten-forge-hall", "features": ["四组玻璃护面的封闭熔岩瀑布与承接池", "中央巨型黑铁砧造型祭坛", "四处锻炉、烟道与吊篮", "铁砧工作台、挂架、装卸轨道与静态矿车"], "substitutions": ["按用户要求省略概念图的门扇和栅栏门，北面仅留平直开口", "矿车用铁块与铁活板门静态搭建，不预放实体", "地板亮纹为发光方块及橙色拼花，不在行走区铺裸露熔岩"]},
    "L4": {"name": "蛛巢神殿", "size": (21, 10, 21), "interior": (10, 1, 4), "stem": "L4-spider-shrine", "features": ["有头、腹、八条完整落地石腿的蛛神像", "顶部蛛网、四组垂茧和侧壁网巢", "四座残柱、蛛卵群与骸骨陈设", "绿釉基座、绿蜡烛及两处灵魂火盆"], "substitutions": ["原版1.20.1灵魂火焰实际为青蓝色；使用绿釉与绿色蜡烛保留绿色情绪，不声称原版存在绿色灵魂火", "蛛网集中于高处和侧边，北入口及绕行通道不铺网", "白色垂茧使用不可燃的方解石及骨块，不使用可燃羊毛"]},
    "S3": {"name": "炼金实验室", "size": (9, 5, 9), "interior": (4, 1, 3), "stem": "S3-alchemist-lab", "features": ["中央后侧炼药台与装水炼药锅", "两组瓶架、书架与药草台", "带打开书本的抄写桌", "紫晶、绿紫地面符纹与安全灯光"], "substitutions": ["由7×7合理扩至9×9，保持小房间但不挤占三格宽入口", "使用空酿造台、药草和紫晶表达炼金操作区，不预装可领取水瓶或奖励箱", "紫色药锅雾气改为紫晶和紫色符纹；不加入命令方块或粒子函数", "蜡烛不点燃，木质书架附近使用不会蔓延火焰的灯笼"]},
}


class Room:
    def __init__(self, room_id):
        self.room_id = room_id
        self.spec = SPECS[room_id]
        self.width, self.height, self.depth = self.spec["size"]
        self.center = self.width // 2
        self.structure = Structure(self.spec["size"])
        self.landmarks = {}
        self.required = []
        self.structure.box((0, 0, 0), (self.width - 1, self.height - 1, self.depth - 1), AIR)
        for pos_z in range(self.depth):
            for pos_x in range(self.width):
                if room_id == "L3":
                    floor = "polished_blackstone_bricks" if (pos_x * 7 + pos_z * 11) % 13 else "cracked_polished_blackstone_bricks"
                    roof = "deepslate_tiles"
                else:
                    floor = "mossy_stone_bricks" if (pos_x * 7 + pos_z * 11) % 9 < 3 else "stone_bricks"
                    roof = "deepslate_bricks"
                self.set((pos_x, 0, pos_z), floor)
                self.set((pos_x, self.height - 1, pos_z), roof)
                if pos_x in (0, self.width - 1) or pos_z in (0, self.depth - 1):
                    for pos_y in range(1, self.height - 1):
                        wall = "polished_blackstone_bricks" if room_id == "L3" else ("mossy_stone_bricks" if (pos_x + pos_z + pos_y * 3) % 7 < 2 else "stone_bricks")
                        self.set((pos_x, pos_y, pos_z), wall)

    def set(self, position, block, entity=None, **properties):
        self.structure.set(position, state(block, **properties) if isinstance(block, str) else block, entity)

    def box(self, start, end, block, **properties):
        self.structure.box(start, end, state(block, **properties))

    def mark(self, name, positions):
        self.landmarks[name] = [list(position) for position in positions]

    def station(self, name, position):
        self.required.append({"name": name, "position": list(position)})

    def lantern(self, position, soul=False, hanging=False):
        self.set(position, "soul_lantern" if soul else "lantern", hanging=hanging, waterlogged=False)

    def anvil(self, position, facing="north"):
        self.set(position, "anvil", facing=facing)

    def campfire(self, position, soul=False):
        self.set(position, "soul_campfire" if soul else "campfire", compound(id=string("minecraft:campfire"), Items=Tag(9, [], 10), CookingTimes=Tag(11, [0] * 4), CookingTotalTimes=Tag(11, [0] * 4)), facing="north", lit=True, signal_fire=False, waterlogged=False)

    def brew(self, position):
        self.set(position, "brewing_stand", compound(id=string("minecraft:brewing_stand"), Items=Tag(9, [], 10), BrewTime=Tag(2, 0), Fuel=Tag(1, 0)), has_bottle_0=False, has_bottle_1=False, has_bottle_2=False)

    def candle(self, position, color="green", lit=False, count=1):
        self.set(position, color + "_candle", candles=count, lit=lit, waterlogged=False)

    def finish(self):
        for pos_x in range(self.center - 1, self.center + 2):
            for pos_z in range(4):
                self.set((pos_x, 0, pos_z), "polished_basalt" if self.room_id == "L3" else "polished_andesite", **({"axis": "y"} if self.room_id == "L3" else {}))
                for pos_y in range(1, 4):
                    self.set((pos_x, pos_y, pos_z), AIR)
        return self


def image_metadata(room):
    return {"image": str(REFERENCE_ROOT / "out" / (room.spec["stem"] + "-hero.png")), "prompt": str(REFERENCE_ROOT / "prompts" / (room.spec["stem"] + ".txt")), "styles": str(REFERENCE_ROOT / "ROOM_STYLES_BATCH2.md")}


def dump_json(path, value):
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2), encoding="utf-8")
