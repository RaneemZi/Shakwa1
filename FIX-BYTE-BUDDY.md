# Fix ByteBuddy Dependency Error

## Problem
The error occurs because Maven cached a failed download attempt for `net.bytebuddy:byte-buddy:jar:1.17.8`. The corrupted cache prevents Maven from retrying the download.

## Solution

### Option 1: Run the Fix Script (Recommended)

1. Open PowerShell in the project root directory
2. Run the fix script:
   ```powershell
   .\fix-bytebuddy-cache.ps1
   ```

### Option 2: Manual Fix

1. **Delete the corrupted cache:**
   - Navigate to: `C:\Users\HP\.m2\repository\net\bytebuddy\byte-buddy\1.17.8`
   - Delete the entire `1.17.8` folder

2. **Clear Maven cache markers:**
   - Delete any `.lastUpdated` files in the byte-buddy directories
   - Delete any `_remote.repositories` files in the byte-buddy directories

3. **Force Maven to update:**
   ```powershell
   cd Shakwa
   .\mvnw.cmd clean install -U
   ```

### Option 3: Clear All ByteBuddy Cache

If the above doesn't work, delete the entire byte-buddy directory:
```powershell
Remove-Item -Path "$env:USERPROFILE\.m2\repository\net\bytebuddy" -Recurse -Force
```

Then run:
```powershell
cd Shakwa
.\mvnw.cmd dependency:resolve -U
```

## After Fixing

1. **Refresh your IDE:**
   - In Eclipse/STS: Right-click project → Maven → Update Project (check "Force Update of Snapshots/Releases")
   - In IntelliJ: Right-click `pom.xml` → Maven → Reload Project

2. **Verify the fix:**
   - The error should disappear
   - Maven should successfully download the ByteBuddy dependency

## Prevention

The `pom.xml` has been updated to explicitly manage the ByteBuddy version, which should help prevent this issue in the future.

