scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 70 31 208 minecraft:air if block 70 32 208 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 70 31 208 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 74 31 198 minecraft:air if block 74 32 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 74 31 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 74 31 213 minecraft:air if block 74 32 213 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 74 31 213 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 70 31 201 minecraft:air if block 70 32 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 70 31 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 74 31 205 minecraft:air if block 74 32 205 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 74 31 205 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 70 31 212 minecraft:air if block 70 32 212 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 70 31 212 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 71 31 204 minecraft:air if block 71 32 204 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 71 31 204 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 72 31 210 minecraft:air if block 72 32 210 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 72 31 210 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 70 31 209 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 70 31 209 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b275 ftm_brick 2
