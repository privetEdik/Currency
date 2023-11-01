package kettlebell.model;

import java.math.BigDecimal;

public class ExchangeRate {
	private Integer id;
	private Currency baseCurrency;
	private Currency targetCurrency;
	private BigDecimal rate;
	
	public ExchangeRate() {
		
	}
	
	public void setRate(BigDecimal rate) {
		this.rate = rate;
	}

	public ExchangeRate(Integer id, Currency baseCurrency, Currency targetCurrency, BigDecimal rate) {
		this.id = id;
		this.baseCurrency = baseCurrency;
		this.targetCurrency = targetCurrency;
		this.rate = rate;
	}
	
	public ExchangeRate(Currency baseCurrency, Currency targetCurrency, BigDecimal rate) {
		this.baseCurrency = baseCurrency;
		this.targetCurrency = targetCurrency;
		this.rate = rate;
	}

	public Integer getId() {
		return id;
	}

	public Currency getBaseCurrency() {
		return baseCurrency;
	}

	public Currency getTargetCurrency() {
		return targetCurrency;
	}

	public BigDecimal getRate() {
		return rate;
	}

	public void setId(Integer id) {
		this.id = id;
	}
	
}
