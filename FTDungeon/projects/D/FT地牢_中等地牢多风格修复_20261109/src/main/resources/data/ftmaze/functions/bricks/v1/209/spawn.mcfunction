scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1169 31 98 minecraft:air if block 1169 32 98 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1169 31 98 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1184 31 115 minecraft:air if block 1184 32 115 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1184 31 115 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1166 31 114 minecraft:air if block 1166 32 114 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1166 31 114 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1184 31 95 minecraft:air if block 1184 32 95 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1184 31 95 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1176 31 107 minecraft:air if block 1176 32 107 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1176 31 107 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1185 31 105 minecraft:air if block 1185 32 105 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1185 31 105 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1165 31 104 minecraft:air if block 1165 32 104 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1165 31 104 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1178 31 99 minecraft:air if block 1178 32 99 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1178 31 99 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1169 31 99 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1169 31 99 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b209 ftm_brick 2
