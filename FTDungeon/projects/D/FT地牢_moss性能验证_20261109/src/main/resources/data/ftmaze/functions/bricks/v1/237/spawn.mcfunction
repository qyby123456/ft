scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1785 211 148 minecraft:air if block 1785 212 148 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1785 211 148 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1800 211 163 minecraft:air if block 1800 212 163 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1800 211 163 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1800 211 143 minecraft:air if block 1800 212 143 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1800 211 143 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1784 211 163 minecraft:air if block 1784 212 163 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1784 211 163 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1792 211 156 minecraft:air if block 1792 212 156 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1792 211 156 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1802 211 153 minecraft:air if block 1802 212 153 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1802 211 153 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1782 211 155 minecraft:air if block 1782 212 155 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1782 211 155 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1790 211 143 minecraft:air if block 1790 212 143 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1790 211 143 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1786 211 148 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1786 211 148 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b237 ftm_brick 2
