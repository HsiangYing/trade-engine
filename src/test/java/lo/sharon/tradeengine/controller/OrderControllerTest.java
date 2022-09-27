package lo.sharon.tradeengine.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lo.sharon.tradeengine.constant.OrderSide;
import lo.sharon.tradeengine.constant.OrderStatus;
import lo.sharon.tradeengine.constant.OrderType;
import lo.sharon.tradeengine.dto.OrderRequest;
import lo.sharon.tradeengine.model.Order;
import lo.sharon.tradeengine.service.OrderService;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = OrderController.class)
public class OrderControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    // --- place order test (start) --- //
    @Test
    public void testPlaceOrderWithCorrectUrlAndyRequestBodyResponseOk() throws Exception {
        String ORDER_ID = "1663953147213-0";
        OrderRequest orderReq = new OrderRequest();
        orderReq.setSide(OrderSide.BUY);
        orderReq.setType(OrderType.MARKET);
        orderReq.setQuantity(20L);
        Mockito.when(orderService.putOrderToPendingOrderQueue(orderReq)).thenReturn(ORDER_ID);
        ObjectMapper objectMapper = new ObjectMapper();
        mockMvc.perform(post("/orders/")
                    .contentType(APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(orderReq)))
                .andExpect(status().isOk())
                .andExpect(content().string(ORDER_ID));
    }
    @Test
    public void testPlaceOrderWithCorrectUrlAndyNotSpecifySideResponse400() throws Exception {
        Map orderRequest = new HashMap();
        orderRequest.put("type", "LIMIT");
        orderRequest.put("quantity", 20L);
        orderRequest.put("price", 100L);
        JSONObject orderRequestJson = new JSONObject(orderRequest);
        mockMvc.perform(post("/orders/")
                        .contentType(APPLICATION_JSON)
                        .content(orderRequestJson.toString()))
                .andExpect(status().isBadRequest());
    }
    @Test
    public void testPlaceOrderWithCorrectUrlAndIncorrectSideResponse400() throws Exception {
        Map orderRequest = new HashMap();
        orderRequest.put("side", "incorrectSide");
        orderRequest.put("type", "LIMIT");
        orderRequest.put("quantity", 20L);
        orderRequest.put("price", 100L);
        JSONObject orderRequestJson = new JSONObject(orderRequest);
        mockMvc.perform(post("/orders/")
                        .contentType(APPLICATION_JSON)
                        .content(String.valueOf(orderRequestJson)))
                .andExpect(status().isBadRequest());
    }
    @Test
    public void testPlaceOrderWithCorrectUrlAndNotSpecifyQuantityResponse400() throws Exception {
        Map orderRequest = new HashMap();
        orderRequest.put("side", "BUY");
        orderRequest.put("type", "LIMIT");
        orderRequest.put("price", 100L);
        JSONObject orderRequestJson = new JSONObject(orderRequest);
        mockMvc.perform(post("/orders/")
                        .contentType(APPLICATION_JSON)
                        .content(String.valueOf(orderRequestJson)))
                .andExpect(status().isBadRequest());
    }
    @Test
    public void testPlaceOrderWithCorrectUrlAndQuantityIsNotPositiveResponse400() throws Exception {
        Map orderRequest = new HashMap();
        orderRequest.put("side", "BUY");
        orderRequest.put("type", "LIMIT");
        orderRequest.put("quantity", -20);
        orderRequest.put("price", 100L);
        JSONObject orderRequestJson = new JSONObject(orderRequest);
        mockMvc.perform(post("/orders/")
                        .contentType(APPLICATION_JSON)
                        .content(String.valueOf(orderRequestJson)))
                .andExpect(status().isBadRequest());
    }
    @Test
    public void testPlaceOrderWithCorrectUrlAndQuantityIsNotIntegerResponse400() throws Exception {
        Map orderRequest = new HashMap();
        orderRequest.put("side", "BUY");
        orderRequest.put("type", "LIMIT");
        orderRequest.put("quantity", 1.6f);
        orderRequest.put("price", 100L);
        JSONObject orderRequestJson = new JSONObject(orderRequest);
        System.out.println(String.valueOf(orderRequestJson));
        mockMvc.perform(post("/orders/")
                        .contentType(APPLICATION_JSON)
                        .content(String.valueOf(orderRequestJson)))
                .andExpect(status().isBadRequest());
    }
    @Test
    public void testPlaceOrderWithCorrectUrlAndQuantityIsZeroResponse400() throws Exception {
        Map orderRequest = new HashMap();
        orderRequest.put("side", "BUY");
        orderRequest.put("type", "LIMIT");
        orderRequest.put("quantity", 0L);
        orderRequest.put("price", 100L);
        JSONObject orderRequestJson = new JSONObject(orderRequest);
        System.out.println(String.valueOf(orderRequestJson));
        mockMvc.perform(post("/orders/")
                        .contentType(APPLICATION_JSON)
                        .content(String.valueOf(orderRequestJson)))
                .andExpect(status().isBadRequest());
    }
    @Test
    public void testPlaceOrderWithCorrectUrlAndNotSpecifyTypeResponse400() throws Exception {
        Map orderRequest = new HashMap();
        orderRequest.put("side", "BUY");
        orderRequest.put("quantity", 10L);
        orderRequest.put("price", 100L);
        JSONObject orderRequestJson = new JSONObject(orderRequest);
        System.out.println(String.valueOf(orderRequestJson));
        mockMvc.perform(post("/orders/")
                        .contentType(APPLICATION_JSON)
                        .content(String.valueOf(orderRequestJson)))
                .andExpect(status().isBadRequest());
    }
    @Test
    public void testPlaceOrderWithCorrectUrlAndIncorrectTypeResponse400() throws Exception {
        Map orderRequest = new HashMap();
        orderRequest.put("side", "BUY");
        orderRequest.put("type", "BUYANDSELL");
        orderRequest.put("quantity", 10L);
        orderRequest.put("price", 100L);
        JSONObject orderRequestJson = new JSONObject(orderRequest);
        System.out.println(String.valueOf(orderRequestJson));
        mockMvc.perform(post("/orders/")
                        .contentType(APPLICATION_JSON)
                        .content(String.valueOf(orderRequestJson)))
                .andExpect(status().isBadRequest());
    }
    @Test
    public void testPlaceOrderWithCorrectUrlAndTypeIsLimitButNotSpecifyPriceResponse400() throws Exception {
        Map orderRequest = new HashMap();
        orderRequest.put("side", "BUY");
        orderRequest.put("type", "LIMIT");
        orderRequest.put("quantity", 10L);
        JSONObject orderRequestJson = new JSONObject(orderRequest);
        System.out.println(String.valueOf(orderRequestJson));
        mockMvc.perform(post("/orders/")
                        .contentType(APPLICATION_JSON)
                        .content(String.valueOf(orderRequestJson)))
                .andExpect(status().isBadRequest());
    }
    @Test
    public void testPlaceOrderWithCorrectUrlAndTypeIsLimitAndPriceIsZeroResponse400() throws Exception {
        Map orderRequest = new HashMap();
        orderRequest.put("side", "BUY");
        orderRequest.put("type", "LIMIT");
        orderRequest.put("quantity", 10L);
        orderRequest.put("price", 0L);
        JSONObject orderRequestJson = new JSONObject(orderRequest);
        System.out.println(String.valueOf(orderRequestJson));
        mockMvc.perform(post("/orders/")
                        .contentType(APPLICATION_JSON)
                        .content(String.valueOf(orderRequestJson)))
                .andExpect(status().isBadRequest());
    }
    @Test
    public void testPlaceOrderWithCorrectUrlAndTypeIsLimitAndPriceIsNegativeResponse400() throws Exception {
        Map orderRequest = new HashMap();
        orderRequest.put("side", "BUY");
        orderRequest.put("type", "LIMIT");
        orderRequest.put("quantity", 10L);
        orderRequest.put("price", -5L);
        JSONObject orderRequestJson = new JSONObject(orderRequest);
        System.out.println(String.valueOf(orderRequestJson));
        mockMvc.perform(post("/orders/")
                        .contentType(APPLICATION_JSON)
                        .content(String.valueOf(orderRequestJson)))
                .andExpect(status().isBadRequest());
    }
    @Test
    public void testPlaceOrderWithCorrectUrlAndTypeIsLimitAndPriceIsNotIntegerResponse400() throws Exception {
        Map orderRequest = new HashMap();
        orderRequest.put("side", "BUY");
        orderRequest.put("type", "LIMIT");
        orderRequest.put("quantity", 10L);
        orderRequest.put("price", 5.5f);
        JSONObject orderRequestJson = new JSONObject(orderRequest);
        System.out.println(String.valueOf(orderRequestJson));
        mockMvc.perform(post("/orders/")
                        .contentType(APPLICATION_JSON)
                        .content(String.valueOf(orderRequestJson)))
                .andExpect(status().isBadRequest());
    }
    @Test
    public void testPlaceOrderWithCorrectUrlAndTypeIsMarketAndPriceIsSpecifyResponse400() throws Exception {
        Map orderRequest = new HashMap();
        orderRequest.put("side", "SELL");
        orderRequest.put("type", "MARKET");
        orderRequest.put("quantity", 10L);
        orderRequest.put("price", 20L);
        JSONObject orderRequestJson = new JSONObject(orderRequest);
        System.out.println(String.valueOf(orderRequestJson));
        mockMvc.perform(post("/orders/")
                        .contentType(APPLICATION_JSON)
                        .content(String.valueOf(orderRequestJson)))
                .andExpect(status().isBadRequest());
    }
    // --- place order by status test (end) --- //

    // --- get orders by status test (start) --- //
    @Test
    public void testGetOrderWithCorrectOrderStatusParamResponseOk() throws Exception {
        String ORDER_ID = "1663953147213-0";
        Order order = new Order();
        order.setOrderId(ORDER_ID);
        order.setSide(OrderSide.BUY);
        order.setType(OrderType.MARKET);
        order.setQuantity(20L);
        List<Order> orderList = new ArrayList<>();
        orderList.add(order);
        Map<String, List<Order>> orderListMap = new HashMap<>();
        orderListMap.put(OrderStatus.PENDING_IN_QUEUE.name(), orderList);
        Mockito.when(orderService.getOrdersByStatus(OrderStatus.PENDING_IN_QUEUE)).thenReturn(orderListMap);
        ObjectMapper objectMapper = new ObjectMapper();
        mockMvc.perform(get("/orders/")
                        .param("orderStatus", OrderStatus.PENDING_IN_QUEUE.name()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.stream)]", is(notNullValue())))
                .andExpect(jsonPath("$[?(@.value)]", is(notNullValue())))
                .andExpect(jsonPath("$[?(@.id)]", is(notNullValue())));
    }
    @Test
    public void testGetOrderWithoutOrderStatusParamResponse400() throws Exception {
        String ORDER_ID = "1663953147213-0";
        Order order = new Order();
        order.setOrderId(ORDER_ID);
        order.setSide(OrderSide.BUY);
        order.setType(OrderType.MARKET);
        order.setQuantity(20L);
        List<Order> orderList = new ArrayList<>();
        orderList.add(order);
        Map<String, List<Order>> orderListMap = new HashMap<>();
        orderListMap.put(OrderStatus.PENDING_IN_QUEUE.name(), orderList);
        Mockito.when(orderService.getOrdersByStatus(OrderStatus.PENDING_IN_QUEUE)).thenReturn(orderListMap);
        ObjectMapper objectMapper = new ObjectMapper();
        mockMvc.perform(get("/orders/"))
                .andExpect(status().isBadRequest());
    }
    @Test
    public void testGetOrderWithIncorrectOrderStatusParamResponse400() throws Exception {
        String ORDER_ID = "1663953147213-0";
        Order order = new Order();
        order.setOrderId(ORDER_ID);
        order.setSide(OrderSide.BUY);
        order.setType(OrderType.MARKET);
        order.setQuantity(20L);
        List<Order> orderList = new ArrayList<>();
        orderList.add(order);
        Map<String, List<Order>> orderListMap = new HashMap<>();
        orderListMap.put(OrderStatus.PENDING_IN_QUEUE.name(), orderList);
        Mockito.when(orderService.getOrdersByStatus(OrderStatus.PENDING_IN_QUEUE)).thenReturn(orderListMap);
        ObjectMapper objectMapper = new ObjectMapper();
        mockMvc.perform(get("/orders/")
                        .param("orderStatus", "notCorrectStatus"))
                .andExpect(status().isBadRequest());
    }
    // --- get orders test (end) --- //
}
