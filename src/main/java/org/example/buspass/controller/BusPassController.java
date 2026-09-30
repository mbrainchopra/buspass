package org.example.buspass.controller;

import jakarta.servlet.http.HttpSession;
import org.example.buspass.entity.BusPass;
import org.example.buspass.entity.Route;
import org.example.buspass.entity.User;
import org.example.buspass.repository.BusPassRepository;
import org.example.buspass.repository.RouteRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/bus-pass")
public class BusPassController {

    private final BusPassRepository busPassRepository;
    private final RouteRepository routeRepository;

    public BusPassController(
            BusPassRepository busPassRepository,
            RouteRepository routeRepository) {

        this.busPassRepository = busPassRepository;
        this.routeRepository = routeRepository;
    }

    // =========================
    // APPLY BUS PASS PAGE
    // =========================

    @GetMapping("/apply")
    public String applyPage(
            HttpSession session,
            Model model) {

        User user =
                (User) session.getAttribute("user");

        if (user == null) {
            return "redirect:/login";
        }

        List<Route> routes =
                routeRepository.findAll();

        model.addAttribute(
                "routes",
                routes
        );

        return "apply-pass";
    }

    // =========================
    // APPLY BUS PASS
    // =========================

    @PostMapping("/apply")
    public String applyBusPass(
            @RequestParam Long routeId,
            @RequestParam String passType,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate,
            HttpSession session) {

        User user =
                (User) session.getAttribute("user");

        if (user == null) {
            return "redirect:/login";
        }

        Route route =
                routeRepository
                        .findById(routeId)
                        .orElse(null);

        if (route == null) {
            return "redirect:/bus-pass/apply";
        }

        BusPass busPass =
                new BusPass();

        busPass.setUser(user);
        busPass.setRoute(route);
        busPass.setPassType(passType);
        busPass.setStartDate(startDate);
        busPass.setEndDate(endDate);
        busPass.setStatus("PENDING");

        busPassRepository.save(busPass);

        return "redirect:/bus-pass/my-passes";
    }

    // =========================
    // MY PASSES
    // =========================

    @GetMapping("/my-passes")
    public String myPasses(
            HttpSession session,
            Model model) {

        User user =
                (User) session.getAttribute("user");

        if (user == null) {
            return "redirect:/login";
        }

        List<BusPass> passes =
                busPassRepository.findByUserId(user.getId());

        long approved = passes.stream()
                .filter(pass -> "APPROVED".equals(pass.getStatus()))
                .count();

        long pending = passes.stream()
                .filter(pass -> "PENDING".equals(pass.getStatus()))
                .count();

        long rejected = passes.stream()
                .filter(pass -> "REJECTED".equals(pass.getStatus()))
                .count();

        model.addAttribute("passes", passes);

        model.addAttribute("approved", approved);
        model.addAttribute("pending", pending);
        model.addAttribute("rejected", rejected);

        return "my-passes";
    }
    // =========================
    // VIEW PASS
    // =========================

    @GetMapping("/view/{id}")
    public String viewPass(
            @PathVariable Long id,
            HttpSession session,
            Model model) {

        User user =
                (User) session.getAttribute("user");

        if (user == null) {
            return "redirect:/login";
        }

        BusPass pass =
                busPassRepository
                        .findById(id)
                        .orElse(null);

        if (pass == null) {
            return "redirect:/bus-pass/my-passes";
        }

        // User can only view their own pass
        if (!pass.getUser().getId()
                .equals(user.getId())) {

            return "redirect:/bus-pass/my-passes";
        }

        model.addAttribute(
                "pass",
                pass
        );

        // Check whether the pass has expired
        boolean expired =
                pass.getEndDate() != null &&
                        pass.getEndDate()
                                .isBefore(LocalDate.now());

        model.addAttribute(
                "expired",
                expired
        );

        return "pass-details";
    }

    // =========================
    // RENEW PASS PAGE
    // =========================

    @GetMapping("/renew/{id}")
    public String renewPage(
            @PathVariable Long id,
            HttpSession session,
            Model model) {

        User user =
                (User) session.getAttribute("user");

        if (user == null) {
            return "redirect:/login";
        }

        BusPass oldPass =
                busPassRepository
                        .findById(id)
                        .orElse(null);

        if (oldPass == null) {
            return "redirect:/bus-pass/my-passes";
        }

        // Only the owner can renew
        if (!oldPass.getUser().getId()
                .equals(user.getId())) {

            return "redirect:/bus-pass/my-passes";
        }

        // Renewal is allowed only for approved passes
        if (!"APPROVED".equals(oldPass.getStatus())) {
            return "redirect:/bus-pass/view/" + id;
        }

        // Renewal is intended for expired passes
        if (oldPass.getEndDate() == null ||
                !oldPass.getEndDate()
                        .isBefore(LocalDate.now())) {

            return "redirect:/bus-pass/view/" + id;
        }

        model.addAttribute(
                "oldPass",
                oldPass
        );

        return "renew-pass";
    }

    // =========================
    // RENEW PASS
    // =========================

    @PostMapping("/renew/{id}")
    public String renewPass(
            @PathVariable Long id,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate,
            HttpSession session) {

        User user =
                (User) session.getAttribute("user");

        if (user == null) {
            return "redirect:/login";
        }

        BusPass oldPass =
                busPassRepository
                        .findById(id)
                        .orElse(null);

        if (oldPass == null) {
            return "redirect:/bus-pass/my-passes";
        }

        // Only owner can renew
        if (!oldPass.getUser().getId()
                .equals(user.getId())) {

            return "redirect:/bus-pass/my-passes";
        }

        // Only approved passes can be renewed
        if (!"APPROVED".equals(oldPass.getStatus())) {

            return "redirect:/bus-pass/view/" + id;
        }

        // Pass must be expired
        if (oldPass.getEndDate() == null ||
                !oldPass.getEndDate()
                        .isBefore(LocalDate.now())) {

            return "redirect:/bus-pass/view/" + id;
        }

        // Validate dates
        if (endDate.isBefore(startDate)) {
            return "redirect:/bus-pass/renew/" + id;
        }

        // Create new pass
        BusPass newPass =
                new BusPass();

        newPass.setUser(oldPass.getUser());
        newPass.setRoute(oldPass.getRoute());
        newPass.setPassType(oldPass.getPassType());
        newPass.setStartDate(startDate);
        newPass.setEndDate(endDate);
        newPass.setStatus("PENDING");
        newPass.setAdminRemark(null);

        busPassRepository.save(newPass);

        return "redirect:/bus-pass/my-passes";
    }
}