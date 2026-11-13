scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 624 151 198 minecraft:air if block 624 152 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 624 151 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 614 151 202 minecraft:air if block 614 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 614 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 629 151 202 minecraft:air if block 629 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 629 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 617 151 198 minecraft:air if block 617 152 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 617 151 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 621 151 202 minecraft:air if block 621 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 621 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 618 151 201 minecraft:air if block 618 152 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 618 151 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 620 151 199 minecraft:air if block 620 152 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 620 151 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 625 151 201 minecraft:air if block 625 152 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 625 151 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 625 151 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 625 151 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b319 ftm_brick 2
