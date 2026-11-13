scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 624 91 198 minecraft:air if block 624 92 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 624 91 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 614 91 202 minecraft:air if block 614 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 614 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 629 91 202 minecraft:air if block 629 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 629 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 617 91 198 minecraft:air if block 617 92 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 617 91 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 621 91 202 minecraft:air if block 621 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 621 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 618 91 201 minecraft:air if block 618 92 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 618 91 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 620 91 199 minecraft:air if block 620 92 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 620 91 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 625 91 201 minecraft:air if block 625 92 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 625 91 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 625 91 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 625 91 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b318 ftm_brick 2
