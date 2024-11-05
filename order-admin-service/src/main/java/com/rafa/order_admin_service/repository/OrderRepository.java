package com.rafa.order_admin_service.repository;

import com.rafa.order_admin_service.model.Orders;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Orders, Long> {

    public List<Orders> findByRoomId(Long roomId);

    @Transactional
    @Modifying
    @Query(value = "UPDATE orders SET order_status = 'FINISHED' WHERE order_status IN ('VALID', 'DISANNUL') AND check_out_date <= NOW()", nativeQuery = true)
    public void updateExpiredOrders();

}
