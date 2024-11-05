package com.rafa.order_service.repository;

import com.rafa.order_service.model.Orders;
import com.rafa.order_service.model.dto.UserDto;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface OrderRepository extends JpaRepository<Orders,Long> {

    public List<Orders> findByUserId(Long userId);

    public List<Orders> findByRoomId(Long roomId);

    @Transactional
    @Modifying
    @Query(value = "UPDATE orders SET order_status = 'FINISHED' WHERE order_status IN ('VALID','DISANNUL') AND check_out_date <= NOW()", nativeQuery = true)
    public void updateExpiredOrders();
}
