scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 98 31 130 minecraft:air if block 98 32 130 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 98 31 130 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 109 31 127 minecraft:air if block 109 32 127 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 109 31 127 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 94 31 138 minecraft:air if block 94 32 138 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 94 31 138 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 103 31 136 minecraft:air if block 103 32 136 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 103 31 136 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 91 31 127 minecraft:air if block 91 32 127 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 91 31 127 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 102 31 126 minecraft:air if block 102 32 126 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 102 31 126 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 97 31 135 minecraft:air if block 97 32 135 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 97 31 135 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 105 31 129 minecraft:air if block 105 32 129 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 105 31 129 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 99 31 130 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 99 31 130 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b77 ftm_brick 2
