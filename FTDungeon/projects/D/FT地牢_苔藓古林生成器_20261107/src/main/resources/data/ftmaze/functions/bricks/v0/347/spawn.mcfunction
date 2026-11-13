scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 777 151 198 minecraft:air if block 777 152 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 777 151 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 767 151 202 minecraft:air if block 767 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 767 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 782 151 202 minecraft:air if block 782 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 782 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 770 151 198 minecraft:air if block 770 152 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 770 151 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 774 151 202 minecraft:air if block 774 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 774 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 771 151 201 minecraft:air if block 771 152 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 771 151 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 773 151 199 minecraft:air if block 773 152 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 773 151 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 778 151 201 minecraft:air if block 778 152 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 778 151 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 778 151 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 778 151 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b347 ftm_brick 2
