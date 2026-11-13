scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 675 211 198 minecraft:air if block 675 212 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 675 211 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 665 211 202 minecraft:air if block 665 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 665 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 680 211 202 minecraft:air if block 680 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 680 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 668 211 198 minecraft:air if block 668 212 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 668 211 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 672 211 202 minecraft:air if block 672 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 672 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 669 211 201 minecraft:air if block 669 212 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 669 211 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 671 211 199 minecraft:air if block 671 212 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 671 211 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 676 211 201 minecraft:air if block 676 212 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 676 211 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 676 211 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 676 211 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b360 ftm_brick 2
