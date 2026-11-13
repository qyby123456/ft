scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 582 91 198 minecraft:air if block 582 92 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 582 91 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 572 91 202 minecraft:air if block 572 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 572 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 587 91 202 minecraft:air if block 587 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 587 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 575 91 198 minecraft:air if block 575 92 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 575 91 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 579 91 202 minecraft:air if block 579 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 579 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 576 91 201 minecraft:air if block 576 92 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 576 91 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 578 91 199 minecraft:air if block 578 92 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 578 91 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 583 91 201 minecraft:air if block 583 92 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 583 91 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 583 91 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 583 91 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b317 ftm_brick 2
