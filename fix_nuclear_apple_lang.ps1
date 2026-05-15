# Fix garbled nuclear_apple lang entries in all 28 lang files.
# [char]0x00A7 builds the section-sign from a numeric literal — no encoding dependency.

$langDir = "E:\Programieren\Projekte\Ultimate Apple Mod\forge\src\main\resources\assets\ultimate_apple_mod\lang"

$s = [char]0x00A7   # U+00A7 section sign — built at runtime, no glyph in source

$enItemVal = 'Nuclear Apple'
$enL1val   = "${s}4!! Obliterates an entire chunk ${s}r(16x16x384 blocks)."
$enL2val   = "${s}7Throw it. Watch it land. Watch everything disappear."

$deItemVal = 'Nuklearapfel'
$deL1val   = "${s}4!! Vernichtet einen ganzen Chunk ${s}r(16x16x384 Blocks)."
$deL2val   = "${s}7Wirf ihn. Er landet. Alles verschwindet."

function Build-Lines([string]$iv, [string]$l1, [string]$l2) {
    @(
        "  `"item.ultimate_apple_mod.nuclear_apple`": `"$iv`","
        "  `"tooltip.ultimate_apple_mod.nuclear_apple.line1`": `"$l1`","
        "  `"tooltip.ultimate_apple_mod.nuclear_apple.line2`": `"$l2`""
    ) -join "`n"
}

$enBlock = Build-Lines $enItemVal $enL1val $enL2val
$deBlock = Build-Lines $deItemVal $deL1val $deL2val

$files = Get-ChildItem -Path $langDir -Filter "*.json"

foreach ($file in $files) {
    $content = [System.IO.File]::ReadAllText($file.FullName, [System.Text.Encoding]::UTF8)

    $pattern = '(?m)^\s+"item\.ultimate_apple_mod\.nuclear_apple":[^\n]*\n\s+"tooltip\.ultimate_apple_mod\.nuclear_apple\.line1":[^\n]*\n\s+"tooltip\.ultimate_apple_mod\.nuclear_apple\.line2":[^\n]*'
    $replacement = if ($file.BaseName -like 'de_*') { $deBlock } else { $enBlock }

    if ($content -match $pattern) {
        $newContent = $content -replace $pattern, $replacement
        [System.IO.File]::WriteAllText($file.FullName, $newContent, [System.Text.Encoding]::UTF8)
        Write-Host "  Fixed  $($file.Name)"
    } else {
        Write-Host "  SKIP   $($file.Name)" -ForegroundColor Yellow
    }
}

Write-Host ""
Write-Host "Byte check: en_us.json nuclear line1 value (expect C2 A7 = UTF-8 section sign)..."
$path = "$langDir\en_us.json"
$bytes = [System.IO.File]::ReadAllBytes($path)
$key = [System.Text.Encoding]::UTF8.GetBytes('nuclear_apple.line1": "')
$pos = -1
for ($i = 0; $i -lt $bytes.Length - $key.Length; $i++) {
    $m = $true
    for ($j = 0; $j -lt $key.Length; $j++) { if ($bytes[$i+$j] -ne $key[$j]) { $m = $false; break } }
    if ($m) { $pos = $i + $key.Length; break }
}
$hex = $bytes[$pos..($pos+9)] | ForEach-Object { "{0:X2}" -f $_ }
Write-Host "Got:      $($hex -join ' ')"
Write-Host "Expected: C2 A7 34 21 21 20 4F 62 6C 69  (section+4!! Obli)"
$ok = ($bytes[$pos] -eq 0xC2 -and $bytes[$pos+1] -eq 0xA7 -and $bytes[$pos+2] -eq 0x34)
if ($ok) { Write-Host "CORRECT - section sign properly encoded as C2 A7." -ForegroundColor Green }
else     { Write-Host "WRONG - encoding issue remains!" -ForegroundColor Red }
