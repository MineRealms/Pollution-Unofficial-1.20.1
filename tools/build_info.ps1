$ErrorActionPreference = 'SilentlyContinue'
$root = 'h:\MinecraftMods\Pollution-Unofficial-1.20.1'

Write-Output '=== build/classes/java/main ==='
$c = Get-ChildItem -Recurse -File (Join-Path $root 'build\classes\java\main')
Write-Output ('class files: ' + $c.Count)
if ($c) { Write-Output ('newest: ' + ($c | Sort-Object LastWriteTime -Descending | Select-Object -First 1).LastWriteTime) }

Write-Output ''
Write-Output '=== lang files ==='
$lang = Join-Path $root 'src\main\resources\assets\pollution\lang'
Get-ChildItem -File $lang | ForEach-Object {
    Write-Output ('{0}: {1} lines' -f $_.Name, (Get-Content $_.FullName).Count)
}

Write-Output ''
Write-Output '=== data files ==='
Get-ChildItem -Recurse -File (Join-Path $root 'src\main\resources\data') | ForEach-Object {
    Write-Output ($_.FullName.Substring((Join-Path $root 'src\main\resources\').Length))
}