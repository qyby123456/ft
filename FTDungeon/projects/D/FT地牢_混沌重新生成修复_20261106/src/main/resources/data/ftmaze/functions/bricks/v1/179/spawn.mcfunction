scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 592 91 119 minecraft:air if block 592 92 119 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 592 91 119 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 608 91 109 minecraft:air if block 608 92 109 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 608 91 109 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 605 91 129 minecraft:air if block 605 92 129 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 605 91 129 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 595 91 109 minecraft:air if block 595 92 109 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 595 91 109 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 590 91 129 minecraft:air if block 590 92 129 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 590 91 129 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 603 91 118 minecraft:air if block 603 92 118 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 603 91 118 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 597 91 125 minecraft:air if block 597 92 125 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 597 91 125 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 608 91 122 minecraft:air if block 608 92 122 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 608 91 122 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 593 91 119 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 593 91 119 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b179 ftm_brick 2
