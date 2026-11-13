from pathlib import Path
import json,shutil
import prepare_papercraft_profiles as gen
r=Path('\\\\?\\'+str(Path.cwd()));out=r/'preview/pineapple-generator-check';a=out/'src/main/resources/assets/farmingtales_drink_anim';a.mkdir(parents=True,exist_ok=True)
(a/'textures/papercraft').mkdir(parents=True,exist_ok=True)
for name in ('drink_profiles','decorations','hand_volumes'):
 shutil.copy2(r/f'src/main/resources/assets/farmingtales_drink_anim/{name}.json',a/f'{name}.json')
(r/'preview/pineapple-generator-check/src/main/resources/assets/minecraft/atlases').mkdir(parents=True,exist_ok=True)
shutil.copy2(r/'src/main/resources/assets/minecraft/atlases/blocks.json',out/'src/main/resources/assets/minecraft/atlases/blocks.json')
pack=r.parents[2]
gen.ROOT=out;gen.ASSETS=a;gen.PACK=pack;gen.SPECS={'ferry_whiskey':gen.SPECS['ferry_whiskey']};gen.main()
current=r/'src/main/resources/assets/farmingtales_drink_anim';key='papercraft_magic_decoration:ferry_whiskey'
for name in ('servings','decorations'):
 assert json.loads((a/f'{name}.json').read_text())[key]==json.loads((current/f'{name}.json').read_text())[key],name
for name in ('ferry_whiskey','ferry_whiskey_serving','ferry_whiskey_stick_1'):
 f=f'models/animation/papercraft/{name}.json'
 assert json.loads((a/f).read_text())==json.loads((current/f).read_text()),name
for file in (a/'textures/papercraft').glob('*.png'):
 assert file.read_bytes()==(current/'textures/papercraft'/file.name).read_bytes(),file
print('Generator reproduces coconut shell, fruit stages, straw, servings and original textures.')
