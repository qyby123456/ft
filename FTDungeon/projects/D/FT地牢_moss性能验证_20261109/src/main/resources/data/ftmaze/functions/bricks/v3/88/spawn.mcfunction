scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 178 31 95 minecraft:air if block 178 32 95 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 178 31 95 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 194 31 102 minecraft:air if block 194 32 102 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 194 31 102 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 189 31 90 minecraft:air if block 189 32 90 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 189 31 90 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 182 31 103 minecraft:air if block 182 32 103 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 182 31 103 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 174 31 101 minecraft:air if block 174 32 101 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 174 31 101 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 186 31 97 minecraft:air if block 186 32 97 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 186 31 97 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 174 31 90 minecraft:air if block 174 32 90 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 174 31 90 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 182 31 91 minecraft:air if block 182 32 91 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 182 31 91 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 178 31 94 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 178 31 94 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b88 ftm_brick 2
