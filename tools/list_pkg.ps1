param([string]$sub)

$upstream = 'H:\MinecraftMods\Pollution\src\main\java\meowmel\pollution'
$port = 'h:\MinecraftMods\Pollution-Unofficial-1.20.1\src\main\java\meowmel\pollution'

function Names($root, $sub) {
    $dir = Join-Path $root $sub
    if (-not (Test-Path $dir)) { return @() }
    Get-ChildItem -Recurse -Filter *.java $dir | ForEach-Object { $_.BaseName }
}

$u = Names $upstream $sub
$p = Names $port $sub

Write-Output ('### ' + $sub)
Write-Output ('UPSTREAM (' + $u.Count + '):')
$u | Sort-Object | ForEach-Object { Write-Output ('  ' + $_) }
Write-Output ('PORT (' + $p.Count + '):')
$p | Sort-Object | ForEach-Object { Write-Output ('  ' + $_) }
Write-Output ''