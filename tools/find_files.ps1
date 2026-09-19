param([string]$root, [string]$pattern, [switch]$recursive=$true)

if (-not (Test-Path $root)) { Write-Output ('MISSING: ' + $root); exit }
Get-ChildItem -Recurse -Filter *.java $root | Where-Object { $_.Name -match $pattern } | ForEach-Object { Write-Output $_.FullName }