scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 735 151 198 minecraft:air if block 735 152 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 735 151 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 725 151 202 minecraft:air if block 725 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 725 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 740 151 202 minecraft:air if block 740 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 740 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 728 151 198 minecraft:air if block 728 152 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 728 151 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 732 151 202 minecraft:air if block 732 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 732 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 729 151 201 minecraft:air if block 729 152 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 729 151 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 731 151 199 minecraft:air if block 731 152 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 731 151 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 736 151 201 minecraft:air if block 736 152 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 736 151 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 736 151 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 736 151 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b346 ftm_brick 2
