scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 94 151 122 minecraft:air if block 94 152 122 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 94 151 122 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 107 151 132 minecraft:air if block 107 152 132 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 107 151 132 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 109 151 119 minecraft:air if block 109 152 119 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 109 151 119 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 91 151 130 minecraft:air if block 91 152 130 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 91 151 130 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 102 151 125 minecraft:air if block 102 152 125 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 102 151 125 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 99 151 118 minecraft:air if block 99 152 118 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 99 151 118 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 96 151 127 minecraft:air if block 96 152 127 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 96 151 127 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 108 151 126 minecraft:air if block 108 152 126 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 108 151 126 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 94 151 123 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 94 151 123 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b29 ftm_brick 2
