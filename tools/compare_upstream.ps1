$upstream = 'H:\MinecraftMods\Pollution\src\main\java\meowmel\pollution'
$port = 'h:\MinecraftMods\Pollution-Unofficial-1.20.1\src\main\java\meowmel\pollution'

function ClassNames($root) {
    Get-ChildItem -Recurse -Filter *.java $root | ForEach-Object {
        $_.FullName.Substring($root.Length + 1) -replace '\.java$','' -replace '\\','.'
    }
}

$u = ClassNames $upstream
$p = ClassNames $port

Write-Output ('UPSTREAM TOTAL: ' + $u.Count)
Write-Output ('PORT TOTAL: ' + $p.Count)
Write-Output ''

# group by second-level package
Write-Output '=== UPSTREAM by package ==='
$u | ForEach-Object { ($_ -split '\.')[0..1] -join '.' } | Group-Object | Sort-Object Count -Descending | ForEach-Object { Write-Output ($_.Name + ' : ' + $_.Count) }
Write-Output ''
Write-Output '=== PORT by package ==='
$p | ForEach-Object { ($_ -split '\.')[0..1] -join '.' } | Group-Object | Sort-Object Count -Descending | ForEach-Object { Write-Output ($_.Name + ' : ' + $_.Count) }
