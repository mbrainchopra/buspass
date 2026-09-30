package org.example.buspass.controller;

import jakarta.servlet.http.HttpSession;
import org.example.buspass.entity.BusPass;
import org.example.buspass.entity.User;
import org.example.buspass.repository.BusPassRepository;
import org.example.buspass.repository.UserRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
public class UserController {

    private final UserRepository userRepository;
    private final BusPassRepository busPassRepository;

    public UserController(
            UserRepository userRepository,
            BusPassRepository busPassRepository) {

        this.userRepository = userRepository;
        this.busPassRepository = busPassRepository;
    }

    // =========================
    // REGISTER
    // =========================

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(
            @ModelAttribute User user,
            Model model) {

        if (user.getName() == null ||
                user.getName().trim().isEmpty()) {

            model.addAttribute(
                    "error",
                    "Name is required."
            );

            return "register";
        }

        if (user.getEmail() == null ||
                user.getEmail().trim().isEmpty()) {

            model.addAttribute(
                    "error",
                    "Email is required."
            );

            return "register";
        }

        String email =
                user.getEmail().trim();

        if (!email.contains("@") ||
                !email.contains(".")) {

            model.addAttribute(
                    "error",
                    "Please enter a valid email address."
            );

            return "register";
        }

        if (user.getPassword() == null ||
                user.getPassword().trim().isEmpty()) {

            model.addAttribute(
                    "error",
                    "Password is required."
            );

            return "register";
        }

        if (user.getPassword().length() < 6) {

            model.addAttribute(
                    "error",
                    "Password must contain at least 6 characters."
            );

            return "register";
        }

        if (user.getPhone() == null ||
                user.getPhone().trim().isEmpty()) {

            model.addAttribute(
                    "error",
                    "Phone number is required."
            );

            return "register";
        }

        if (!user.getPhone().matches("\\d{10}")) {

            model.addAttribute(
                    "error",
                    "Phone number must contain 10 digits."
            );

            return "register";
        }

        if (userRepository.existsByEmail(email)) {

            model.addAttribute(
                    "error",
                    "Email already registered!"
            );

            return "register";
        }

        user.setName(user.getName().trim());
        user.setEmail(email);
        user.setPhone(user.getPhone().trim());
        user.setRole("USER");

        userRepository.save(user);

        return "redirect:/login";
    }

    // =========================
    // LOGIN PAGE
    // =========================

    @GetMapping("/login")
    public String loginPage(
            HttpSession session) {

        User loggedUser =
                (User) session.getAttribute("user");

        if (loggedUser != null) {

            if ("ADMIN".equals(loggedUser.getRole())) {
                return "redirect:/admin/dashboard";
            }

            return "redirect:/user/dashboard";
        }

        return "login";
    }

    // =========================
    // LOGIN
    // =========================

    @PostMapping("/login")
    public String loginUser(
            @RequestParam String email,
            @RequestParam String password,
            Model model,
            HttpSession session) {

        if (email == null ||
                email.trim().isEmpty()) {

            model.addAttribute(
                    "error",
                    "Email is required."
            );

            return "login";
        }

        if (password == null ||
                password.trim().isEmpty()) {

            model.addAttribute(
                    "error",
                    "Password is required."
            );

            return "login";
        }

        Optional<User> optionalUser =
                userRepository.findByEmail(
                        email.trim()
                );

        if (optionalUser.isEmpty()) {

            model.addAttribute(
                    "error",
                    "Invalid email or password."
            );

            return "login";
        }

        User user =
                optionalUser.get();

        if (!user.getPassword().equals(password)) {

            model.addAttribute(
                    "error",
                    "Invalid email or password."
            );

            return "login";
        }

        session.setAttribute(
                "user",
                user
        );

        if ("ADMIN".equals(user.getRole())) {
            return "redirect:/admin/dashboard";
        }

        return "redirect:/user/dashboard";
    }

    // =========================
    // USER DASHBOARD
    // =========================

    @GetMapping("/user/dashboard")
    public String userDashboard(
            HttpSession session,
            Model model) {

        User user =
                (User) session.getAttribute("user");

        if (user == null) {
            return "redirect:/login";
        }

        if ("ADMIN".equals(user.getRole())) {
            return "redirect:/admin/dashboard";
        }

        Long userId =
                user.getId();

        long total =
                busPassRepository.countByUserId(userId);

        long pending =
                busPassRepository.countByUserIdAndStatus(
                        userId,
                        "PENDING"
                );

        long approved =
                busPassRepository.countByUserIdAndStatus(
                        userId,
                        "APPROVED"
                );

        long rejected =
                busPassRepository.countByUserIdAndStatus(
                        userId,
                        "REJECTED"
                );

        List<BusPass> passes =
                busPassRepository.findByUserId(userId);

        model.addAttribute("user", user);
        model.addAttribute("total", total);
        model.addAttribute("pending", pending);
        model.addAttribute("approved", approved);
        model.addAttribute("rejected", rejected);
        model.addAttribute("passes", passes);

        return "user-dashboard";
    }

    // =========================
    // USER PROFILE
    // =========================

    @GetMapping("/user/profile")
    public String profile(
            HttpSession session,
            Model model) {

        User user =
                (User) session.getAttribute("user");

        if (user == null) {
            return "redirect:/login";
        }

        if ("ADMIN".equals(user.getRole())) {
            return "redirect:/admin/dashboard";
        }

        model.addAttribute("user", user);

        return "profile";
    }

    // =========================
    // CHANGE PASSWORD PAGE
    // =========================

    @GetMapping("/user/change-password")
    public String changePasswordPage(
            HttpSession session,
            Model model) {

        User user =
                (User) session.getAttribute("user");

        if (user == null) {
            return "redirect:/login";
        }

        if ("ADMIN".equals(user.getRole())) {
            return "redirect:/admin/dashboard";
        }

        return "change-password";
    }

    // =========================
    // CHANGE PASSWORD
    // =========================

    @PostMapping("/user/change-password")
    public String changePassword(
            @RequestParam String currentPassword,
            @RequestParam String newPassword,
            @RequestParam String confirmPassword,
            HttpSession session,
            Model model) {

        User sessionUser =
                (User) session.getAttribute("user");

        if (sessionUser == null) {
            return "redirect:/login";
        }

        if ("ADMIN".equals(sessionUser.getRole())) {
            return "redirect:/admin/dashboard";
        }

        Optional<User> optionalUser =
                userRepository.findById(
                        sessionUser.getId()
                );

        if (optionalUser.isEmpty()) {
            session.invalidate();
            return "redirect:/login";
        }

        User user =
                optionalUser.get();

        // Check current password

        if (!user.getPassword()
                .equals(currentPassword)) {

            model.addAttribute(
                    "error",
                    "Current password is incorrect."
            );

            return "change-password";
        }

        // New password validation

        if (newPassword == null ||
                newPassword.trim().isEmpty()) {

            model.addAttribute(
                    "error",
                    "New password is required."
            );

            return "change-password";
        }

        if (newPassword.length() < 6) {

            model.addAttribute(
                    "error",
                    "New password must contain at least 6 characters."
            );

            return "change-password";
        }

        // Confirm password

        if (!newPassword.equals(confirmPassword)) {

            model.addAttribute(
                    "error",
                    "New password and confirm password do not match."
            );

            return "change-password";
        }

        // Prevent same password

        if (newPassword.equals(currentPassword)) {

            model.addAttribute(
                    "error",
                    "New password must be different from the current password."
            );

            return "change-password";
        }

        user.setPassword(newPassword);

        userRepository.save(user);

        // Update session user

        session.setAttribute(
                "user",
                user
        );

        model.addAttribute(
                "success",
                "Password changed successfully."
        );

        return "change-password";
    }

    // =========================
    // USER NOTIFICATIONS
    // =========================

    @GetMapping("/user/notifications")
    public String notifications(
            HttpSession session,
            Model model) {

        User user =
                (User) session.getAttribute("user");

        if (user == null) {
            return "redirect:/login";
        }

        if ("ADMIN".equals(user.getRole())) {
            return "redirect:/admin/dashboard";
        }

        List<BusPass> passes =
                busPassRepository.findByUserId(
                        user.getId()
                );

        model.addAttribute(
                "passes",
                passes
        );

        return "notifications";
    }

    // =========================
    // LOGOUT
    // =========================

    @GetMapping("/logout")
    public String logout(
            HttpSession session) {

        session.invalidate();

        return "redirect:/login";
    }
}