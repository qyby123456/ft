from pathlib import Path

source=Path(__file__).with_name('freeze_s1_01.py').read_text(encoding='utf-8')
source=source.replace('nature-redo-b01-s1-01-20261104','nature-redo-b02-s1-02-20261105').replace('nature-redo-b01-s1-01','nature-redo-b02-s1-02').replace("folder=ROOT/'S1-01-independent'","folder=ROOT/'S1-02-independent'").replace('ca9efd56f98374bdb17a7154e8a8dc67e3d2d5578ad4be90a1734e2bdfebee7c','f541344aa8a3a2f3cc47d1f4580efd9a06036b60bf5bfd43ca6b4ef6858467c2')
exec(compile(source,__file__,'exec'))
