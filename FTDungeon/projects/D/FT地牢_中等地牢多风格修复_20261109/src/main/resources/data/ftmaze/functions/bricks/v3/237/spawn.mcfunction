scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1785 151 148 minecraft:air if block 1785 152 148 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1785 151 148 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1800 151 163 minecraft:air if block 1800 152 163 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1800 151 163 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1800 151 143 minecraft:air if block 1800 152 143 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1800 151 143 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1784 151 163 minecraft:air if block 1784 152 163 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1784 151 163 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1792 151 156 minecraft:air if block 1792 152 156 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1792 151 156 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1802 151 153 minecraft:air if block 1802 152 153 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1802 151 153 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1782 151 155 minecraft:air if block 1782 152 155 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1782 151 155 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1790 151 143 minecraft:air if block 1790 152 143 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1790 151 143 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1786 151 148 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1786 151 148 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b237 ftm_brick 2
