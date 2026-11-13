scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 622 31 198 minecraft:air if block 622 32 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 622 31 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 614 31 201 minecraft:air if block 614 32 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 614 31 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 626 31 203 minecraft:air if block 626 32 203 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 626 31 203 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 616 31 197 minecraft:air if block 616 32 197 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 616 31 197 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 622 31 197 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 622 31 197 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b309 ftm_brick 2
