scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 122 31 122 minecraft:air if block 122 32 122 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 122 31 122 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 134 31 133 minecraft:air if block 134 32 133 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 134 31 133 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 135 31 120 minecraft:air if block 135 32 120 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 135 31 120 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 120 31 133 minecraft:air if block 120 32 133 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 120 31 133 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 122 31 123 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 122 31 123 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b81 ftm_brick 2
