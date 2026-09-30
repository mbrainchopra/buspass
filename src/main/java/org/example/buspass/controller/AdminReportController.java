package org.example.buspass.controller;

import jakarta.servlet.http.HttpSession;
import org.example.buspass.entity.BusPass;
import org.example.buspass.entity.User;
import org.example.buspass.repository.BusPassRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@Controller
@RequestMapping("/admin/reports")
public class AdminReportController {

    private final BusPassRepository busPassRepository;

    public AdminReportController(
            BusPassRepository busPassRepository) {

        this.busPassRepository = busPassRepository;
    }

    @GetMapping
    public String reports(
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

        List<BusPass> passes =
                busPassRepository.findAll();

        long total =
                busPassRepository.count();

        long pending =
                busPassRepository.countByStatus("PENDING");

        long approved =
                busPassRepository.countByStatus("APPROVED");

        long rejected =
                busPassRepository.countByStatus("REJECTED");

        model.addAttribute("passes", passes);
        model.addAttribute("total", total);
        model.addAttribute("pending", pending);
        model.addAttribute("approved", approved);
        model.addAttribute("rejected", rejected);

        return "admin-reports";
    }

    @GetMapping("/export")
    public void exportReport(
            HttpSession session,
            HttpServletResponse response)
            throws IOException {

        User admin =
                (User) session.getAttribute("user");

        if (admin == null ||
                !"ADMIN".equals(admin.getRole())) {

            response.sendRedirect("/login");
            return;
        }

        List<BusPass> passes =
                busPassRepository.findAll();

        response.setContentType("text/csv");

        response.setHeader(
                "Content-Disposition",
                "attachment; filename=bus-pass-report.csv"
        );

        PrintWriter writer =
                response.getWriter();

        writer.println(
                "ID,Applicant Name,Email,Phone,Route," +
                        "Start Location,End Location,Pass Type," +
                        "Start Date,End Date,Status,Admin Remark"
        );

        for (BusPass pass : passes) {

            String name =
                    pass.getUser() != null
                            ? pass.getUser().getName()
                            : "";

            String email =
                    pass.getUser() != null
                            ? pass.getUser().getEmail()
                            : "";

            String phone =
                    pass.getUser() != null
                            ? pass.getUser().getPhone()
                            : "";

            String routeName =
                    pass.getRoute() != null
                            ? pass.getRoute().getRouteName()
                            : "";

            String startLocation =
                    pass.getRoute() != null
                            ? pass.getRoute().getStartLocation()
                            : "";

            String endLocation =
                    pass.getRoute() != null
                            ? pass.getRoute().getEndLocation()
                            : "";

            String remark =
                    pass.getAdminRemark() != null
                            ? pass.getAdminRemark()
                            : "";

            writer.println(
                    escape(pass.getId()) + "," +
                            escape(name) + "," +
                            escape(email) + "," +
                            escape(phone) + "," +
                            escape(routeName) + "," +
                            escape(startLocation) + "," +
                            escape(endLocation) + "," +
                            escape(pass.getPassType()) + "," +
                            escape(pass.getStartDate()) + "," +
                            escape(pass.getEndDate()) + "," +
                            escape(pass.getStatus()) + "," +
                            escape(remark)
            );
        }

        writer.flush();
    }

    private String escape(Object value) {

        if (value == null) {
            return "";
        }

        String text =
                String.valueOf(value);

        text = text.replace("\"", "\"\"");

        return "\"" + text + "\"";
    }
}