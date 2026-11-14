'use strict';

const fs = require('node:fs');
const assert = require('node:assert/strict');

const roles = {
  'ftboss:sugar_queen': '远程施法与召唤协同的控场型Boss，战斗风格偏持续压制。',
  'ftboss:meat_mountain': '强调近身压迫与持久作战的重装近战型Boss。',
  'ftboss:tea_tree': '擅长持续施法与区域控制的植物系Boss。',
  'ftboss:wine_barrel': '兼具远程火力与近身爆发的重装混合型Boss。',
  'ftboss:rat_boss': '依靠鼠群协同形成包围与持续压力的召唤型Boss。',
  'ftboss:defect_core': '以机械火力与范围压制为特色的远程型Boss。',
  'ftboss:compost_boss': '以持续消耗与战场干扰为特色的区域压制型Boss。',
  'ftboss:apple_boss': '以灵活突进与范围爆发为特色的进攻型Boss。',
  'ftboss:benger': '以高速追击和近身压迫为特色的敏捷近战型Boss。',
  'ftboss:tide_fish_boss': '以潮汐施法和战场控制为特色的水系Boss。',
  'manor:0': '庄园中的重装近战成员，侧重正面承压与持续缠斗。',
  'manor:1': '庄园中的机动近战成员，侧重追击与空间控制。',
  'manor:2': '庄园中的远程火力成员，侧重持续射击与游击压制。',
  'manor:3': '庄园中的强攻近战成员，侧重正面压迫与连续进攻。',
  'manor:4': '庄园中的混合进攻成员，兼具近战压迫与召唤协同。',
  'manor:5': '庄园中的多面手成员，兼具近战、远程与持续作战能力。',
  'manor:6': '庄园中的机械远程成员，侧重高压火力与范围控制。',
  'manor:7': '庄园中的灵活游击成员，侧重机动、牵制与连续进攻。',
  'manor:8': '庄园中的持续消耗成员，侧重远程施法与区域压制。',
  'manor:9': '庄园中的团队支援成员，侧重保护与协助其他成员。',
  'manor:10': '庄园中的近战控制成员，侧重突进与战场压迫。',
  'manor:11': '庄园中的星光法师，侧重远程爆发与大范围压制。'
};

const labels = {
  zh_cn: {
    mechanics: 'Boss介绍', role: 'Boss定位', appearance: '返回外观页',
    reader_help: '简要定位介绍 · Esc返回外观',
    help: '左键拖动旋转 · 滚轮缩放',
    empty: '放入对应 Boss 的召唤物，查看外观与定位。这里只查看，不会开始战斗。',
    kills: '你的击杀：%s', no_summary: '该角色的定位介绍暂不可用。'
  },
  en_us: {
    mechanics: 'Boss Overview', role: 'Boss Role', appearance: 'Back to Model',
    reader_help: 'Brief role overview - Esc to return',
    help: 'Drag to rotate - Scroll to zoom',
    empty: 'Insert a boss summon to view its model and role. Viewing does not start a fight.',
    kills: 'Kills: %s', no_summary: 'A role overview is currently unavailable.'
  }
};

function summarizeCatalog(catalog) {
  return catalog.map(entry => {
    const key = entry.variant === undefined ? entry.entity : 'manor:' + entry.variant;
    const summary = roles[key];
    assert(summary, 'Missing boss role: ' + key);
    const result = { ...entry, summary, guide: [summary],
      sections: [{ id: 'overview', title: 'Boss定位', paragraphs: [summary] }] };
    delete result.coverage;
    return result;
  });
}

function applyLabels(language, locale) {
  for (const [key, value] of Object.entries(labels[locale])) language['ui.ftboss.preview.' + key] = value;
  return language;
}

module.exports = { roles, labels, summarizeCatalog, applyLabels };

if (require.main === module) {
  const path = require('node:path');
  const assets = path.resolve(__dirname, '../src/main/resources/assets/ftboss');
  const catalog = path.join(assets, 'boss_preview/catalog.json');
  fs.writeFileSync(catalog, JSON.stringify(summarizeCatalog(JSON.parse(fs.readFileSync(catalog, 'utf8'))), null, 2) + '\n');
  for (const locale of Object.keys(labels)) {
    const file = path.join(assets, 'lang', locale + '.json');
    fs.writeFileSync(file, JSON.stringify(applyLabels(JSON.parse(fs.readFileSync(file, 'utf8')), locale), null, 2) + '\n');
  }
  console.log('Generated brief roles for all 22 bosses and updated preview labels.');
}
