Write-Output "========================================="
Write-Output "  全面端到端测试脚本 - ThesisBuddy"
Write-Output "========================================="
Write-Output ""

$BASE = "http://localhost:8080/api/student/team"
$HEADERS = @{"Content-Type"="application/json"}

# 辅助函数
function Test-Result($step, $result) {
    if ($result.code -eq 200) {
        Write-Output "[PASS] Step $step - $($result.message)"
    } else {
        Write-Output "[FAIL] Step $step - $($result.message)"
    }
}

# ===== 1. 获取队伍ID =====
Write-Output "=== 1. 查询已创建的队伍 ==="
$myTeam = Invoke-RestMethod -Uri "$BASE/my-team?studentId=1" -Method GET
$teamId = $myTeam.data.team.teamId
Write-Output "[INFO] 创建的队伍ID: $teamId, 名称: $($myTeam.data.team.teamName)"
$teamId | Out-File -FilePath G:\shujuhuancun\IDEA\start\ThesisBuddy\team_id.txt

# ===== 2. 学生2查看自己的队伍状态 =====
Write-Output "`n=== 2. 学生2(王光震)查看队伍状态 ==="
$r2 = Invoke-RestMethod -Uri "$BASE/my-team?studentId=2" -Method GET
if ($r2.data -eq $null) { Write-Output "[PASS] 学生2没有队伍" }
else { Write-Output "[FAIL] 学生2已经有队伍了" }

# ===== 3. 搜索学生(老问题测试) =====
Write-Output "`n=== 3. 搜索学生功能(按学号/姓名搜索) ==="
$r3 = Invoke-RestMethod -Uri "$BASE/search-students?keyword=2023126054&studentId=1" -Method GET
$r3.data | ConvertTo-Json -Depth 3 | Write-Output
if ($r3.data.Count -gt 0 -and $r3.data[0].account -eq "2023126054") {
    Write-Output "[PASS] 按学号搜索成功"
} else {
    Write-Output "[FAIL] 按学号搜索失败"
}

$r3b = Invoke-RestMethod -Uri "$BASE/search-students?keyword=王&studentId=1" -Method GET
$r3b.data | ConvertTo-Json -Depth 3 | Write-Output
if ($r3b.data.Count -gt 0) {
    Write-Output "[PASS] 按姓名搜索成功: 找到 $($r3b.data.Count) 人"
} else {
    Write-Output "[FAIL] 按姓名搜索失败"
}

# ===== 4. 发送邀请给学生2 =====
Write-Output "`n=== 4. 队长邀请学生2(王光震)加入 ==="
$r4 = Invoke-RestMethod -Uri "$BASE/invite" -Method POST -ContentType "application/json" -Body "{`"teamId`":$teamId,`"inviterId`":1,`"inviteeId`":2}"
Test-Result "4" $r4

# ===== 5. 查看学生2收到的邀请 =====
Write-Output "`n=== 5. 学生2查看收到的邀请 ==="
$r5 = Invoke-RestMethod -Uri "$BASE/invites?studentId=2" -Method GET
$r5.data | ConvertTo-Json -Depth 3 | Write-Output
$inviteId = $r5.data[0].inviteId
Write-Output "[INFO] 邀请ID: $inviteId"

# ===== 6. 学生2接受邀请 =====
Write-Output "`n=== 6. 学生2接受邀请 ==="
$r6 = Invoke-RestMethod -Uri "$BASE/invite/handle?inviteId=$inviteId&action=accepted" -Method PUT
Test-Result "6" $r6

# ===== 7. 验证队伍成员 =====
Write-Output "`n=== 7. 验证队伍成员(应2人) ==="
$r7 = Invoke-RestMethod -Uri "$BASE/my-team?studentId=1" -Method GET
Write-Output "[INFO] 当前成员数: $($r7.data.memberCount)"
if ($r7.data.memberCount -eq 2) { Write-Output "[PASS] 成员数正确" }

# ===== 8. 学生3提交加入申请 =====
Write-Output "`n=== 8. 学生3(王唯赫)提交加入申请 ==="
$r8 = Invoke-RestMethod -Uri "$BASE/join-request" -Method POST -ContentType "application/json" -Body "{`"studentId`":3,`"teamId`":$teamId,`"reason`":`"我想加入队伍`"}"
Test-Result "8" $r8

# ===== 9. 队长查看加入申请 =====
Write-Output "`n=== 9. 队长查看加入申请列表 ==="
$r9 = Invoke-RestMethod -Uri "$BASE/join-requests?teamId=$teamId" -Method GET
$r9.data | ConvertTo-Json -Depth 3 | Write-Output
$requestId = $r9.data[0].id
Write-Output "[INFO] 申请ID: $requestId"

# ===== 10. 队长同意申请 =====
Write-Output "`n=== 10. 队长同意加入申请 ==="
$r10 = Invoke-RestMethod -Uri "$BASE/join-request/handle?requestId=$requestId&action=accepted&teamId=$teamId&captainId=1" -Method PUT
Test-Result "10" $r10

# ===== 11. 验证3人满员 =====
Write-Output "`n=== 11. 验证队伍满员(应3人) ==="
$r11 = Invoke-RestMethod -Uri "$BASE/my-team?studentId=1" -Method GET
Write-Output "[INFO] 当前成员数: $($r11.data.memberCount)"
if ($r11.data.memberCount -eq 3) { Write-Output "[PASS] 队伍满员" }

# ===== 12. 发送邀请给学生4(但队伍已满) =====
Write-Output "`n=== 12. 尝试邀请学生4(苗一迪)(队伍已满) ==="
try {
    $r12 = Invoke-RestMethod -Uri "$BASE/invite" -Method POST -ContentType "application/json" -Body "{`"teamId`":$teamId,`"inviterId`":1,`"inviteeId`":4}"
    $r12 | ConvertTo-Json -Depth 3 | Write-Output
} catch {
    Write-Output "[INFO] 预期失败: 队伍已满"
}

Write-Output "`n========== 测试完成 =========="
