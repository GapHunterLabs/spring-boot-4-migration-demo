package dev.gaphunter.orders.api;

import dev.gaphunter.orders.domain.Order;
import java.util.List;

/** All orders, in the shape the CSV export writes. */
public record OrderExport(List<Order> orders) {
}
