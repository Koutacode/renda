$ErrorActionPreference = "Stop"

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$projectDir = Split-Path -Parent $scriptDir

Push-Location $projectDir
try {
    .\\gradlew.bat clean assembleDebug

    $srcApk = Join-Path $projectDir "app\\build\\outputs\\apk\\debug\\app-debug.apk"
    if (!(Test-Path $srcApk)) {
        throw "APK not found: $srcApk"
    }

    $outDir = Join-Path $projectDir "output"
    New-Item -ItemType Directory -Force -Path $outDir | Out-Null

    $dstApk = Join-Path $outDir "renda-debug.apk"
    Copy-Item -Force $srcApk $dstApk

    Write-Host "Wrote: $dstApk"
} finally {
    Pop-Location
}

