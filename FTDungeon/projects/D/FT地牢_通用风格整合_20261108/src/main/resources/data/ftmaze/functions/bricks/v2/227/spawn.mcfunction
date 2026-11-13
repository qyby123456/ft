scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1601 151 135 minecraft:air if block 1601 152 135 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1601 151 135 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1618 151 148 minecraft:air if block 1618 152 148 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1618 151 148 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1616 151 130 minecraft:air if block 1616 152 130 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1616 151 130 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1598 151 148 minecraft:air if block 1598 152 148 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1598 151 148 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1608 151 143 minecraft:air if block 1608 152 143 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1608 151 143 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1606 151 129 minecraft:air if block 1606 152 129 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1606 151 129 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1611 151 135 minecraft:air if block 1611 152 135 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1611 151 135 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1616 151 140 minecraft:air if block 1616 152 140 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1616 151 140 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1602 151 135 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1602 151 135 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b227 ftm_brick 2
