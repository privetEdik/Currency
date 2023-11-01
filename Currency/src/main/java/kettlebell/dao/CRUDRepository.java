package kettlebell.dao;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface CRUDRepository<T> { 
	 Integer add(T model ) throws SQLException;
	 Optional<T> getById(Integer id) throws SQLException;
	 List<T> getAll() throws SQLException;
	 void put(T model) throws SQLException;
	 void remove(Integer id) throws SQLException; 
}
