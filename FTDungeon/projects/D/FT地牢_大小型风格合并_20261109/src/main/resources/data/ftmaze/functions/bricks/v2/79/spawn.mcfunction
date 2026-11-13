scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 94 31 178 minecraft:air if block 94 32 178 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 94 31 178 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 105 31 188 minecraft:air if block 105 32 188 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 105 31 188 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 109 31 174 minecraft:air if block 109 32 174 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 109 31 174 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 90 31 188 minecraft:air if block 90 32 188 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 90 31 188 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 99 31 172 minecraft:air if block 99 32 172 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 99 31 172 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 97 31 185 minecraft:air if block 97 32 185 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 97 31 185 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 102 31 180 minecraft:air if block 102 32 180 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 102 31 180 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 108 31 182 minecraft:air if block 108 32 182 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 108 31 182 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 94 31 177 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 94 31 177 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b79 ftm_brick 2
