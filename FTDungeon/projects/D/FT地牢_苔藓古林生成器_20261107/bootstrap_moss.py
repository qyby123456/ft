from pathlib import Path
import hashlib
import json

root = Path(__file__).resolve().parent
source = root / 'src/main/java/eclipse/generator'
target = root / 'src/main/java/moss/generator'
target.mkdir(parents=True, exist_ok=False)
for path in source.glob('*.java'):
    text = path.read_text(encoding='utf-8').replace('eclipse.generator', 'moss.generator').replace('fteclipsegen', 'ftmossgen').replace('EclipseMod', 'MossMod')
    (target / path.name.replace('EclipseMod', 'MossMod')).write_text(text, encoding='utf-8')
installed = Path('C:/Users/YOUR_USERNAME/AppData/Roaming/.minecraft/versions/FarmingTales_ForgeⅡ v1.5.2/mods/FT地牢-Forge-1.20.1-0.24.2.jar')
report = {'baselineVersion': '0.24.2', 'baselineJar': str(installed), 'baselineSha256': hashlib.sha256(installed.read_bytes()).hexdigest(), 'sourceProject': 'D:/ft工程文件/FT地牢_三生成器合并_20261106', 'theme': '苔藓古林', 'provider': 'ftmossgen:forest', 'status': 'IMPLEMENTING', 'playerWorldsModified': False}
(root / 'baseline.json').write_text(json.dumps(report, ensure_ascii=False, indent=2), encoding='utf-8')
