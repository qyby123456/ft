'use strict';

const fs = require('fs');
const path = require('path');
const assert = require('assert/strict');
const { ordinary } = require('./boss_preview_native.cjs');
const manor = require('./boss_preview_manor.cjs');
const { summarizeCatalog, applyLabels } = require('./boss_preview_summaries.cjs');
const root = path.resolve(__dirname, '..');
const factsFile = process.argv[2];
const languageFile = process.argv[3];
if (!factsFile || !languageFile) throw new Error('Usage: node tools/gen_boss_preview_guide.cjs <runtime-facts.json> <runtime-zh_cn.json>');
const facts = JSON.parse(fs.readFileSync(factsFile, 'utf8'));
const language = JSON.parse(fs.readFileSync(languageFile, 'utf8'));
const assets = path.join(root, 'src/main/resources/assets/ftboss');
const catalogFile = path.join(assets, 'boss_preview/catalog.json');
const catalog = JSON.parse(fs.readFileSync(catalogFile, 'utf8'));
assert.equal(catalog.length, 22);
assert.equal(facts.domains.length, 12);
assert.equal(facts.domainRadius, 76);
const section = (id, title, paragraphs) => ({ id, title, paragraphs });
const supportIds = ['effigy_ward', 'crimson_oath', 'hearth_feast', 'halo_sanctuary', 'wingstep', 'tidal_lotus', 'feather_refuge'];
const spellParagraph = id => {
  const name = language['spell.ftboss.' + id];
  const effect = language['spell.ftboss.' + id + '.effect'];
  assert(name && effect, 'Runtime spell description missing: ' + id);
  return '【' + name + '】' + effect;
};
const effects = [
  '伤害与数值：技能说明中的半径、持续时间、触发比例描述规则；基础伤害还会受难度、人数、攻击/法术属性、护甲和抗性影响，不等于玩家最终损失的生命。1点生命为半颗心。',
  '缓慢、虚弱、挖掘疲劳：分别降低移动速度、近战攻击能力、挖掘速度；缓慢不等于完全定身，虚弱也不等于降低所有法术伤害。',
  '锚定眩晕：限制行动/位移；与普通缓慢区分。击飞、推离、牵引会改变位置，可能把玩家送入下一段范围。',
  '中毒、凋零、流血、蓝火：不同来源的持续损血效果。流血可叠层；蓝火不只是普通火焰贴图，不能把常规防火当成保证免疫。',
  '暗影侵蚀/深黯侵蚀：暗影系持续压制，可包含生命比例或虚空伤害；普通护甲、防火或牛奶不能保证完全应对。',
  '护甲撕裂：降低护甲，增加后续承伤风险；不直接等于禁疗。发光：提供可见标记，部分金果/星光法术会消耗它触发增伤或护盾。',
  '失明、黑暗、反胃：主要干扰视野与观察，不应写成同一个定身效果。饥饿：提高饱食消耗，要留意恢复能力与食物库存。',
  '禁疗：拦截生命恢复，不是只降低治疗25%；不同来源有3秒、5秒或30秒等持续时间，同源再命中可刷新。查看状态图标，别靠不断吃治疗物硬撑。',
  '伤害吸收与抗性：吸收先承受伤害，抗性降低适用伤害；护盾不等于永久无敌。回血、吸血、净化、承伤分摊也分别是不同机制。'
];
const normalLimits = [
  '本图鉴：放入召唤物只是查看，不会召唤Boss、不消耗物品；切换外观/介绍页保留同一容器，关闭时归还查看物品，背包满则按原版容器规则掉落。',
  '真实召唤：玩家召唤入口只允许主世界；下界、末地及其它维度拒绝，不消费召唤物。图鉴查看与真实召唤不是同一操作。',
  '场地准备：为冲锋、弹幕和落点攻击留出平整开阔空间；地形遮挡是否阻断攻击按具体技能计算，不保证所有法术都能靠墙免疫。',
  '破困：Boss会清理卡住身体的船、矿车及坐垫/座椅类困人方块、蜘蛛网、细雪等指定对象，通常不掉落；不要把家具困杀当作稳定解法。这不等于禁止玩家放置或破坏所有方块。',
  '同场目标：Boss及其同阵营召唤物不互相作为普通敌方攻击目标；创意/旁观模式不等于生存模式参战体验。'
];
const manorLimits = [
  '图鉴权限：将“甜苹果庄园”设为当前佩戴头衔，再把“血腥契约”放入石像；仅已解锁但未佩戴不满足。查看不启动战斗、不消费契约，取消佩戴后预览权限也会失效。',
  '真实挑战：召唤/挑战入口只允许主世界，正式庄园按11名战斗成员轮换；麻雀是支援，不占一个正式轮换。场内实体按本场身份管理，不会把围观路人自动补进参战名单。',
  '全程禁飞：战斗进行期间，战场锚点水平96格范围内禁止飞行与鞘翅；登记参战者即使跑到这个圆外仍受禁飞。创意/旁观豁免。不是旧版“120秒未命中后临时禁飞60秒”。',
  '召回边界：正式参战者与当前Boss超过76格时有5秒倒计时，68格开始提前警告；跨维度或持续失去目标也可触发召回。比较玩家与Boss离战场锚点的距离，把更偏离者拉回另一方，不是所有路人都被拉。',
  '领域与边界不同：战斗领域以战场锚点为中心、水平半径76格；禁飞圈96格、蛋蛋珍珠封锁48格各自独立，不能共用一个“领域范围”解释。',
  '禁用法术：黑洞 black_hole、止心术 heartstop、深渊庇佑 abyssal_shroud 在普通与终结挑战都不能新施放；不把该规则扩写成删除玩家所有历史增益。',
  '指定禁食效果：两档都清除“饱腹代偿” kaleidoscope_cookery:satiated_shield。终结额外清除“滋养” farmersdelight:nourishment、“饱腹” farm_and_charm:satiation，并禁止橡肤 oakskin；普通并不统一清除这两种食物增益或橡肤。',
  '破困与地形：船、矿车、坐垫/座椅类困人方块、蜘蛛网、细雪等可被Boss清理且不掉落；这不是全场禁止放置/破坏所有方块。建议选择平整开阔场地，别依赖家具、门墙把Boss永久卡住。',
  '场地生命周期：正式战斗锚点与参战身份随存档保存，活动区域维持加载；场地不安全时战斗可安全搬迁。领域、召回和禁飞随后使用本场有效位置，战斗结束不应残留常驻限制。'
];
const manorCommonSupport = [
  '共通增益复制：约每15秒复制合格的玩家正面效果，持续30秒；排除伤害吸收、生命提升、修改最大生命的效果以及 herbalbrews:renewal。复制不等于默认把玩家身上的原效果全部偷走。',
  '连锁增益：彩蛋Boss获得合格正面效果时可额外获得2种随机增益，并给附近战斗目标1种随机负面；有冷却和递归防护，不能解释成无限自触发。',
  '麻雀支援：支援对象每3秒恢复最大生命0.5%并获得3种随机增益，5秒一次净化，1秒一次双向共享正面效果；麻雀承担目标50%来袭伤害。复制玩家增益相关触发有10%概率造成30秒禁疗。',
  '麻雀登场：普通单人整场计划支援4至10个阶段，多人增加每位Boss额度和额外机会；全局同时最多一只。普通基础生命250，终结基础生命1000，终结由额外跨场支援逻辑管理。',
  '指定剥除与普通buff：当前没有“所有Boss统一清除玩家全部长期增益”的规则；蛋蛋、紫菜、文玄蓝等特定招式的剥除，以及指定禁食效果，是独立限制。'
];
const difficulty = [
  '普通与终结：选择难度会改变攻击、移动、部分恢复和支援行为；不要把图鉴法术卷轴的固定说明当成所有难度下Boss最终伤害。',
  '多人缩放：正式参战人数影响Boss强度和麻雀支援额度；只围观不等于被登记为参战。',
  '终结特殊限制：额外禁用橡肤，清除滋养、饱腹；基础攻击与移速强化，麻雀基础生命1000。终结难度提示为“该难度几乎无法通过而且不会获取成就”。',
  '最终判定：以正式战斗阶段、当前目标和通关提示为准；迷你分身、演出体、普通助手或麻雀死亡不等于整场完成。'
];

for (const [index, entry] of catalog.entries()) {
  const isManor = index >= 10;
  const data = isManor ? manor[entry.variant] : ordinary[index];
  assert(data && data.skills.length, 'Native skills missing: ' + entry.name);
  const entity = entry.entity.split(':')[1];
  const spellIds = isManor ? facts.manorSpells.filter(spell => spell.variant === entry.variant).map(spell => spell.id)
    : facts.bossSpells.filter(spell => spell.boss === entity).map(spell => spell.spell);
  const nativeSkills = isManor ? facts.manorSkills.filter(skill => skill.variant === entry.variant).map(skill => skill.title) : [];
  for (const title of nativeSkills) assert(data.skills.some(paragraph => paragraph.includes(title)), 'Native skill undocumented: ' + title);
  const borrowed = isManor && entry.variant === 11 ? supportIds : [];
  const spellNotes = spellIds.length ? ['以下列出本角色全部独立特色法术。范围、段数和状态取当前运行包；同源玩家卷轴与Boss施法的最终威力会受各自倍率影响。', ...spellIds.map(spellParagraph)]
    : ['本角色未注册独立Boss特色法术组；原生主动技能均在“主动技能”章节列出，不虚构同名卷轴技能。'];
  if (isManor && entry.variant === 9) spellNotes.unshift('麻雀实际战斗只使用支援法术；千羽散华是同源法术资料，并非支援麻雀主动攻击玩家的技能。');
  const support = [...data.support, ...(isManor ? manorCommonSupport : [])];
  if (borrowed.length) support.push('祁崽两门辅助法术从下列完整池中选取，不是每次全部释放：', ...borrowed.map(spellParagraph));
  const sections = [section('overview', '基础与阶段', data.basic), section('skills', '主动技能', data.skills), section('spells', '特色法术', spellNotes)];
  if (isManor) {
    const domain = facts.domains.find(value => value.variant === entry.variant);
    const paragraphs = entry.variant === 9 ? ['麻雀不拥有独立战斗领域，不参与正式Boss轮换；支援出现时，领域仍属于当前登场的战斗Boss。'] : [
      '【' + domain.name + '】类型：' + domain.kind + '。' + domain.description,
      '范围：以本场战场锚点为中心，水平半径' + facts.domainRadius + '格；不是跟随本体移动的球形范围。只有当前有效登场的活体Boss维持本场领域，迷你分身、隐藏入场演出体不单独开启领域。',
      '切换：Boss退场、阶段轮换、战斗结束时原领域失效；场地搬迁后按新的有效锚点判断。只有处于有效圈内且符合本场目标条件才获得对应效果。',
      '结算：攻击属性、护甲、击退抗性、移速、承伤比例、回血是不同维度；明确写“Boss受益”的部分不提供给玩家。“双方共享”也不意味着玩家对其它世界目标无限享受同样收益。'
    ];
    sections.push(section('domain', '领域效果', paragraphs));
  }
  sections.push(section('support', '支援与恢复', support), section('arena', '场地与限制', [...(isManor ? manorLimits : normalLimits), ...(data.limits || [])]));
  if (isManor) sections.push(section('difficulty', '难度与多人', difficulty));
  sections.push(section('effects', '状态效果', effects), section('strategy', '应对要点', data.tips));
  entry.sections = sections;
  entry.guide = [data.basic[0], data.skills[0], data.support[0], data.tips[0]];
  entry.coverage = { spellIds, nativeSkills, borrowedSupportSpellIds: borrowed, referenceSHA256: facts.baselineSHA256 };
}
fs.writeFileSync(catalogFile, JSON.stringify(summarizeCatalog(catalog), null, 2) + '\n');
for (const locale of ['zh_cn', 'en_us']) {
  const filename = path.join(assets, 'lang/' + locale + '.json');
  const target = JSON.parse(fs.readFileSync(filename, 'utf8'));
  const labels = locale === 'zh_cn' ? {
    mechanics: '技能机制介绍', appearance: '返回外观页', reader_help: '左侧章节 · 滚轮 / 方向键翻页 · Tab切章节 · Esc返回外观',
    help: '左键拖动旋转 · 滚轮缩放 · 点击技能机制介绍打开独立页面'
  } : {
    mechanics: 'Skills & Mechanics', appearance: 'Appearance', reader_help: 'Chapters at left · Wheel / arrows: pages · Tab: chapter · Esc: appearance',
    help: 'Drag to rotate · Wheel to zoom · Open Skills & Mechanics for the separate guide'
  };
  for (const [key, value] of Object.entries(labels)) target['ui.ftboss.preview.' + key] = value;
  fs.writeFileSync(filename, JSON.stringify(applyLabels(target, locale), null, 2) + '\n');
}
console.log('Generated ' + catalog.length + ' brief boss role introductions; skill and strategy chapters are not published.');
