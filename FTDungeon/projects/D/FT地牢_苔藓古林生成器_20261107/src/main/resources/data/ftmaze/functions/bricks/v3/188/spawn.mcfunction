scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 834 91 115 minecraft:air if block 834 92 115 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 834 91 115 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 850 91 129 minecraft:air if block 850 92 129 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 850 91 129 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 849 91 109 minecraft:air if block 849 92 109 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 849 91 109 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 830 91 129 minecraft:air if block 830 92 129 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 830 91 129 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 840 91 124 minecraft:air if block 840 92 124 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 840 91 124 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 846 91 118 minecraft:air if block 846 92 118 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 846 91 118 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 830 91 109 minecraft:air if block 830 92 109 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 830 91 109 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 839 91 110 minecraft:air if block 839 92 110 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 839 91 110 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 834 91 114 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 834 91 114 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b188 ftm_brick 2
