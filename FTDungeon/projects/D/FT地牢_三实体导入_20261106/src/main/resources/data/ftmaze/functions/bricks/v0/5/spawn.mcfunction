scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 94 211 122 minecraft:air if block 94 212 122 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 94 211 122 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 105 211 132 minecraft:air if block 105 212 132 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 105 211 132 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 109 211 118 minecraft:air if block 109 212 118 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 109 211 118 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 90 211 132 minecraft:air if block 90 212 132 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 90 211 132 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 99 211 116 minecraft:air if block 99 212 116 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 99 211 116 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 97 211 129 minecraft:air if block 97 212 129 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 97 211 129 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 102 211 124 minecraft:air if block 102 212 124 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 102 211 124 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 108 211 126 minecraft:air if block 108 212 126 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 108 211 126 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 94 211 121 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 94 211 121 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b5 ftm_brick 2
