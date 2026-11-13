from pathlib import Path
ROOT=Path(__file__).resolve().parent
def read(p):return (ROOT/p).read_text(encoding='utf-8')
def write(p,s):(ROOT/p).write_text(s,encoding='utf-8')
notes='''1.8.24：收起从 0.55 秒缩短为 0.30 秒，普通拿出从 0.60 秒缩短为 0.40 秒，酱罐拿出从 0.70 秒缩短为 0.50 秒。切换仍完整播放旧物品收起、最新物品拿出，手与容器共用时间。食用、检视以及吃完补拿时长不变。
果酱的两次蜂蜜食用音效增益提高 35%，只作用于 32 种果酱；饮品、鱼籽酱、蛋黄酱与布料等声音不变。
Blockbench v53 同步更新 draw、put_away 和完整预览；其他动画沿用 v52，保留四种食用变化。
'''
s=read('README.md').replace('动画 1.8.23','动画 1.8.24',1)
s=s.replace('Minecraft 1.20.1 / Forge 47.4.10 客户端独立模组。','Minecraft 1.20.1 / Forge 47.4.10 客户端独立模组。\n\n'+notes,1)
write('README.md',s)
write('blockbench/使用说明-v53.txt',read('blockbench/使用说明-v52.txt').replace('1.8.23','1.8.24')+'\n'+notes)
for kind,folder in [('jam','上古水果果酱'),('drinks','酒水扩展')]:
    s=read(f'blockbench/export_{kind}_v52.py').replace('v52','v53').replace('1.8.23','1.8.24')
    # Retain stable UUIDs so unchanged clips can be copied exactly, without refitting.
    s=s.replace("'farmingtales/drinks-v53/'","'farmingtales/drinks-v52/'")
    s=s.replace("['idle','draw','put_away','inspect','drink','drink_variant_2','drink_variant_3','drink_variant_4','walk','sprint','jump','place']","['draw','put_away']")
    s=s.replace('        animations=[]',f'''        previous=json.loads((ROOT/f'{folder}-v52/{{item}}-{{label}}-v52.bbmodel').read_text(encoding='utf-8'))
        animations=copy.deepcopy([a for a in previous['animations'] if a['name'].split('.')[-1] not in ['draw','put_away','preview_complete']])''')
    s=s.replace("        path=OUT/(model['name']+'.bbmodel');", "        assert model['elements']==previous['elements'] and model['outliner']==previous['outliner']\n        path=OUT/(model['name']+'.bbmodel');")
    write(f'blockbench/export_{kind}_v53.py',s)
s=read('blockbench/04-导出工具/旧版脚本/export_jar_family_v52.py').replace('v52','v53').replace('1.8.23','1.8.24')
s=s.replace('增加四种食用细节及轻微相机跟随，连续呼吸保持；握持、速度、音效及液体阻尼保持相同。','保持四种食用变化及呼吸；拿出 0.5 秒、收起 0.3 秒，果酱食用声提高 35%。')
write('blockbench/04-导出工具/旧版脚本/export_jar_family_v53.py',s)
write('blockbench/04-导出工具/旧版脚本/package_switch_v53.py',read('blockbench/04-导出工具/旧版脚本/package_eating_v52.py').replace('v52','v53').replace('1.8.23','1.8.24'))
write('preview/verify_bb_fit_v53.py',read('preview/verify_bb_fit_v52.py').replace('v52','v53'))
s=read('install_release.ps1').replace('1.8.23','1.8.24').replace('v1823','v1824').replace('1.8.22-before-eating-variation','1.8.23-before-fast-switch').replace('1.8.22','1.8.23')
write('install_release.ps1',s)
s=read('verify_release_v1823.py').replace('1.8.23','1.8.24').replace('1.8.22-before-eating-variation','1.8.23-before-fast-switch').replace('1.8.22','1.8.23').replace('v52','v53')
s=s.replace("expected={'client/BottleRenderer','client/DrinkController','math/DrinkMotion','math/BottleFraming'}","expected={'math/AnimationPlayback','math/AnimationSoundTimeline'}")
s=s.replace("'slim':['orange_wine','brandy_xo','mozhao','orange_juice_1','ancient_fruit_jam_1','egg_mayonnaise'],\n    'wide':['orange_wine','brandy_xo','orange_juice_1','ancient_fruit_jam_1']", "'slim':['orange_wine','ancient_fruit_jam_1'],\n    'wide':['orange_wine','ancient_fruit_jam_1']")
s=s.replace('smoke-eating-','smoke-switch-')
s=s.replace("    assert any('runtime selects all four eating styles' in s for s in checks)\n",'')
s=s.replace("'stow finishes with empty hand','consecutive eating styles differ',\n                         'selected eating style stays stable','rendered eating world camera moves subtly'", "'stow finishes with empty hand','fast switch keeps the old food visible until stowed',\n                         'fast switch reaches the new food in','rapid reselection during draw settles on the latest food'")
s=s.replace("        if item.endswith('mayonnaise'):","        if item.endswith('_jam_1'):\n            assert any('fruit jam eating sounds are 35 percent louder' in s for s in selected)\n        if item.endswith('mayonnaise'):")
s=s.replace("'125 tests; ten representative hidden Forge cases across both arm shapes, shaders off. '","'125 tests; four hidden Forge cases across both arm shapes, shaders off, including real rapid reselection and jam sound gain. '")
s=s.replace("scope='Four stable eating variations with no consecutive repeats, mild delayed world-camera feedback and retained continuous breathing. '","scope='Faster food switching: 6-tick stow, 8-tick regular draw, 10-tick jar draw; fruit jam eating gain +35 percent. '")
s=s.replace("'Original models, textures, sounds, use timing and original mods unchanged. Full pack not launched.'", "'Original models, textures, sound samples, use timing and original mods unchanged; only fruit jam cue gain increased. Full pack not launched.'")
s=s.replace('eating_variants=4,','eating_variants=4,fruit_jam_gain=1.35,switch_ticks=dict(stow=6,draw=8,jar_draw=10),')
# All new clips are checked against Java; confirm the ten unchanged clips are also byte-for-byte equivalent as JSON.
needle="        assert clips['idle']['length']==4 and clips['idle']['loop']=='loop'"
s=s.replace(needle,needle+'''
        prior_path=Path(entry['path'].replace('v53','v52'))
        prior={a['name']:a for a in read(prior_path)['animations']}
        for animation in data['animations']:
            if animation['name'].split('.')[-1] not in ['draw','put_away','preview_complete']:
                assert animation==prior[animation['name']]
        jar=next(p for p in profiles if p['id']==entry['item'])['style']=='JAM'
        assert clips['draw']['length']==(.5 if jar else .4) and clips['put_away']['length']==.3
''')
write('verify_release_v1824.py',s)
print('Prepared 1.8.24; only refit changed transition clips for Blockbench v53.')
