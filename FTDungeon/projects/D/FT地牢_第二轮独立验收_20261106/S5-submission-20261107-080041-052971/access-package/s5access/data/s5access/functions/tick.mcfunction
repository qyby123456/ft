execute as @a[scores={s5a_enter=1..}] at @s run function s5access:request_enter
execute as @a[scores={s5a_exit=1..}] at @s run function s5access:request_exit
scoreboard players enable @a s5a_enter
scoreboard players enable @a s5a_exit
