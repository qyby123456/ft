scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 601 31 198 minecraft:air if block 601 32 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 601 31 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 591 31 202 minecraft:air if block 591 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 591 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 606 31 202 minecraft:air if block 606 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 606 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 594 31 198 minecraft:air if block 594 32 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 594 31 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 598 31 202 minecraft:air if block 598 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 598 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 595 31 201 minecraft:air if block 595 32 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 595 31 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 597 31 199 minecraft:air if block 597 32 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 597 31 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 602 31 201 minecraft:air if block 602 32 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 602 31 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 602 31 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 602 31 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b347 ftm_brick 2
