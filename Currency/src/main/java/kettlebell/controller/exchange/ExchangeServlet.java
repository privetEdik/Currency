package kettlebell.controller.exchange;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.NoSuchElementException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.ObjectMapper;

import kettlebell.dto.ErrorResponseDTO;
import kettlebell.dto.ExchangeDTO;
import kettlebell.service.ExchangeRateService;

import static kettlebell.utils.Validation.isValidCurrencyCode;
import static javax.servlet.http.HttpServletResponse.*;

@WebServlet(name = "ExchangeServlet", urlPatterns = "/exchange")
public class ExchangeServlet extends HttpServlet{

	private static final long serialVersionUID = 1L;
	ObjectMapper objectMapper = new ObjectMapper();
	ExchangeRateService service = new ExchangeRateService();
	
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String baseCurrencyCode = req.getParameter("from");
		String targetCurrencyCode = req.getParameter("to");
		String amountToConvert = req.getParameter("amount");
		
		String missParam = "";
		if(baseCurrencyCode == null||baseCurrencyCode.isBlank()) {
			missParam = "from";
		}else if(targetCurrencyCode == null||targetCurrencyCode.isBlank()) {
			missParam = "to";
		}else if(amountToConvert == null||amountToConvert.isBlank()) {
			missParam = "amount";
		}
		
		if(!missParam.equals("")) {
			resp.setStatus(SC_BAD_REQUEST);
			objectMapper.writeValue(resp.getWriter(), new ErrorResponseDTO(
					SC_BAD_REQUEST,
					"Missing parameter - " + missParam));
			return;
		}
		
		if(!isValidCurrencyCode(baseCurrencyCode)) {
			resp.setStatus(SC_BAD_REQUEST);
			objectMapper.writeValue(resp.getWriter(),new ErrorResponseDTO(
					SC_BAD_REQUEST,
					"Base currency code must be in ISO 4217 format"));
			return;
		}
		if(!isValidCurrencyCode(targetCurrencyCode)) {
			resp.setStatus(SC_BAD_REQUEST);
			objectMapper.writeValue(resp.getWriter(),new ErrorResponseDTO(
					SC_BAD_REQUEST,
					"Target currency code must be in ISO 4217 format"));
			return;
		}
		
		BigDecimal amount;
		try {
			amount = BigDecimal.valueOf(Double.parseDouble(amountToConvert));
		} catch (NumberFormatException e) {
			resp.setStatus(SC_BAD_REQUEST);
			objectMapper.writeValue(resp.getWriter(), new ErrorResponseDTO(
					SC_BAD_REQUEST,
					"Incorrect value of amount parameter"));
			return;
		}
		
		try {
			ExchangeDTO exchangeDTO = service.convertCurrency(baseCurrencyCode, targetCurrencyCode, amount);
			objectMapper.writeValue(resp.getWriter(), exchangeDTO);
			
		} catch (SQLException e) {
			resp.setStatus(SC_INTERNAL_SERVER_ERROR);
			objectMapper.writeValue(resp.getWriter(),
											new ErrorResponseDTO(SC_INTERNAL_SERVER_ERROR,
													"Something happened with the database, try again!"));
			return;
		} catch (NoSuchElementException e) {
			resp.setStatus(SC_NOT_FOUND);
			objectMapper.writeValue(resp.getWriter(), new ErrorResponseDTO(
						SC_NOT_FOUND,
						"There is not exchange rate for this currency pair"));
		}
		
	}
	
}
