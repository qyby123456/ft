from pathlib import Path
from types import SimpleNamespace
import unittest

import verify_saved_world as subject


class SavedWorldTests(unittest.TestCase):
    def test_independent_negative_cell_rotations(self):
        origin = dict(x=100, y=200, z=-100)
        expected = [(102, 203, -96), (95, 203, -98), (97, 203, -105), (104, 203, -103)]
        for turn in range(4):
            self.assertEqual(expected[turn], subject.block_position((2, 3, 4), dict(origin=origin, quarterTurns=turn)))

    def test_rotated_stair_and_pillar_states(self):
        stair = SimpleNamespace(name='minecraft:deepslate_brick_stairs', properties=(('facing', 'north'), ('half', 'bottom'), ('shape', 'inner_left'), ('waterlogged', 'false')))
        result = subject.rotated_state(stair, 3)
        self.assertEqual('west', dict(result[1])['facing'])
        self.assertEqual('inner_left', dict(result[1])['shape'])
        pillar = SimpleNamespace(name='minecraft:chain', properties=(('axis', 'x'),))
        self.assertEqual((('axis', 'z'),), subject.rotated_state(pillar, 1)[1])

    def test_block_palette_bit_packing_does_not_cross_words(self):
        world = subject.World(None, Path('.'))
        palette = [{'Name': f'fixture:{index}'} for index in range(17)]
        bits = 5
        per_long = 12
        values = [index % 17 for index in range(4096)]
        data = []
        for start in range(0, 4096, per_long):
            word = sum(value << (index * bits) for index, value in enumerate(values[start:start + per_long]))
            data.append(word)
        world.chunks[(0, 0)] = {12: {'block_states': {'palette': palette, 'data': data}}}
        for linear in (0, 11, 12, 255, 256, 4095):
            position = (linear % 16, 192 + linear // 256, (linear // 16) % 16)
            self.assertEqual('fixture:' + str(values[linear]), world.state(position)[0])

    def test_single_palette_and_negative_chunk_coordinates(self):
        world = subject.World(None, Path('.'))
        world.chunks[(-1, -2)] = {12: {'block_states': {'palette': [{'Name': 'minecraft:barrier'}]}}}
        self.assertEqual('minecraft:barrier', world.state((-1, 200, -17))[0])
        self.assertEqual('minecraft:air', world.state((-1, 216, -17))[0])


if __name__ == '__main__':
    unittest.main(verbosity=2)
