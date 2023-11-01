package kettlebell.main;

import java.sql.Connection;
import java.util.List;

import kettlebell.dao.CurrencyRepository;
import kettlebell.model.Currency;
import kettlebell.repository.Connector;
import kettlebell.repository.JdbcCurrencyRepository;

public class Service extends Connector{
	public void get() {
		try(Connection connection = getConnection();) {
			System.out.println("ok");
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		CurrencyRepository repository = new JdbcCurrencyRepository();
		try {
			List<Currency> currencies = repository.getAll();
			currencies.stream()
							.map(s->s.getId()+s.getCode()+s.getFullName()+s.getSign())
							.forEach(System.out::println);
		} catch (Exception e) {
			// TODO: handle exception
		}
		//List<Currency> currencies = repository.getAll();
	}
}
