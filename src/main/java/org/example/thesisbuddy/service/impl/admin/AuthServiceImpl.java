package org.example.thesisbuddy.service.impl.admin;


import org.example.thesisbuddy.dao.AdminDao;
import org.example.thesisbuddy.dto.admin.AdminLoginDTO;
import org.example.thesisbuddy.entity.AdminAccount;

import org.example.thesisbuddy.common.Result;
import org.example.thesisbuddy.service.admin.AdminAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AdminAuthService {

    @Autowired
    private AdminDao adminDao;

    @Override
    public Result login(AdminLoginDTO loginDTO) {
        AdminAccount admin = adminDao.selectByAccount(loginDTO.getAccount());
        if (admin == null) return Result.error("账号不存在");
        if (!admin.getPasswordHash().equals(loginDTO.getPassword())) return Result.error("密码错误");
        admin.setPasswordHash(null);
        return Result.success(admin);
    }
}
