package com.example.login.controller;

import com.example.login.model.User;
import com.example.login.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class AuthController {

    @Autowired
    private UserRepository userRepo;

    // === HOME PAGE ===
    @GetMapping("/")
    public String home() {
        return "index"; // index.html
    }

    // === LOGIN PAGE ===
    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    // === SIGNUP PAGE ===
    @GetMapping("/signup")
    public String signupPage(Model model) {
        model.addAttribute("user", new User());
        return "signup";
    }

    // === PROCESS SIGNUP ===
    @PostMapping("/signup")
    public String processSignup(@ModelAttribute User user, HttpServletRequest request) {
        userRepo.save(user);
        request.getSession().setAttribute("user", user);
        return "redirect:/studyhomepage";
    }

    // === PROCESS LOGIN ===
    @PostMapping("/login")
    public String processLogin(HttpServletRequest request, Model model) {
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        List<User> users = userRepo.findByEmail(email);

        if (users.size() == 1 && users.get(0).getPassword().equals(password)) {
            request.getSession().setAttribute("user", users.get(0));
            return "redirect:/studyhomepage";
        } else {
            model.addAttribute("error", users.isEmpty() ? "Email not found." : "Incorrect password.");
            return "login";
        }
    }

    // === USER HOME PAGE (AFTER LOGIN) ===
    @GetMapping("/studyhomepage")
    public String studyHomepage(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user != null) {
            model.addAttribute("user", user);
            return "studyhomepage";
        }
        return "redirect:/login";
    }

    // === LOGOUT ===
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        if (session != null) {
            session.invalidate();
        }
        return "redirect:/";
    }

    // === DELETE USER FROM SESSION ===
    @GetMapping("/delete")
    public String deleteSessionUser(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user != null) {
            userRepo.deleteById(user.getId());
            session.invalidate();
        }
        return "redirect:/";
    }

    // === DELETE USER BY EMAIL (FOR DEV/DEBUGGING) ===
    @DeleteMapping("/user/{email}")
    @ResponseBody
    public String deleteUserByEmail(@PathVariable String email, HttpSession session) {
        List<User> users = userRepo.findByEmail(email);
        if (!users.isEmpty()) {
            userRepo.deleteById(users.get(0).getId());
            if (session != null) {
                session.invalidate();
            }
            return "User deleted successfully.";
        }
        return "User not found.";
    }

    // === GET USER DETAILS (WITHOUT PASSWORD) ===
    @GetMapping("/user/details")
    @ResponseBody
    public Map<String, String> getUserDetails(HttpSession session) {
        Map<String, String> userDetails = new HashMap<>();
        User user = (User) session.getAttribute("user");
        if (user != null) {
            userDetails.put("firstName", user.getFirstName());
            userDetails.put("lastName", user.getLastName());
            userDetails.put("gender", user.getGender());
            userDetails.put("email", user.getEmail());
        }
        return userDetails;
    }

    // === GET FULL USER OBJECT (FOR INTERNAL USE) ===
    @GetMapping("/profile")
    @ResponseBody
    public User getProfile(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) throw new RuntimeException("User not logged in");
        return user;
    }

    // === ADMIN LOGIN PAGE ===
    @GetMapping("/admin-login")
    public String showAdminLoginPage() {
        return "admin-login";
    }

    // === ADMIN LOGIN POST ===
    @PostMapping("/admin-login")
    public String processAdminLogin(HttpServletRequest request, Model model) {
        String email = request.getParameter("email");
        String password = request.getParameter("password");

        if ("suryaloveskala@gmail.com".equals(email) && "suryakala143".equals(password)) {
            request.getSession().setAttribute("admin", true);
            return "redirect:/admin";
        } else {
            model.addAttribute("error", "Invalid admin credentials.");
            return "admin-login";
        }
    }

    // === ADMIN PAGE (AUTH CHECK) ===
    @GetMapping("/admin")
    public String showAdminPage(HttpSession session) {
        Boolean isAdmin = (Boolean) session.getAttribute("admin");
        if (isAdmin != null && isAdmin) {
            return "admin"; // admin.html
        }
        return "redirect:/admin-login";
    }

    // === GET ALL USERS (ADMIN PANEL) ===
    @GetMapping("/api/users")
    @ResponseBody
    public List<User> getAllUsers() {
        return userRepo.findAll();
    }

    // === DELETE USER BY ID (ADMIN PANEL) ===
    @DeleteMapping("/api/users/{id}")
    public ResponseEntity<?> deleteUserById(@PathVariable Long id) {
        userRepo.deleteById(id);
        return ResponseEntity.ok().build();
    }

    // === EXPORT USERS TO CSV (ADMIN FEATURE) ===
    @GetMapping("/api/users/export")
    public void exportToCSV(HttpServletResponse response) throws IOException {
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=users.csv");

        List<User> users = userRepo.findAll();
        PrintWriter writer = response.getWriter();
        writer.println("First Name,Last Name,Email,Gender,Password");

        for (User user : users) {
            writer.printf("%s,%s,%s,%s,%s%n",
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getGender(),
                user.getPassword()
            );
        }
        writer.flush();
        writer.close();
    }
}
