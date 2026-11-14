import hashlib
import json
import struct
import sys
from pathlib import Path


class CanonicalReader:
    def __init__(self, data):
        if len(data) > 1048576:
            raise ValueError('Canonical byte budget exceeded')
        self.data = data
        self.offset = 0
        self.nodes = 0

    def take(self, length):
        if length < 0 or self.offset + length > len(self.data):
            raise ValueError('Truncated canonical input')
        result = self.data[self.offset:self.offset + length]
        self.offset += length
        return result

    def number(self, format_code):
        return struct.unpack('>' + format_code, self.take(struct.calcsize('>' + format_code)))[0]

    def length(self):
        length = self.number('i')
        if not 0 <= length <= 1048576:
            raise ValueError('Invalid canonical length')
        return length

    def string(self):
        return self.take(self.length()).decode('utf-8', errors='strict')

    def node(self, depth=0):
        self.nodes += 1
        if depth > 64 or self.nodes > 65536:
            raise ValueError('Canonical complexity exceeded')
        tag = self.number('B')
        if tag == 10:
            value = {}
            for index in range(self.length()):
                key = self.string()
                if key in value:
                    raise ValueError('Duplicate compound key')
                value[key] = self.node(depth + 1)
        elif tag == 9:
            element_type = self.number('B')
            value = {'elementType': element_type, 'elements': [self.node(depth + 1) for index in range(self.length())]}
        elif tag == 8:
            value = self.string()
        elif tag in {1, 2, 3, 4}:
            value = self.number({1: 'b', 2: 'h', 3: 'i', 4: 'q'}[tag])
        elif tag in {5, 6}:
            value = {'rawBits': self.take({5: 4, 6: 8}[tag]).hex()}
        elif tag == 7:
            value = self.take(self.length()).hex()
        elif tag in {11, 12}:
            value = [self.number({11: 'i', 12: 'q'}[tag]) for index in range(self.length())]
        elif tag == 0:
            value = None
        else:
            raise ValueError('Unknown canonical tag: ' + str(tag))
        return {'tag': tag, 'value': value}

    def read(self):
        if self.number('i') != 1179932499 or self.number('i') != 3:
            raise ValueError('Canonical magic/version mismatch')
        value = self.node()
        if self.offset != len(self.data):
            raise ValueError('Trailing canonical bytes')
        return value


def differences(expected, actual, path='$'):
    if isinstance(expected, dict) and isinstance(actual, dict):
        result = []
        for key in sorted(set(expected) | set(actual)):
            if key not in expected or key not in actual:
                result.append({'path': path + '/' + key, 'expected': expected.get(key), 'actual': actual.get(key)})
            else:
                result.extend(differences(expected[key], actual[key], path + '/' + key))
        return result
    if expected != actual:
        return [{'path': path, 'expected': expected, 'actual': actual}]
    return []


if __name__ == '__main__':
    expected_path, actual_path, output_path = map(Path, sys.argv[1:])
    root = Path(__file__).resolve().parents[3]
    if not output_path.resolve().is_relative_to(root):
        raise ValueError('Report must stay inside the sole C workspace')
    expected_bytes, actual_bytes = expected_path.read_bytes(), actual_path.read_bytes()
    changed = differences(CanonicalReader(expected_bytes).read(), CanonicalReader(actual_bytes).read())
    report = {'expectedSha256': hashlib.sha256(expected_bytes).hexdigest(), 'actualSha256': hashlib.sha256(actual_bytes).hexdigest(),
              'expectedBytes': len(expected_bytes), 'actualBytes': len(actual_bytes), 'differences': changed,
              'differenceCount': len(changed), 'readOnlyDiagnostic': True, 'productModified': False, 'fullSessionAccepted': False}
    if output_path.exists():
        raise ValueError('Frozen evidence must not be overwritten')
    output_path.write_text(json.dumps(report, ensure_ascii=True, indent=2) + chr(10), encoding='utf-8')
    print(json.dumps({'report': str(output_path), 'differenceCount': len(changed), 'paths': [entry['path'] for entry in changed]}, ensure_ascii=True))
