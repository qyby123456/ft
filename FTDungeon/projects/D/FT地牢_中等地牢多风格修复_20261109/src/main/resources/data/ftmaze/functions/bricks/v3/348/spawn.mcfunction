scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 769 211 198 minecraft:air if block 769 212 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 769 211 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 759 211 202 minecraft:air if block 759 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 759 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 774 211 202 minecraft:air if block 774 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 774 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 762 211 198 minecraft:air if block 762 212 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 762 211 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 766 211 202 minecraft:air if block 766 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 766 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 763 211 201 minecraft:air if block 763 212 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 763 211 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 765 211 199 minecraft:air if block 765 212 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 765 211 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 770 211 201 minecraft:air if block 770 212 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 770 211 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 770 211 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 770 211 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b348 ftm_brick 2
