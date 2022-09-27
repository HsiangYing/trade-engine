package lo.sharon.tradeengine.controller;


import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.Operation;
import lo.sharon.tradeengine.constant.OrderStatus;
import lo.sharon.tradeengine.model.Order;
import lo.sharon.tradeengine.model.Transaction;
import lo.sharon.tradeengine.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@Api(tags = "交易單")
public class TransactionController {
    @Autowired
    TransactionService transactionService;

    @GetMapping("/transactions")
    @Operation(summary = "查詢所有交易單")
    public List<Transaction> getTransaction(){
        List<Transaction> transactions = transactionService.getAllTransactions();
        return transactions;
    }
}
