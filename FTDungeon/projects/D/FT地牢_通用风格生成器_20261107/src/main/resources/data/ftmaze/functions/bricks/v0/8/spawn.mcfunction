scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 133 211 93 minecraft:air if block 133 212 93 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 133 211 93 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 119 211 100 minecraft:air if block 119 212 100 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 119 211 100 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 122 211 90 minecraft:air if block 122 212 90 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 122 211 90 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 137 211 102 minecraft:air if block 137 212 102 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 137 211 102 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 126 211 96 minecraft:air if block 126 212 96 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 126 211 96 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 129 211 88 minecraft:air if block 129 212 88 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 129 211 88 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 128 211 102 minecraft:air if block 128 212 102 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 128 211 102 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 131 211 98 minecraft:air if block 131 212 98 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 131 211 98 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 134 211 93 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 134 211 93 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b8 ftm_brick 2
