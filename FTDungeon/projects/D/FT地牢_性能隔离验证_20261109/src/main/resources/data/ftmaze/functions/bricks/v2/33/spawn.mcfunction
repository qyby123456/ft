scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 122 151 125 minecraft:air if block 122 152 125 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 122 151 125 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 134 151 134 minecraft:air if block 134 152 134 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 134 151 134 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 132 151 121 minecraft:air if block 132 152 121 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 132 151 121 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 123 151 134 minecraft:air if block 123 152 134 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 123 151 134 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 122 151 124 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 122 151 124 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b33 ftm_brick 2
