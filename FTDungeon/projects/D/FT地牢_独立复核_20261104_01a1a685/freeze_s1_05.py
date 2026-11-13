from pathlib import Path

source=Path(__file__).with_name('freeze_s1_01.py').read_text(encoding='utf-8')
source=source.replace('nature-redo-b01-s1-01-20261104','nature-redo-b05-s1-05-20261105').replace('nature-redo-b01-s1-01','nature-redo-b05-s1-05').replace("folder=ROOT/'S1-01-independent'","folder=ROOT/'S1-05-independent'").replace('ca9efd56f98374bdb17a7154e8a8dc67e3d2d5578ad4be90a1734e2bdfebee7c','c5d7a612b19b3eea25c33bcc3cafacff820cdaf7242cc6abb74fe78471e6c38d')
exec(compile(source,__file__,'exec'))
