Set-Location "C:\Users\Utilisateur\Desktop\Java's Project\TripWise"
$output = @()

$output += "=== Current Branch ==="
$output += git branch --show-current 2>&1

$output += "`n=== Git Status ==="
$output += git status 2>&1

$output += "`n=== Adding all files ==="
$output += git add -A 2>&1

$output += "`n=== Committing ==="
$output += git commit -m "Traveler + Flight Operations - Dashboard complete, Bookings, Seat Selection, Email notifications, Analytics" 2>&1

$output += "`n=== Checkout/Create Branch ==="
$branch = "Eya---Traveler-+-Flight-operations"
git checkout $branch 2>$null
if ($LASTEXITCODE -ne 0) {
    $output += git checkout -b $branch 2>&1
} else {
    $output += "Switched to branch $branch"
}

$output += "`n=== Pushing to GitHub ==="
$output += git push -u origin $branch 2>&1

$output += "`n=== Done ==="

$output | Out-File -FilePath "C:\Users\Utilisateur\Desktop\Java's Project\TripWise\git-output.txt" -Encoding UTF8
$output | Write-Host
