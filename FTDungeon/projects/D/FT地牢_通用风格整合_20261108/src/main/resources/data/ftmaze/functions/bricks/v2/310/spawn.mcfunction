scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 622 211 198 minecraft:air if block 622 212 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 622 211 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 614 211 201 minecraft:air if block 614 212 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 614 211 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 626 211 203 minecraft:air if block 626 212 203 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 626 211 203 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 616 211 197 minecraft:air if block 616 212 197 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 616 211 197 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 622 211 197 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 622 211 197 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b310 ftm_brick 2
