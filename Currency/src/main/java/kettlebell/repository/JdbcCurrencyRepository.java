package kettlebell.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import kettlebell.dao.CurrencyRepository;
import kettlebell.model.Currency;


public class JdbcCurrencyRepository extends Connector implements CurrencyRepository{

	@Override
	public Optional<Currency> getById(Integer id) throws SQLException {
		final String sql = "SELECT id, code, full_name, sign "
							  + "FROM currencies WHERE id = ?";
		try(Connection connection = getConnection();
				PreparedStatement preparedStatement = connection.prepareStatement(sql)){
			ResultSet resultSet = preparedStatement.executeQuery();
			if(resultSet.next()) {
				return Optional.of(getCurrency(resultSet));
			}
		}
		return Optional.empty();
	}

	@Override
	public List<Currency> getAll() throws SQLException {
		List<Currency> list = new ArrayList<>();
		final String sql = "SELECT id, code, full_name, sign FROM currencies";
		try(Connection connection = getConnection();
				PreparedStatement preparedStatement = connection.prepareStatement(sql)){
			ResultSet resultSet = preparedStatement.executeQuery();
			while(resultSet.next()) {
				 list.add(getCurrency(resultSet));
			}
		}		
		return list; 
	}
	
	@Override
	public Integer add(Currency model) throws SQLException {
		final String sql = "INSERT INTO currencies(code,full_name,sign) VALUES(?,?,?)";
		Integer id=0;
		try(Connection connection = getConnection()){
			try(PreparedStatement preparedStatement = connection.prepareStatement(sql)){
				preparedStatement.setString(1, model.getCode());
				preparedStatement.setString(2, model.getFullName());
				preparedStatement.setString(3, model.getSign());
				ResultSet resultSet = preparedStatement.getGeneratedKeys();
				if(resultSet.next()) {
					id = resultSet.getInt("id");
				}
			}
		}
		return id;
	}

	@Override
	public void put(Currency model) throws SQLException {
		final String sql = "UPDATE currencies SET code=?,full_name=?,sign=? "
				+ "WHERE id=?";
		try(Connection connection = getConnection();
				PreparedStatement preparedStatement = connection.prepareStatement(sql)){
			preparedStatement.setString(1, model.getCode());
			preparedStatement.setString(2, model.getFullName());
			preparedStatement.setString(3, model.getSign());
			preparedStatement.setInt(4, model.getId());
			
			preparedStatement.executeUpdate();
		}
	}

	@Override
	public void remove(Integer id) throws SQLException {
		final String sql = "DELETE FROM currencies WHERE id=?";
		try(Connection connection = getConnection();
				PreparedStatement preparedStatement = connection.prepareStatement(sql)){
			preparedStatement.setInt(1, id);
			
			preparedStatement.execute();
		}
	}
	
	
	
	@Override
	public Optional<Currency> findByCode(String code) throws SQLException {
		final String sql = "SELECT * FROM currencies WHERE code = ?";
		
		try(Connection connection = getConnection();
				PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
			preparedStatement.setString(1, code);
			ResultSet resultSet = preparedStatement.executeQuery();
			if(resultSet.next()) {
				return Optional.of(getCurrency(resultSet));
			}
			
		}
		return Optional.empty();
	}

	private static Currency getCurrency(ResultSet resultSet) throws SQLException{
		return new Currency(resultSet.getInt("id"),
								  resultSet.getString("code"),
								  resultSet.getString("full_name"),
								  resultSet.getString("sign"));
	}
}
