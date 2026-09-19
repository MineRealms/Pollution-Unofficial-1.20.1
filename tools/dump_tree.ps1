param([string]$root, [string]$out)

$prefix = $root.TrimEnd('\') + '\'
$lines = Get-ChildItem -Recurse -File -Filter *.java $root | ForEach-Object { $_.FullName.Substring($prefix.Length) } | Sort-Object
Set-Content -Path $out -Value $lines -Encoding UTF8
Write-Output ('wrote ' + $lines.Count + ' lines to ' + $out)