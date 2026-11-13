scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 750 211 198 minecraft:air if block 750 212 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 750 211 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 740 211 202 minecraft:air if block 740 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 740 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 755 211 202 minecraft:air if block 755 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 755 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 743 211 198 minecraft:air if block 743 212 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 743 211 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 747 211 202 minecraft:air if block 747 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 747 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 744 211 201 minecraft:air if block 744 212 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 744 211 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 746 211 199 minecraft:air if block 746 212 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 746 211 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 751 211 201 minecraft:air if block 751 212 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 751 211 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 751 211 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 751 211 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b337 ftm_brick 2
