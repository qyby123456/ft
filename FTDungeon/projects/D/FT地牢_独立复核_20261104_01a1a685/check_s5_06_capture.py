from pathlib import Path

source=Path(__file__).with_name('check_s5_s6_04.py').read_text(encoding='utf-8')
header=source[:source.index("folder=ROOT/'S6-04-independent'")]
body=source[source.index("folder=ROOT/'S5-04-independent'"):]
body=body.replace('S5-04-independent','S5-06-independent').replace('04-coral-processional-hall','06-coral-lantern-corner')
body=body.replace("doors=[((17,3,0),(0,0,-1),7),((17,3,42),(0,0,1),7),((0,3,20),(-1,0,0),5),((34,3,20),(1,0,0),5)]","doors=[((0,3,24),(-1,0,0),5),((24,3,0),(0,0,-1),5)]")
body=body.replace('width,depth=35,43','width,depth=31,31').replace('(35,43) if turn%2==0 else (43,35)','(31,31)').replace('range(3,20)','range(3,18)').replace('range(20,28)','range(18,26)').replace('four_gates_source_and_outside_3deep','two_gates_source_and_outside_3deep')
body=body[:body.index("print('S6-04")]+"print('S5-06 four-rotation source ring, cover, bed and two gates PASS')\n"
exec(compile(header+body,__file__,'exec'))
