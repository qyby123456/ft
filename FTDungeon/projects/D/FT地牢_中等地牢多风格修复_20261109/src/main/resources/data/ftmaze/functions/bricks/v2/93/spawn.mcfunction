scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 206 31 123 minecraft:air if block 206 32 123 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 206 31 123 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 222 31 130 minecraft:air if block 222 32 130 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 222 31 130 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 217 31 118 minecraft:air if block 217 32 118 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 217 31 118 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 210 31 131 minecraft:air if block 210 32 131 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 210 31 131 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 202 31 129 minecraft:air if block 202 32 129 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 202 31 129 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 214 31 125 minecraft:air if block 214 32 125 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 214 31 125 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 202 31 118 minecraft:air if block 202 32 118 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 202 31 118 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 210 31 119 minecraft:air if block 210 32 119 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 210 31 119 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 206 31 122 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 206 31 122 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b93 ftm_brick 2
