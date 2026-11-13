from pathlib import Path

source=Path(__file__).with_name('check_s5_s6_04.py').read_text(encoding='utf-8')
header=source[:source.index("folder=ROOT/'S6-04-independent'")]
body=source[source.index("folder=ROOT/'S5-04-independent'"):]
body=body.replace('S5-04-independent','S5-08-independent-r1').replace('04-coral-processional-hall','08-coral-skylight-courtyard').replace("worldtest/{name}","worldtest-r2/{name}")
body=body.replace('doors=[((17,3,0),(0,0,-1),7),((17,3,42),(0,0,1),7),((0,3,20),(-1,0,0),5),((34,3,20),(1,0,0),5)]','doors=[((17,3,0),(0,0,-1),5),((0,3,18),(-1,0,0),5),((34,3,18),(1,0,0),5)]')
body=body.replace('width,depth=35,43','width,depth=35,35').replace('(35,43) if turn%2==0 else (43,35)','(35,35)').replace('range(3,20)','range(3,22)').replace('range(20,28)','range(22,30)').replace('four_gates_source_and_outside_3deep','three_gates_source_and_outside_3deep')
body=body[:body.index("print('S6-04")]+"print('S5-08 four-rotation source ring, cover, bed and three gates PASS')\n"
exec(compile(header+body,__file__,'exec'))
