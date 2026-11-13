scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 567 151 198 minecraft:air if block 567 152 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 567 151 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 557 151 202 minecraft:air if block 557 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 557 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 572 151 202 minecraft:air if block 572 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 572 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 560 151 198 minecraft:air if block 560 152 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 560 151 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 564 151 202 minecraft:air if block 564 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 564 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 561 151 201 minecraft:air if block 561 152 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 561 151 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 563 151 199 minecraft:air if block 563 152 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 563 151 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 568 151 201 minecraft:air if block 568 152 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 568 151 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 568 151 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 568 151 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b342 ftm_brick 2
