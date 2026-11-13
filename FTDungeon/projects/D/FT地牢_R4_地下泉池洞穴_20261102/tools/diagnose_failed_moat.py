import io
import zlib
from functools import lru_cache

from build_room import ROOT, write_json
from nbtio import plain, read_payload, read_string


WORLD = ROOT / 'qa/forge-run-20261102-124950/world'


@lru_cache(maxsize=None)
def chunk(chunk_x, chunk_z):
    path = WORLD / f'region/r.{chunk_x // 32}.{chunk_z // 32}.mca'
    data = path.read_bytes()
    slot = (chunk_x % 32) + (chunk_z % 32) * 32
    offset = int.from_bytes(data[slot * 4:slot * 4 + 3], 'big') * 4096
    length = int.from_bytes(data[offset:offset + 4], 'big')
    if not offset or data[offset + 4] != 2:
        raise ValueError('Missing/non-zlib diagnostic chunk')
    stream = io.BytesIO(zlib.decompress(data[offset + 5:offset + 4 + length]))
    kind = stream.read(1)[0]
    read_string(stream)
    return plain(read_payload(stream, kind))


def block(pos_x, pos_y, pos_z):
    data = chunk(pos_x // 16, pos_z // 16)
    section = next(entry for entry in data['sections'] if entry['Y'] == pos_y // 16)
    states = section['block_states']
    palette = states['palette']
    if len(palette) == 1:
        return palette[0]
    bits = max(4, (len(palette) - 1).bit_length())
    entries = 64 // bits
    index = (pos_y % 16) * 256 + (pos_z % 16) * 16 + pos_x % 16
    encoded = states['data'][index // entries] & ((1 << 64) - 1)
    return palette[(encoded >> ((index % entries) * bits)) & ((1 << bits) - 1)]


def main():
    fluids = []
    for pos_x in range(47, 66):
        for pos_y in range(-37, -22):
            for pos_z in range(47, 68):
                if 48 <= pos_x <= 64 and -36 <= pos_y <= -24 and 48 <= pos_z <= 66:
                    continue
                value = block(pos_x, pos_y, pos_z)
                if value['Name'] in ('minecraft:water', 'minecraft:lava'):
                    fluids.append({'world': [pos_x, pos_y, pos_z], 'state': value})
    write_json(ROOT / 'qa/failed-underground-moat-diagnostics.json', {
        'status': 'DIAGNOSTIC', 'world': str(WORLD), 'first_fixture_origin': [48, -36, 48],
        'moat_fluids': fluids, 'count': len(fluids),
        'note': 'Saved failed QA world only. The room foundation and interior had passed native checks before the exterior-moat assertion. Final tests move to a fresh Y=256 sky fixture, keeping the same moat assertions.',
    })
    print('Underground first-fixture moat fluid cells:', len(fluids))


if __name__ == '__main__':
    main()
