package com.rafa.hotel_service.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
public class Hotel implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long hotelId;

	private Long bossId;

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "hotel_pictures", joinColumns = @JoinColumn(name = "hotel_id"))
	@Column(name = "picture_url")
	private List<String> pictures;
	private String chName;
	private String enName;
	private String introduction;

	@ElementCollection(fetch = FetchType.EAGER)
	private List<String> facilities;

	@OneToOne(cascade = CascadeType.ALL)
	private Address address;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime buildDate;

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "hotel_liked_by_users_ids", joinColumns = @JoinColumn(name = "hotel_id"))
	@Column(name = "user_id")
	private List<Long> likedByUsersIds;

	private double score;

}
