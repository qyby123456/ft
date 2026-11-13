from pathlib import Path

source = Path(__file__).with_name('prepare_s104_routes.py').read_text(encoding='utf-8').replace('S1-04-independent', 'S1-07-independent')
exec(compile(source, __file__, 'exec'))
source = Path(__file__).with_name('prepare_s407_open.py').read_text(encoding='utf-8').replace('S4-07-independent', 'S1-07-independent')
exec(compile(source, __file__, 'exec'))
