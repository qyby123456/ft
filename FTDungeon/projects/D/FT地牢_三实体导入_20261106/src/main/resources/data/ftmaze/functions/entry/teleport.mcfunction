tp @a[tag=ftmaze.returning] ~ ~ ~
execute as @a[tag=ftmaze.returning] run function ftmaze:entry/restore
scoreboard players set #returned ftm_state 1
