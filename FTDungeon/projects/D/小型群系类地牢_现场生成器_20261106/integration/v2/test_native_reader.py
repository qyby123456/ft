import io
from pathlib import Path
import struct
import tempfile
import unittest
import zlib

import read_native_be as subject


class NativeReaderTests(unittest.TestCase):
    def test_saved_native_items_patterns_and_negative_chunk_index(self):
        home = Path(__file__).resolve().parent
        (home / 'test-work').mkdir(exist_ok=True)
        with tempfile.TemporaryDirectory(dir=home / 'test-work') as temporary:
            world = Path(temporary)
            reader = subject.base.load_reader(subject.integrate.HOME / 'staging/20261106_132015_129205')
            raw = {'DataVersion': 3465, 'xPos': -1, 'zPos': 31, 'sections': [{'Y': 12, 'block_states': {'palette': [{'Name': 'minecraft:chest', 'Properties': {'facing': 'north', 'type': 'single', 'waterlogged': 'false'}}]}}], 'block_entities': [{'id': 'minecraft:chest', 'x': -1, 'y': 200, 'z': 496, 'Items': [{'Slot': 0, 'id': 'minecraft:diamond', 'Count': 5}]}, {'id': 'minecraft:banner', 'x': -2, 'y': 201, 'z': 496, 'Patterns': [{'Pattern': 'bs', 'Color': 3}]}]}

            def tag(value):
                if isinstance(value, dict):
                    return reader.Tag(10, {key: tag(entry) for key, entry in value.items()})
                if isinstance(value, list):
                    return reader.Tag(9, [tag(entry) for entry in value], 10)
                if isinstance(value, str):
                    return reader.Tag(8, value)
                return reader.Tag(3, value)

            payload = zlib.compress(b'\x0a\x00\x00' + reader.encode_payload(tag(raw)))
            region = bytearray(12288)
            header = 4 * (31 + 31 * 32)
            region[header:header + 4] = bytes([0, 0, 2, 1])
            region[8192:8197 + len(payload)] = struct.pack('>I', len(payload) + 1) + b'\x02' + payload
            (world / 'region').mkdir()
            (world / 'region/r.-1.0.mca').write_bytes(region)
            decoded = subject.NativeWorld(reader, world)
            self.assertEqual('minecraft:chest', decoded.state((-1, 200, 496))[0])
            self.assertEqual(raw['block_entities'][0], decoded.entity((-1, 200, 496)))
            self.assertEqual(raw['block_entities'][1], decoded.entity((-2, 201, 496)))
            self.assertIsNone(decoded.entity((-3, 200, 496)))


if __name__ == '__main__':
    unittest.main(verbosity=2)
