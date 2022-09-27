package lo.sharon.tradeengine.controller;


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
public class TransactionController {
    @Autowired
    TransactionService transactionService;

    @GetMapping("/transactions")
    public List<Transaction> getTransaction(){
        List<Transaction> transactions = transactionService.getAllTransactions();
        return transactions;
    }
}
