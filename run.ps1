# -------------------------------------------------------------
# run.ps1 – one‑click starter for HireSenseAI
# -------------------------------------------------------------
$projectRoot = "D:\Projects\HireSenseAI\hiresenseai\hiresenseai"

$envPath = Join-Path $projectRoot ".env"
if (Test-Path $envPath) {
    Write-Host "Loading variables from .env ..."
    Get-Content $envPath | ForEach-Object {
        if ($_ -match '^\s*([^#=]+)=(.*)$') {
            $name  = $matches[1].Trim()
            $value = $matches[2].Trim()
            Set-Item -Path ("Env:" + $name) -Value $value
        }
    }
}
else {
    Write-Host ".env not found – using built‑in defaults."
}

Write-Host "`nStarting HireSenseAI (Ctrl+C to stop)…`n"
& "$projectRoot\mvnw.cmd" spring-boot:run
