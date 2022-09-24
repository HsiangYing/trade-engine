package lo.sharon.tradeengine.controller;

import lo.sharon.tradeengine.constant.OrderStatus;
import lo.sharon.tradeengine.dto.OrderRequest;
import lo.sharon.tradeengine.model.Order;
import lo.sharon.tradeengine.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.util.List;
@Slf4j
@RestController
@Validated
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping("/orders")
    public String placeOrder(@RequestBody @Valid OrderRequest orderRequest) {
        String orderId = orderService.putOrderToPendingOrderQueue(orderRequest);
        return orderId;
    }

    @GetMapping(value = "/orders")
    public List<Order> getOrdersByStatus(@RequestParam @Valid OrderStatus orderStatus) {
        List<Order> orders = orderService.getOrdersByStatus(orderStatus);
        return orders;
    }
}
