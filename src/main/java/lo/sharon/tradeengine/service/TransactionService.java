package lo.sharon.tradeengine.service;

import lo.sharon.tradeengine.model.Transaction;

import java.util.List;
import java.util.Optional;

public interface TransactionService {
    List<Transaction> getAllTransactions();
}
