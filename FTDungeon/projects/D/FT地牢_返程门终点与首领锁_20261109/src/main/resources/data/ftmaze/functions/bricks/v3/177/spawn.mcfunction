scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 641 31 135 minecraft:air if block 641 32 135 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 641 31 135 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 658 31 148 minecraft:air if block 658 32 148 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 658 31 148 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 656 31 130 minecraft:air if block 656 32 130 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 656 31 130 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 638 31 148 minecraft:air if block 638 32 148 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 638 31 148 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 648 31 143 minecraft:air if block 648 32 143 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 648 31 143 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 646 31 129 minecraft:air if block 646 32 129 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 646 31 129 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 651 31 135 minecraft:air if block 651 32 135 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 651 31 135 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 656 31 140 minecraft:air if block 656 32 140 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 656 31 140 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 642 31 135 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 642 31 135 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b177 ftm_brick 2
