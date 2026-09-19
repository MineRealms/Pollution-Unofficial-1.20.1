param([string]$root, [string]$pattern, [string]$filter='*')

if (-not (Test-Path $root)) { Write-Output ('MISSING: ' + $root); exit }
$files = Get-ChildItem -Recurse -File -Filter *.java $root -ErrorAction SilentlyContinue
$total = 0
foreach ($f in $files) {
    try { $hits = Select-String -Path $f.FullName -Pattern $pattern -ErrorAction Stop } catch { continue }
    if ($hits) {
        $total += $hits.Count
        Write-Output ('--- ' + $f.FullName.Replace($root,'') + ' (' + $hits.Count + ')')
        $hits | Select-Object -First 4 | ForEach-Object { Write-Output ('    L' + $_.LineNumber + ': ' + $_.Line.Trim()) }
    }
}
Write-Output ('TOTAL HITS: ' + $total)