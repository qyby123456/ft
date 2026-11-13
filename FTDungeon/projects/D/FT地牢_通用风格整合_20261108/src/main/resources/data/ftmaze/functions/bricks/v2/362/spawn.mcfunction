scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 759 211 198 minecraft:air if block 759 212 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 759 211 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 749 211 202 minecraft:air if block 749 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 749 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 764 211 202 minecraft:air if block 764 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 764 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 752 211 198 minecraft:air if block 752 212 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 752 211 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 756 211 202 minecraft:air if block 756 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 756 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 753 211 201 minecraft:air if block 753 212 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 753 211 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 755 211 199 minecraft:air if block 755 212 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 755 211 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 760 211 201 minecraft:air if block 760 212 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 760 211 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 760 211 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 760 211 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b362 ftm_brick 2
