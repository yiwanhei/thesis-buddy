param($account = "root", $password = "123456")
$body = @{account=$account;password=$password} | ConvertTo-Json
try {
    $result = Invoke-RestMethod -Uri "http://localhost:8080/api/admin/auth/login" -Method Post -Body $body -ContentType "application/json"
    Write-Output "SUCCESS: $($result | ConvertTo-Json -Compress)"
} catch {
    Write-Output "ERROR: $_"
}
