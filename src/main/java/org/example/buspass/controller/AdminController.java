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
@RequestMapping("/admin")
public class AdminController {

    private final BusPassRepository busPassRepository;
    private final UserRepository userRepository;

    public AdminController(
            BusPassRepository busPassRepository,
            UserRepository userRepository) {

        this.busPassRepository = busPassRepository;
        this.userRepository = userRepository;
    }

    // =========================
    // ADMIN DASHBOARD
    // =========================

    @GetMapping("/dashboard")
    public String dashboard(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            HttpSession session,
            Model model) {

        User admin =
                (User) session.getAttribute("user");

        if (admin == null) {
            return "redirect:/login";
        }

        if (!"ADMIN".equals(admin.getRole())) {
            return "redirect:/user/dashboard";
        }

        List<BusPass> passes;

        if (search != null &&
                !search.trim().isEmpty()) {

            String keyword =
                    search.trim();

            passes =
                    busPassRepository
                            .findByUser_NameContainingIgnoreCaseOrUser_EmailContainingIgnoreCaseOrUser_PhoneContaining(
                                    keyword,
                                    keyword,
                                    keyword
                            );

            if (status != null &&
                    !status.isEmpty()) {

                passes = passes.stream()
                        .filter(pass ->
                                status.equals(
                                        pass.getStatus()
                                ))
                        .toList();
            }

        } else if (status != null &&
                !status.isEmpty()) {

            passes =
                    busPassRepository
                            .findByStatus(status);

        } else {

            passes =
                    busPassRepository.findAll();
        }

        long total =
                busPassRepository.count();

        long pending =
                busPassRepository.countByStatus(
                        "PENDING"
                );

        long approved =
                busPassRepository.countByStatus(
                        "APPROVED"
                );

        long rejected =
                busPassRepository.countByStatus(
                        "REJECTED"
                );

        model.addAttribute(
                "passes",
                passes
        );

        model.addAttribute(
                "total",
                total
        );

        model.addAttribute(
                "pending",
                pending
        );

        model.addAttribute(
                "approved",
                approved
        );

        model.addAttribute(
                "rejected",
                rejected
        );

        model.addAttribute(
                "selectedStatus",
                status
        );

        model.addAttribute(
                "search",
                search
        );

        return "admin-dashboard";
    }

    // =========================
    // ADMIN PROFILE
    // =========================

    @GetMapping("/profile")
    public String profile(
            HttpSession session,
            Model model) {

        User admin =
                (User) session.getAttribute("user");

        if (admin == null) {
            return "redirect:/login";
        }

        if (!"ADMIN".equals(admin.getRole())) {
            return "redirect:/user/dashboard";
        }

        model.addAttribute(
                "user",
                admin
        );

        return "admin-profile";
    }

    // =========================
    // ADMIN CHANGE PASSWORD PAGE
    // =========================

    @GetMapping("/change-password")
    public String changePasswordPage(
            HttpSession session) {

        User admin =
                (User) session.getAttribute("user");

        if (admin == null) {
            return "redirect:/login";
        }

        if (!"ADMIN".equals(admin.getRole())) {
            return "redirect:/user/dashboard";
        }

        return "admin-change-password";
    }

    // =========================
    // ADMIN CHANGE PASSWORD
    // =========================

    @PostMapping("/change-password")
    public String changePassword(
            @RequestParam String currentPassword,
            @RequestParam String newPassword,
            @RequestParam String confirmPassword,
            HttpSession session,
            Model model) {

        User admin =
                (User) session.getAttribute("user");

        if (admin == null) {
            return "redirect:/login";
        }

        if (!"ADMIN".equals(admin.getRole())) {
            return "redirect:/user/dashboard";
        }

        Optional<User> optionalUser =
                userRepository.findById(
                        admin.getId()
                );

        if (optionalUser.isEmpty()) {

            session.invalidate();

            return "redirect:/login";
        }

        User user =
                optionalUser.get();

        // Current password

        if (!user.getPassword()
                .equals(currentPassword)) {

            model.addAttribute(
                    "error",
                    "Current password is incorrect."
            );

            return "admin-change-password";
        }

        // New password

        if (newPassword == null ||
                newPassword.trim().isEmpty()) {

            model.addAttribute(
                    "error",
                    "New password is required."
            );

            return "admin-change-password";
        }

        if (newPassword.length() < 6) {

            model.addAttribute(
                    "error",
                    "New password must contain at least 6 characters."
            );

            return "admin-change-password";
        }

        // Confirm password

        if (!newPassword.equals(confirmPassword)) {

            model.addAttribute(
                    "error",
                    "New password and confirm password do not match."
            );

            return "admin-change-password";
        }

        // Same password

        if (newPassword.equals(currentPassword)) {

            model.addAttribute(
                    "error",
                    "New password must be different from the current password."
            );

            return "admin-change-password";
        }

        user.setPassword(newPassword);

        userRepository.save(user);

        session.setAttribute(
                "user",
                user
        );

        model.addAttribute(
                "success",
                "Admin password changed successfully."
        );

        return "admin-change-password";
    }

    // =========================
    // VIEW APPLICATION
    // =========================

    @GetMapping("/pass/{id}")
    public String viewPass(
            @PathVariable Long id,
            HttpSession session,
            Model model) {

        User admin =
                (User) session.getAttribute("user");

        if (admin == null) {
            return "redirect:/login";
        }

        if (!"ADMIN".equals(admin.getRole())) {
            return "redirect:/user/dashboard";
        }

        BusPass pass =
                busPassRepository
                        .findById(id)
                        .orElse(null);

        if (pass == null) {
            return "redirect:/admin/dashboard";
        }

        model.addAttribute(
                "pass",
                pass
        );

        return "admin-pass-details";
    }

    // =========================
    // APPROVE
    // =========================

    @PostMapping("/approve/{id}")
    public String approvePass(
            @PathVariable Long id,
            @RequestParam(required = false) String remark,
            HttpSession session) {

        User admin =
                (User) session.getAttribute("user");

        if (admin == null) {
            return "redirect:/login";
        }

        if (!"ADMIN".equals(admin.getRole())) {
            return "redirect:/user/dashboard";
        }

        BusPass pass =
                busPassRepository
                        .findById(id)
                        .orElse(null);

        if (pass != null) {

            pass.setStatus("APPROVED");

            if (remark != null &&
                    !remark.trim().isEmpty()) {

                pass.setAdminRemark(
                        remark.trim()
                );

            } else {

                pass.setAdminRemark(
                        "Bus pass approved."
                );
            }

            busPassRepository.save(pass);
        }

        return "redirect:/admin/pass/" + id;
    }

    // =========================
    // REJECT
    // =========================

    @PostMapping("/reject/{id}")
    public String rejectPass(
            @PathVariable Long id,
            @RequestParam(required = false) String remark,
            HttpSession session) {

        User admin =
                (User) session.getAttribute("user");

        if (admin == null) {
            return "redirect:/login";
        }

        if (!"ADMIN".equals(admin.getRole())) {
            return "redirect:/user/dashboard";
        }

        BusPass pass =
                busPassRepository
                        .findById(id)
                        .orElse(null);

        if (pass != null) {

            pass.setStatus("REJECTED");

            if (remark != null &&
                    !remark.trim().isEmpty()) {

                pass.setAdminRemark(
                        remark.trim()
                );

            } else {

                pass.setAdminRemark(
                        "Bus pass rejected."
                );
            }

            busPassRepository.save(pass);
        }

        return "redirect:/admin/pass/" + id;
    }

    // =========================
    // MANAGE USERS
    // =========================

    @GetMapping("/users")
    public String users(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String role,
            HttpSession session,
            Model model) {

        User admin =
                (User) session.getAttribute("user");

        if (admin == null) {
            return "redirect:/login";
        }

        if (!"ADMIN".equals(admin.getRole())) {
            return "redirect:/user/dashboard";
        }

        List<User> users;

        if (search != null &&
                !search.trim().isEmpty()) {

            String keyword =
                    search.trim();

            users =
                    userRepository
                            .findByNameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrPhoneContaining(
                                    keyword,
                                    keyword,
                                    keyword
                            );

        } else if (role != null &&
                !role.isEmpty()) {

            users =
                    userRepository.findByRole(role);

        } else {

            users =
                    userRepository.findAll();
        }

        model.addAttribute(
                "users",
                users
        );

        model.addAttribute(
                "search",
                search
        );

        model.addAttribute(
                "selectedRole",
                role
        );

        return "manage-users";
    }

    // =========================
    // DELETE USER
    // =========================

    @GetMapping("/users/delete/{id}")
    public String deleteUser(
            @PathVariable Long id,
            HttpSession session) {

        User admin =
                (User) session.getAttribute("user");

        if (admin == null) {
            return "redirect:/login";
        }

        if (!"ADMIN".equals(admin.getRole())) {
            return "redirect:/user/dashboard";
        }

        if (admin.getId().equals(id)) {
            return "redirect:/admin/users";
        }

        userRepository.deleteById(id);

        return "redirect:/admin/users";
    }
}