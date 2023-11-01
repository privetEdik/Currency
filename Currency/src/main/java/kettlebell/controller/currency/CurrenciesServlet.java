package kettlebell.controller.currency;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static javax.servlet.http.HttpServletResponse.*;

import com.fasterxml.jackson.databind.ObjectMapper;

import kettlebell.dao.CurrencyRepository;
import kettlebell.dto.ErrorResponseDTO;
import kettlebell.model.Currency;
import kettlebell.repository.JdbcCurrencyRepository;

@WebServlet(name = "CurrenciesServlet", urlPatterns = "currencies")
public class CurrenciesServlet extends HttpServlet {
	
	private final CurrencyRepository currencyRepository = new JdbcCurrencyRepository();
	private final ObjectMapper objectMapper = new ObjectMapper();
	private final String INTEGRITY_CONSTRAINT_VIOLATION_CODE = "23505";
	
	private static final long serialVersionUID = 1L;
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		try {
		List<Currency> currenciesList = currencyRepository.getAll();
			objectMapper.writeValue(resp.getWriter(), currenciesList);
		}catch(SQLException e) {
			resp.setStatus(SC_INTERNAL_SERVER_ERROR);
			objectMapper.writeValue(resp.getWriter(), new ErrorResponseDTO(
					SC_INTERNAL_SERVER_ERROR,
					"Something happened with the database, try again later!"
					));
		}
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		String name = req.getParameter("name");
		String code = req.getParameter("code");
		String sign = req.getParameter("sign");
		
		String missParam = "";
		if(name == null||name.isBlank()){
			missParam = "name";
		}else if(code == null||code.isBlank()) {
			missParam = "code";
		}else if(sign == null||sign.isBlank()) {
			missParam = "sign";			
		}
		if(!missParam.equals("")) {
			resp.setStatus(SC_BAD_REQUEST);
			objectMapper.writeValue(resp.getWriter(), new ErrorResponseDTO(
					SC_BAD_REQUEST,
					"Missing parametr - " + missParam));
			return;
		}
		
		try {
			Currency currency = new Currency(code, name, sign);
			Integer addCurrencyId = currencyRepository.add(currency);
			currency.setId(addCurrencyId);
			
			objectMapper.writeValue(resp.getWriter(), currency);
		}catch(SQLException e) {
			if(e.getSQLState().equals(INTEGRITY_CONSTRAINT_VIOLATION_CODE)) {
				resp.setStatus(SC_CONFLICT);
				objectMapper.writeValue(resp.getWriter(), new ErrorResponseDTO(
						SC_CONFLICT,
						e.getMessage()));
			}
			resp.setStatus(SC_INTERNAL_SERVER_ERROR);
			objectMapper.writeValue(resp.getWriter(),new ErrorResponseDTO(
					SC_INTERNAL_SERVER_ERROR,
					"Something happened with the database, try again later!"));
		}
	}
	
}
