ItemEvents.tooltip(event => {
  event.addAdvanced('farmingtales:wishing_well_core', (item, advanced, text) => {
    text.add('§7投下心愿，等待井水回应。')
    text.add('§a绿宝石许愿§7：手持右键，或投入井水。')
    text.add('§6个人累计投入 100 个金币锅§7，一次升满；升级用锅不抽奖。')
    text.add('§e满级后每个金币锅抽奖一次§7；空手右键商店。')
    text.add('§8潜行右键查看玩法简介。')
  })
})
