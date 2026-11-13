scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1076 151 62 minecraft:air if block 1076 152 62 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1076 151 62 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1093 151 54 minecraft:air if block 1093 152 54 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1093 151 54 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1088 151 72 minecraft:air if block 1088 152 72 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1088 151 72 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1080 151 52 minecraft:air if block 1080 152 52 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1080 151 52 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1085 151 59 minecraft:air if block 1085 152 59 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1085 151 59 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1073 151 70 minecraft:air if block 1073 152 70 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1073 151 70 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1082 151 67 minecraft:air if block 1082 152 67 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1082 151 67 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1091 151 64 minecraft:air if block 1091 152 64 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1091 151 64 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1077 151 62 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1077 151 62 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b203 ftm_brick 2
