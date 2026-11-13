scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 98 151 130 minecraft:air if block 98 152 130 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 98 151 130 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 109 151 127 minecraft:air if block 109 152 127 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 109 151 127 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 94 151 138 minecraft:air if block 94 152 138 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 94 151 138 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 103 151 136 minecraft:air if block 103 152 136 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 103 151 136 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 91 151 127 minecraft:air if block 91 152 127 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 91 151 127 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 102 151 126 minecraft:air if block 102 152 126 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 102 151 126 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 97 151 135 minecraft:air if block 97 152 135 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 97 151 135 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 105 151 129 minecraft:air if block 105 152 129 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 105 151 129 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 99 151 130 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 99 151 130 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b29 ftm_brick 2
