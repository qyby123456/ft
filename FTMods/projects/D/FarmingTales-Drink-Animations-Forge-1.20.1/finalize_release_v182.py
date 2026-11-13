from pathlib import Path
import zipfile
root=Path(__file__).absolute().parent
folder=root/'blockbench/酒水扩展-v32'
with zipfile.ZipFile(root/'blockbench/酒水扩展-v32.zip','w',zipfile.ZIP_DEFLATED,compresslevel=6) as z:
    for f in sorted(folder.iterdir()):
        if f.is_file():z.write(f,f.name)
s=(root/'install_release.ps1').read_text(encoding='utf-8-sig')
if "$animVersion = '1.8.1'" in s:
    s=s.replace('1.8.1','1.8.2').replace('1.8.0','1.8.1').replace('1.8.1-before-motion-refinement','1.8.1-before-surface-fix')
    (root/'install_release.ps1').write_text(s,encoding='utf-8-sig')
(root/'verify_release.py').write_text('from verify_release_v182 import *\n',encoding='utf-8')
print('Prepared archive and installer.')
