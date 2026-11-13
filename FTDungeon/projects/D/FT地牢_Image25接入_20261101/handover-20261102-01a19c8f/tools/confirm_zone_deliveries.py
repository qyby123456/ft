import intake_zones as intake
from recheck_zone_batch import EXPECTED, ROOT, stamp


def main():
    slots = intake.load(ROOT / 'slots.json')
    candidates = slots['zones']
    confirmations = {}
    for room_id, expected in EXPECTED.items():
        entry = next(item for item in candidates if item['id'] == room_id)
        source = intake.DELIVERIES / room_id
        for name in intake.REQUIRED:
            intake.require(intake.io_path(source / name).is_file(), 'Final delivery incomplete: ' + room_id)
        intake.require(intake.digest(source / 'room.nbt') == expected, 'Unexpected final hash: ' + room_id)
        intake.static_check(source, {**entry, 'candidate_sha256': expected})
        confirmations[room_id] = {'candidate_sha256': expected, 'reference_sha256': entry['reference']['sha256'],
                                  'producer_thread': entry['producer_thread'], 'source': 'Trusted final delivery received in this chat',
                                  'receiver_accepted': False}
    previous = intake.load(ROOT / 'holds.json')
    intake.save(ROOT / 'hold-history' / (stamp() + '.json'), {'previous': previous, 'released': [key for key in previous if key in confirmations],
                                                          'basis': confirmations}, exclusive=True)
    holds = {key: value for key, value in previous.items() if key not in confirmations}
    holds['zone-P01'] = {'producer_thread': '01a19fab-339a-7a40-957f-a21eba9ab5b3',
                         'reason': 'PRODUCER_FINAL_DELIVERY_NOT_CONFIRMED', 'final_delivery_confirmed': False,
                         'note': 'Do not freeze a partial bundle based solely on native PASS.'}
    intake.save(ROOT / 'final-confirmations.json', confirmations)
    intake.save(ROOT / 'holds.json', holds)
    print('CONFIRMED_FINAL_HASHES', len(confirmations), 'HOLDS', list(holds))


if __name__ == '__main__':
    main()
