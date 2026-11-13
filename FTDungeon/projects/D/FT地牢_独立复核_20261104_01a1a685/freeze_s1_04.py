from pathlib import Path

source=Path(__file__).with_name('freeze_s1_01.py').read_text(encoding='utf-8')
source=source.replace('nature-redo-b01-s1-01-20261104','nature-redo-b04-s1-04-20261105').replace('nature-redo-b01-s1-01','nature-redo-b04-s1-04').replace("folder=ROOT/'S1-01-independent'","folder=ROOT/'S1-04-independent'").replace('ca9efd56f98374bdb17a7154e8a8dc67e3d2d5578ad4be90a1734e2bdfebee7c','a7b46fb3d1e71bb41f743acdc3e72412c58b34a98d2e771805803697d5340265')
exec(compile(source,__file__,'exec'))
