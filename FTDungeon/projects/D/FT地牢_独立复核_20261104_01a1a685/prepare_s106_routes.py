from pathlib import Path

source=Path(__file__).with_name('prepare_s104_routes.py').read_text(encoding='utf-8').replace('S1-04-independent','S1-06-independent')
exec(compile(source,__file__,'exec'))
