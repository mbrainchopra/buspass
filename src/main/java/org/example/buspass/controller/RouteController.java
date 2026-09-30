package org.example.buspass.controller;

import jakarta.servlet.http.HttpSession;
import org.example.buspass.entity.Route;
import org.example.buspass.entity.User;
import org.example.buspass.repository.RouteRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin/routes")
public class RouteController {

    private final RouteRepository routeRepository;

    public RouteController(RouteRepository routeRepository) {
        this.routeRepository = routeRepository;
    }

    // =========================
    // ROUTE MANAGEMENT PAGE
    // =========================

    @GetMapping
    public String routes(
            HttpSession session,
            Model model) {

        User user = (User) session.getAttribute("user");

        if (user == null) {
            return "redirect:/login";
        }

        if (!"ADMIN".equals(user.getRole())) {
            return "redirect:/user/dashboard";
        }

        List<Route> routes = routeRepository.findAll();

        model.addAttribute("routes", routes);

        model.addAttribute("route", new Route());

        return "manage-routes";
    }

    // =========================
    // ADD ROUTE
    // =========================

    @PostMapping("/save")
    public String saveRoute(
            @ModelAttribute Route route,
            HttpSession session) {

        User user = (User) session.getAttribute("user");

        if (user == null) {
            return "redirect:/login";
        }

        if (!"ADMIN".equals(user.getRole())) {
            return "redirect:/user/dashboard";
        }

        routeRepository.save(route);

        return "redirect:/admin/routes";
    }

    // =========================
    // DELETE ROUTE
    // =========================

    @GetMapping("/delete/{id}")
    public String deleteRoute(
            @PathVariable Long id,
            HttpSession session) {

        User user = (User) session.getAttribute("user");

        if (user == null) {
            return "redirect:/login";
        }

        if (!"ADMIN".equals(user.getRole())) {
            return "redirect:/user/dashboard";
        }

        routeRepository.deleteById(id);

        return "redirect:/admin/routes";
    }

    // =========================
    // EDIT ROUTE PAGE
    // =========================

    @GetMapping("/edit/{id}")
    public String editRoute(
            @PathVariable Long id,
            HttpSession session,
            Model model) {

        User user = (User) session.getAttribute("user");

        if (user == null) {
            return "redirect:/login";
        }

        if (!"ADMIN".equals(user.getRole())) {
            return "redirect:/user/dashboard";
        }

        Route route = routeRepository
                .findById(id)
                .orElse(null);

        if (route == null) {
            return "redirect:/admin/routes";
        }

        List<Route> routes = routeRepository.findAll();

        model.addAttribute("routes", routes);

        model.addAttribute("route", route);

        return "manage-routes";
    }
}