package tikape.runko.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import tikape.runko.domain.RaakaAine;
import tikape.runko.domain.RaakaAineDrinkissa;

public class RaakaAineDao implements Dao<RaakaAine, Integer> {

    private Database database;

    public RaakaAineDao(Database database) {
        this.database = database;
    }

    public RaakaAine findOnebyName(String nimi) throws SQLException {
        Connection connection = database.getConnection();
        PreparedStatement stmt = connection.prepareStatement("SELECT * FROM RaakaAine WHERE nimi = ?");
        stmt.setObject(1, nimi);

        ResultSet rs = stmt.executeQuery();
        boolean hasOne = rs.next();
        if (!hasOne) {
            return null;
        }

        Integer id = rs.getInt("id");

        RaakaAine d = new RaakaAine(id, nimi);

        rs.close();
        stmt.close();
        connection.close();

        return d;
    }

    @Override
    public RaakaAine findOne(Integer key) throws SQLException {
        Connection connection = database.getConnection();
        PreparedStatement stmt = connection.prepareStatement("SELECT * FROM RaakaAine WHERE id = ?");
        stmt.setObject(1, key);

        ResultSet rs = stmt.executeQuery();
        boolean hasOne = rs.next();
        if (!hasOne) {
            return null;
        }

        Integer id = rs.getInt("id");
        String nimi = rs.getString("nimi");

        RaakaAine r = new RaakaAine(id, nimi);

        rs.close();
        stmt.close();
        connection.close();

        return r;
    }

    public List<RaakaAineDrinkissa> findAllInDrink(Integer key) throws SQLException {
        Connection connection = database.getConnection();
        PreparedStatement stmt = connection.prepareStatement("SELECT RaakaAine.id,"
                + "RaakaAine.nimi, DrinkkiRaakaAine.maara FROM RaakaAine, "
                + "DrinkkiRaakaAine WHERE DrinkkiRaakaAine.drinkki_id = ?"
                + "AND DrinkkiRaakaAine.raakaaine_id = RaakaAine.id ORDER BY DrinkkiRaakaAine.jarjestys");
        stmt.setObject(1, key);

        ResultSet rs = stmt.executeQuery();
        List<RaakaAineDrinkissa> raakaAineet = new ArrayList<>();
        while (rs.next()) {
            Integer id = rs.getInt("id");
            String nimi = rs.getString("nimi");
            String maara = rs.getString("maara");

            raakaAineet.add(new RaakaAineDrinkissa(id, nimi, maara));
        }

        rs.close();
        stmt.close();
        connection.close();

        return raakaAineet;
    }

    public List<RaakaAine> findAllNotInDrink(Integer key) throws SQLException {
        Connection conn = database.getConnection();
        PreparedStatement stmt = conn.prepareStatement("SELECT * FROM RaakaAine "
                + "WHERE id NOT IN (SELECT DISTINCT raakaaine_id from DrinkkiRaakaAine "
                + "WHERE DrinkkiRaakaAine.drinkki_id = ?) ORDER BY nimi");
        stmt.setObject(1, key);

        ResultSet rs = stmt.executeQuery();
        List<RaakaAine> raakaAineet = new ArrayList<>();
        while (rs.next()) {
            Integer id = rs.getInt("id");
            String nimi = rs.getString("nimi");

            raakaAineet.add(new RaakaAine(id, nimi));
        }

        rs.close();
        stmt.close();
        conn.close();

        return raakaAineet;
    }

    @Override
    public List<RaakaAine> findAll() throws SQLException {

        Connection connection = database.getConnection();
        PreparedStatement stmt = connection.prepareStatement("SELECT * FROM RaakaAine ORDER BY nimi");

        ResultSet rs = stmt.executeQuery();
        List<RaakaAine> drinkit = new ArrayList<>();
        while (rs.next()) {
            Integer id = rs.getInt("id");
            String nimi = rs.getString("nimi");

            drinkit.add(new RaakaAine(id, nimi));
        }

        rs.close();
        stmt.close();
        connection.close();

        return drinkit;
    }

    public List<String> findStatistics() throws SQLException {

        Connection connection = database.getConnection();
        PreparedStatement stmt = connection.prepareStatement("SELECT RaakaAine.nimi "
                + "AS nimi, COUNT(*) AS total FROM RaakaAine LEFT JOIN "
                + "DrinkkiRaakaAine ON RaakaAine.id = DrinkkiRaakaAine.raakaaine_id "
                + "GROUP BY nimi ORDER BY total DESC, nimi");

        ResultSet rs = stmt.executeQuery();

        List<String> tilasto = new ArrayList<>();

        while (rs.next()) {
            int maara = rs.getInt("total");
            String nimi = rs.getString("nimi");
            tilasto.add(nimi + ", käytössä yhteensä " + maara + " drinkissä");
        }

        rs.close();
        stmt.close();
        connection.close();

        return tilasto;
    }

    @Override
    public void delete(Integer key) throws SQLException {
        Connection conn = database.getConnection();

        conn.setAutoCommit(false);

        PreparedStatement stmt1 = conn.prepareStatement("DELETE FROM RaakaAine WHERE id = ?");
        PreparedStatement stmt2 = conn.prepareStatement("DELETE FROM DrinkkiRaakaAine WHERE raakaaine_id = ?");

        stmt1.setInt(1, key);
        stmt2.setInt(1, key);

        int rowAffected = stmt1.executeUpdate();

        if (rowAffected != 1) {
            conn.rollback();
        }

        stmt2.executeUpdate();

        conn.commit();

        stmt1.close();
        stmt2.close();
        conn.close();
    }

    @Override
    public void save(RaakaAine raakaAine) throws SQLException {
        Connection conn = database.getConnection();
        PreparedStatement stmt = conn.prepareStatement("INSERT INTO RaakaAine"
                + " (nimi)"
                + " VALUES (?)");
        stmt.setString(1, raakaAine.getNimi());

        stmt.executeUpdate();
        stmt.close();

        conn.close();

    }

    public boolean onkoKaytossa(int id) throws SQLException {
        Connection conn = database.getConnection();
        PreparedStatement stmt2 = conn.prepareStatement("SELECT COUNT(*) AS total FROM DrinkkiRaakaAine WHERE raakaAine_id = ?");
        stmt2.setInt(1, id);
        ResultSet rs2 = stmt2.executeQuery();
        int maara = 0;
        if (rs2.next()) {
            maara = rs2.getInt("total");
        }

        stmt2.close();
        conn.close();

        return maara != 0;
    }
}
