scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 750 151 198 minecraft:air if block 750 152 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 750 151 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 740 151 202 minecraft:air if block 740 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 740 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 755 151 202 minecraft:air if block 755 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 755 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 743 151 198 minecraft:air if block 743 152 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 743 151 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 747 151 202 minecraft:air if block 747 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 747 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 744 151 201 minecraft:air if block 744 152 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 744 151 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 746 151 199 minecraft:air if block 746 152 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 746 151 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 751 151 201 minecraft:air if block 751 152 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 751 151 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 751 151 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 751 151 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b333 ftm_brick 2
