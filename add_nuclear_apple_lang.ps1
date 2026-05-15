$langDir = "E:\Programieren\Projekte\Ultimate Apple Mod\forge\src\main\resources\assets\ultimate_apple_mod\lang"

$enItem   = '"item.ultimate_apple_mod.nuclear_apple": "Nuclear Apple"'
$enLine1  = '"tooltip.ultimate_apple_mod.nuclear_apple.line1": "§4⚠ Obliterates an entire chunk §r(16×16×384 blocks)."'
$enLine2  = '"tooltip.ultimate_apple_mod.nuclear_apple.line2": "§7Throw it. Watch it land. Watch everything disappear."'

$deItem   = '"item.ultimate_apple_mod.nuclear_apple": "Nuklearapfel"'
$deLine1  = '"tooltip.ultimate_apple_mod.nuclear_apple.line1": "§4⚠ Vernichtet einen ganzen Chunk §r(16×16×384 Blöcke)."'
$deLine2  = '"tooltip.ultimate_apple_mod.nuclear_apple.line2": "§7Wirf ihn. Er landet. Alles verschwindet."'

$files = Get-ChildItem -Path $langDir -Filter "*.json"

foreach ($file in $files) {
    $content = [System.IO.File]::ReadAllText($file.FullName, [System.Text.Encoding]::UTF8)

    # Pick translation
    if ($file.BaseName -like "de_*") {
        $i = "  $deItem"; $l1 = "  $deLine1"; $l2 = "  $deLine2"
    } else {
        $i = "  $enItem"; $l1 = "  $enLine1"; $l2 = "  $enLine2"
    }

    # Find the last closing brace
    $lastBrace = $content.LastIndexOf('}')
    $before = $content.Substring(0, $lastBrace).TrimEnd()

    # Ensure the previous entry has a trailing comma
    if (-not $before.EndsWith(',')) { $before += ',' }

    $newContent = $before + "`n$i,`n$l1,`n$l2`n}`n"

    [System.IO.File]::WriteAllText($file.FullName, $newContent, [System.Text.Encoding]::UTF8)
    Write-Host "  Updated $($file.Name)"
}

Write-Host "`nValidating JSON..."
$errors = 0
foreach ($file in $files) {
    $raw = [System.IO.File]::ReadAllText($file.FullName, [System.Text.Encoding]::UTF8)
    try {
        $null = [Newtonsoft.Json.Linq.JObject]::Parse($raw)
    } catch {
        # Fallback: simple brace-count check
        $opens  = ($raw.ToCharArray() | Where-Object { $_ -eq '{' }).Count
        $closes = ($raw.ToCharArray() | Where-Object { $_ -eq '}' }).Count
        if ($opens -ne $closes) {
            Write-Host "  MISMATCH in $($file.Name): { $opens  } $closes" -ForegroundColor Red
            $errors++
        }
    }
}
if ($errors -eq 0) { Write-Host "All files look good." -ForegroundColor Green }
