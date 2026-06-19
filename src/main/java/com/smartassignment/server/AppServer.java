package com.smartassignment.server;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import com.smartassignment.dao.*;
import com.smartassignment.model.*;
import com.smartassignment.util.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.InetSocketAddress;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Main Java SE application entry point. Starts an HTTP server on port 8080.
 * Replaces Tomcat container by exposing RESTful API endpoints for React to communicate with.
 */
public class AppServer {

    private static final AdminDAO adminDAO = new AdminDAOImpl();
    private static final TeacherDAO teacherDAO = new TeacherDAOImpl();
    private static final StudentDAO studentDAO = new StudentDAOImpl();
    private static final ClassRoomDAO classRoomDAO = new ClassRoomDAOImpl();
    private static final AssignmentDAO assignmentDAO = new AssignmentDAOImpl();
    private static final SubmissionDAO submissionDAO = new SubmissionDAOImpl();
    private static final ActivityLogDAO logDAO = new ActivityLogDAOImpl();

    private static final String UPLOADS_BASE_PATH = new File("uploads").getAbsolutePath();

    public static void main(String[] args) throws IOException {
        int port = 8080;
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        // Auth Endpoints
        server.createContext("/api/auth/login", new LoginHandler());
        server.createContext("/api/auth/logout", new LogoutHandler());
        server.createContext("/api/auth/change-password", new ChangePasswordHandler());

        // Admin Endpoints
        server.createContext("/api/admin/dashboard", new AdminDashboardHandler());
        server.createContext("/api/admin/classes", new AdminClassesHandler());
        server.createContext("/api/admin/users", new AdminUsersHandler());
        server.createContext("/api/admin/reset-password", new AdminResetPasswordHandler());

        // Teacher Endpoints
        server.createContext("/api/teacher/dashboard", new TeacherDashboardHandler());
        server.createContext("/api/teacher/assignments", new TeacherAssignmentsHandler());
        server.createContext("/api/teacher/submissions", new TeacherSubmissionsHandler());
        server.createContext("/api/teacher/grade", new TeacherGradeHandler());

        // Student Endpoints
        server.createContext("/api/student/dashboard", new StudentDashboardHandler());
        server.createContext("/api/student/submit", new StudentSubmitHandler());

        // File Serving Endpoint
        server.createContext("/api/download", new DownloadHandler());

        server.setExecutor(null); // use default executor
        System.out.println("=================================================");
        System.out.println(" Smart Assignment Submission & Tracking System   ");
        System.out.println(" Standalone Backend Server Running on Port: " + port);
        System.out.println(" Uploads base directory: " + UPLOADS_BASE_PATH);
        System.out.println("=================================================");
        server.start();
    }

    // =========================================================================
    // GLOBAL HELPERS
    // =========================================================================

    private static boolean handleOptions(HttpExchange exchange) throws IOException {
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization");
            exchange.sendResponseHeaders(200, -1);
            return true;
        }
        return false;
    }

    private static void sendResponse(HttpExchange exchange, int statusCode, String responseText) throws IOException {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization");
        exchange.getResponseHeaders().set("Content-Type", "application/json");

        byte[] bytes = responseText.getBytes("UTF-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static String getRequestBody(HttpExchange exchange) throws IOException {
        java.io.ByteArrayOutputStream result = new java.io.ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        try (InputStream is = exchange.getRequestBody()) {
            while ((length = is.read(buffer)) != -1) {
                result.write(buffer, 0, length);
            }
        }
        return result.toString("UTF-8");
    }

    private static SessionManager.UserSession validateSession(HttpExchange exchange, String requiredRole) throws IOException {
        String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            sendResponse(exchange, 401, "{\"error\": \"Unauthorized. Missing token.\"}");
            return null;
        }
        String token = authHeader.substring(7).trim();
        SessionManager.UserSession session = SessionManager.getSession(token);
        if (session == null) {
            sendResponse(exchange, 401, "{\"error\": \"Unauthorized. Invalid or expired session.\"}");
            return null;
        }
        if (requiredRole != null && !session.getRole().equalsIgnoreCase(requiredRole)) {
            sendResponse(exchange, 403, "{\"error\": \"Forbidden. Access denied.\"}");
            return null;
        }
        return session;
    }

    private static String getRemoteIp(HttpExchange exchange) {
        return exchange.getRemoteAddress().getAddress().getHostAddress();
    }

    private static Map<String, String> parseQueryParams(String query) {
        Map<String, String> result = new HashMap<>();
        if (query == null || query.isEmpty()) {
            return result;
        }
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            if (idx > 0) {
                try {
                    String key = java.net.URLDecoder.decode(pair.substring(0, idx), "UTF-8");
                    String value = java.net.URLDecoder.decode(pair.substring(idx + 1), "UTF-8");
                    result.put(key, value);
                } catch (Exception e) {
                    // Ignore decoding error
                }
            }
        }
        return result;
    }

    private static String optString(Map<String, Object> map, String key) {
        Object val = map.get(key);
        return val != null ? val.toString() : null;
    }

    private static Integer optInt(Map<String, Object> map, String key) {
        Object val = map.get(key);
        if (val instanceof Number) {
            return ((Number) val).intValue();
        }
        if (val != null) {
            try {
                return Integer.parseInt(val.toString());
            } catch (NumberFormatException e) {
                // Ignore
            }
        }
        return null;
    }

    // =========================================================================
    // HANDLER IMPLEMENTATIONS
    // =========================================================================

    static class LoginHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (handleOptions(exchange)) return;
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, "{\"error\": \"Method not allowed\"}");
                return;
            }
            try {
                String body = getRequestBody(exchange);
                Map<String, Object> req = JsonUtil.parseObject(body);
                String email = optString(req, "email");
                String password = optString(req, "password");
                String role = optString(req, "role");

                if (ValidationUtil.isEmptyOrBlank(email) || ValidationUtil.isEmptyOrBlank(password) || ValidationUtil.isEmptyOrBlank(role)) {
                    sendResponse(exchange, 400, "{\"error\": \"Email, password, and role are required.\"}");
                    return;
                }

                email = email.trim();
                role = role.trim().toUpperCase();

                boolean authenticated = false;
                int userId = 0;
                String displayName = "";
                Integer classId = null;

                if ("ADMIN".equals(role)) {
                    Admin admin = adminDAO.getByEmail(email);
                    if (admin != null && admin.isActive() && PasswordUtil.verifyPassword(password, admin.getPasswordHash())) {
                        authenticated = true;
                        userId = admin.getAdminId();
                        displayName = admin.getFullName();
                    }
                } else if ("TEACHER".equals(role)) {
                    Teacher teacher = teacherDAO.getByEmail(email);
                    if (teacher != null && teacher.isActive() && PasswordUtil.verifyPassword(password, teacher.getPasswordHash())) {
                        authenticated = true;
                        userId = teacher.getTeacherId();
                        displayName = teacher.getFullName();
                    }
                } else if ("STUDENT".equals(role)) {
                    Student student = studentDAO.getByEmail(email);
                    if (student != null && student.isActive() && PasswordUtil.verifyPassword(password, student.getPasswordHash())) {
                        authenticated = true;
                        userId = student.getStudentId();
                        displayName = student.getFullName();
                        classId = student.getClassId();
                    }
                }

                if (authenticated) {
                    SessionManager.UserSession session = SessionManager.createSession(userId, email, role, displayName, classId);
                    logDAO.insert(new ActivityLog(userId, ActivityLog.Role.valueOf(role), "LOGIN", role, userId, "User logged in: " + email, getRemoteIp(exchange)));

                    Map<String, Object> resp = new HashMap<>();
                    resp.put("token", session.getToken());
                    resp.put("userId", userId);
                    resp.put("role", role);
                    resp.put("displayName", displayName);
                    resp.put("email", email);
                    if (classId != null) {
                        resp.put("classId", classId);
                    }
                    sendResponse(exchange, 200, JsonUtil.toJson(resp));
                } else {
                    sendResponse(exchange, 401, "{\"error\": \"Invalid credentials or account inactive.\"}");
                }
            } catch (Exception e) {
                e.printStackTrace();
                sendResponse(exchange, 500, "{\"error\": \"Internal server error\"}");
            }
        }
    }

    static class LogoutHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (handleOptions(exchange)) return;
            SessionManager.UserSession session = validateSession(exchange, null);
            if (session == null) return;
            SessionManager.destroySession(session.getToken());
            sendResponse(exchange, 200, "{\"success\": true}");
        }
    }

    static class ChangePasswordHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (handleOptions(exchange)) return;
            SessionManager.UserSession session = validateSession(exchange, null);
            if (session == null) return;

            try {
                Map<String, Object> req = JsonUtil.parseObject(getRequestBody(exchange));
                String oldPassword = optString(req, "oldPassword");
                String newPassword = optString(req, "newPassword");
                String confirmPassword = optString(req, "confirmPassword");

                if (ValidationUtil.isEmptyOrBlank(oldPassword) || ValidationUtil.isEmptyOrBlank(newPassword) || ValidationUtil.isEmptyOrBlank(confirmPassword)) {
                    sendResponse(exchange, 400, "{\"error\": \"All password fields are required.\"}");
                    return;
                }
                if (!newPassword.equals(confirmPassword)) {
                    sendResponse(exchange, 400, "{\"error\": \"New password and confirmation do not match.\"}");
                    return;
                }
                if (!ValidationUtil.isValidPassword(newPassword)) {
                    sendResponse(exchange, 400, "{\"error\": \"Password must be at least 8 characters and contain both letters and digits.\"}");
                    return;
                }

                boolean updated = false;
                String role = session.getRole();
                int userId = session.getUserId();

                if ("ADMIN".equals(role)) {
                    Admin admin = adminDAO.getById(userId);
                    if (admin != null && PasswordUtil.verifyPassword(oldPassword, admin.getPasswordHash())) {
                        admin.setPasswordHash(PasswordUtil.hashPassword(newPassword));
                        updated = adminDAO.update(admin);
                    }
                } else if ("TEACHER".equals(role)) {
                    Teacher teacher = teacherDAO.getById(userId);
                    if (teacher != null && PasswordUtil.verifyPassword(oldPassword, teacher.getPasswordHash())) {
                        teacher.setPasswordHash(PasswordUtil.hashPassword(newPassword));
                        updated = teacherDAO.update(teacher);
                    }
                } else if ("STUDENT".equals(role)) {
                    Student student = studentDAO.getById(userId);
                    if (student != null && PasswordUtil.verifyPassword(oldPassword, student.getPasswordHash())) {
                        student.setPasswordHash(PasswordUtil.hashPassword(newPassword));
                        updated = studentDAO.update(student);
                    }
                }

                if (updated) {
                    logDAO.insert(new ActivityLog(userId, ActivityLog.Role.valueOf(role), "CHANGE_PASSWORD", role, userId, "Password changed by user", getRemoteIp(exchange)));
                    sendResponse(exchange, 200, "{\"message\": \"Password updated successfully.\"}");
                } else {
                    sendResponse(exchange, 400, "{\"error\": \"Incorrect old password.\"}");
                }
            } catch (Exception e) {
                sendResponse(exchange, 500, "{\"error\": \"Server error\"}");
            }
        }
    }

    static class AdminDashboardHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (handleOptions(exchange)) return;
            SessionManager.UserSession session = validateSession(exchange, "ADMIN");
            if (session == null) return;

            try {
                int totalClasses = classRoomDAO.getAll().size();
                int totalTeachers = teacherDAO.getAll().size();
                int totalStudents = studentDAO.getAll().size();
                int totalAssignments = assignmentDAO.getAll().size();

                int totalSubmissions = 0;
                List<Assignment> assignments = assignmentDAO.getAll();
                for (Assignment a : assignments) {
                    totalSubmissions += submissionDAO.getSubmissionCount(a.getAssignmentId());
                }

                List<ActivityLog> allLogs = logDAO.getAll();
                List<ActivityLog> recentLogs = allLogs.stream().limit(10).collect(Collectors.toList());

                Map<String, Object> stats = new HashMap<>();
                stats.put("totalClasses", totalClasses);
                stats.put("totalTeachers", totalTeachers);
                stats.put("totalStudents", totalStudents);
                stats.put("totalAssignments", totalAssignments);
                stats.put("totalSubmissions", totalSubmissions);
                stats.put("recentLogs", recentLogs);

                sendResponse(exchange, 200, JsonUtil.toJson(stats));
            } catch (Exception e) {
                sendResponse(exchange, 500, "{\"error\": \"Database error\"}");
            }
        }
    }

    static class AdminClassesHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (handleOptions(exchange)) return;
            SessionManager.UserSession session = validateSession(exchange, "ADMIN");
            if (session == null) return;

            String method = exchange.getRequestMethod();
            try {
                if ("GET".equalsIgnoreCase(method)) {
                    List<ClassRoom> list = classRoomDAO.getAll();
                    sendResponse(exchange, 200, JsonUtil.toJson(list));
                } else if ("POST".equalsIgnoreCase(method)) {
                    Map<String, Object> req = JsonUtil.parseObject(getRequestBody(exchange));
                    String className = optString(req, "className");
                    String section = optString(req, "section");
                    String academicYear = optString(req, "academicYear");
                    String description = optString(req, "description");

                    if (ValidationUtil.isEmptyOrBlank(className) || ValidationUtil.isEmptyOrBlank(section) || ValidationUtil.isEmptyOrBlank(academicYear)) {
                       sendResponse(exchange, 400, "{\"error\": \"Class Name, Section, and Academic Year are required.\"}");
                       return;
                    }

                    ClassRoom existing = classRoomDAO.getByNameSectionAndAcademicYear(className, section, academicYear);
                    if (existing != null) {
                       sendResponse(exchange, 400, "{\"error\": \"Classroom record already exists for the given section and year.\"}");
                       return;
                    }

                    ClassRoom classRoom = new ClassRoom(className, section, academicYear, description, true, session.getUserId());
                    boolean success = classRoomDAO.insert(classRoom);
                    if (success) {
                        logDAO.insert(new ActivityLog(session.getUserId(), ActivityLog.Role.ADMIN, "CREATE_CLASS", "CLASS", classRoom.getClassId(), "Created class: " + className, getRemoteIp(exchange)));
                        sendResponse(exchange, 200, "{\"message\": \"Class created successfully.\"}");
                    } else {
                        sendResponse(exchange, 500, "{\"error\": \"Failed to create class.\"}");
                    }
                } else if ("PUT".equalsIgnoreCase(method)) {
                    Map<String, Object> req = JsonUtil.parseObject(getRequestBody(exchange));
                    Integer id = optInt(req, "classId");
                    String className = optString(req, "className");
                    String section = optString(req, "section");
                    String academicYear = optString(req, "academicYear");
                    String description = optString(req, "description");
                    Boolean isActive = (Boolean) req.get("isActive");

                    if (id == null || ValidationUtil.isEmptyOrBlank(className) || ValidationUtil.isEmptyOrBlank(section) || ValidationUtil.isEmptyOrBlank(academicYear)) {
                       sendResponse(exchange, 400, "{\"error\": \"Missing required fields for class update.\"}");
                       return;
                    }

                    ClassRoom classRoom = classRoomDAO.getById(id);
                    if (classRoom == null) {
                       sendResponse(exchange, 404, "{\"error\": \"Class not found.\"}");
                       return;
                    }

                    if (!className.equalsIgnoreCase(classRoom.getClassName()) || !section.equalsIgnoreCase(classRoom.getSection()) || !academicYear.equalsIgnoreCase(classRoom.getAcademicYear())) {
                       ClassRoom existing = classRoomDAO.getByNameSectionAndAcademicYear(className, section, academicYear);
                       if (existing != null) {
                           sendResponse(exchange, 400, "{\"error\": \"Another class already exists with those details.\"}");
                           return;
                       }
                    }

                    classRoom.setClassName(className);
                    classRoom.setSection(section);
                    classRoom.setAcademicYear(academicYear);
                    classRoom.setDescription(description);
                    if (isActive != null) {
                       classRoom.setActive(isActive);
                    }

                    boolean success = classRoomDAO.update(classRoom);
                    if (success) {
                        logDAO.insert(new ActivityLog(session.getUserId(), ActivityLog.Role.ADMIN, "UPDATE_CLASS", "CLASS", id, "Updated class: " + className, getRemoteIp(exchange)));
                        sendResponse(exchange, 200, "{\"message\": \"Class updated successfully.\"}");
                    } else {
                        sendResponse(exchange, 500, "{\"error\": \"Failed to update class.\"}");
                    }
                } else if ("DELETE".equalsIgnoreCase(method)) {
                    Map<String, String> params = parseQueryParams(exchange.getRequestURI().getQuery());
                    String idStr = params.get("id");
                    if (idStr == null) {
                        sendResponse(exchange, 400, "{\"error\": \"Missing class ID\"}");
                        return;
                    }
                    int classId = Integer.parseInt(idStr);
                    ClassRoom classRoom = classRoomDAO.getById(classId);
                    if (classRoom == null) {
                       sendResponse(exchange, 404, "{\"error\": \"Class not found.\"}");
                       return;
                    }
                    boolean success = classRoomDAO.delete(classId);
                    if (success) {
                        logDAO.insert(new ActivityLog(session.getUserId(), ActivityLog.Role.ADMIN, "DELETE_CLASS", "CLASS", classId, "Deleted class: " + classRoom.getClassName(), getRemoteIp(exchange)));
                        sendResponse(exchange, 200, "{\"message\": \"Class deleted successfully.\"}");
                    } else {
                        sendResponse(exchange, 400, "{\"error\": \"Failed to delete class. Ensure it has no active dependencies.\"}");
                    }
                }
            } catch (Exception e) {
                sendResponse(exchange, 500, "{\"error\": \"Database error\"}");
            }
        }
    }

    static class AdminUsersHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (handleOptions(exchange)) return;
            SessionManager.UserSession session = validateSession(exchange, "ADMIN");
            if (session == null) return;

            String method = exchange.getRequestMethod();
            try {
                if ("GET".equalsIgnoreCase(method)) {
                    List<Teacher> teachers = teacherDAO.getAll();
                    List<Student> students = studentDAO.getAll();
                    List<ClassRoom> classes = classRoomDAO.getAll();
                    
                    List<Map<String, Object>> teacherList = new ArrayList<>();
                    for (Teacher t : teachers) {
                        Map<String, Object> tm = new HashMap<>();
                        tm.put("teacherId", t.getTeacherId());
                        tm.put("fullName", t.getFullName());
                        tm.put("email", t.getEmail());
                        tm.put("phone", t.getPhone());
                        tm.put("subject", t.getSubject());
                        tm.put("employeeCode", t.getEmployeeCode());
                        tm.put("active", t.isActive());
                        tm.put("assignedClasses", teacherDAO.getAssignedClassIds(t.getTeacherId()));
                        teacherList.add(tm);
                    }

                    Map<String, Object> data = new HashMap<>();
                    data.put("teachers", teacherList);
                    data.put("students", students);
                    data.put("classes", classes);
                    sendResponse(exchange, 200, JsonUtil.toJson(data));
                } else if ("POST".equalsIgnoreCase(method)) {
                    Map<String, Object> req = JsonUtil.parseObject(getRequestBody(exchange));
                    String role = optString(req, "role");
                    String fullName = optString(req, "fullName");
                    String email = optString(req, "email");
                    String phone = optString(req, "phone");
                    String password = optString(req, "password");

                    if (ValidationUtil.isEmptyOrBlank(role) || ValidationUtil.isEmptyOrBlank(fullName) || ValidationUtil.isEmptyOrBlank(email) || ValidationUtil.isEmptyOrBlank(password)) {
                        sendResponse(exchange, 400, "{\"error\": \"Role, Name, Email, and Password are required.\"}");
                        return;
                    }
                    if (!ValidationUtil.isValidEmail(email)) {
                        sendResponse(exchange, 400, "{\"error\": \"Invalid email format.\"}");
                        return;
                    }
                    if (teacherDAO.getByEmail(email) != null || studentDAO.getByEmail(email) != null) {
                        sendResponse(exchange, 400, "{\"error\": \"Email address already registered.\"}");
                        return;
                    }

                    String passwordHash = PasswordUtil.hashPassword(password);

                    if ("TEACHER".equalsIgnoreCase(role)) {
                        String subject = optString(req, "subject");
                        String employeeCode = optString(req, "employeeCode");
                        
                        if (ValidationUtil.isEmptyOrBlank(employeeCode)) {
                            sendResponse(exchange, 400, "{\"error\": \"Employee code cannot be empty.\"}");
                            return;
                        }
                        if (teacherDAO.getByEmployeeCode(employeeCode) != null) {
                            sendResponse(exchange, 400, "{\"error\": \"Employee code already exists.\"}");
                            return;
                        }

                        Teacher teacher = new Teacher(fullName, email, passwordHash, phone, subject, employeeCode, true, session.getUserId());
                        boolean success = teacherDAO.insert(teacher);
                        if (success) {
                            Object assignedObj = req.get("assignedClasses");
                            if (assignedObj instanceof List) {
                                for (Object cid : (List<?>) assignedObj) {
                                    teacherDAO.assignToClass(teacher.getTeacherId(), Integer.parseInt(cid.toString()));
                                }
                            }
                            logDAO.insert(new ActivityLog(session.getUserId(), ActivityLog.Role.ADMIN, "CREATE_TEACHER", "TEACHER", teacher.getTeacherId(), "Created teacher: " + fullName, getRemoteIp(exchange)));
                            sendResponse(exchange, 200, "{\"message\": \"Teacher created successfully.\"}");
                        } else {
                            sendResponse(exchange, 500, "{\"error\": \"Failed to create teacher.\"}");
                        }
                    } else if ("STUDENT".equalsIgnoreCase(role)) {
                       String rollNumber = optString(req, "rollNumber");
                       Integer classId = optInt(req, "classId");

                       if (ValidationUtil.isEmptyOrBlank(rollNumber) || classId == null) {
                           sendResponse(exchange, 400, "{\"error\": \"Roll number and class selection are required.\"}");
                           return;
                       }
                       if (studentDAO.getByRollNumberAndClassId(rollNumber, classId) != null) {
                           sendResponse(exchange, 400, "{\"error\": \"Roll number already exists in selected class.\"}");
                           return;
                       }

                       Student student = new Student(fullName, email, passwordHash, rollNumber, phone, classId, true, session.getUserId());
                       boolean success = studentDAO.insert(student);
                       if (success) {
                           logDAO.insert(new ActivityLog(session.getUserId(), ActivityLog.Role.ADMIN, "CREATE_STUDENT", "STUDENT", student.getStudentId(), "Created student: " + fullName, getRemoteIp(exchange)));
                           sendResponse(exchange, 200, "{\"message\": \"Student created successfully.\"}");
                       } else {
                           sendResponse(exchange, 500, "{\"error\": \"Failed to create student.\"}");
                       }
                    }
                } else if ("PUT".equalsIgnoreCase(method)) {
                   Map<String, Object> req = JsonUtil.parseObject(getRequestBody(exchange));
                   Integer userId = optInt(req, "userId");
                   String role = optString(req, "role");
                   String fullName = optString(req, "fullName");
                   String email = optString(req, "email");
                   String phone = optString(req, "phone");
                   String password = optString(req, "password");
                   Boolean isActive = (Boolean) req.get("isActive");

                   if (userId == null || ValidationUtil.isEmptyOrBlank(role) || ValidationUtil.isEmptyOrBlank(fullName) || ValidationUtil.isEmptyOrBlank(email)) {
                       sendResponse(exchange, 400, "{\"error\": \"Missing required fields for update.\"}");
                       return;
                   }

                   boolean activeVal = isActive != null ? isActive : true;

                   if ("TEACHER".equalsIgnoreCase(role)) {
                       Teacher teacher = teacherDAO.getById(userId);
                       if (teacher == null) {
                           sendResponse(exchange, 404, "{\"error\": \"Teacher not found.\"}");
                           return;
                       }
                       String subject = optString(req, "subject");
                       String employeeCode = optString(req, "employeeCode");

                       if (ValidationUtil.isEmptyOrBlank(employeeCode)) {
                           sendResponse(exchange, 400, "{\"error\": \"Employee code cannot be empty.\"}");
                           return;
                       }
                       if (!email.equalsIgnoreCase(teacher.getEmail()) && (teacherDAO.getByEmail(email) != null || studentDAO.getByEmail(email) != null)) {
                           sendResponse(exchange, 400, "{\"error\": \"Email address already registered.\"}");
                           return;
                       }
                       if (!employeeCode.equalsIgnoreCase(teacher.getEmployeeCode()) && teacherDAO.getByEmployeeCode(employeeCode) != null) {
                           sendResponse(exchange, 400, "{\"error\": \"Employee code already exists.\"}");
                           return;
                       }

                       teacher.setFullName(fullName);
                       teacher.setEmail(email);
                       teacher.setPhone(phone);
                       teacher.setSubject(subject);
                       teacher.setEmployeeCode(employeeCode);
                       teacher.setActive(activeVal);
                       if (!ValidationUtil.isEmptyOrBlank(password)) {
                           teacher.setPasswordHash(PasswordUtil.hashPassword(password));
                       }

                       boolean success = teacherDAO.update(teacher);
                       if (success) {
                           List<Integer> currentlyAssigned = teacherDAO.getAssignedClassIds(userId);
                           for (int cid : currentlyAssigned) {
                               teacherDAO.removeFromClass(userId, cid);
                           }
                           Object assignedObj = req.get("assignedClasses");
                           if (assignedObj instanceof List) {
                               for (Object cid : (List<?>) assignedObj) {
                                   teacherDAO.assignToClass(userId, Integer.parseInt(cid.toString()));
                               }
                           }
                           logDAO.insert(new ActivityLog(session.getUserId(), ActivityLog.Role.ADMIN, "UPDATE_TEACHER", "TEACHER", userId, "Updated teacher: " + fullName, getRemoteIp(exchange)));
                           sendResponse(exchange, 200, "{\"message\": \"Teacher updated successfully.\"}");
                       } else {
                           sendResponse(exchange, 500, "{\"error\": \"Failed to update teacher.\"}");
                       }
                   } else if ("STUDENT".equalsIgnoreCase(role)) {
                       Student student = studentDAO.getById(userId);
                       if (student == null) {
                           sendResponse(exchange, 404, "{\"error\": \"Student not found.\"}");
                           return;
                       }
                       String rollNumber = optString(req, "rollNumber");
                       Integer classId = optInt(req, "classId");

                       if (ValidationUtil.isEmptyOrBlank(rollNumber) || classId == null) {
                           sendResponse(exchange, 400, "{\"error\": \"Roll number and class selection are required.\"}");
                           return;
                       }
                       if (!email.equalsIgnoreCase(student.getEmail()) && (teacherDAO.getByEmail(email) != null || studentDAO.getByEmail(email) != null)) {
                           sendResponse(exchange, 400, "{\"error\": \"Email address already registered.\"}");
                           return;
                       }
                       if ((!rollNumber.equalsIgnoreCase(student.getRollNumber()) || classId != student.getClassId()) && studentDAO.getByRollNumberAndClassId(rollNumber, classId) != null) {
                           sendResponse(exchange, 400, "{\"error\": \"Roll number already exists in selected class.\"}");
                           return;
                       }

                       student.setFullName(fullName);
                       student.setEmail(email);
                       student.setPhone(phone);
                       student.setRollNumber(rollNumber);
                       student.setClassId(classId);
                       student.setActive(activeVal);
                       if (!ValidationUtil.isEmptyOrBlank(password)) {
                           student.setPasswordHash(PasswordUtil.hashPassword(password));
                       }

                       boolean success = studentDAO.update(student);
                       if (success) {
                           logDAO.insert(new ActivityLog(session.getUserId(), ActivityLog.Role.ADMIN, "UPDATE_STUDENT", "STUDENT", userId, "Updated student: " + fullName, getRemoteIp(exchange)));
                           sendResponse(exchange, 200, "{\"message\": \"Student updated successfully.\"}");
                       } else {
                           sendResponse(exchange, 500, "{\"error\": \"Failed to update student.\"}");
                       }
                   }
               } else if ("DELETE".equalsIgnoreCase(method)) {
                   Map<String, String> params = parseQueryParams(exchange.getRequestURI().getQuery());
                   String idStr = params.get("id");
                   String role = params.get("role");
                   if (idStr == null || role == null) {
                       sendResponse(exchange, 400, "{\"error\": \"Missing id or role.\"}");
                       return;
                   }
                   int userId = Integer.parseInt(idStr);
                   boolean success = false;
                   String userName = "";

                   if ("TEACHER".equalsIgnoreCase(role)) {
                       Teacher teacher = teacherDAO.getById(userId);
                       if (teacher != null) {
                           userName = teacher.getFullName();
                           success = teacherDAO.delete(userId);
                       }
                   } else if ("STUDENT".equalsIgnoreCase(role)) {
                       Student student = studentDAO.getById(userId);
                       if (student != null) {
                           userName = student.getFullName();
                           success = studentDAO.delete(userId);
                       }
                   }

                   if (success) {
                       logDAO.insert(new ActivityLog(session.getUserId(), ActivityLog.Role.ADMIN, "DELETE_USER", role.toUpperCase(), userId, "Deleted " + role + ": " + userName, getRemoteIp(exchange)));
                       sendResponse(exchange, 200, "{\"message\": \"User deleted successfully.\"}");
                   } else {
                       sendResponse(exchange, 500, "{\"error\": \"Failed to delete user.\"}");
                   }
               }
           } catch (Exception e) {
               sendResponse(exchange, 500, "{\"error\": \"Database error\"}");
           }
       }
   }

   static class AdminResetPasswordHandler implements HttpHandler {
       @Override
       public void handle(HttpExchange exchange) throws IOException {
           if (handleOptions(exchange)) return;
           SessionManager.UserSession session = validateSession(exchange, "ADMIN");
           if (session == null) return;

           if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
               sendResponse(exchange, 405, "{\"error\": \"Method not allowed\"}");
               return;
           }

           try {
               Map<String, Object> req = JsonUtil.parseObject(getRequestBody(exchange));
               Integer targetUserId = optInt(req, "targetUserId");
               String targetRole = optString(req, "targetRole");

               if (targetUserId == null || ValidationUtil.isEmptyOrBlank(targetRole)) {
                   sendResponse(exchange, 400, "{\"error\": \"Missing user details.\"}");
                   return;
               }

               String tempPassword = PasswordUtil.generateRandomPassword();
               String tempHash = PasswordUtil.hashPassword(tempPassword);
               boolean success = false;
               String userDisplayName = "";

               if ("TEACHER".equalsIgnoreCase(targetRole)) {
                   Teacher teacher = teacherDAO.getById(targetUserId);
                   if (teacher != null) {
                       userDisplayName = teacher.getFullName();
                       teacher.setPasswordHash(tempHash);
                       success = teacherDAO.update(teacher);
                   }
               } else if ("STUDENT".equalsIgnoreCase(targetRole)) {
                   Student student = studentDAO.getById(targetUserId);
                   if (student != null) {
                       userDisplayName = student.getFullName();
                       student.setPasswordHash(tempHash);
                       success = studentDAO.update(student);
                   }
               }

               if (success) {
                   logDAO.insert(new ActivityLog(session.getUserId(), ActivityLog.Role.ADMIN, "RESET_PASSWORD", targetRole.toUpperCase(), targetUserId, "Reset password by admin for " + targetRole + ": " + userDisplayName, getRemoteIp(exchange)));
                   Map<String, Object> resp = new HashMap<>();
                   resp.put("tempPassword", tempPassword);
                   sendResponse(exchange, 200, JsonUtil.toJson(resp));
               } else {
                   sendResponse(exchange, 500, "{\"error\": \"Failed to reset password.\"}");
               }
           } catch (Exception e) {
               sendResponse(exchange, 500, "{\"error\": \"Database error\"}");
           }
       }
   }

   static class TeacherDashboardHandler implements HttpHandler {
       @Override
       public void handle(HttpExchange exchange) throws IOException {
           if (handleOptions(exchange)) return;
           SessionManager.UserSession session = validateSession(exchange, "TEACHER");
           if (session == null) return;
           int teacherId = session.getUserId();

           try {
               List<ClassRoom> assignedClasses = classRoomDAO.getClassesByTeacherId(teacherId);
               List<Assignment> teacherAssignments = assignmentDAO.getByTeacherId(teacherId);

               List<Map<String, Object>> assignmentStatsList = new ArrayList<>();
               int totalSubmissionsReceived = 0;

               Map<Integer, String> classNamesMap = new HashMap<>();
               for (ClassRoom classroom : assignedClasses) {
                   classNamesMap.put(classroom.getClassId(), classroom.getClassName() + " (" + classroom.getSection() + ")");
               }

               for (Assignment assignment : teacherAssignments) {
                   Map<String, Object> statMap = new HashMap<>();
                   statMap.put("assignment", assignment);
                   statMap.put("className", classNamesMap.getOrDefault(assignment.getClassId(), "Unknown Class"));
                   
                   int totalStudents = studentDAO.getByClassId(assignment.getClassId()).size();
                   statMap.put("totalStudents", totalStudents);

                   List<Submission> submissions = submissionDAO.getByAssignmentId(assignment.getAssignmentId());
                   int submissionCount = submissions.size();
                   totalSubmissionsReceived += submissionCount;
                   statMap.put("submissionCount", submissionCount);

                   long gradedCount = submissions.stream().filter(sub -> sub.getMarks() != null).count();
                   statMap.put("gradedCount", gradedCount);
                   statMap.put("pendingGrading", submissionCount - gradedCount);

                   assignmentStatsList.add(statMap);
               }

               Map<String, Object> data = new HashMap<>();
               data.put("assignedClasses", assignedClasses);
               data.put("assignmentsStats", assignmentStatsList);
               data.put("assignedClassesCount", assignedClasses.size());
               data.put("totalAssignmentsCount", teacherAssignments.size());
               data.put("totalSubmissionsCount", totalSubmissionsReceived);

               sendResponse(exchange, 200, JsonUtil.toJson(data));
           } catch (Exception e) {
               sendResponse(exchange, 500, "{\"error\": \"Database error\"}");
           }
       }
   }

   static class TeacherAssignmentsHandler implements HttpHandler {
       @Override
       public void handle(HttpExchange exchange) throws IOException {
           if (handleOptions(exchange)) return;
           SessionManager.UserSession session = validateSession(exchange, "TEACHER");
           if (session == null) return;
           int teacherId = session.getUserId();

           String method = exchange.getRequestMethod();
           try {
               if ("GET".equalsIgnoreCase(method)) {
                   Map<String, String> params = parseQueryParams(exchange.getRequestURI().getQuery());
                   String idStr = params.get("id");
                   if (idStr != null) {
                       int assignmentId = Integer.parseInt(idStr);
                       Assignment a = assignmentDAO.getById(assignmentId);
                       if (a == null || a.getTeacherId() != teacherId) {
                           sendResponse(exchange, 403, "{\"error\": \"Access denied\"}");
                           return;
                       }
                       sendResponse(exchange, 200, JsonUtil.toJson(a));
                   } else {
                       List<Assignment> list = assignmentDAO.getByTeacherId(teacherId);
                       sendResponse(exchange, 200, JsonUtil.toJson(list));
                   }
               } else if ("POST".equalsIgnoreCase(method)) {
                   Map<String, Object> req = JsonUtil.parseObject(getRequestBody(exchange));
                   String title = optString(req, "title");
                   String description = optString(req, "description");
                   Integer classId = optInt(req, "classId");
                   String deadlineStr = optString(req, "deadline");
                   Integer maxMarks = optInt(req, "maxMarks");
                   String statusStr = optString(req, "status"); // ACTIVE or DRAFT

                   if (ValidationUtil.isEmptyOrBlank(title) || classId == null || ValidationUtil.isEmptyOrBlank(deadlineStr) || maxMarks == null || ValidationUtil.isEmptyOrBlank(statusStr)) {
                       sendResponse(exchange, 400, "{\"error\": \"Missing required fields.\"}");
                       return;
                   }

                   Timestamp deadline = DateUtil.parseHtmlDateTime(deadlineStr);
                   Assignment.Status status = Assignment.Status.valueOf(statusStr.toUpperCase());

                   Assignment a = new Assignment();
                   a.setTitle(title);
                   a.setDescription(description);
                   a.setClassId(classId);
                   a.setTeacherId(teacherId);
                   a.setDeadline(deadline);
                   a.setMaxMarks(maxMarks);
                   a.setStatus(status);

                   // Handle Base64 file upload
                   String fileName = optString(req, "fileName");
                   String fileBase64 = optString(req, "fileBase64");
                   if (fileName != null && fileBase64 != null) {
                       String relativePath = FileUploadUtil.saveFile(fileName, fileBase64, UPLOADS_BASE_PATH, "assignments");
                       if (relativePath != null) {
                           a.setFileName(fileName);
                           a.setFilePath(relativePath);
                           
                           // Roughly calculate base64 byte size
                           byte[] decoded = Base64.getDecoder().decode(fileBase64.contains(",") ? fileBase64.substring(fileBase64.indexOf(",") + 1) : fileBase64);
                           a.setFileSizeBytes((long) decoded.length);
                           a.setFileType(FileUploadUtil.getFileExtension(fileName));
                       } else {
                           sendResponse(exchange, 400, "{\"error\": \"File validation failed (size > 10MB or disallowed extension).\"}");
                           return;
                       }
                   }

                   boolean success = assignmentDAO.insert(a);
                   if (success) {
                       logDAO.insert(new ActivityLog(teacherId, ActivityLog.Role.TEACHER, "CREATE_ASSIGNMENT", "ASSIGNMENT", a.getAssignmentId(), "Created assignment: " + title, getRemoteIp(exchange)));
                       sendResponse(exchange, 200, "{\"message\": \"Assignment created successfully.\"}");
                   } else {
                       sendResponse(exchange, 500, "{\"error\": \"Failed to save assignment.\"}");
                   }
               } else if ("PUT".equalsIgnoreCase(method)) {
                   Map<String, Object> req = JsonUtil.parseObject(getRequestBody(exchange));
                   Integer assignmentId = optInt(req, "assignmentId");
                   String title = optString(req, "title");
                   String description = optString(req, "description");
                   Integer classId = optInt(req, "classId");
                   String deadlineStr = optString(req, "deadline");
                   Integer maxMarks = optInt(req, "maxMarks");
                   String statusStr = optString(req, "status");

                   if (assignmentId == null || ValidationUtil.isEmptyOrBlank(title) || classId == null || ValidationUtil.isEmptyOrBlank(deadlineStr) || maxMarks == null || ValidationUtil.isEmptyOrBlank(statusStr)) {
                       sendResponse(exchange, 400, "{\"error\": \"Missing fields for assignment update.\"}");
                       return;
                   }

                   Assignment a = assignmentDAO.getById(assignmentId);
                   if (a == null || a.getTeacherId() != teacherId) {
                       sendResponse(exchange, 403, "{\"error\": \"Access denied.\"}");
                       return;
                   }

                   a.setTitle(title);
                   a.setDescription(description);
                   a.setClassId(classId);
                   a.setDeadline(DateUtil.parseHtmlDateTime(deadlineStr));
                   a.setMaxMarks(maxMarks);
                   a.setStatus(Assignment.Status.valueOf(statusStr.toUpperCase()));

                   String fileName = optString(req, "fileName");
                   String fileBase64 = optString(req, "fileBase64");
                   if (fileName != null && fileBase64 != null) {
                       // Delete old physical file if exists
                       if (a.getFilePath() != null) {
                           FileUploadUtil.deleteFile(UPLOADS_BASE_PATH, a.getFilePath());
                       }
                       String relativePath = FileUploadUtil.saveFile(fileName, fileBase64, UPLOADS_BASE_PATH, "assignments");
                       if (relativePath != null) {
                           a.setFileName(fileName);
                           a.setFilePath(relativePath);
                           byte[] decoded = Base64.getDecoder().decode(fileBase64.contains(",") ? fileBase64.substring(fileBase64.indexOf(",") + 1) : fileBase64);
                           a.setFileSizeBytes((long) decoded.length);
                           a.setFileType(FileUploadUtil.getFileExtension(fileName));
                       }
                   }

                   boolean success = assignmentDAO.update(a);
                   if (success) {
                       logDAO.insert(new ActivityLog(teacherId, ActivityLog.Role.TEACHER, "UPDATE_ASSIGNMENT", "ASSIGNMENT", assignmentId, "Updated assignment: " + title, getRemoteIp(exchange)));
                       sendResponse(exchange, 200, "{\"message\": \"Assignment updated successfully.\"}");
                   } else {
                       sendResponse(exchange, 500, "{\"error\": \"Failed to update assignment.\"}");
                   }
               } else if ("DELETE".equalsIgnoreCase(method)) {
                   Map<String, String> params = parseQueryParams(exchange.getRequestURI().getQuery());
                   String idStr = params.get("id");
                   if (idStr == null) {
                       sendResponse(exchange, 400, "{\"error\": \"Missing assignment ID.\"}");
                       return;
                   }
                   int assignmentId = Integer.parseInt(idStr);
                   Assignment a = assignmentDAO.getById(assignmentId);
                   if (a == null || a.getTeacherId() != teacherId) {
                       sendResponse(exchange, 403, "{\"error\": \"Access denied.\"}");
                       return;
                   }

                   if (a.getFilePath() != null) {
                       FileUploadUtil.deleteFile(UPLOADS_BASE_PATH, a.getFilePath());
                   }

                   boolean success = assignmentDAO.delete(assignmentId);
                   if (success) {
                       logDAO.insert(new ActivityLog(teacherId, ActivityLog.Role.TEACHER, "DELETE_ASSIGNMENT", "ASSIGNMENT", assignmentId, "Deleted assignment: " + a.getTitle(), getRemoteIp(exchange)));
                       sendResponse(exchange, 200, "{\"message\": \"Assignment deleted successfully.\"}");
                   } else {
                       sendResponse(exchange, 500, "{\"error\": \"Failed to delete assignment.\"}");
                   }
               }
           } catch (Exception e) {
               sendResponse(exchange, 500, "{\"error\": \"Database error\"}");
           }
        }
    }

    static class TeacherSubmissionsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (handleOptions(exchange)) return;
            SessionManager.UserSession session = validateSession(exchange, "TEACHER");
            if (session == null) return;
            int teacherId = session.getUserId();

            try {
                Map<String, String> params = parseQueryParams(exchange.getRequestURI().getQuery());
                String assignmentIdStr = params.get("assignmentId");
                if (assignmentIdStr == null) {
                    sendResponse(exchange, 400, "{\"error\": \"Missing assignmentId.\"}");
                    return;
                }

                int assignmentId = Integer.parseInt(assignmentIdStr);
                Assignment assignment = assignmentDAO.getById(assignmentId);
                if (assignment == null || assignment.getTeacherId() != teacherId) {
                    sendResponse(exchange, 403, "{\"error\": \"Access denied.\"}");
                    return;
                }

                String filter = params.getOrDefault("filter", "all");
                String search = params.getOrDefault("search", "").trim().toLowerCase();

                List<Student> students = studentDAO.getByClassId(assignment.getClassId());
                List<Submission> submissions = submissionDAO.getByAssignmentId(assignmentId);

                Map<Integer, Submission> submissionMap = new HashMap<>();
                for (Submission sub : submissions) {
                    submissionMap.put(sub.getStudentId(), sub);
                }

                List<Map<String, Object>> filteredRoster = new ArrayList<>();
                for (Student student : students) {
                    Submission sub = submissionMap.get(student.getStudentId());

                    // Filter
                    if ("submitted".equals(filter) && sub == null) continue;
                    if ("missing".equals(filter) && sub != null) continue;

                    // Search keyword (matches name or roll number)
                    if (!search.isEmpty()) {
                        boolean nameMatches = student.getFullName().toLowerCase().contains(search);
                        boolean rollMatches = student.getRollNumber().toLowerCase().contains(search);
                        if (!nameMatches && !rollMatches) {
                            continue;
                        }
                    }

                    Map<String, Object> map = new HashMap<>();
                    map.put("student", student);
                    map.put("submission", sub);
                    filteredRoster.add(map);
                }

                Map<String, Object> resp = new HashMap<>();
                resp.put("assignment", assignment);
                resp.put("roster", filteredRoster);
                sendResponse(exchange, 200, JsonUtil.toJson(resp));
            } catch (Exception e) {
                sendResponse(exchange, 500, "{\"error\": \"Database error\"}");
            }
        }
    }

    static class TeacherGradeHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (handleOptions(exchange)) return;
            SessionManager.UserSession session = validateSession(exchange, "TEACHER");
            if (session == null) return;
            int teacherId = session.getUserId();

            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, "{\"error\": \"Method not allowed\"}");
                return;
            }

            try {
                Map<String, Object> req = JsonUtil.parseObject(getRequestBody(exchange));
                Integer submissionId = optInt(req, "submissionId");
                String marksStr = optString(req, "marks");
                String feedback = optString(req, "feedback");

                if (submissionId == null || ValidationUtil.isEmptyOrBlank(marksStr)) {
                    sendResponse(exchange, 400, "{\"error\": \"Submission ID and marks are required.\"}");
                    return;
                }

                Submission sub = submissionDAO.getById(submissionId);
                if (sub == null) {
                    sendResponse(exchange, 404, "{\"error\": \"Submission not found.\"}");
                    return;
                }

                Assignment a = assignmentDAO.getById(sub.getAssignmentId());
                if (a == null || a.getTeacherId() != teacherId) {
                    sendResponse(exchange, 403, "{\"error\": \"Access denied.\"}");
                    return;
                }

                BigDecimal marks = new BigDecimal(marksStr);
                if (marks.compareTo(BigDecimal.ZERO) < 0 || marks.compareTo(new BigDecimal(a.getMaxMarks())) > 0) {
                    sendResponse(exchange, 400, "{\"error\": \"Marks must be between 0 and " + a.getMaxMarks() + "\"}");
                    return;
                }

                boolean success = submissionDAO.gradeSubmission(submissionId, marks, feedback);
                if (success) {
                    logDAO.insert(new ActivityLog(teacherId, ActivityLog.Role.TEACHER, "GRADE_SUBMISSION", "SUBMISSION", submissionId, "Graded submission ID " + submissionId + " with marks: " + marks, getRemoteIp(exchange)));
                    sendResponse(exchange, 200, "{\"message\": \"Submission graded successfully.\"}");
                } else {
                    sendResponse(exchange, 500, "{\"error\": \"Failed to grade submission.\"}");
                }
            } catch (Exception e) {
                sendResponse(exchange, 500, "{\"error\": \"Database error\"}");
            }
        }
    }

    static class StudentDashboardHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (handleOptions(exchange)) return;
            SessionManager.UserSession session = validateSession(exchange, "STUDENT");
            if (session == null) return;
            int studentId = session.getUserId();
            int classId = session.getClassId();

            try {
                List<Assignment> allAssignments = assignmentDAO.getByClassId(classId);
                List<Submission> studentSubmissions = submissionDAO.getByStudentId(studentId);

                Map<Integer, Submission> submissionMap = new HashMap<>();
                for (Submission sub : studentSubmissions) {
                    submissionMap.put(sub.getAssignmentId(), sub);
                }

                List<Map<String, Object>> assignmentList = new ArrayList<>();
                int submittedCount = 0;
                int pendingCount = 0;
                int missingCount = 0;

                for (Assignment assignment : allAssignments) {
                    if (Assignment.Status.DRAFT == assignment.getStatus()) {
                        continue;
                    }

                    Map<String, Object> map = new HashMap<>();
                    map.put("assignment", assignment);

                    Submission sub = submissionMap.get(assignment.getAssignmentId());
                    map.put("submission", sub);

                    String statusStr;
                    boolean isReuploadAllowed = false;

                    if (sub != null) {
                        submittedCount++;
                        statusStr = sub.getStatus().name();
                        isReuploadAllowed = sub.isReUploadAllowed(assignment.getActiveDeadline());
                    } else {
                        Timestamp activeDeadline = assignment.getActiveDeadline();
                        if (DateUtil.isPastDeadline(activeDeadline) || Assignment.Status.CLOSED == assignment.getStatus()) {
                            statusStr = "MISSING";
                            missingCount++;
                        } else {
                            statusStr = "PENDING";
                            pendingCount++;
                        }
                    }

                    map.put("status", statusStr);
                    map.put("isReuploadAllowed", isReuploadAllowed);
                    assignmentList.add(map);
                }

                Map<String, Object> data = new HashMap<>();
                data.put("assignmentsStats", assignmentList);
                data.put("totalAssignmentsCount", assignmentList.size());
                data.put("submittedCount", submittedCount);
                data.put("pendingCount", pendingCount);
                data.put("missingCount", missingCount);

                sendResponse(exchange, 200, JsonUtil.toJson(data));
            } catch (Exception e) {
                sendResponse(exchange, 500, "{\"error\": \"Database error\"}");
            }
        }
    }

    static class StudentSubmitHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (handleOptions(exchange)) return;
            SessionManager.UserSession session = validateSession(exchange, "STUDENT");
            if (session == null) return;
            int studentId = session.getUserId();

            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, "{\"error\": \"Method not allowed\"}");
                return;
            }

            try {
                Map<String, Object> req = JsonUtil.parseObject(getRequestBody(exchange));
                Integer assignmentId = optInt(req, "assignmentId");
                String fileName = optString(req, "fileName");
                String fileBase64 = optString(req, "fileBase64");

                if (assignmentId == null || ValidationUtil.isEmptyOrBlank(fileName) || ValidationUtil.isEmptyOrBlank(fileBase64)) {
                    sendResponse(exchange, 400, "{\"error\": \"Assignment ID, file name, and file content are required.\"}");
                    return;
                }

                Assignment assignment = assignmentDAO.getById(assignmentId);
                if (assignment == null || assignment.getStatus() == Assignment.Status.DRAFT) {
                    sendResponse(exchange, 404, "{\"error\": \"Assignment not found.\"}");
                    return;
                }

                Submission existingSub = submissionDAO.getByAssignmentAndStudent(assignmentId, studentId);
                Timestamp activeDeadline = assignment.getActiveDeadline();
                Timestamp currentTime = new Timestamp(System.currentTimeMillis());

                if (existingSub == null) {
                    // Create New Submission
                    Submission submission = new Submission();
                    submission.setAssignmentId(assignmentId);
                    submission.setStudentId(studentId);
                    submission.setFileName(fileName);
                    
                    // Decode bytes for metadata size
                    byte[] decoded = Base64.getDecoder().decode(fileBase64.contains(",") ? fileBase64.substring(fileBase64.indexOf(",") + 1) : fileBase64);
                    submission.setFileSizeBytes((long) decoded.length);
                    submission.setFileType(FileUploadUtil.getFileExtension(fileName));

                    if (activeDeadline != null && currentTime.after(activeDeadline)) {
                        submission.setStatus(Submission.Status.LATE);
                    } else {
                        submission.setStatus(Submission.Status.SUBMITTED);
                    }

                    String relativePath = FileUploadUtil.saveFile(fileName, fileBase64, UPLOADS_BASE_PATH, "submissions");
                    if (relativePath == null) {
                        sendResponse(exchange, 400, "{\"error\": \"File validation failed (size > 10MB or disallowed type).\"}");
                        return;
                    }
                    submission.setFilePath(relativePath);

                    boolean success = submissionDAO.insert(submission);
                    if (success) {
                        logDAO.insert(new ActivityLog(studentId, ActivityLog.Role.STUDENT, "SUBMIT_ASSIGNMENT", "SUBMISSION", submission.getSubmissionId(), "Submitted homework: " + assignment.getTitle(), getRemoteIp(exchange)));
                        sendResponse(exchange, 200, "{\"message\": \"Assignment submitted successfully.\"}");
                    } else {
                        sendResponse(exchange, 500, "{\"error\": \"Failed to save submission in database.\"}");
                    }
                } else {
                    // Update existing submission (resubmission)
                    boolean isAllowed = existingSub.isReUploadAllowed(activeDeadline);
                    if (!isAllowed) {
                        sendResponse(exchange, 400, "{\"error\": \"Re-upload window expired or deadline has passed.\"}");
                        return;
                    }

                    // Delete old file
                    FileUploadUtil.deleteFile(UPLOADS_BASE_PATH, existingSub.getFilePath());

                    String relativePath = FileUploadUtil.saveFile(fileName, fileBase64, UPLOADS_BASE_PATH, "submissions");
                    if (relativePath == null) {
                        sendResponse(exchange, 400, "{\"error\": \"File validation failed on re-upload.\"}");
                        return;
                    }

                    existingSub.setFileName(fileName);
                    existingSub.setFilePath(relativePath);
                    byte[] decoded = Base64.getDecoder().decode(fileBase64.contains(",") ? fileBase64.substring(fileBase64.indexOf(",") + 1) : fileBase64);
                    existingSub.setFileSizeBytes((long) decoded.length);
                    existingSub.setFileType(FileUploadUtil.getFileExtension(fileName));
                    existingSub.setResubmitCount(existingSub.getResubmitCount() + 1);

                    if (activeDeadline != null && currentTime.after(activeDeadline)) {
                        existingSub.setStatus(Submission.Status.LATE);
                    } else {
                        existingSub.setStatus(Submission.Status.SUBMITTED);
                    }

                    boolean success = submissionDAO.update(existingSub);
                    if (success) {
                        logDAO.insert(new ActivityLog(studentId, ActivityLog.Role.STUDENT, "RESUBMIT_ASSIGNMENT", "SUBMISSION", existingSub.getSubmissionId(), "Resubmitted assignment: " + assignment.getTitle(), getRemoteIp(exchange)));
                        sendResponse(exchange, 200, "{\"message\": \"Assignment re-uploaded successfully.\"}");
                    } else {
                        sendResponse(exchange, 500, "{\"error\": \"Failed to update submission in database.\"}");
                    }
                }
            } catch (Exception e) {
                sendResponse(exchange, 500, "{\"error\": \"Database error during upload.\"}");
            }
        }
    }

    static class DownloadHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (handleOptions(exchange)) return;

            try {
                Map<String, String> params = parseQueryParams(exchange.getRequestURI().getQuery());
                String idStr = params.get("id");
                String type = params.get("type"); // "assignment" or "submission"
                String token = params.get("token");

                if (idStr == null || type == null || token == null) {
                    sendResponse(exchange, 400, "{\"error\": \"Missing id, type, or token parameter.\"}");
                    return;
                }

                SessionManager.UserSession session = SessionManager.getSession(token);
                if (session == null) {
                    sendResponse(exchange, 401, "{\"error\": \"Unauthorized. Invalid session token.\"}");
                    return;
                }

                int recordId = Integer.parseInt(idStr);
                String relativePath = null;
                String originalFileName = null;

                if ("assignment".equalsIgnoreCase(type)) {
                    Assignment a = assignmentDAO.getById(recordId);
                    if (a != null) {
                        // Students in this class or the teacher who created it can download
                        if ("STUDENT".equalsIgnoreCase(session.getRole()) && session.getClassId() != a.getClassId()) {
                            sendResponse(exchange, 403, "{\"error\": \"Access denied.\"}");
                            return;
                        }
                        if ("TEACHER".equalsIgnoreCase(session.getRole()) && session.getUserId() != a.getTeacherId()) {
                            sendResponse(exchange, 403, "{\"error\": \"Access denied.\"}");
                            return;
                        }
                        relativePath = a.getFilePath();
                        originalFileName = a.getFileName();
                    }
                } else if ("submission".equalsIgnoreCase(type)) {
                    Submission sub = submissionDAO.getById(recordId);
                    if (sub != null) {
                        // Student who submitted or the teacher who created the assignment can download
                        if ("STUDENT".equalsIgnoreCase(session.getRole()) && session.getUserId() != sub.getStudentId()) {
                            sendResponse(exchange, 403, "{\"error\": \"Access denied.\"}");
                            return;
                        }
                        if ("TEACHER".equalsIgnoreCase(session.getRole())) {
                            Assignment a = assignmentDAO.getById(sub.getAssignmentId());
                            if (a == null || a.getTeacherId() != session.getUserId()) {
                                sendResponse(exchange, 403, "{\"error\": \"Access denied.\"}");
                                return;
                            }
                        }
                        relativePath = sub.getFilePath();
                        originalFileName = sub.getFileName();
                    }
                }

                if (relativePath == null || originalFileName == null) {
                    sendResponse(exchange, 404, "{\"error\": \"File record not found.\"}");
                    return;
                }

                // Strip leading "uploads/" from db path
                String cleanedPath = relativePath;
                if (cleanedPath.startsWith("uploads/")) {
                    cleanedPath = cleanedPath.substring("uploads/".length());
                } else if (cleanedPath.startsWith("uploads\\")) {
                    cleanedPath = cleanedPath.substring("uploads\\".length());
                }

                String fullPath = UPLOADS_BASE_PATH + File.separator + cleanedPath.replace("/", File.separator).replace("\\", File.separator);
                File file = new File(fullPath);

                if (!file.exists()) {
                    sendResponse(exchange, 404, "{\"error\": \"Physical file not found on server.\"}");
                    return;
                }

                // Set headers for file download
                exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
                exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, OPTIONS");
                exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
                exchange.getResponseHeaders().set("Content-Disposition", "attachment; filename=\"" + originalFileName + "\"");
                exchange.getResponseHeaders().set("Content-Type", "application/octet-stream");

                exchange.sendResponseHeaders(200, file.length());
                try (FileInputStream fis = new FileInputStream(file);
                     OutputStream os = exchange.getResponseBody()) {
                    byte[] buffer = new byte[4096];
                    int read;
                    while ((read = fis.read(buffer)) != -1) {
                        os.write(buffer, 0, read);
                    }
                }
            } catch (Exception e) {
                sendResponse(exchange, 500, "{\"error\": \"Server error during file download.\"}");
            }
        }
    }
}
