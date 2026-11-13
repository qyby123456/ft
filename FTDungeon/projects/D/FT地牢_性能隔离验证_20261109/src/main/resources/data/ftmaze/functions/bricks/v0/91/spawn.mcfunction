scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 178 31 178 minecraft:air if block 178 32 178 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 178 31 178 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 192 31 189 minecraft:air if block 192 32 189 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 192 31 189 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 190 31 176 minecraft:air if block 190 32 176 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 190 31 176 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 176 31 189 minecraft:air if block 176 32 189 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 176 31 189 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 178 31 179 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 178 31 179 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b91 ftm_brick 2
