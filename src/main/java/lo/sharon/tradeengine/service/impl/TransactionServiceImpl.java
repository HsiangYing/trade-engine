package lo.sharon.tradeengine.service.impl;

import lo.sharon.tradeengine.dao.TransactionRepository;
import lo.sharon.tradeengine.model.Transaction;
import lo.sharon.tradeengine.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Service
public class TransactionServiceImpl implements TransactionService {

    @Autowired
    TransactionRepository transactionRepository;

    @Override
    public List<Transaction> getAllTransactions() {
        List<Transaction> transactions = new ArrayList<>();
        Iterator<Transaction> transactionIterator = transactionRepository.findAll().iterator();
        while(transactionIterator.hasNext()) {
            transactions.add(transactionIterator.next());
        }
        return transactions;
    }
}
