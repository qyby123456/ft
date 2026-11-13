scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 177 31 124 minecraft:air if block 177 32 124 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 177 31 124 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 191 31 133 minecraft:air if block 191 32 133 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 191 31 133 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 188 31 121 minecraft:air if block 188 32 121 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 188 31 121 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 181 31 131 minecraft:air if block 181 32 131 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 181 31 131 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 178 31 124 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 178 31 124 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b89 ftm_brick 2
