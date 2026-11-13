from pathlib import Path

source=Path(__file__).with_name('freeze_s1_05.py').read_text(encoding='utf-8').replace('b05-s1-05','b06-s1-06').replace('S1-05-independent','S1-06-independent').replace('c5d7a612b19b3eea25c33bcc3cafacff820cdaf7242cc6abb74fe78471e6c38d','07bc6264e9e02738a2c55e5738abcd09122d2b118dba307fe6c5bf2bda4326e7')
exec(compile(source,__file__,'exec'))
