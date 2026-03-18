$s = New-Object Microsoft.PowerShell.Commands.WebRequestSession
$r = Invoke-WebRequest -Uri 'http://localhost:8080/patient/book' -WebSession $s -UseBasicParsing
$csrf = [regex]::Match($r.Content, 'name="_csrf" value="([^"]+)"').Groups[1].Value
Invoke-WebRequest -Uri 'http://localhost:8080/login' -Method POST -Body @{email='patient@medicare.com';password='Patient@123';_csrf=$csrf} -WebSession $s -UseBasicParsing
$page = (Invoke-WebRequest -Uri 'http://localhost:8080/patient/book' -WebSession $s -UseBasicParsing).Content
$page | Out-File C:\tmp_page.html
Write-Host "Done"
