import hashlib
import json
import shutil
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
NOTES = {
    ('interior', 'north'): '可见两处有边浅池、护栏、废轨及侧晶柱；中间干燥转角区域无家具占道。主晶台部分被前景柱梁遮挡，由东剖面补足检查。',
    ('interior', 'south'): '主紫晶群落在连续方解石/深板岩台座上；池沿和两组顶梁清晰，灯链接梁，主路保持同高。',
    ('interior', 'west'): '北门洞开放；六格废轨完整落地；柱脚与柱帽连贯。主晶群和池沿离开中央通路，柱梁造成局部遮挡。',
    ('interior', 'east'): '主晶台及其高低晶柱轮廓最清晰；两池分居侧面；修正后的木箱与晶芽分开，箱顶灯有完整木顶支撑。选作总预览。',
    ('exterior', 'north'): '北侧中央开放门洞及整块门槛完整；西侧封闭，洞顶与地基连续，无外露水体。',
    ('exterior', 'south'): '南侧围护封闭；东门洞位于相邻侧，符合转角端口；完整屋顶，无外伸晶体或水流。',
    ('exterior', 'west'): '西、南侧均封闭；整体围护和地基无孔洞，外壳为埋入地下的结构包络。',
    ('exterior', 'east'): '东侧中央开放出口可见，北口位于相邻侧；门框、门楣和洞顶完整，外边界无不当突出。'
}


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def main():
    manifest_path = ROOT / 'qa/render-manifest.json'
    manifest = json.loads(manifest_path.read_text(encoding='utf-8'))
    if manifest['sha256'] != digest(ROOT / 'room.nbt'):
        raise ValueError('Stale rendered views')
    shutil.copyfile(ROOT / 'preview/interior-east.png', ROOT / 'preview.png')
    manifest['preview_sha256'] = digest(ROOT / 'preview.png')
    manifest['preview_view'] = 'interior-east'
    manifest_path.write_text(json.dumps(manifest, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
    views = []
    for view in manifest['views']:
        if digest(ROOT / view['path']) != view['sha256']:
            raise ValueError('Changed view')
        views.append({**view, 'status': 'PASS', 'reviewed_individually': True, 'observation': NOTES[(view['mode'], view['view'])]})
    report = {'status': 'PASS', 'id': 'zone-C05', 'sha256': manifest['sha256'], 'reviewer': 'C05 producer / Codex; individually inspected all eight final images', 'views': views, 'reference_match': '保留深色矿洞砌筑、独立承重柱、蓝紫晶群、石台、浅水、护栏、灯链、木箱和废轨。', 'preview_view': 'interior-east', 'limitations': ['紫水晶为1.20.1方块转译，外观更方正；无概念图着色器蓝光。', '内剖面保留结构梁，部分角度有遮挡；四个方向联合检查。', '软件预览不等于客户端游戏截图；外壳是地下结构包络。'], 'producer_review_only': True, 'receiver_independent_acceptance_required': True}
    (ROOT / 'qa/visual-review.json').write_text(json.dumps(report, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')


if __name__ == '__main__':
    main()
