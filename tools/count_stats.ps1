param([string]$sub, [string]$pattern)

$upstream = 'H:\MinecraftMods\Pollution\src\main\java\meowmel\pollution'
$port = 'h:\MinecraftMods\Pollution-Unofficial-1.20.1\src\main\java\meowmel\pollution'

function Stats($root, $sub, $pattern) {
    $dir = Join-Path $root $sub
    if (-not (Test-Path $dir)) { return }
    Get-ChildItem -Recurse -Filter *.java $dir | ForEach-Object {
        $lines = (Get-Content $_.FullName).Count
        $hits = (Select-String -Path $_.FullName -Pattern $pattern | Measure-Object).Count
        Write-Output ('{0,-45} lines={1,-6} hits={2}' -f $_.BaseName, $lines, $hits)
    }
}

Write-Output ('=== UPSTREAM ' + $sub + ' (pattern: ' + $pattern + ') ===')
Stats $upstream $sub $pattern
Write-Output ''
Write-Output ('=== PORT ' + $sub + ' (pattern: ' + $pattern + ') ===')
Stats $port $sub $pattern