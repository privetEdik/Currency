package kettlebell.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import kettlebell.dao.ExchangeRateRepository;
import kettlebell.model.Currency;
import kettlebell.model.ExchangeRate;

public class JdbcExchangeRateRepository extends Connector implements ExchangeRateRepository{
	
	private static final String SQL_GET = "SELECT e.id, "
								+ "cb.id AS b_id,"
								+ "cb.code AS b_code,"
								+ "cb.full_name AS b_fullname,"
								+ "cb.sign AS b_sign,"
								+ "ct.id AS t_id,"
								+ "ct.code AS t_code,"
								+ "ct.full_name AS t_fullname,"
								+ "ct.sign AS t_sign,"
							+ "e.rate "
					+ "FROM exchange_rates AS e  "
					+ "JOIN currencies AS cb ON "
					+ "e.base_currency_id=cb.id "
					+ "JOIN currencies AS ct ON "
					+ "e.target_currency_id=ct.id ";
	
	@Override
	public Optional<ExchangeRate> getById(Integer id) throws SQLException {
		String sql = SQL_GET + "WHERE e.id=?";
		try (Connection connection = getConnection()){
			try (PreparedStatement preparedStatement = connection.prepareStatement(sql)){
				preparedStatement.setInt(1, id);				
				
				ResultSet resultSet = preparedStatement.executeQuery();
				if(resultSet.next()) {
					return Optional.of(getExchangeRate(resultSet));
				}				
			} 
		} 
		return Optional.empty();
	}
	
	@Override
	public List<ExchangeRate> getAll() throws SQLException {
		String sql = SQL_GET + "ORDER BY e.id";
		List<ExchangeRate> list = new ArrayList<ExchangeRate>();
		try (Connection connection = getConnection()){
			try (PreparedStatement preparedStatement = connection.prepareStatement(sql)){
				
				ResultSet resultSet = preparedStatement.executeQuery();
				
				while(resultSet.next()) {
					list.add(getExchangeRate(resultSet));
				}				
			} 
		} 		
		return list;
	}

	@Override
	public Integer add(ExchangeRate model) throws SQLException {
		String sql = "INSERT INTO exchange_rate(base_id,target_id,rate) VALUES(?,?,?);";
		Integer id = 0;
		try (Connection connection = getConnection()){
			try (PreparedStatement preparedStatement = connection.prepareStatement(sql)){
				preparedStatement.setInt(1, model.getBaseCurrency().getId());
				preparedStatement.setInt(2, model.getTargetCurrency().getId());
				preparedStatement.setBigDecimal(3, model.getRate());
				
				ResultSet resultSet = preparedStatement.getGeneratedKeys();
				resultSet.next();
				id = resultSet.getInt("id");
			} 
		} 
		return id;
	}

	@Override
	public void put(ExchangeRate model) throws SQLException {
		//@formatter:off
		String sql = "UPDATE exchange_rates "
					  + "SET base_id=?, target_id=?, rate=? "
					  + "WHERE id=?";
		//@formatter:on
		try (Connection connection = getConnection()){
			try (PreparedStatement preparedStatement = connection.prepareStatement(sql)){
				
				preparedStatement.setInt(1, model.getBaseCurrency().getId());
				preparedStatement.setInt(2, model.getTargetCurrency().getId());
				preparedStatement.setBigDecimal(3, model.getRate());
				preparedStatement.setInt(4, model.getId());				
				preparedStatement.executeUpdate();											
			} 
		}		
	}

	@Override
	public void remove(Integer id) throws SQLException {
		
		String sql = "DELETE FROM exchange_rates WHERE id=?";
			
		try (Connection connection = getConnection()){
			try (PreparedStatement preparedStatement = connection.prepareStatement(sql)){
				
				preparedStatement.setInt(1, id);				
				preparedStatement.executeUpdate();											
			} 
		}
	}
	
	@Override
	public Optional<ExchangeRate> getByCode(String baseCurrencyCode, String targetCurrencyCode) throws SQLException {
		String sql = SQL_GET +" WHERE (cb.code=? AND ct.code=?)"; 
		try (Connection connection = getConnection()){
			try (PreparedStatement preparedStatement = connection.prepareStatement(sql)){
				
				preparedStatement.setString(1, baseCurrencyCode);				
				preparedStatement.setString(1, targetCurrencyCode);				
				
				ResultSet resultSet = preparedStatement.executeQuery();
				if(resultSet.next()) {
					return Optional.of(getExchangeRate(resultSet));
				}
			} 
		}
		return Optional.empty();
	}
	
	public List<ExchangeRate> getByCodeWithUsdBase(String baseCurrencyCode, String targetCurrencyCode) throws SQLException{
		String sql = SQL_GET + " WHERE ( (cb.code='USD' AND (ct.code=? OR ct.code=?)) OR ((cb.code=? OR cb.code=?) AND ct.code='USD') )";
		List<ExchangeRate> list = new ArrayList<>();
		try(Connection connection = getConnection()) {
			try(PreparedStatement preparedStatement = connection.prepareStatement(sql)){
				preparedStatement.setString(1, baseCurrencyCode);
				preparedStatement.setString(2, targetCurrencyCode);
				preparedStatement.setString(3, baseCurrencyCode);
				preparedStatement.setString(4, targetCurrencyCode);
				
				ResultSet resultSet = preparedStatement.executeQuery();
				while(resultSet.next()) {
					list.add(getExchangeRate(resultSet));
				}
			}
		}  
		return list;
	}
	
	private static ExchangeRate getExchangeRate(ResultSet resultSet) throws SQLException{
		//@formatter:off
		return new ExchangeRate(resultSet.getInt("id"),
										new Currency(
												resultSet.getInt("b_id"),
												resultSet.getString("b_code"),
												resultSet.getString("b_fullname"),
												resultSet.getString("b_sign")),
										new Currency(
												resultSet.getInt("t_id"),
												resultSet.getString("t_code"),
												resultSet.getString("t_fullname"),
												resultSet.getString("t_sign")),
										resultSet.getBigDecimal("rate")
										);
		//@formatter:on
	}

}
