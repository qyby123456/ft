scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 149 91 96 minecraft:air if block 149 92 96 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 149 91 96 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 163 91 105 minecraft:air if block 163 92 105 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 163 91 105 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 160 91 93 minecraft:air if block 160 92 93 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 160 91 93 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 153 91 103 minecraft:air if block 153 92 103 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 153 91 103 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 150 91 96 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 150 91 96 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b60 ftm_brick 2
