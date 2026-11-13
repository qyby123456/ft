scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 624 211 198 minecraft:air if block 624 212 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 624 211 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 614 211 202 minecraft:air if block 614 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 614 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 629 211 202 minecraft:air if block 629 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 629 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 617 211 198 minecraft:air if block 617 212 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 617 211 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 621 211 202 minecraft:air if block 621 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 621 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 618 211 201 minecraft:air if block 618 212 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 618 211 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 620 211 199 minecraft:air if block 620 212 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 620 211 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 625 211 201 minecraft:air if block 625 212 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 625 211 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 625 211 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 625 211 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b334 ftm_brick 2
