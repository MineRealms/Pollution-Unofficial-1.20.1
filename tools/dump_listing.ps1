param([string]$root, [string]$out)

$prefix = 'h:\MinecraftMods\Pollution-Unofficial-1.20.1\src\main\java\'
Get-ChildItem -Recurse -File -Filter *.java $root | ForEach-Object {
    $_.FullName.Replace($prefix, '')
} | Sort-Object | Set-Content -Path $out -Encoding UTF8
Write-Output ('written ' + $out + ' lines=' + (Get-Content $out).Count)