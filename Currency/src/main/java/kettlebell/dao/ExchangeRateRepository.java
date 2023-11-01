package kettlebell.dao;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import kettlebell.model.ExchangeRate;

public interface ExchangeRateRepository extends CRUDRepository<ExchangeRate>{
	Optional<ExchangeRate> getByCode (String baseCurrencyCode, String targetCurrencyCode) throws SQLException;
	List<ExchangeRate> getByCodeWithUsdBase(String baseCurrencyCode, String targetCurrencyCode) throws SQLException;
}
