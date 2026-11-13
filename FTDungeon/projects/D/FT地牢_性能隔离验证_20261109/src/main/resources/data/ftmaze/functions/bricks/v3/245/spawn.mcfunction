scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1785 153 34 minecraft:air if block 1785 154 34 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1785 153 34 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1802 153 24 minecraft:air if block 1802 154 24 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1802 153 24 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1798 153 44 minecraft:air if block 1798 154 44 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1798 153 44 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1782 153 24 minecraft:air if block 1782 154 24 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1782 153 24 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1782 153 44 minecraft:air if block 1782 154 44 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1782 153 44 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1792 153 27 minecraft:air if block 1792 154 27 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1792 153 27 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1799 153 33 minecraft:air if block 1799 154 33 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1799 153 33 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1790 153 40 minecraft:air if block 1790 154 40 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1790 153 40 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1786 153 34 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1786 153 34 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b245 ftm_brick 2
