scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 121 91 121 minecraft:air if block 121 92 121 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 121 91 121 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 134 91 134 minecraft:air if block 134 92 134 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 134 91 134 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 135 91 121 minecraft:air if block 135 92 121 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 135 91 121 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 121 91 134 minecraft:air if block 121 92 134 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 121 91 134 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 122 91 121 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 122 91 121 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b57 ftm_brick 2
