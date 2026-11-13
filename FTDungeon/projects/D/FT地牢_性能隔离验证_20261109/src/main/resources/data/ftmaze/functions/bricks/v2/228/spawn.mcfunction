scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1601 151 100 minecraft:air if block 1601 152 100 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1601 151 100 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1618 151 115 minecraft:air if block 1618 152 115 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1618 151 115 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1617 151 95 minecraft:air if block 1617 152 95 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1617 151 95 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1598 151 115 minecraft:air if block 1598 152 115 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1598 151 115 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1608 151 109 minecraft:air if block 1608 152 109 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1608 151 109 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1611 151 101 minecraft:air if block 1611 152 101 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1611 151 101 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1606 151 95 minecraft:air if block 1606 152 95 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1606 151 95 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1616 151 106 minecraft:air if block 1616 152 106 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1616 151 106 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1602 151 100 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1602 151 100 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b228 ftm_brick 2
