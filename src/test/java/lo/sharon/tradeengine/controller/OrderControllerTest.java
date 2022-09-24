package lo.sharon.tradeengine.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lo.sharon.tradeengine.constant.OrderSide;
import lo.sharon.tradeengine.constant.OrderType;
import lo.sharon.tradeengine.dto.OrderRequest;
import lo.sharon.tradeengine.service.OrderService;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = OrderController.class)
public class OrderControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    @Test
    public void testPlaceOrderWithCorrectUrlAndyRequestBodyResponseOk() throws Exception {
        String ORDER_ID = "1663953147213-0";
        OrderRequest orderReq = new OrderRequest();
        orderReq.setSide(OrderSide.BUY);
        orderReq.setType(OrderType.MARKET);
        orderReq.setQuantity(20);
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
        orderRequest.put("quantity", 20);
        orderRequest.put("price", 100);
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
        orderRequest.put("quantity", 20);
        orderRequest.put("price", 100);
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
        orderRequest.put("price", 100);
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
        orderRequest.put("price", 100);
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
        orderRequest.put("price", 100);
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
        orderRequest.put("quantity", 0);
        orderRequest.put("price", 100);
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
        orderRequest.put("quantity", 10);
        orderRequest.put("price", 100);
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
        orderRequest.put("quantity", 10);
        orderRequest.put("price", 100);
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
        orderRequest.put("quantity", 10);
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
        orderRequest.put("quantity", 10);
        orderRequest.put("price", 0);
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
        orderRequest.put("quantity", 10);
        orderRequest.put("price", -5);
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
        orderRequest.put("quantity", 10);
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
        orderRequest.put("quantity", 10);
        orderRequest.put("price", 20);
        JSONObject orderRequestJson = new JSONObject(orderRequest);
        System.out.println(String.valueOf(orderRequestJson));
        mockMvc.perform(post("/orders/")
                        .contentType(APPLICATION_JSON)
                        .content(String.valueOf(orderRequestJson)))
                .andExpect(status().isBadRequest());
    }
}
