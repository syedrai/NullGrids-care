$s = New-Object Microsoft.PowerShell.Commands.WebRequestSession
$r = Invoke-WebRequest -Uri 'http://localhost:8080/login' -WebSession $s -UseBasicParsing
$csrf = [regex]::Match($r.Content, 'name="_csrf" value="([^"]+)"').Groups[1].Value
Invoke-WebRequest -Uri 'http://localhost:8080/login' -Method POST -Body @{email='admin@medicare.com';password='Admin@123';_csrf=$csrf} -WebSession $s -UseBasicParsing
$page = (Invoke-WebRequest -Uri 'http://localhost:8080/patient/book' -WebSession $s -UseBasicParsing).Content
$matches = [regex]::Matches($page, '<button[^>]*doctor-card[^>]*>')
foreach ($m in $matches) { Write-Host $m.Value }
Write-Host "---doctorId input---"
$inp = [regex]::Match($page, '<input[^>]*doctorId[^>]*>')
Write-Host $inp.Value
