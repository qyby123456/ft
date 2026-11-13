from pathlib import Path

source = Path(__file__).with_name('freeze_s1_01.py').read_text(encoding='utf-8')
source = source.replace('nature-redo-b01-s1-01-20261104', 'nature-redo-b07-s1-07-20261105').replace('nature-redo-b01-s1-01', 'nature-redo-b07-s1-07').replace("folder=ROOT/'S1-01-independent'", "folder=ROOT/'S1-07-independent'").replace('ca9efd56f98374bdb17a7154e8a8dc67e3d2d5578ad4be90a1734e2bdfebee7c', '8f693bd558354d9444e654300197c0f74b1b3870af9069d51897bb510de63133')
exec(compile(source, __file__, 'exec'))
