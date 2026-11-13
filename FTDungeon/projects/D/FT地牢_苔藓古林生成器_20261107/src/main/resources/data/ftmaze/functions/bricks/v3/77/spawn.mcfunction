scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 94 31 122 minecraft:air if block 94 32 122 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 94 31 122 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 105 31 132 minecraft:air if block 105 32 132 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 105 31 132 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 109 31 118 minecraft:air if block 109 32 118 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 109 31 118 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 90 31 132 minecraft:air if block 90 32 132 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 90 31 132 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 99 31 116 minecraft:air if block 99 32 116 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 99 31 116 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 97 31 129 minecraft:air if block 97 32 129 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 97 31 129 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 102 31 124 minecraft:air if block 102 32 124 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 102 31 124 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 108 31 126 minecraft:air if block 108 32 126 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 108 31 126 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 94 31 121 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 94 31 121 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b77 ftm_brick 2
