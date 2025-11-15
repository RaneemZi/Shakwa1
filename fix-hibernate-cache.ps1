# PowerShell script to clear Maven cached dependency failures for Hibernate
# This script removes the failed artifact markers from Maven local repository

$mavenRepo = "$env:USERPROFILE\.m2\repository"

Write-Host "Maven Local Repository: $mavenRepo" -ForegroundColor Cyan

# Hibernate artifact to clear
$failedArtifacts = @(
    "org\hibernate\orm\hibernate-core\6.5.3.Final"
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
    Where-Object { $_.FullName -match "hibernate-core" } |
    ForEach-Object {
        Write-Host "  Removing: $($_.FullName)" -ForegroundColor Red
        Remove-Item -Path $_.FullName -Force -ErrorAction SilentlyContinue
    }

Write-Host "`nDone! Now try to refresh your Maven project in your IDE." -ForegroundColor Green
Write-Host "In Eclipse/STS: Right-click project -> Maven -> Update Project (check 'Force Update of Snapshots/Releases')" -ForegroundColor Cyan
Write-Host "Or run: .\mvnw.cmd clean install -U" -ForegroundColor Cyan

