package com.leenow.wrench.dynamic.config.center.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 动态配置中心 Redis 注册配置属性类
 * 
 * <p>用于读取和存储 application.yml 中的 Redis 连接相关配置。</p>
 * 
 * <h3>配置示例：</h3>
 * <pre>
 * {@code
 * wl:
 *   wrench:
 *     config:
 *       register:
 *         host: 127.0.0.1          # Redis 服务器地址
 *         port: 6379               # Redis 端口
 *         password: yourPassword   # Redis 密码（可选）
 *         poolSize: 64             # 连接池大小
 *         minIdleSize: 10          # 最小空闲连接数
 * }
 * </pre>
 * 
 * <h3>主要功能：</h3>
 * <ul>
 *     <li>配置 Redis 连接参数（host, port, password）</li>
 *     <li>配置连接池参数（poolSize, minIdleSize, idleTimeout）</li>
 *     <li>配置连接超时和重试参数（connectTimeout, retryAttempts, retryInterval）</li>
 *     <li>配置连接保活参数（pingInterval, keepAlive）</li>
 * </ul>
 * 
 * @author WangLi
 * @see org.springframework.boot.context.properties.ConfigurationProperties
 */
@ConfigurationProperties(prefix = "wl.wrench.config.register", ignoreInvalidFields = true)
public class DynamicConfigCenterRegisterProperties {

    /** 
     * Redis 服务器主机地址
     * 
     * <p>例如：{@code "127.0.0.1"}, {@code "redis.example.com"}</p>
     */
    private String host;
    
    /** 
     * Redis 服务器端口号
     * 
     * <p>默认端口：{@code 6379}</p>
     */
    private int port;
    
    /** 
     * Redis 密码
     * 
     * <p>如果 Redis 未配置密码，此字段可为空</p>
     */
    private String password;
    
    /** 
     * 连接池大小
     * 
     * <p>控制最大并发连接数，默认值：{@code 64}</p>
     * <p>建议根据应用并发量调整此值</p>
     */
    private int poolSize = 64;
    
    /** 
     * 最小空闲连接数
     * 
     * <p>连接池中保持的最小空闲连接数，默认值：{@code 10}</p>
     * <p>保证有足够的连接可用，避免频繁创建连接</p>
     */
    private int minIdleSize = 10;
    
    /** 
     * 连接最大空闲时间（毫秒）
     * 
     * <p>超过此时间的空闲连接将被关闭，默认值：{@code 10000ms (10 秒)}</p>
     * <p>及时释放不用的连接，节省资源</p>
     */
    private int idleTimeout = 10000;
    
    /** 
     * 连接超时时间（毫秒）
     * 
     * <p>建立连接的最大等待时间，默认值：{@code 10000ms (10 秒)}</p>
     * <p>超时后会抛出连接超时异常</p>
     */
    private int connectTimeout = 10000;
    
    /** 
     * 连接重试次数
     * 
     * <p>连接失败后的最大重试次数，默认值：{@code 3}</p>
     * <p>提高连接的可靠性</p>
     */
    private int retryAttempts = 3;
    
    /** 
     * 连接重试间隔时间（毫秒）
     * 
     * <p>两次重试之间的等待时间，默认值：{@code 1000ms (1 秒)}</p>
     * <p>避免频繁重试导致服务器压力过大</p>
     */
    private int retryInterval = 1000;
    
    /** 
     * Ping 检查间隔时间（毫秒）
     * 
     * <p>定期检查连接是否可用的时间间隔，默认值：{@code 0}（不进行定期检查）</p>
     * <p>设置为正数可以及时发现和关闭无效连接</p>
     */
    private int pingInterval = 0;
    
    /** 
     * 是否保持长连接
     * 
     * <p>启用 TCP keepalive 机制，默认值：{@code true}</p>
     * <p>保持连接活跃，防止被防火墙关闭</p>
     */
    private boolean keepAlive = true;

    // ==================== Getter 和 Setter 方法 ====================

    /**
     * 获取 Redis 主机地址
     * 
     * @return Redis 主机地址
     */
    public String getHost() {
        return host;
    }

    /**
     * 设置 Redis 主机地址
     * 
     * @param host Redis 主机地址
     */
    public void setHost(String host) {
        this.host = host;
    }

    /**
     * 获取 Redis 端口号
     * 
     * @return Redis 端口号
     */
    public int getPort() {
        return port;
    }

    /**
     * 设置 Redis 端口号
     * 
     * @param port Redis 端口号
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * 获取 Redis 密码
     * 
     * @return Redis 密码
     */
    public String getPassword() {
        return password;
    }

    /**
     * 设置 Redis 密码
     * 
     * @param password Redis 密码
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * 获取连接池大小
     * 
     * @return 连接池大小
     */
    public int getPoolSize() {
        return poolSize;
    }

    /**
     * 设置连接池大小
     * 
     * @param poolSize 连接池大小
     */
    public void setPoolSize(int poolSize) {
        this.poolSize = poolSize;
    }

    /**
     * 获取最小空闲连接数
     * 
     * @return 最小空闲连接数
     */
    public int getMinIdleSize() {
        return minIdleSize;
    }

    /**
     * 设置最小空闲连接数
     * 
     * @param minIdleSize 最小空闲连接数
     */
    public void setMinIdleSize(int minIdleSize) {
        this.minIdleSize = minIdleSize;
    }

    /**
     * 获取连接最大空闲时间
     * 
     * @return 连接最大空闲时间（毫秒）
     */
    public int getIdleTimeout() {
        return idleTimeout;
    }

    /**
     * 设置连接最大空闲时间
     * 
     * @param idleTimeout 连接最大空闲时间（毫秒）
     */
    public void setIdleTimeout(int idleTimeout) {
        this.idleTimeout = idleTimeout;
    }

    /**
     * 获取连接超时时间
     * 
     * @return 连接超时时间（毫秒）
     */
    public int getConnectTimeout() {
        return connectTimeout;
    }

    /**
     * 设置连接超时时间
     * 
     * @param connectTimeout 连接超时时间（毫秒）
     */
    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    /**
     * 获取连接重试次数
     * 
     * @return 连接重试次数
     */
    public int getRetryAttempts() {
        return retryAttempts;
    }

    /**
     * 设置连接重试次数
     * 
     * @param retryAttempts 连接重试次数
     */
    public void setRetryAttempts(int retryAttempts) {
        this.retryAttempts = retryAttempts;
    }

    /**
     * 获取连接重试间隔时间
     * 
     * @return 连接重试间隔时间（毫秒）
     */
    public int getRetryInterval() {
        return retryInterval;
    }

    /**
     * 设置连接重试间隔时间
     * 
     * @param retryInterval 连接重试间隔时间（毫秒）
     */
    public void setRetryInterval(int retryInterval) {
        this.retryInterval = retryInterval;
    }

    /**
     * 获取 Ping 检查间隔时间
     * 
     * @return Ping 检查间隔时间（毫秒）
     */
    public int getPingInterval() {
        return pingInterval;
    }

    /**
     * 设置 Ping 检查间隔时间
     * 
     * @param pingInterval Ping 检查间隔时间（毫秒）
     */
    public void setPingInterval(int pingInterval) {
        this.pingInterval = pingInterval;
    }

    /**
     * 检查是否启用长连接
     * 
     * @return 如果启用长连接返回 true，否则返回 false
     */
    public boolean isKeepAlive() {
        return keepAlive;
    }

    /**
     * 设置是否启用长连接
     * 
     * @param keepAlive 是否启用长连接
     */
    public void setKeepAlive(boolean keepAlive) {
        this.keepAlive = keepAlive;
    }

}
