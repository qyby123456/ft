from pathlib import Path

source=Path(__file__).with_name('freeze_s1_01.py').read_text(encoding='utf-8')
source=source.replace('nature-redo-b01-s1-01-20261104','nature-redo-b01-s1-01-r3-20261105').replace("folder=ROOT/'S1-01-independent'","folder=ROOT/'S1-01-R3-independent'").replace('ca9efd56f98374bdb17a7154e8a8dc67e3d2d5578ad4be90a1734e2bdfebee7c','1174e5ae0e8824d1f0ab4ff78498fe7ffbe71a1561603145c7d69c5894e5ddea')
exec(compile(source,__file__,'exec'))
