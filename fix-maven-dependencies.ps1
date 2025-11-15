# PowerShell script to clear Maven cached dependency failures
# This script removes the failed artifact markers from Maven local repository

$mavenRepo = "$env:USERPROFILE\.m2\repository"

Write-Host "Maven Local Repository: $mavenRepo" -ForegroundColor Cyan

# List of failed artifacts to clear
$failedArtifacts = @(
    "com\knuddels\jtokkit\1.1.0",
    "org\bouncycastle\bcprov-jdk18on\1.81",
    "org\apache\poi\poi-ooxml-lite\5.4.1",
    "net\bytebuddy\byte-buddy\1.17.8"
)

Write-Host "`nClearing cached failures for the following artifacts:" -ForegroundColor Yellow
foreach ($artifact in $failedArtifacts) {
    $artifactPath = Join-Path $mavenRepo $artifact
    if (Test-Path $artifactPath) {
        Write-Host "  Removing: $artifactPath" -ForegroundColor Red
        Remove-Item -Path $artifactPath -Recurse -Force -ErrorAction SilentlyContinue
    } else {
        Write-Host "  Not found: $artifactPath" -ForegroundColor Gray
    }
}

# Also clear any _remote.repositories files that might contain failure markers
Write-Host "`nClearing remote repository markers..." -ForegroundColor Yellow
Get-ChildItem -Path $mavenRepo -Filter "_remote.repositories" -Recurse -ErrorAction SilentlyContinue | 
    Where-Object { $_.FullName -match "(jtokkit|bcprov|poi-ooxml-lite|byte-buddy)" } |
    ForEach-Object {
        Write-Host "  Removing: $($_.FullName)" -ForegroundColor Red
        Remove-Item -Path $_.FullName -Force -ErrorAction SilentlyContinue
    }

Write-Host "`nDone! Now try to refresh your Maven project in your IDE." -ForegroundColor Green
Write-Host "In Eclipse/STS: Right-click project -> Maven -> Update Project (check 'Force Update of Snapshots/Releases')" -ForegroundColor Cyan
Write-Host "Or run: mvn clean install -U" -ForegroundColor Cyan

