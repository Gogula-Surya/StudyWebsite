package com.example.login.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.example.login.model.User;
import com.example.login.repository.UserRepository;

@Controller
public class AuthController {

    @Autowired
    private UserRepository userRepo;

    @GetMapping("/")
    public String home() {
        return "index"; // home.html
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login"; // login.html
    }

    @GetMapping("/signup")
    public String signupPage(Model model) {
        model.addAttribute("user", new User());
        return "signup"; // signup.html
    }
    
    @GetMapping("/admin-login")
    public String showAdminLoginPage() {
        return "admin-login"; // This should match your admin-login.html file
    }

    @GetMapping("/admin")
    public String showAdminPage(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("admin") != null && (Boolean) session.getAttribute("admin")) {
            return "admin"; // admin.html
        } else {
            return "redirect:/admin-login"; // not logged in → redirect to login
        }
    }
    
    
    // admin page login details 
    
    @PostMapping("/admin-login")
    public String processAdminLogin(HttpServletRequest request, Model model) {
        String email = request.getParameter("email");
        String password = request.getParameter("password");

        // You can change this to values fetched from DB or application.properties later
        String adminEmail = "suryaloveskala@gmail.com";
        String adminPassword = "suryakala143";

        if (adminEmail.equals(email) && adminPassword.equals(password)) {
            HttpSession session = request.getSession();
            session.setAttribute("admin", true);
            return "redirect:/admin"; // redirects to admin.html
        } else {
            model.addAttribute("error", "Invalid admin credentials.");
            return "admin-login"; // reloads the admin-login.html with error message
        }
    }
    
    @PostMapping("/signup")
    public String processSignup(@ModelAttribute User user, HttpServletRequest request) {
        userRepo.save(user);
        HttpSession session = request.getSession();
        session.setAttribute("user", user);
        return "redirect:/studyhomepage";
    }

    @PostMapping("/login")
    public String processLogin(HttpServletRequest request, Model model) {
        String email = request.getParameter("email");
        String password = request.getParameter("password");

        List<User> users = userRepo.findByEmail(email);

        if (users.size() == 1) {
            User user = users.get(0);
            if (user.getPassword().equals(password)) {
                HttpSession session = request.getSession();
                session.setAttribute("user", user);
                return "redirect:/studyhomepage";
            } else {
                model.addAttribute("error", "Incorrect password.");
            }
        } else {
            model.addAttribute("error", "Email not found.");
        }
        return "login";
    }

    @GetMapping("/studyhomepage")
    public String studyHomepage(HttpServletRequest request, Model model) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            User user = (User) session.getAttribute("user");
            if (user != null) {
                model.addAttribute("user", user);
                return "studyhomepage";
            }
        }
        return "redirect:/login";
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return "redirect:/";
    }

    // ✅ User Delete from studyhomepage.html (session user)
    @GetMapping("/delete")
    public String deleteSessionUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            User user = (User) session.getAttribute("user");
            if (user != null) {
                userRepo.deleteById(user.getId());
                session.invalidate();
            }
        }
        return "redirect:/";
    }

    // ✅ User Delete by Email (for special cases)
    @DeleteMapping("/user/{email}")
    @ResponseBody
    public String deleteUserByEmail(@PathVariable String email, HttpServletRequest request) {
        List<User> users = userRepo.findByEmail(email);
        if (!users.isEmpty()) {
            User user = users.get(0);
            userRepo.deleteById(user.getId());

            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }
            return "User deleted successfully.";
        } else {
            return "User not found.";
        }
    }

    // ✅ Return User session details (for JavaScript)
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
            userDetails.put("password", user.getPassword());
        }
        return userDetails;
    }

    // ✅ Profile API
    @GetMapping("/profile")
    @ResponseBody
    public User getProfile(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            throw new RuntimeException("User not logged in");
        }
        return user;
    }

    // ✅ Get all users for Admin Page
    @GetMapping("/api/users")
    @ResponseBody
    public List<User> getAllUsers() {
        return userRepo.findAll();
    }

    // ✅ Delete user by ID (Admin Panel)
    @DeleteMapping("/api/users/{id}")
    public ResponseEntity<?> deleteUserById(@PathVariable Long id) {
        userRepo.deleteById(id);
        return ResponseEntity.ok().build();
    }

    // ✅ Export all users to CSV
    @GetMapping("/api/users/export")
    public void exportToCSV(HttpServletResponse response) throws IOException {
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=users.csv");

        List<User> users = userRepo.findAll();

        PrintWriter writer = response.getWriter();
        writer.println("First Name,Last Name,Email,Gender,Password");

        for (User user : users) {
            writer.println(user.getFirstName() + "," + user.getLastName() + "," +
                           user.getEmail() + "," + user.getGender() + "," + user.getPassword());
        }
        writer.flush();
        writer.close();
    }
    
    

}
