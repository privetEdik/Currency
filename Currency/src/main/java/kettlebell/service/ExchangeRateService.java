package kettlebell.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import kettlebell.dao.ExchangeRateRepository;
import kettlebell.dto.ExchangeDTO;
import kettlebell.model.ExchangeRate;
import kettlebell.repository.JdbcExchangeRateRepository;

import static java.math.MathContext.DECIMAL64;

public class ExchangeRateService {
	
	ExchangeRateRepository repository = new JdbcExchangeRateRepository();
	
	public ExchangeDTO convertCurrency(String baseCurrenctCode, String tergetCurrenctCode, BigDecimal amount) throws SQLException, NoSuchElementException {
		
		ExchangeRate exchangeRate = getExchangeRate(baseCurrenctCode, tergetCurrenctCode).orElseThrow();
		BigDecimal convertedAmount = amount.multiply(exchangeRate.getRate()).setScale(2,RoundingMode.HALF_EVEN);
		return new ExchangeDTO(
				exchangeRate.getBaseCurrency(),
				exchangeRate.getTargetCurrency(),
				exchangeRate.getRate(),
				amount,
				convertedAmount);
	}
	
	private Optional<ExchangeRate> getExchangeRate(String baseCurrencyId,String targetCurrencyId) throws SQLException{
		Optional<ExchangeRate> exchangeRateOpt = getDirectRate(baseCurrencyId, targetCurrencyId);
		
		if(exchangeRateOpt.isEmpty()) {
			exchangeRateOpt = getReverseRate(baseCurrencyId,targetCurrencyId);
		}
		
		if(exchangeRateOpt.isEmpty()) {
			exchangeRateOpt = getRateForUSD(baseCurrencyId, targetCurrencyId);
		}
		
		return exchangeRateOpt;
	}
	
	private Optional<ExchangeRate> getDirectRate(String baseCurrencyId,String targetCurrencyId) throws SQLException{		
		return repository.getByCode(baseCurrencyId, targetCurrencyId);		
	}
	
	private Optional<ExchangeRate> getReverseRate(String baseCurrencyId,String targetCurrencyId) throws SQLException{		
		Optional<ExchangeRate> exchangeRateOpt = repository.getByCode(targetCurrencyId, baseCurrencyId);
		
		if(exchangeRateOpt.isEmpty()) {
			return Optional.empty();
		}
		ExchangeRate reversedExchangeRate = exchangeRateOpt.get();
		
		ExchangeRate directExchangeRate = new ExchangeRate(
				reversedExchangeRate.getTargetCurrency(),
				reversedExchangeRate.getBaseCurrency(),
				BigDecimal.ONE.divide(reversedExchangeRate.getRate(), DECIMAL64)
				); 

		return Optional.of(directExchangeRate);
	}
	
	private Optional<ExchangeRate> getRateForUSD(String baseCurrencyId, String targetCurrencyId)throws SQLException, NoSuchElementException{
		List<ExchangeRate> ratesWithUsdBase = repository.getByCodeWithUsdBase(baseCurrencyId, targetCurrencyId);
		
		ExchangeRate usdToBaseExchange = getExchangeRateForCode(ratesWithUsdBase, baseCurrencyId);
		ExchangeRate usdToTargetExchange = getExchangeRateForCode(ratesWithUsdBase, targetCurrencyId);
		
		BigDecimal usdToBaseRate = usdToBaseExchange.getRate();
		BigDecimal usdToTargetRate = usdToTargetExchange.getRate();
		
		BigDecimal baseToTargetRate = usdToTargetRate.divide(usdToBaseRate, DECIMAL64);
		
		ExchangeRate exchangeRate = new ExchangeRate(
				usdToBaseExchange.getTargetCurrency(),
				usdToTargetExchange.getTargetCurrency(),
				baseToTargetRate
				);
		
		return Optional.of(exchangeRate);
	}
	
	private static ExchangeRate getExchangeRateForCode(List<ExchangeRate> rates, String code) throws NoSuchElementException {
		return rates.stream()
				.filter(rate ->rate.getTargetCurrency().getCode().equals(code))
				.findFirst()
				.orElse(
						rates.stream()
							.filter(rate ->rate.getBaseCurrency().getCode().equals(code))
							.map(s->
								new ExchangeRate(
										s.getTargetCurrency(),
										s.getBaseCurrency(),
										BigDecimal.ONE.divide(s.getRate(),DECIMAL64))
									)
							.findFirst()
							.orElseThrow()
						);
	}
 }
