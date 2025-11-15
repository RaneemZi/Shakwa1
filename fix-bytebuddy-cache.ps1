# Fix ByteBuddy cache issue
# This script removes corrupted ByteBuddy artifacts from Maven local repository

$mavenRepo = "$env:USERPROFILE\.m2\repository"
$byteBuddyPath = "$mavenRepo\net\bytebuddy\byte-buddy\1.17.8"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Fixing ByteBuddy Dependency Cache Issue" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Step 1: Remove the corrupted version directory
Write-Host "Step 1: Removing corrupted ByteBuddy cache..." -ForegroundColor Yellow
if (Test-Path $byteBuddyPath) {
    try {
        Remove-Item -Path $byteBuddyPath -Recurse -Force -ErrorAction Stop
        Write-Host "  [OK] Removed: $byteBuddyPath" -ForegroundColor Green
    } catch {
        Write-Host "  [ERROR] Failed to remove: $byteBuddyPath" -ForegroundColor Red
        Write-Host "  Error: $_" -ForegroundColor Red
        Write-Host "  Try closing any IDE or processes using Maven, then run this script again." -ForegroundColor Yellow
    }
} else {
    Write-Host "  [INFO] Path not found: $byteBuddyPath" -ForegroundColor Gray
    Write-Host "  (May have been already removed)" -ForegroundColor Gray
}

# Step 2: Remove any .lastUpdated files (these mark failed downloads)
Write-Host ""
Write-Host "Step 2: Removing .lastUpdated files..." -ForegroundColor Yellow
$lastUpdatedFiles = Get-ChildItem -Path $mavenRepo -Filter "*.lastUpdated" -Recurse -ErrorAction SilentlyContinue | 
    Where-Object { $_.DirectoryName -match "byte-buddy" }
    
if ($lastUpdatedFiles) {
    $lastUpdatedFiles | ForEach-Object {
        try {
            Remove-Item -Path $_.FullName -Force -ErrorAction Stop
            Write-Host "  [OK] Removed: $($_.FullName)" -ForegroundColor Green
        } catch {
            Write-Host "  [WARN] Could not remove: $($_.FullName)" -ForegroundColor Yellow
        }
    }
} else {
    Write-Host "  [INFO] No .lastUpdated files found" -ForegroundColor Gray
}

# Step 3: Remove _remote.repositories files
Write-Host ""
Write-Host "Step 3: Removing _remote.repositories files..." -ForegroundColor Yellow
$remoteRepoFiles = Get-ChildItem -Path $mavenRepo -Filter "_remote.repositories" -Recurse -ErrorAction SilentlyContinue | 
    Where-Object { $_.FullName -match "byte-buddy" }
    
if ($remoteRepoFiles) {
    $remoteRepoFiles | ForEach-Object {
        try {
            Remove-Item -Path $_.FullName -Force -ErrorAction Stop
            Write-Host "  [OK] Removed: $($_.FullName)" -ForegroundColor Green
        } catch {
            Write-Host "  [WARN] Could not remove: $($_.FullName)" -ForegroundColor Yellow
        }
    }
} else {
    Write-Host "  [INFO] No _remote.repositories files found" -ForegroundColor Gray
}

# Step 4: Force Maven update
Write-Host ""
Write-Host "Step 4: Forcing Maven to update dependencies..." -ForegroundColor Yellow
Write-Host ""

$shakwaPath = Join-Path $PSScriptRoot "Shakwa"
if (Test-Path $shakwaPath) {
    Push-Location $shakwaPath
    try {
        if (Test-Path ".\mvnw.cmd") {
            Write-Host "Running: .\mvnw.cmd dependency:resolve -U" -ForegroundColor Cyan
            & .\mvnw.cmd dependency:resolve -U
            if ($LASTEXITCODE -eq 0) {
                Write-Host ""
                Write-Host "  [SUCCESS] Maven dependencies updated successfully!" -ForegroundColor Green
            } else {
                Write-Host ""
                Write-Host "  [WARN] Maven command completed with exit code: $LASTEXITCODE" -ForegroundColor Yellow
                Write-Host "  You may need to refresh your IDE project." -ForegroundColor Yellow
            }
        } else {
            Write-Host "  [INFO] Maven wrapper not found. Please run manually:" -ForegroundColor Yellow
            Write-Host "    cd Shakwa" -ForegroundColor Cyan
            Write-Host "    mvn dependency:resolve -U" -ForegroundColor Cyan
        }
    } catch {
        Write-Host "  [ERROR] Failed to run Maven: $_" -ForegroundColor Red
    } finally {
        Pop-Location
    }
} else {
    Write-Host "  [WARN] Shakwa directory not found. Please run Maven update manually:" -ForegroundColor Yellow
    Write-Host "    cd Shakwa" -ForegroundColor Cyan
    Write-Host "    .\mvnw.cmd dependency:resolve -U" -ForegroundColor Cyan
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Fix Complete!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Next steps:" -ForegroundColor Yellow
Write-Host "1. Refresh your IDE project (Maven -> Update Project)" -ForegroundColor White
Write-Host "2. If errors persist, try: mvn clean install -U" -ForegroundColor White
Write-Host ""

