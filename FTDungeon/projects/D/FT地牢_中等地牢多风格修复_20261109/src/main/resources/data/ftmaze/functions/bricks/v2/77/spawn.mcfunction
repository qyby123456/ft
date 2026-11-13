scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 105 31 121 minecraft:air if block 105 32 121 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 105 31 121 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 91 31 128 minecraft:air if block 91 32 128 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 91 31 128 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 94 31 118 minecraft:air if block 94 32 118 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 94 31 118 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 109 31 130 minecraft:air if block 109 32 130 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 109 31 130 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 98 31 124 minecraft:air if block 98 32 124 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 98 31 124 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 101 31 116 minecraft:air if block 101 32 116 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 101 31 116 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 100 31 130 minecraft:air if block 100 32 130 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 100 31 130 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 103 31 126 minecraft:air if block 103 32 126 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 103 31 126 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 106 31 121 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 106 31 121 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b77 ftm_brick 2
