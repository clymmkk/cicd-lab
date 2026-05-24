package com.ops.monitor.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "servers")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Server {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 100)
    private String host;

    @Column(length = 50)
    private String os;

    @Column(length = 20)
    private String status = "ONLINE";

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}
