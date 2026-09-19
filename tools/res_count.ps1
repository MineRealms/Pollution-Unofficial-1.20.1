param([string]$root, [int]$depth = 2)

$full = (Resolve-Path $root).Path
$groups = @{}
Get-ChildItem -Recurse -File $full | ForEach-Object {
    $rel = $_.FullName.Substring($full.Length + 1)
    $parts = $rel -split '\\'
    $take = [Math]::Min($depth, $parts.Count - 1)
    $key = ($parts[0..($take-1)] -join '/')
    if (-not $groups.ContainsKey($key)) { $groups[$key] = 0 }
    $groups[$key] = $groups[$key] + 1
}
$groups.GetEnumerator() | Sort-Object Name | ForEach-Object { Write-Output ('{0,-55} {1}' -f $_.Key, $_.Value) }
Write-Output ('TOTAL FILES: ' + (Get-ChildItem -Recurse -File $full).Count)