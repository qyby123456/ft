# 扫描整合包 mods 里的物品/伤害类型 id（全 ASCII 关键字，避免控制台中文乱码）
# 用法: pwsh -File tools\scan_pack_ids.ps1
$mods = "C:\Users\YOUR_USERNAME\AppData\Roaming\.minecraft\versions\FarmingTales Forge\mods"
Add-Type -AssemblyName System.IO.Compression.FileSystem
$keys = @("rolling_pin", "stockpot", "soup_pot", "soup", "cooking_pot", "skillet", "wok", "frying_pan", "pan_", "cook_pot")
$dmg  = @("crushing", "fan", "mechanical", "create")
$found = New-Object System.Collections.Generic.List[string]
$dmgFound = New-Object System.Collections.Generic.List[string]

$jars = Get-ChildItem $mods -Filter *.jar -File
Write-Output ("jars: " + $jars.Count)
foreach ($j in $jars) {
    try {
        $z = [System.IO.Compression.ZipFile]::OpenRead($j.FullName)
        foreach ($e in $z.Entries) {
            $n = $e.FullName
            if ($n -like "*/lang/en_us.json") {
                $sr = New-Object System.IO.StreamReader($e.Open())
                $txt = $sr.ReadToEnd(); $sr.Close()
                foreach ($k in $keys) {
                    foreach ($m in [regex]::Matches($txt, '"([a-z0-9_\.]*' + $k + '[a-z0-9_\.]*)"\s*:\s*"([^"]{1,40})"')) {
                        $found.Add(("{0}|{1}={2}" -f $j.Name, $m.Groups[1].Value, $m.Groups[2].Value))
                    }
                }
            }
            elseif ($n -like "data/*/damage_type/*.json") {
                if ($n -match "crushing|create|fan|mechanical") { $dmgFound.Add(("{0}|{1}" -f $j.Name, $n)) }
            }
        }
        $z.Dispose()
    } catch { }
}
Write-Output "=== item-like keys ==="
$found | Sort-Object -Unique | Select-Object -First 120
Write-Output "=== damage types ==="
$dmgFound | Sort-Object -Unique | Select-Object -First 60
