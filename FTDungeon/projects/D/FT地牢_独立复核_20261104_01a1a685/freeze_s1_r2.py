from pathlib import Path

source=Path(__file__).with_name('freeze_s1_01.py').read_text(encoding='utf-8')
source=source.replace('nature-redo-b01-s1-01-20261104','nature-redo-b01-s1-01-r2-20261105').replace("folder=ROOT/'S1-01-independent'","folder=ROOT/'S1-01-R2-independent'").replace('ca9efd56f98374bdb17a7154e8a8dc67e3d2d5578ad4be90a1734e2bdfebee7c','434148147cac7ae3a8156bc90f16765aa069784553966cb2fa46dae9eda9821d')
exec(compile(source,__file__,'exec'))
