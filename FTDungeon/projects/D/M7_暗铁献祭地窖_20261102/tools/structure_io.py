import gzip
import io
import json
import struct


class Byte(int):
    pass


def encode_string(value):
    encoded = value.encode('utf-8')
    return struct.pack('>H', len(encoded)) + encoded


def payload(value):
    if isinstance(value, Byte):
        return 1, struct.pack('>b', value)
    if isinstance(value, int):
        return 3, struct.pack('>i', value)
    if isinstance(value, str):
        return 8, encode_string(value)
    if isinstance(value, list):
        entries = [payload(child) for child in value]
        kind = entries[0][0] if entries else 0
        assert all(child[0] == kind for child in entries)
        return 9, bytes([kind]) + struct.pack('>i', len(entries)) + b''.join(child[1] for child in entries)
    if isinstance(value, dict):
        encoded = bytearray()
        for name, child in value.items():
            kind, data = payload(child)
            encoded.extend(bytes([kind]) + encode_string(name) + data)
        return 10, bytes(encoded) + b'\x00'
    raise TypeError(type(value))


def write_nbt(path, value):
    kind, data = payload(value)
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_bytes(gzip.compress(bytes([kind]) + encode_string('') + data, mtime=0))


def read_string(stream):
    return stream.read(struct.unpack('>H', stream.read(2))[0]).decode('utf-8')


def read_payload(stream, kind):
    if kind == 1:
        return Byte(struct.unpack('>b', stream.read(1))[0])
    if kind == 3:
        return struct.unpack('>i', stream.read(4))[0]
    if kind == 8:
        return read_string(stream)
    if kind == 9:
        element = stream.read(1)[0]
        return [read_payload(stream, element) for _ in range(struct.unpack('>i', stream.read(4))[0])]
    if kind == 10:
        result = {}
        while True:
            element = stream.read(1)[0]
            if not element:
                return result
            name = read_string(stream)
            result[name] = read_payload(stream, element)
    raise ValueError(kind)


def load_nbt(path):
    stream = io.BytesIO(gzip.decompress(path.read_bytes()))
    assert stream.read(1) == b'\x0a'
    read_string(stream)
    return read_payload(stream, 10)


def write_json(path, value):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
