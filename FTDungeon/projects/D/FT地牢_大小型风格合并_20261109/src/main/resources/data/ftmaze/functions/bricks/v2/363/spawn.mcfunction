scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 801 211 198 minecraft:air if block 801 212 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 801 211 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 791 211 202 minecraft:air if block 791 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 791 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 806 211 202 minecraft:air if block 806 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 806 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 794 211 198 minecraft:air if block 794 212 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 794 211 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 798 211 202 minecraft:air if block 798 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 798 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 795 211 201 minecraft:air if block 795 212 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 795 211 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 797 211 199 minecraft:air if block 797 212 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 797 211 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 802 211 201 minecraft:air if block 802 212 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 802 211 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 802 211 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 802 211 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b363 ftm_brick 2
