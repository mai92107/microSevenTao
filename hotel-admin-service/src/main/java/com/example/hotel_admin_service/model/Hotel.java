package com.example.hotel_admin_service.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Data
@Entity
public class Hotel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long hotelId;

    private Long bossId;

    @ElementCollection
    @CollectionTable(name = "hotel_pictures", joinColumns = @JoinColumn(name = "hotel_id"))
    @Column(name = "picture_url")
    private List<String> pictures;

    private String chName;
    private String enName;
    private String introduction;

    @ElementCollection
    private List<String> facilities;

    @OneToOne(cascade = CascadeType.PERSIST)
    private Address address;

    private LocalDateTime buildDate;

    @ElementCollection
    @CollectionTable(name = "hotel_liked_by_users_ids", joinColumns = @JoinColumn(name = "hotel_id"))
    @Column(name = "user_id")
    private List<Long> likedByUsersIds;

    private double score;

}
