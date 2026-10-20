package dev.gaphunter.orders.domain;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {

    private final OrderRepository repository;
    private final Clock clock;

    public OrderService(OrderRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<Order> findAll() {
        return repository.findAllByOrderByIdAsc();
    }

    @Transactional(readOnly = true)
    public Optional<Order> findById(long id) {
        return repository.findById(id);
    }

    @Transactional
    public Order create(String customerName, BigDecimal total) {
        return repository.save(new Order(customerName, total, OrderStatus.NEW, OffsetDateTime.now(clock)));
    }
}
