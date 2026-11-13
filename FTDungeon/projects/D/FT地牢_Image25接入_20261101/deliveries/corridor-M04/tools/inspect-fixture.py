import io
import json
import sys
import zlib
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, "C:/Users/YOUR_USERNAME/AppData/Roaming/.minecraft/versions/FarmingTales_ForgeⅡ v1.5.2/output/dungeon-expansion-20261101/tools")
from nbtio import read_payload, read_string


def main():
    regions = ROOT / "qa/forge-run/world/region"
    chunks = {}

    def chunk(chunk_x, chunk_z):
        key = (chunk_x, chunk_z)
        if key not in chunks:
            data = (regions / f"r.{chunk_x // 32}.{chunk_z // 32}.mca").read_bytes()
            index = chunk_x % 32 + (chunk_z % 32) * 32
            offset = int.from_bytes(data[index * 4:index * 4 + 3], "big") * 4096
            if offset == 0:
                raise ValueError("Chunk not saved")
            length = int.from_bytes(data[offset:offset + 4], "big")
            method = data[offset + 4]
            compressed = data[offset + 5:offset + 4 + length]
            if method != 2:
                raise ValueError("Expected zlib region chunk")
            stream = io.BytesIO(zlib.decompress(compressed))
            kind = stream.read(1)[0]
            read_string(stream)
            chunks[key] = read_payload(stream, kind).value
        return chunks[key]

    def state(block_x, block_y, block_z):
        for section_tag in chunk(block_x // 16, block_z // 16)["sections"].value:
            section = section_tag.value
            if section["Y"].value != block_y // 16 or "block_states" not in section:
                continue
            states = section["block_states"].value
            palette = states["palette"].value
            index = 0
            if len(palette) > 1:
                bits = max(4, (len(palette) - 1).bit_length())
                per_long = 64 // bits
                position = (block_y % 16) * 256 + (block_z % 16) * 16 + block_x % 16
                index = states["data"].value[position // per_long] >> (position % per_long * bits) & ((1 << bits) - 1)
            entry = palette[index].value
            properties = entry.get("Properties")
            return {"name": entry["Name"].value, "properties": {key: value.value for key, value in properties.value.items()} if properties else {}}
        return {"name": "minecraft:air", "properties": {}}

    failed_position = [386, -57, 90]
    evidence = [{"position": failed_position, **state(*failed_position)}]
    for position in ([386, -56, 90], [386, -55, 90], [386, -58, 90], [386, -55, 88]):
        evidence.append({"position": position, **state(*position)})
    sources = []
    external_flows = []
    for block_x in range(372, 440):
        for block_z in range(72, 112):
            for block_y in range(-60, -20):
                outside_fixture = block_x < 383 or block_x > 428 or block_z < 83 or block_z > 97 or block_y < -57
                if not outside_fixture:
                    continue
                entry = state(block_x, block_y, block_z)
                if entry["name"] == "minecraft:water":
                    external_flows.append({"position": [block_x, block_y, block_z], **entry})
                    if entry["properties"].get("level") == "0":
                        sources.append({"position": [block_x, block_y, block_z], **entry})
    result = {
        "status": "DIAGNOSED_FIXTURE_CONTAMINATION", "failed_log": "qa/forge-console-initial-aquifer-failure.log",
        "failed_rotation": "COUNTERCLOCKWISE_90", "initial_origin": [384, -56, 96],
        "initial_fixture_clearance": {"min": [383, -57, 83], "max": [428, -44, 97]},
        "saved_world_observations": evidence,
        "ambient_source_water_outside_original_clearance_count": len(sources),
        "ambient_source_water_examples": sources[:16],
        "external_water_blocks_count": len(external_flows),
        "external_water_examples": external_flows[:16],
        "interpretation": "The first test cleared a one-block air gap below the intact template floor at cave elevation. Saved region data show flow below the intact floor and source water beyond the fixture clearance. Treat that run as a failure; repeat at a dry sky elevation and assert the larger ambient buffer is fluid-free before placement. Do not relax leak checks or modify the delivered NBT to mask terrain water.",
    }
    if not sources:
        raise ValueError("Ambient source-water diagnosis not confirmed")
    (ROOT / "qa/fixture-diagnosis.json").write_text(json.dumps(result, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(json.dumps({"status": result["status"], "ambient_sources": len(sources), "failed_block": evidence[0]}))


if __name__ == "__main__":
    main()
