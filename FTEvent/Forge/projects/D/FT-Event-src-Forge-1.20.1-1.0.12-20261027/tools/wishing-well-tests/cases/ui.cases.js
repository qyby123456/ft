section('UI01 concise interaction guide without construction or blueprint text');
{
  const candidate = create(); load(candidate); world(candidate);
  const actor = player(candidate, { uuid: 'ui-guide', shift: true });
  candidate.sandbox.ftwwShowGuide(actor);
  eq(actor.messages.some(message => /蓝图|搭建|xyj\.nbt|Y[+\-]?\d/.test(message)), false, 'shift-right-click no longer prints construction instructions');
  eq(actor.messages.some(message => message.includes('64 个绿宝石块')), true, 'guide retains the actual unlock condition');
  eq(actor.messages.some(message => message.includes('空手右键')), true, 'guide retains the shop interaction');
  eq(actor.messages.length <= 6, true, 'guide is short including progress');
}
section('UI02 redrawn client layout preserves server product slots and pricing');
{
  const candidate = manorShopSetup(); openShopGui(candidate);
  eq(candidate.p.gui.title, '许愿井 Lv.3 · 左键购买', 'client skin recognizes the unchanged server menu title');
  eq(candidate.p.gui.rows, 4, 'native four-row menu remains compatible');
  eq(candidate.p.gui.buttons.length, 14, 'all thirteen products and progress marker remain');
  const summons = Array.from(K(candidate, 'FTWW_SUMMONS'));
  for (let index = 0; index < summons.length; index++) {
    const button = candidate.p.gui.buttons.find(entry => entry.stack.id === summons[index]);
    eq(button.x + button.y * 9, index + 9, 'summon keeps its server slot: ' + summons[index]);
    eq(button.text.getString().includes('2 金币锅'), true, 'summon price displays two pots');
  }
  const weapon = candidate.p.gui.buttons.find(button => button.text.getString().includes('5000 万'));
  const spell = candidate.p.gui.buttons.find(button => button.text.getString().includes('500 万'));
  eq(weapon.x + weapon.y * 9, 30, 'weapon card targets server slot 30');
  eq(spell.x + spell.y * 9, 32, 'spell card targets server slot 32');
}
