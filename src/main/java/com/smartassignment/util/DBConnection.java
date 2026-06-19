package com.smartassignment.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Utility class for Database Connection Management.
 * Implements a lightweight, thread-safe connection pool using Java dynamic proxies.
 * Automatically respects the configurations defined in 'database.properties'.
 */
public class DBConnection {
    private static final Logger logger = LoggerFactory.getLogger(DBConnection.class);
    
    private static String dbUrl;
    private static String dbUsername;
    private static String dbPassword;
    private static int maxSize = 10;
    private static int minIdle = 2;
    private static long connectionTimeoutMs = 30000;
    private static String validationQuery = "SELECT 1";
    
    private static final BlockingQueue<Connection> pool = new LinkedBlockingQueue<>();
    private static final AtomicInteger totalConnectionsCreated = new AtomicInteger(0);
    private static boolean isInitialized = false;

    static {
        try {
            loadConfig();
            initializePool();
        } catch (Exception e) {
            logger.error("Failed to initialize database connection pool", e);
            throw new RuntimeException("Database initialization error", e);
        }
    }

    private static void loadConfig() throws Exception {
        Properties props = new Properties();
        InputStream in = DBConnection.class.getClassLoader().getResourceAsStream("database.properties");
        if (in == null) {
            // Fallback for standalone command line execution
            java.io.File file = new java.io.File("src/main/resources/database.properties");
            if (!file.exists()) {
                file = new java.io.File("database.properties");
            }
            if (file.exists()) {
                in = new java.io.FileInputStream(file);
            }
        }

        if (in == null) {
            throw new RuntimeException("database.properties not found in classpath or directory paths");
        }

        try (InputStream input = in) {
            props.load(input);
            
            String driverClass = props.getProperty("db.driver");
            Class.forName(driverClass); // Initialize Driver Class
            
            dbUrl = props.getProperty("db.url");
            dbUsername = props.getProperty("db.username");
            dbPassword = props.getProperty("db.password");
            
            maxSize = Integer.parseInt(props.getProperty("db.pool.maxSize", "10"));
            minIdle = Integer.parseInt(props.getProperty("db.pool.minIdle", "2"));
            connectionTimeoutMs = Long.parseLong(props.getProperty("db.pool.connectionTimeout", "30000"));
            validationQuery = props.getProperty("db.pool.validationQuery", "SELECT 1");
            
            logger.info("Database configuration loaded successfully. Driver: {}", driverClass);
        }
    }

    private static void initializePool() throws SQLException {
        synchronized (pool) {
            if (isInitialized) return;
            
            // Warm up the pool to minIdle size
            for (int i = 0; i < minIdle; i++) {
                Connection conn = createPhysicalConnection();
                if (conn != null) {
                    pool.offer(conn);
                }
            }
            isInitialized = true;
            logger.info("Database pool initialized with {} warm connections (maxSize: {})", totalConnectionsCreated.get(), maxSize);
        }
    }

    private static Connection createPhysicalConnection() throws SQLException {
        if (totalConnectionsCreated.get() >= maxSize) {
            return null;
        }
        Connection conn = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
        totalConnectionsCreated.incrementAndGet();
        return conn;
    }

    /**
     * Checks if a physical connection is valid.
     */
    private static boolean isConnectionValid(Connection conn) {
        try {
            if (conn == null || conn.isClosed()) {
                return false;
            }
            // Execute validation query
            try (java.sql.Statement stmt = conn.createStatement()) {
                stmt.execute(validationQuery);
            }
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * Obtains a Connection from the pool.
     * Returns a dynamic proxy connection wrapper. Calling close() on this returned proxy
     * connection will return it to the pool instead of physically closing it.
     * 
     * @return Connection proxy instance.
     * @throws SQLException if a timeout occurs or database connection fails.
     */
    public static Connection getConnection() throws SQLException {
        Connection physicalConnection = null;
        long startTime = System.currentTimeMillis();
        
        while (physicalConnection == null) {
            // 1. Try to take an idle connection
            physicalConnection = pool.poll();
            
            if (physicalConnection != null) {
                // Validate connections before handing them out
                if (!isConnectionValid(physicalConnection)) {
                    closePhysicalConnection(physicalConnection);
                    physicalConnection = null; // Re-loop to allocate new one
                }
            } else {
                // 2. If pool is empty, check if we can create a new physical connection
                if (totalConnectionsCreated.get() < maxSize) {
                    synchronized (pool) {
                        if (totalConnectionsCreated.get() < maxSize) {
                            physicalConnection = createPhysicalConnection();
                        }
                    }
                }
                
                // 3. If we cannot create a new one, wait for a returned connection
                if (physicalConnection == null) {
                    long elapsed = System.currentTimeMillis() - startTime;
                    long remainingTimeout = connectionTimeoutMs - elapsed;
                    
                    if (remainingTimeout <= 0) {
                        throw new SQLException("Database connection timeout. Pool exhausted.");
                    }
                    
                    try {
                        physicalConnection = pool.poll(remainingTimeout, TimeUnit.MILLISECONDS);
                        if (physicalConnection != null && !isConnectionValid(physicalConnection)) {
                            closePhysicalConnection(physicalConnection);
                            physicalConnection = null;
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new SQLException("Thread interrupted while waiting for database connection", e);
                    }
                }
            }
        }

        final Connection finalPhysicalConn = physicalConnection;
        
        // Return a proxy instance that wraps the physical connection
        return (Connection) Proxy.newProxyInstance(
                DBConnection.class.getClassLoader(),
                new Class<?>[]{Connection.class},
                (proxy, method, args) -> {
                    if ("close".equals(method.getName())) {
                        // Return physical connection back to pool
                        if (isConnectionValid(finalPhysicalConn)) {
                            pool.offer(finalPhysicalConn);
                        } else {
                            closePhysicalConnection(finalPhysicalConn);
                        }
                        return null;
                    }
                    // Handle isClosed checks for the proxy
                    if ("isClosed".equals(method.getName())) {
                        return finalPhysicalConn.isClosed();
                    }
                    
                    // Delegate standard method execution
                    try {
                        return method.invoke(finalPhysicalConn, args);
                    } catch (java.lang.reflect.InvocationTargetException e) {
                        throw e.getCause(); // Propagate original SQL exception
                    }
                }
        );
    }

    private static void closePhysicalConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                logger.warn("Error closing database connection: {}", e.getMessage());
            } finally {
                totalConnectionsCreated.decrementAndGet();
            }
        }
    }

    /**
     * Shutdown hook to close all connections on application exit.
     */
    public static void shutdown() {
        logger.info("Shutting down database connection pool...");
        Connection conn;
        while ((conn = pool.poll()) != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                // ignore shutdown log warnings
            }
        }
        totalConnectionsCreated.set(0);
        logger.info("Database connection pool shutdown complete.");
    }
}
