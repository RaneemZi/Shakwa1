# Script to fix byte-buddy dependency issue
$mavenRepo = "$env:USERPROFILE\.m2\repository"

Write-Host "Fixing byte-buddy dependency issue..." -ForegroundColor Yellow

# Remove the corrupted byte-buddy directory
$byteBuddyPath = Join-Path $mavenRepo "net\bytebuddy\byte-buddy\1.17.8"
if (Test-Path $byteBuddyPath) {
    Write-Host "Removing corrupted byte-buddy cache: $byteBuddyPath" -ForegroundColor Red
    Remove-Item -Path $byteBuddyPath -Recurse -Force -ErrorAction SilentlyContinue
    Write-Host "Removed successfully!" -ForegroundColor Green
} else {
    Write-Host "Byte-buddy cache not found (may have been already removed)" -ForegroundColor Gray
}

# Also remove any .lastUpdated files
Get-ChildItem -Path $mavenRepo -Filter "*.lastUpdated" -Recurse -ErrorAction SilentlyContinue | 
    Where-Object { $_.DirectoryName -match "byte-buddy" } |
    ForEach-Object {
        Write-Host "Removing: $($_.FullName)" -ForegroundColor Red
        Remove-Item -Path $_.FullName -Force -ErrorAction SilentlyContinue
    }

# Remove _remote.repositories files
Get-ChildItem -Path $mavenRepo -Filter "_remote.repositories" -Recurse -ErrorAction SilentlyContinue | 
    Where-Object { $_.FullName -match "byte-buddy" } |
    ForEach-Object {
        Write-Host "Removing: $($_.FullName)" -ForegroundColor Red
        Remove-Item -Path $_.FullName -Force -ErrorAction SilentlyContinue
    }

Write-Host "`nDone! Now:" -ForegroundColor Green
Write-Host "1. Refresh Maven project in your IDE" -ForegroundColor Cyan
Write-Host "2. Or run: cd Shakwa; .\mvnw.cmd clean install -U" -ForegroundColor Cyan

