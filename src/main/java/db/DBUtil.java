package db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public class DBUtil {

    private static final HikariDataSource dataSource;

    static {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://localhost:3306/assetdb?serverTimezone=Asia/Seoul");
        config.setUsername("root");
        config.setPassword("119rnwheo~");

        config.setMaximumPoolSize(20);       // 동시에 유지할 최대 연결 수
        config.setMinimumIdle(5);            // 최소로 항상 대기시켜둘 연결 수
        config.setConnectionTimeout(10000);  // 풀에서 연결 못 받으면 10초 후 예외
        config.setIdleTimeout(300000);       // 5분 이상 안 쓰면 idle 연결 회수
        config.setMaxLifetime(1800000);      // 연결 하나의 최대 수명 30분 (오래된 연결 자동 교체)

        dataSource = new HikariDataSource(config);
    }

    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }
}