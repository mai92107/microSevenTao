package com.rafa.room_admin_service.model;

import jakarta.persistence.*;
import lombok.Data;
import java.io.Serializable;
import java.util.List;

@Entity
@Data
public class Room implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long roomId;

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(
			name = "room_pic",
			joinColumns = @JoinColumn(name = "room_id",
					foreignKey = @ForeignKey(
							name = "FK_ROOM_PIC",
							foreignKeyDefinition = "FOREIGN KEY (room_id) REFERENCES room(room_id) ON DELETE CASCADE"
					)
			)
	)
	@Column(name = "pic_url")
	private List<String> roomPic;

	private String roomName;

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(
			name = "room_specialties",
			joinColumns = @JoinColumn(name = "room_id",
					foreignKey = @ForeignKey(
							name = "FK_ROOM_SPECIALTIES",
							foreignKeyDefinition = "FOREIGN KEY (room_id) REFERENCES room(room_id) ON DELETE CASCADE"
					)
			)
	)
	@Column(name = "specialty")
	private List<String> specialties;

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(
			name = "room_prices",
			joinColumns = @JoinColumn(name = "room_id",
					foreignKey = @ForeignKey(
							name = "FK_ROOM_PRICES",
							foreignKeyDefinition = "FOREIGN KEY (room_id) REFERENCES room(room_id) ON DELETE CASCADE"
					)
			)
	)
	@Column(name = "price")
	private List<Integer> prices;

	private int roomSize;

	private int capacity;

	private Long hotelId;
}
