scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 593 211 98 minecraft:air if block 593 212 98 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 593 211 98 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 608 211 115 minecraft:air if block 608 212 115 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 608 211 115 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 590 211 114 minecraft:air if block 590 212 114 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 590 211 114 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 608 211 95 minecraft:air if block 608 212 95 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 608 211 95 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 600 211 107 minecraft:air if block 600 212 107 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 600 211 107 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 609 211 105 minecraft:air if block 609 212 105 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 609 211 105 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 589 211 104 minecraft:air if block 589 212 104 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 589 211 104 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 602 211 99 minecraft:air if block 602 212 99 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 602 211 99 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 593 211 99 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 593 211 99 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b179 ftm_brick 2
