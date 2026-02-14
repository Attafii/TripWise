@echo off
cd /d "C:\Users\Utilisateur\Desktop\Java's Project\TripWise"
echo === Current Branch ===
git branch --show-current
echo.
echo === Adding all files ===
git add -A
echo.
echo === Committing ===
git commit -m "Traveler + Flight Operations - Dashboard, Bookings, Seat Selection, Email, Analytics"
echo.
echo === Creating and switching to branch ===
git checkout -b Eya---Traveler-+-Flight-operations 2>nul || git checkout Eya---Traveler-+-Flight-operations
echo.
echo === Pushing to GitHub ===
git push -u origin Eya---Traveler-+-Flight-operations
echo.
echo === Done ===
pause
