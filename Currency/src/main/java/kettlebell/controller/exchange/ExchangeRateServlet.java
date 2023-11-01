package kettlebell.controller.exchange;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.NoSuchElementException;
import java.util.Optional;

import static javax.servlet.http.HttpServletResponse.*;
import static kettlebell.utils.Validation.isValidCurrencyCode;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.ObjectMapper;

import kettlebell.dao.ExchangeRateRepository;
import kettlebell.dto.ErrorResponseDTO;
import kettlebell.model.ExchangeRate;
import kettlebell.repository.JdbcExchangeRateRepository;

@WebServlet(name = "ExchangeRateServlet", urlPatterns = "/exchangeRate/*")
public class ExchangeRateServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private final ObjectMapper mapper = new ObjectMapper();
	private final ExchangeRateRepository exchangeRateRepository = new JdbcExchangeRateRepository(); 
	private String baseCurrencyCode;
	private String targetCurrencyCode;
	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String url = req.getPathInfo().replaceAll("/", "");
		
		if(url.length()!=6) {
			resp.setStatus(SC_BAD_REQUEST);
			mapper.writeValue(resp.getWriter(), new ErrorResponseDTO(
					SC_BAD_REQUEST,
					"Currency codes are either not provided or provided in an incorrect format"));
			return;
		}
		
		 baseCurrencyCode = url.substring(0,3);
		 targetCurrencyCode = url.substring(3);
		
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
		
		if (req.getMethod().equalsIgnoreCase("PATCH")) {
			doPatch(req, resp);
		} else {
			super.service(req, resp);
		}
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		try {
			Optional<ExchangeRate> optionalExchangeRate = exchangeRateRepository.getByCode(baseCurrencyCode, targetCurrencyCode);
			if(optionalExchangeRate.isEmpty()) {
				resp.setStatus(SC_NOT_FOUND);
				mapper.writeValue(resp.getWriter(), new ErrorResponseDTO(
						SC_NOT_FOUND,
						"There is no exchange rate for this currency pair"));
			}
			resp.setStatus(SC_OK);
			mapper.writeValue(resp.getWriter(), optionalExchangeRate.get());
		}catch (SQLException e) {
			resp.setStatus(SC_INTERNAL_SERVER_ERROR);
			mapper.writeValue(resp.getWriter(), new ErrorResponseDTO(
					SC_INTERNAL_SERVER_ERROR,
					"Something happened with the database, try again later!"));
		}
	}

	protected void doPatch(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		try {
			BigDecimal rate = new BigDecimal(Double.valueOf(req.getParameter("rate")));
			
			ExchangeRate optExchangeRate = exchangeRateRepository
					.getByCode(baseCurrencyCode, targetCurrencyCode)
					.orElseThrow();
			
			optExchangeRate.setRate(rate);
			exchangeRateRepository.put(optExchangeRate);
			
		} catch(NumberFormatException e){
			resp.setStatus(SC_BAD_REQUEST);
			mapper.writeValue(resp.getWriter(), new ErrorResponseDTO(
					SC_BAD_REQUEST,
					"Incorrect value of rate parameter"));
		} catch (NoSuchElementException e) {
			resp.setStatus(SC_NOT_FOUND);
			mapper.writeValue(resp.getWriter(), new ErrorResponseDTO(
					SC_NOT_FOUND,
					"There is no exchange rate for this currency pair"));
		} catch (SQLException e) {
			resp.setStatus(SC_INTERNAL_SERVER_ERROR);
			mapper.writeValue(resp.getWriter(), new ErrorResponseDTO(
					SC_INTERNAL_SERVER_ERROR,
					"Something happened with database, try again later!"));
		}
		 
	}

}
