scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 97 152 186 minecraft:air if block 97 153 186 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 97 152 186 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 109 152 175 minecraft:air if block 109 153 175 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 109 152 175 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 91 152 175 minecraft:air if block 91 153 175 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 91 152 175 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 108 152 190 minecraft:air if block 108 153 190 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 108 152 190 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 100 152 177 minecraft:air if block 100 153 177 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 100 152 177 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 91 152 190 minecraft:air if block 91 153 190 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 91 152 190 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 103 152 183 minecraft:air if block 103 153 183 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 103 152 183 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 92 152 182 minecraft:air if block 92 153 182 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 92 152 182 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 98 152 186 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 98 152 186 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b31 ftm_brick 2
