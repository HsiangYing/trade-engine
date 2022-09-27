package lo.sharon.tradeengine.dao;

import lo.sharon.tradeengine.model.Transaction;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.util.concurrent.ListenableFuture;

@Repository
public interface TransactionRepository extends CrudRepository<Transaction, Long> {

}
