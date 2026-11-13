forceload add ~ ~
summon minecraft:marker ~ ~ ~ {Tags:["ftmaze.lobby"]}
tellraw @a {"text":"候场点已创建。参加者在8格内 /trigger ftm_join，管理员随后 /function ftmaze:install。","color":"aqua"}
