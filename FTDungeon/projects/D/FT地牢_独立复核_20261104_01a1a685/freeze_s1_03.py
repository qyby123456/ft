from pathlib import Path

source=Path(__file__).with_name('freeze_s1_01.py').read_text(encoding='utf-8')
source=source.replace('nature-redo-b01-s1-01-20261104','nature-redo-b03-s1-03-20261105').replace('nature-redo-b01-s1-01','nature-redo-b03-s1-03').replace("folder=ROOT/'S1-01-independent'","folder=ROOT/'S1-03-independent'").replace('ca9efd56f98374bdb17a7154e8a8dc67e3d2d5578ad4be90a1734e2bdfebee7c','c135fdaeb27306b9a018dca393f00c21225d7dcb7595341b6a2f466f6deac2d3')
exec(compile(source,__file__,'exec'))
