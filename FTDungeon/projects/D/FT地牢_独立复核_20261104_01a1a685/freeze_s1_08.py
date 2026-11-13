from pathlib import Path

source = Path(__file__).with_name('freeze_s1_01.py').read_text(encoding='utf-8').replace('nature-redo-b01-s1-01-20261104','nature-redo-b08-s1-08-20261105').replace('nature-redo-b01-s1-01','nature-redo-b08-s1-08').replace("folder=ROOT/'S1-01-independent'","folder=ROOT/'S1-08-independent'").replace('ca9efd56f98374bdb17a7154e8a8dc67e3d2d5578ad4be90a1734e2bdfebee7c','bfd3cbcda1e46ae8bf941c15a85b655b006b322d562e692fabf30f8958700463')
exec(compile(source,__file__,'exec'))
source = Path(__file__).with_name('prepare_s104_routes.py').read_text(encoding='utf-8').replace('S1-04-independent','S1-08-independent')
exec(compile(source,__file__,'exec'))
