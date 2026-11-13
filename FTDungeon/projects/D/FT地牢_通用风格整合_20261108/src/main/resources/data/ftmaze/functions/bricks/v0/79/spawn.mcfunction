scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 94 31 178 minecraft:air if block 94 32 178 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 94 31 178 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 107 31 188 minecraft:air if block 107 32 188 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 107 31 188 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 109 31 175 minecraft:air if block 109 32 175 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 109 31 175 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 91 31 186 minecraft:air if block 91 32 186 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 91 31 186 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 102 31 181 minecraft:air if block 102 32 181 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 102 31 181 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 99 31 174 minecraft:air if block 99 32 174 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 99 31 174 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 96 31 183 minecraft:air if block 96 32 183 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 96 31 183 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 108 31 182 minecraft:air if block 108 32 182 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 108 31 182 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 94 31 179 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 94 31 179 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b79 ftm_brick 2
