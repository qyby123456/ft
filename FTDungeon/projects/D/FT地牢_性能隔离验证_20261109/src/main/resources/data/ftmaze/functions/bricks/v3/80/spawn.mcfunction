scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 122 31 94 minecraft:air if block 122 32 94 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 122 31 94 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 135 31 104 minecraft:air if block 135 32 104 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 135 31 104 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 137 31 91 minecraft:air if block 137 32 91 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 137 31 91 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 119 31 102 minecraft:air if block 119 32 102 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 119 31 102 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 130 31 97 minecraft:air if block 130 32 97 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 130 31 97 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 127 31 90 minecraft:air if block 127 32 90 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 127 31 90 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 124 31 99 minecraft:air if block 124 32 99 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 124 31 99 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 136 31 98 minecraft:air if block 136 32 98 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 136 31 98 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 122 31 95 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 122 31 95 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b80 ftm_brick 2
