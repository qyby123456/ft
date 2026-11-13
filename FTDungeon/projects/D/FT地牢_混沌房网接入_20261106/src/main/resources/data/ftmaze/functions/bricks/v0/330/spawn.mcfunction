scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 624 31 198 minecraft:air if block 624 32 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 624 31 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 614 31 202 minecraft:air if block 614 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 614 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 629 31 202 minecraft:air if block 629 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 629 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 617 31 198 minecraft:air if block 617 32 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 617 31 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 621 31 202 minecraft:air if block 621 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 621 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 618 31 201 minecraft:air if block 618 32 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 618 31 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 620 31 199 minecraft:air if block 620 32 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 620 31 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 625 31 201 minecraft:air if block 625 32 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 625 31 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 625 31 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 625 31 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b330 ftm_brick 2
