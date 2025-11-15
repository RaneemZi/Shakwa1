# Script to clear Maven cache for failed dependencies
$mavenRepo = "$env:USERPROFILE\.m2\repository"

Write-Host "Clearing Maven cache for failed dependencies..." -ForegroundColor Yellow

# Remove specific failed artifact directories
$artifacts = @(
    "com\knuddels\jtokkit",
    "org\bouncycastle\bcprov-jdk18on",
    "org\apache\poi\poi-ooxml-lite",
    "net\bytebuddy\byte-buddy"
)

foreach ($artifact in $artifacts) {
    $path = Join-Path $mavenRepo $artifact
    if (Test-Path $path) {
        Write-Host "Removing: $path" -ForegroundColor Red
        Remove-Item -Path $path -Recurse -Force -ErrorAction SilentlyContinue
    }
}

# Remove any .lastUpdated files (these mark failed downloads)
Get-ChildItem -Path $mavenRepo -Filter "*.lastUpdated" -Recurse -ErrorAction SilentlyContinue | 
    Where-Object { $_.DirectoryName -match "(jtokkit|bcprov|poi-ooxml-lite|byte-buddy)" } |
    Remove-Item -Force -ErrorAction SilentlyContinue

Write-Host "`nCache cleared! Now refresh your Maven project in IDE." -ForegroundColor Green

