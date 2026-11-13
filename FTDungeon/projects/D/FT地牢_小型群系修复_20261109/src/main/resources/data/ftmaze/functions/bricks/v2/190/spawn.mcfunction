scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 883 151 115 minecraft:air if block 883 152 115 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 883 151 115 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 898 151 129 minecraft:air if block 898 152 129 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 898 151 129 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 897 151 109 minecraft:air if block 897 152 109 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 897 151 109 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 880 151 129 minecraft:air if block 880 152 129 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 880 151 129 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 889 151 123 minecraft:air if block 889 152 123 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 889 151 123 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 894 151 117 minecraft:air if block 894 152 117 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 894 151 117 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 887 151 109 minecraft:air if block 887 152 109 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 887 151 109 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 879 151 120 minecraft:air if block 879 152 120 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 879 151 120 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 883 151 114 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 883 151 114 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b190 ftm_brick 2
