package tikape.runko.database;

import java.sql.*;
import java.util.*;

public interface Dao<T, K> {

    T findOne(K key) throws SQLException;
    
    void save(T element) throws SQLException;

    List<T> findAll() throws SQLException;

    void delete(K key) throws SQLException;
}
