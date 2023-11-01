package kettlebell.dto;

import java.math.BigDecimal;

import kettlebell.model.Currency;

public class ExchangeDTO {
	private Currency baseCurrency;
	
	private Currency targetCurrency;
	
	private BigDecimal rate;
	
	private BigDecimal amount;
	
	private BigDecimal convertedAmount;

	public ExchangeDTO(Currency baseCurrency, Currency targetCurrency, BigDecimal rate, BigDecimal amount,
			BigDecimal convertedAmount) {
		this.baseCurrency = baseCurrency;
		this.targetCurrency = targetCurrency;
		this.rate = rate;
		this.amount = amount;
		this.convertedAmount = convertedAmount;
	}
	
	public ExchangeDTO() {

	}

	public Currency getBaseCurrency() {
		return baseCurrency;
	}

	public void setBaseCurrency(Currency baseCurrency) {
		this.baseCurrency = baseCurrency;
	}

	public Currency getTargetCurrency() {
		return targetCurrency;
	}

	public void setTargetCurrency(Currency targetCurrency) {
		this.targetCurrency = targetCurrency;
	}

	public BigDecimal getRate() {
		return rate;
	}

	public void setRate(BigDecimal rate) {
		this.rate = rate;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public BigDecimal getConvertedAmount() {
		return convertedAmount;
	}

	public void setConvertedAmount(BigDecimal convertedAmount) {
		this.convertedAmount = convertedAmount;
	}
	
	
	
	
}
