# Team页面功能改进 - 完成总结

## ✅ 已完成的功能改进

### 1. 创建队伍功能改进
- ✅ **去除队名输入**：创建队伍时不再需要手动输入队名，系统自动生成（格式：队伍+队长ID）
- ✅ **显示指导教师信息**：选题下拉列表中显示剩余名额和指导教师姓名
- ✅ **30分钟名额保留**：创建队伍后自动启动30分钟倒计时，超时自动释放名额

### 2. 加入队伍功能改进
- ✅ **搜索队伍**：学生可通过队长学号或选题名称搜索队伍
- ✅ **申请理由选填**：申请理由改为非必填项
- ✅ **队长审核机制**：队长收到加入请求，可查看申请人姓名和申请理由
- ✅ **同意/拒绝操作**：队长可同意或拒绝加入请求

### 3. 消息+邀请功能
- ✅ **位置调整**：消息区域移到队伍页面下半部分
- ✅ **队长可见申请消息**：只有队长可以看到申请加入的消息
- ✅ **邀请同班同学**：队长可通过学号或姓名搜索同班学生并发送邀请
- ✅ **队员可见邀请**：队员可以看到收到的邀请并接受/拒绝

### 4. 队伍详情改进
- ✅ **隐藏队名和状态**：队伍详情中不再显示队名和状态字段
- ✅ **显示姓名**：成员列表和队长信息都以姓名显示（而非学号）
- ✅ **直接编辑**：去除"编辑队伍信息"按钮，支持直接在队伍详情中修改数据
- ✅ **职责分配**：添加职责输入框，队长和队员都可以修改自己的职责
- ✅ **提交后锁定**：队伍状态变为confirmed后，所有信息锁定不可修改

## 📋 数据库变更

### 新增表
1. **team_join_request** - 队伍加入请求表
   - id: 主键
   - team_id: 队伍ID
   - student_id: 申请学生ID
   - reason: 申请理由
   - status: 状态（pending/accepted/rejected）
   - create_time: 申请时间

### 修改表
1. **team_member** - 添加task字段
   - task VARCHAR(500): 成员职责

2. **team_info** - 添加topic_id字段
   - topic_id INT: 选题ID

## 🔧 后端API新增接口

### Controller层新增接口
1. `GET /api/student/team/search?keyword={keyword}` - 搜索队伍
2. `POST /api/student/team/join-request` - 提交加入请求
3. `GET /api/student/team/join-requests?teamId={teamId}` - 获取队伍加入请求列表
4. `PUT /api/student/team/join-request/handle` - 处理加入请求
5. `GET /api/student/team/search-students` - 搜索同班学生
6. `PUT /api/student/team/member/task` - 更新成员职责

### Service层新增方法
1. `searchTeams(String keyword)` - 搜索队伍
2. `submitJoinRequest(int studentId, int teamId, String reason)` - 提交加入请求
3. `getJoinRequests(int teamId)` - 获取加入请求列表
4. `handleJoinRequest(int requestId, String action, int teamId, int captainId)` - 处理加入请求
5. `searchStudents(String keyword, int studentId)` - 搜索同班学生
6. `updateMemberTask(int teamId, int studentId, String task)` - 更新成员职责

### DAO层新增方法
1. `searchTeams(String keyword)` - 搜索队伍SQL
2. `selectJoinRequests(int teamId)` - 查询加入请求
3. `insertJoinRequest(...)` - 插入加入请求
4. `updateJoinRequestStatus(...)` - 更新请求状态
5. `searchStudents(...)` - 搜索学生
6. `updateMemberTask(...)` - 更新成员职责
7. `selectStudentName(int studentId)` - 查询学生姓名
8. `selectStudentClassId(int studentId)` - 查询学生班级ID

## 📝 前端改动

### TeamView.vue 主要改动
1. 移除队名输入框
2. 选题下拉列表显示教师信息
3. 加入队伍表单改为搜索模式
4. 添加队长审核区域（显示申请人姓名和理由）
5. 添加邀请同学功能（搜索同班学生）
6. 队伍详情显示优化（显示姓名、职责输入框）
7. 移除编辑模式，支持直接编辑
8. 添加锁定状态判断

## 🚀 部署步骤

### 1. 执行数据库迁移
```bash
# Flyway会自动执行迁移文件
V4__add_team_join_request_table.sql
```

或者手动执行SQL：
```sql
-- 创建加入请求表
CREATE TABLE IF NOT EXISTS team_join_request (
    id INT AUTO_INCREMENT PRIMARY KEY,
    team_id INT NOT NULL,
    student_id INT NOT NULL,
    reason VARCHAR(500),
    status VARCHAR(20) DEFAULT 'pending',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 添加task字段
ALTER TABLE team_member ADD COLUMN task VARCHAR(500) AFTER role_in_team;

-- 添加topic_id字段
ALTER TABLE team_info ADD COLUMN topic_id INT AFTER apply_topic;
```

### 2. 重启后端服务
```bash
# 确保所有代码变更生效
```

### 3. 测试功能
1. 创建队伍（不输入队名，选择题目和人数）
2. 搜索队伍并申请加入
3. 队长审核加入请求
4. 邀请同班同学
5. 修改成员职责
6. 提交申请后验证锁定功能

## ⚠️ 注意事项

1. **数据库迁移**：必须先执行数据库迁移，否则会出现表不存在的错误
2. **班级信息**：搜索同班学生依赖student_account表的class_id字段，确保该字段有正确数据
3. **姓名显示**：确保student_account表中有real_name字段且有数据
4. **名额释放**：30分钟名额释放依赖定时任务，确保ReserveReleaseTask正常运行

## 🐛 已知问题

1. 搜索队伍时的SQL可能需要优化（当前使用子查询）
2. 处理加入请求时使用了遍历方式获取studentId，可以优化为直接查询

## 📊 测试清单

- [ ] 创建队伍（无队名）
- [ ] 30分钟倒计时显示
- [ ] 搜索队伍（通过学号）
- [ ] 搜索队伍（通过选题名称）
- [ ] 申请加入队伍（有理由）
- [ ] 申请加入队伍（无理由）
- [ ] 队长查看加入请求
- [ ] 队长同意加入请求
- [ ] 队长拒绝加入请求
- [ ] 搜索同班学生
- [ ] 发送邀请
- [ ] 接受邀请
- [ ] 拒绝邀请
- [ ] 修改成员职责
- [ ] 提交申请后信息锁定
- [ ] 已锁定状态下无法修改职责
- [ ] 名额超时自动释放
