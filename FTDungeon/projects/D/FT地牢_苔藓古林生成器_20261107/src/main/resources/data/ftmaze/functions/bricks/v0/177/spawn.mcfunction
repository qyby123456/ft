scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 633 91 148 minecraft:air if block 633 92 148 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 633 91 148 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 648 91 163 minecraft:air if block 648 92 163 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 648 91 163 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 648 91 143 minecraft:air if block 648 92 143 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 648 91 143 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 632 91 163 minecraft:air if block 632 92 163 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 632 91 163 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 640 91 156 minecraft:air if block 640 92 156 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 640 91 156 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 650 91 153 minecraft:air if block 650 92 153 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 650 91 153 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 630 91 155 minecraft:air if block 630 92 155 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 630 91 155 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 638 91 143 minecraft:air if block 638 92 143 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 638 91 143 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 634 91 148 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 634 91 148 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b177 ftm_brick 2
