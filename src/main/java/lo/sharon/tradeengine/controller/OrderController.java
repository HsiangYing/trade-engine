package lo.sharon.tradeengine.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiModel;
import io.swagger.v3.oas.annotations.Operation;
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
import java.util.Map;

@Slf4j
@RestController
@Validated
@Api(tags="委託單")
public class OrderController {
    @Autowired
    private OrderService orderService;

    @PostMapping("/orders")
    @Operation(summary = "提交委託單")
    public String placeOrder(@RequestBody @Valid OrderRequest orderRequest) {
        String orderId = orderService.putOrderToPendingOrderQueue(orderRequest);
        return orderId;
    }

    @GetMapping(value = "/orders")
    @Operation(summary = "依據狀態查詢委託單")
    public Map<String, List<Order>> getOrdersByStatus(@RequestParam @Valid OrderStatus orderStatus) {
        Map<String, List<Order>> orders = orderService.getOrdersByStatus(orderStatus);
        return orders;
    }
}
