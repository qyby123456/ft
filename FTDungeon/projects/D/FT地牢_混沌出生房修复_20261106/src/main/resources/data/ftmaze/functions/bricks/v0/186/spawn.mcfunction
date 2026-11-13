scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 676 91 30 minecraft:air if block 676 92 30 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 676 91 30 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 693 91 44 minecraft:air if block 693 92 44 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 693 91 44 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 691 91 25 minecraft:air if block 691 92 25 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 691 91 25 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 673 91 44 minecraft:air if block 673 92 44 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 673 91 44 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 683 91 38 minecraft:air if block 683 92 38 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 683 91 38 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 681 91 25 minecraft:air if block 681 92 25 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 681 91 25 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 686 91 30 minecraft:air if block 686 92 30 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 686 91 30 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 691 91 35 minecraft:air if block 691 92 35 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 691 91 35 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 677 91 30 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 677 91 30 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b186 ftm_brick 2
