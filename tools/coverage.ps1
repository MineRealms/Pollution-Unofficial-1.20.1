$upstream = 'H:\MinecraftMods\Pollution\src\main\java\meowmel\pollution'
$port = 'h:\MinecraftMods\Pollution-Unofficial-1.20.1\src\main\java\meowmel\pollution'

# 归一化：去掉上游特有的前缀/后缀，便于与 1.20.1 命名对齐
function Normalize([string]$name) {
    $n = $name
    $n = $n -replace '^MetaTileEntity', ''
    $n = $n -replace '^TileEntity', ''
    $n = $n -replace '^PO', ''
    $n = $n -replace '^Meta', ''
    $n = $n -replace 'Machine$', ''
    $n = $n -replace 'TileEntity$', ''
    $n = $n -replace 'BlockEntity$', ''
    $n = $n -replace '^Meta', ''
    return $n.ToLower()
}

function Names($root) {
    Get-ChildItem -Recurse -File -Filter *.java $root |
        ForEach-Object { $_.BaseName }
}

$u = Names $upstream | Sort-Object -Unique
$p = Names $port | Sort-Object -Unique

$pn = @{}
foreach ($x in $p) { $pn[(Normalize $x)] = $x }

$matched = @()
$missing = @()
foreach ($x in $u) {
    $key = Normalize $x
    if ($pn.ContainsKey($key)) { $matched += $x } else { $missing += $x }
}

Write-Output ('UPSTREAM unique classes : ' + $u.Count)
Write-Output ('PORT unique classes     : ' + $p.Count)
Write-Output ('MATCHED (by normalized) : ' + $matched.Count)
Write-Output ('MISSING in port         : ' + $missing.Count)
Write-Output ('COVERAGE                : ' + [math]::Round(100 * $matched.Count / $u.Count, 1) + '%')
Write-Output ''
Write-Output '=== MISSING (upstream classes with no normalized port counterpart) ==='
$missing | Sort-Object | ForEach-Object { Write-Output $_ }