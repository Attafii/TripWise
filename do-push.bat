@echo off
cd /d "C:\Users\Utilisateur\Desktop\Java's Project\TripWise"
echo === Current Branch === > git-result.txt
git branch --show-current >> git-result.txt 2>&1
echo. >> git-result.txt
echo === Git Status === >> git-result.txt
git status --short >> git-result.txt 2>&1
echo. >> git-result.txt
echo === Adding all files === >> git-result.txt
git add -A >> git-result.txt 2>&1
echo. >> git-result.txt
echo === Committing === >> git-result.txt
git commit -m "Traveler + Flight Operations - Dashboard, Bookings, Seat Selection, Email, Analytics" >> git-result.txt 2>&1
echo. >> git-result.txt
echo === Checking out branch === >> git-result.txt
git checkout -B Eya---Traveler-+-Flight-operations >> git-result.txt 2>&1
echo. >> git-result.txt
echo === Force Pushing to GitHub === >> git-result.txt
git push -f origin Eya---Traveler-+-Flight-operations >> git-result.txt 2>&1
echo. >> git-result.txt
echo === Done === >> git-result.txt
