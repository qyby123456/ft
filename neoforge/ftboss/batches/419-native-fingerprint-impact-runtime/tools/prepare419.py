from pathlib import Path

LOCAL = Path(__file__).resolve().parents[3]
BATCH = Path(__file__).resolve().parents[1]
prior = LOCAL / 'batches/418-native-fingerprint-runtime'
for folder in ['reports', 'tests', 'build']:
    (BATCH / folder).mkdir(exist_ok=True)
for source in (prior / 'tests').glob('*.java'):
    text = source.read_text(encoding='utf-8').replace('418', '419')
    if source.name == 'LoadedWeapon418.java':
        old = 'target.moveTo(0.0, -59.0, 7.0, 0.0F, 0.0F);'
        assert text.count(old) == 1
        text = text.replace(old, 'target.moveTo(0.0, -59.0, 3.0, 0.0F, 0.0F);')
        marker = '                        flight.put(§ownerIdentity§, arrow.getOwner() == restored);'.replace('§', chr(34))
        additional = '''                        flight.put(§launchPosition419§, arrow.position().toString());
                        flight.put(§launchVelocity419§, arrow.getDeltaMovement().toString());
                        flight.put(§targetBounds419§, target.getBoundingBox().toString());
'''.replace('§', chr(34))
        assert text.count(marker) == 1
        text = text.replace(marker, additional + marker)
        marker = '                        flight.put(§removedAfterFlight§, arrow.isRemoved()); arrows.add(flight); arrow.discard();'.replace('§', chr(34))
        additional = '''                        flight.put(§finalPosition419§, arrow.position().toString());
                        flight.put(§targetHealthAfterFlight419§, target.getHealth());
                        flight.put(§nativeImpactObserved419§, !observations.isEmpty());
'''.replace('§', chr(34))
        assert text.count(marker) == 1
        text = text.replace(marker, additional + marker)
        marker = '                    List<Object> publications = new ArrayList<>();'
        additional = '''                    if (sample == 0 && (observations.isEmpty() || target.getHealth() >= before)) {
                        row.put(§projectileFlightDiagnostic419§, arrows);
                        throw new AssertionError(§Native projectile did not damage the positive fixture target§);
                    }
'''.replace('§', chr(34))
        assert text.count(marker) == 1
        text = text.replace(marker, additional + marker)
    (BATCH / 'tests' / source.name.replace('418', '419')).write_text(text, encoding='utf-8')
for name in ['run', 'seal', 'diagnose']:
    text = (prior / 'tools' / (name + '418.py')).read_text(encoding='utf-8').replace('418', '419')
    (BATCH / 'tools' / (name + '419.py')).write_text(text, encoding='utf-8')
print({'prepared': True, 'productionCandidate': 417, 'fixtureTargetDistance': 3, 'nativeProjectileCollisionRequired': True, 'authorityFieldsWritten': False})
