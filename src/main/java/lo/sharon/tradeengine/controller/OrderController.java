package lo.sharon.tradeengine.controller;

import lo.sharon.tradeengine.dto.OrderRequest;
import lo.sharon.tradeengine.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;

@RestController
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping("/orders")
    public String placeOrder(@RequestBody @Valid OrderRequest orderRequest) {
        String orderId = orderService.putOrderToPendingOrderQueue(orderRequest);
        return orderId;
    }
}
