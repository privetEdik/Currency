package kettlebell.dao;

import java.sql.SQLException;
import java.util.Optional;

import kettlebell.model.Currency;

public interface CurrencyRepository extends CRUDRepository<Currency>{
	Optional<Currency> findByCode(String code) throws SQLException;
}
