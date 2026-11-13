scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 622 91 198 minecraft:air if block 622 92 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 622 91 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 614 91 201 minecraft:air if block 614 92 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 614 91 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 626 91 203 minecraft:air if block 626 92 203 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 626 91 203 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 616 91 197 minecraft:air if block 616 92 197 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 616 91 197 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 622 91 197 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 622 91 197 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b313 ftm_brick 2
