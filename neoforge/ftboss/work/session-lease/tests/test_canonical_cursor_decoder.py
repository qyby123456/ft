import struct
import sys
import unittest
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parents[1] / 'tools'))
from decode_native_canonical_cursor import CanonicalReader, differences


def header():
    return struct.pack('>ii', 1179932499, 3)


def string(value):
    encoded = value.encode('utf-8')
    return struct.pack('>i', len(encoded)) + encoded


class CanonicalCursorDecoderTests(unittest.TestCase):
    def test_original_compound_list_arrays_and_raw_float_bits(self):
        fields = [
            ('number', bytes([3]) + struct.pack('>i', -41)),
            ('text', bytes([8]) + string('snapshot')),
            ('float', bytes([5]) + bytes.fromhex('80000000')),
            ('long', bytes([4]) + struct.pack('>q', 9223372036854775807)),
            ('bytes', bytes([7]) + struct.pack('>i', 2) + bytes.fromhex('00ff')),
            ('ints', bytes([11]) + struct.pack('>iii', 2, -4, 9)),
            ('list', bytes([9, 1]) + struct.pack('>i', 2) + bytes([1, 0, 1, 1])),
        ]
        data = header() + bytes([10]) + struct.pack('>i', len(fields))
        data += b''.join(string(name) + value for name, value in fields)
        decoded = CanonicalReader(data).read()['value']
        self.assertEqual(decoded['number']['value'], -41)
        self.assertEqual(decoded['float']['value'], {'rawBits': '80000000'})
        self.assertEqual(decoded['bytes']['value'], '00ff')
        self.assertEqual(decoded['ints']['value'], [-4, 9])
        self.assertEqual(decoded['list']['value']['elements'][1]['value'], 1)

    def test_truncation_wrong_version_trailing_bytes_and_duplicate_keys_rejected(self):
        scalar = header() + bytes([3]) + struct.pack('>i', 12)
        duplicate = header() + bytes([10]) + struct.pack('>i', 2)
        duplicate += (string('same') + bytes([1, 0])) * 2
        invalid = [scalar[:-1], scalar + bytes([0]), struct.pack('>ii', 1179932499, 4) + scalar[8:], duplicate]
        for data in invalid:
            with self.assertRaises(ValueError):
                CanonicalReader(data).read()

    def test_exact_field_delta_and_missing_key_are_reported(self):
        expected = {'component': {'value': 7}, 'removed': 3}
        actual = {'component': {'value': 8}, 'added': 4}
        changed = differences(expected, actual)
        self.assertEqual([entry['path'] for entry in changed], ['$/added', '$/component/value', '$/removed'])


if __name__ == '__main__':
    unittest.main()
