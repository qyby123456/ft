scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 121 31 233 minecraft:air if block 121 32 233 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 121 31 233 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 135 31 243 minecraft:air if block 135 32 243 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 135 31 243 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 123 31 244 minecraft:air if block 123 32 244 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 123 31 244 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 133 31 232 minecraft:air if block 133 32 232 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 133 31 232 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 122 31 233 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 122 31 233 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b250 ftm_brick 2
