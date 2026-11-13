scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1027 31 76 minecraft:air if block 1027 32 76 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1027 31 76 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1042 31 88 minecraft:air if block 1042 32 88 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1042 31 88 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1041 31 68 minecraft:air if block 1041 32 68 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1041 31 68 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1022 31 88 minecraft:air if block 1022 32 88 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1022 31 88 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1022 31 68 minecraft:air if block 1022 32 68 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1022 31 68 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1035 31 81 minecraft:air if block 1035 32 81 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1035 31 81 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1029 31 85 minecraft:air if block 1029 32 85 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1029 31 85 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1030 31 69 minecraft:air if block 1030 32 69 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1030 31 69 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1026 31 76 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1026 31 76 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b202 ftm_brick 2
