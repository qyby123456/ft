import json
import subprocess
import sys
from pathlib import Path
import audit

ROOT=Path(__file__).resolve().parent
CASES=[
    (2,3,'S2-03-independent','e15d04c1769ebc03bc28fb516f14fe8eb5b699cb3bcc1b762db25370fa68beca','FT地牢_第2组重做B_20261104/S2-03','incoming/G2B-03/room.nbt','incoming/G2B-03/preview.png'),
    (3,4,'S3-04-independent','37ad90011ed98a49c46494174dd3e1810e8c10c536ddab59ac0ae507fe99a8a3','FT地牢_第3组重做B_20261104/S3-04','room.nbt','preview.png'),
    (4,3,'S4-03-R1-independent','a0b37f5e493403716ed417c0c8e3cafebb7adc2d17ba31da085fb05643fd45a5','FT地牢_第4组逐图返工_20261104/S4-03','incoming/S4-03/room.nbt','preview-default.png'),
    (6,3,'S6-03-independent-final','5b04775cf2137a4f668cef09b261ef46341f582842dcfd35a8757f6f33cf5ce7','FT地牢_Image25接入_20261101/deliveries/S6-redo-single-20261104-01/03-small-room','room.nbt','lodestone/lodestone-interior-cutaway.png'),
    (7,4,'S7-04-independent','1dc48874bc84275ba00b0156c17f7a8b5ff00a08e6cd2e17d7670fb5cca99678','FT地牢_第7组逐图重做_20261104/04-great-hall','incoming/S7R2-04/room.nbt','lodestone-interior.png'),
    (8,4,'S8-04-independent','30e2cb79d231714c9facf8847474db7775a185172d411fc6d04bcd2ac76a1c3b','FT地牢_Image25接入_20261101/deliveries/S8-rebuild-single-20261104-04','incoming/S8R_IMG04/room.nbt','views/north/interior-north.png')
]
if __name__=='__main__':
    for group,image,label,expected,relative,candidate,preview in CASES:
        if (ROOT/label/'room.nbt').is_file():
            assert audit.digest(ROOT/label/'room.nbt')==expected
            continue
        subprocess.run([sys.executable,str(ROOT/'freeze_revision.py'),str(group),expected,label,relative,preview,candidate],check=True)
