scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 641 37 58 minecraft:air if block 641 38 58 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 641 37 58 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 658 37 72 minecraft:air if block 658 38 72 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 658 37 72 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 656 37 52 minecraft:air if block 656 38 52 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 656 37 52 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 638 37 72 minecraft:air if block 638 38 72 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 638 37 72 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 648 37 66 minecraft:air if block 648 38 66 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 648 37 66 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 652 37 59 minecraft:air if block 652 38 59 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 652 37 59 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 645 37 52 minecraft:air if block 645 38 52 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 645 37 52 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 656 37 64 minecraft:air if block 656 38 64 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 656 37 64 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 642 37 58 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 642 37 58 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b182 ftm_brick 2
