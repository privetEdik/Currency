package kettlebell.controller.currency;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import static kettlebell.utils.Validation.isValidCurrencyCode;
import static javax.servlet.http.HttpServletResponse.*;

import kettlebell.dao.CurrencyRepository;
import kettlebell.dto.ErrorResponseDTO;
import kettlebell.model.Currency;
import kettlebell.repository.JdbcCurrencyRepository;

@WebServlet(name = "CurrencyServlet", urlPatterns = "/currency/*" )
public class CurrencyServlet extends HttpServlet{

	private static final long serialVersionUID = 1L;
	private final CurrencyRepository currencyRepository = new JdbcCurrencyRepository();
	private final ObjectMapper objectMapper = new ObjectMapper();
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String code = req.getPathInfo().replaceAll("/", "");
		
		if(isValidCurrencyCode(code)) {
			resp.setStatus(SC_BAD_REQUEST);
			objectMapper.writeValue(resp.getWriter(), new ErrorResponseDTO(
					SC_BAD_REQUEST,
					"Currency code must be in ISO 4217 format"));
			return;
		}
			
		try {
			Optional<Currency> currencyOptional = currencyRepository.findByCode(code);
			if(currencyOptional.isEmpty()) {
				resp.setStatus(SC_NOT_FOUND);
				objectMapper.writeValue(resp.getWriter(), new ErrorResponseDTO(
						SC_NOT_FOUND,
						"There is no such in the database"));
				return;
			}
			
			objectMapper.writeValue(resp.getWriter(), currencyOptional.get());
			
		}catch(SQLException e) {
			resp.setStatus(SC_INTERNAL_SERVER_ERROR);
			objectMapper.writeValue(resp.getWriter(), new ErrorResponseDTO(
					SC_INTERNAL_SERVER_ERROR,
					"Something happened with the database? try again later!"
					));
		}
	}	
}
