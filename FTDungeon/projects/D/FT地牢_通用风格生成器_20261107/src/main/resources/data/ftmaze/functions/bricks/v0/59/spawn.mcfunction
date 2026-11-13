scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 122 91 177 minecraft:air if block 122 92 177 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 122 91 177 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 134 91 189 minecraft:air if block 134 92 189 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 134 91 189 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 122 91 189 minecraft:air if block 122 92 189 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 122 91 189 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 129 91 182 minecraft:air if block 129 92 182 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 129 91 182 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 122 91 178 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 122 91 178 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b59 ftm_brick 2
