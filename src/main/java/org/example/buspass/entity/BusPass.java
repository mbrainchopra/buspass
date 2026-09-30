package org.example.buspass.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "bus_passes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BusPass {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "route_id")
    private Route route;

    private String passType;

    private LocalDate startDate;

    private LocalDate endDate;

    private String status;

    private String adminRemark;
}