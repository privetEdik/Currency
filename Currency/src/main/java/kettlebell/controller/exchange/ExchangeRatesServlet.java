package kettlebell.controller.exchange;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.NoSuchElementException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static javax.servlet.http.HttpServletResponse.*;
import static kettlebell.utils.Validation.isValidCurrencyCode;

import com.fasterxml.jackson.databind.ObjectMapper;

import kettlebell.dao.CurrencyRepository;
import kettlebell.dao.ExchangeRateRepository;
import kettlebell.dto.ErrorResponseDTO;
import kettlebell.model.ExchangeRate;
import kettlebell.repository.JdbcCurrencyRepository;
import kettlebell.repository.JdbcExchangeRateRepository;

@WebServlet(name = "ExchangeRatesServlet", urlPatterns = "/exchangeRates")
public class ExchangeRatesServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private final ExchangeRateRepository exchangeRateRepository = new JdbcExchangeRateRepository();
	private final CurrencyRepository currencyRepository = new JdbcCurrencyRepository();
	private ObjectMapper mapper = new ObjectMapper();
	private final static String INTEGRITY_CONSTRAINT_VIOLATION_CODE = "23505";

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		try {
			List<ExchangeRate> listExchangeRates = exchangeRateRepository.getAll();
			mapper.writeValue(resp.getWriter(), listExchangeRates);

		} catch (SQLException e) {
			resp.setStatus(SC_INTERNAL_SERVER_ERROR);
			mapper.writeValue(resp.getWriter(),new ErrorResponseDTO(
					SC_INTERNAL_SERVER_ERROR,
					"Something happened with the database, try again later!"));
		}
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String baseCurrencyCode = req.getParameter("baseCurrencyCode");
		String targetCurrencyCode = req.getParameter("targetCurrencyCode");
		String rateParam = req.getParameter("rate");
		
		String missParam = "";
		if(baseCurrencyCode == null||baseCurrencyCode.isBlank()){
			missParam = "baseCurrencyCode";
		}else if(targetCurrencyCode == null||targetCurrencyCode.isBlank()) {
			missParam = "targetCurrencyCode";
		}else if(rateParam == null||rateParam.isBlank()) {
			missParam = "rate";			
		}
		if( !missParam.equals("") ) {
			resp.setStatus(SC_BAD_REQUEST);
			mapper.writeValue(resp.getWriter(), new ErrorResponseDTO(
					SC_BAD_REQUEST, 
					"Missing parametr - " + missParam));
			return;
		}
		
		if(!isValidCurrencyCode(baseCurrencyCode)) {
			resp.setStatus(SC_BAD_REQUEST);
			mapper.writeValue(resp.getWriter(), new ErrorResponseDTO(
					SC_BAD_REQUEST,
					"Base currency code must be in ISO 4217 format"));
			return;
		}
		if(!isValidCurrencyCode(targetCurrencyCode)) {
			resp.setStatus(SC_BAD_REQUEST);
			mapper.writeValue(resp.getWriter(), new ErrorResponseDTO(
					SC_BAD_REQUEST,
					"Target currency code must be in ISO 4217 format"));
			return;
		}
		
		BigDecimal rate;
		try {
			rate = BigDecimal.valueOf(Double.parseDouble(rateParam));
		}catch (NumberFormatException e) {
			resp.setStatus(SC_BAD_REQUEST);
			mapper.writeValue(resp.getWriter(),
					new ErrorResponseDTO(SC_BAD_REQUEST,
							"Incorrect value of rate parameter"));
			return;
		}
		
		try {
			ExchangeRate exchangeRate = new ExchangeRate(
					currencyRepository.findByCode(baseCurrencyCode).orElseThrow(),
					currencyRepository.findByCode(targetCurrencyCode).orElseThrow(),
					rate); 
			Integer exchangeRateId = exchangeRateRepository.add(exchangeRate);
			exchangeRate.setId(exchangeRateId);
			
			mapper.writeValue(resp.getWriter(), exchangeRate);
			
		} catch (NoSuchElementException e) {
			resp.setStatus(SC_BAD_REQUEST);
			mapper.writeValue(resp.getWriter(), new ErrorResponseDTO(
					SC_BAD_REQUEST,
					"One or both currencies for which you are trying to add an exchange rate does not exist in the database"));
		} catch (SQLException e) {
			if(e.getSQLState().equals(INTEGRITY_CONSTRAINT_VIOLATION_CODE)) {
				resp.setStatus(SC_CONFLICT);
				mapper.writeValue(resp.getWriter(), new ErrorResponseDTO(
						SC_CONFLICT,
						e.getMessage()));
			}
			resp.setStatus(SC_INTERNAL_SERVER_ERROR);
			mapper.writeValue(resp.getWriter(), new ErrorResponseDTO(
					SC_INTERNAL_SERVER_ERROR,
					"Something happended with database, try again later!"));
		}
	}

}
