scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 97 152 130 minecraft:air if block 97 153 130 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 97 152 130 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 109 152 119 minecraft:air if block 109 153 119 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 109 152 119 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 91 152 119 minecraft:air if block 91 153 119 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 91 152 119 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 108 152 134 minecraft:air if block 108 153 134 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 108 152 134 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 100 152 121 minecraft:air if block 100 153 121 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 100 152 121 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 91 152 134 minecraft:air if block 91 153 134 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 91 152 134 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 103 152 127 minecraft:air if block 103 153 127 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 103 152 127 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 92 152 126 minecraft:air if block 92 153 126 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 92 152 126 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 98 152 130 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 98 152 130 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b29 ftm_brick 2
