scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1602 151 115 minecraft:air if block 1602 152 115 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1602 151 115 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1618 151 129 minecraft:air if block 1618 152 129 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1618 151 129 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1617 151 109 minecraft:air if block 1617 152 109 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1617 151 109 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1598 151 129 minecraft:air if block 1598 152 129 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1598 151 129 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1608 151 124 minecraft:air if block 1608 152 124 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1608 151 124 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1614 151 118 minecraft:air if block 1614 152 118 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1614 151 118 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1598 151 109 minecraft:air if block 1598 152 109 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1598 151 109 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1607 151 110 minecraft:air if block 1607 152 110 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1607 151 110 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1602 151 114 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1602 151 114 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b228 ftm_brick 2
