// Multiblock Wishing Well controller.
// The surrounding vanilla blocks are validated by server_scripts/block_events/wishing_well.js.
StartupEvents.registry('block', event => {
  event.create('farmingtales:wishing_well_core', 'cardinal')
    .displayName('许愿井核心')
    .soundType('stone')
    .hardness(3.0)
    .resistance(6.0)
    .requiresTool(true)
    .tagBlock('minecraft:mineable/pickaxe')
    .tagBlock('minecraft:needs_stone_tool')
    .lightLevel(4)
    .box(0, 0, 0, 16, 4, 16)
    .box(3, 4, 3, 13, 8, 13)
    .model('farmingtales:block/wishing_well_core')
    .item(item => {
      item.rarity('uncommon')
    })
})
