package org.example.thesisbuddy.service.impl.admin;

import org.example.thesisbuddy.common.JwtUtil;
import org.example.thesisbuddy.dao.AdminDao;
import org.example.thesisbuddy.dto.admin.AdminLoginDTO;
import org.example.thesisbuddy.entity.AdminAccount;

import org.example.thesisbuddy.common.Result;
import org.example.thesisbuddy.service.admin.AdminAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AuthServiceImpl implements AdminAuthService {

    @Autowired
    private AdminDao adminDao;

    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public Result login(AdminLoginDTO loginDTO) {
        AdminAccount admin = adminDao.selectByAccount(loginDTO.getAccount());
        if (admin == null) return Result.error("账号不存在");
        if (!passwordEncoder.matches(loginDTO.getPassword(), admin.getPasswordHash())) return Result.error("密码错误");

        // 生成JWT Token
        String token = jwtUtil.generateToken(admin.getAdminId(), admin.getAccount());
        admin.setPasswordHash(null);

        Map<String, Object> data = new HashMap<>();
        data.put("admin", admin);
        data.put("token", token);
        return Result.success(data);
    }
}
