package com.ops.monitor.config;

import com.ops.monitor.entity.Server;
import com.ops.monitor.entity.User;
import com.ops.monitor.repository.ServerRepository;
import com.ops.monitor.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ServerRepository serverRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           ServerRepository serverRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.serverRepository = serverRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // 初始化或修复 admin 用户
        User admin = userRepository.findByUsername("admin").orElseGet(() -> {
            User u = new User();
            u.setUsername("admin");
            u.setRole("ADMIN");
            return u;
        });

        // 检查密码是否为有效的 BCrypt 格式且密码为 "admin"，如果不是则重新编码
        boolean needReset = false;
        try {
            if (admin.getPassword() == null || !passwordEncoder.matches("admin", admin.getPassword())) {
                needReset = true;
            }
        } catch (Exception e) {
            needReset = true;
        }
        if (needReset) {
            admin.setPassword(passwordEncoder.encode("admin"));
            userRepository.save(admin);
            System.out.println("[DataInit] admin 用户密码已重置 (BCrypt)");
        }

        // 初始化默认服务器（供本地指标采集使用）
        if (serverRepository.count() == 0) {
            Server server = new Server();
            server.setName("localhost");
            server.setHost("127.0.0.1");
            server.setOs(System.getProperty("os.name"));
            server.setStatus("ONLINE");
            serverRepository.save(server);
            System.out.println("[DataInit] 默认服务器 localhost 已创建");
        }
    }
}
