package tikape.runko.database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Database {

    private String databaseAddress;

    public Database(String databaseAddress) throws ClassNotFoundException {
        this.databaseAddress = databaseAddress;
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(databaseAddress);
    }

    public void init() {
        List<String> lauseet = sqliteLauseet();

        // "try with resources" sulkee resurssin automaattisesti lopuksi
        try (Connection conn = getConnection()) {
            Statement st = conn.createStatement();

            // suoritetaan komennot
            for (String lause : lauseet) {
                System.out.println("Running command >> " + lause);
                st.executeUpdate(lause);
            }

        } catch (Throwable t) {
            // jos tietokantataulu on jo olemassa, ei komentoja suoriteta
            System.out.println("Error >> " + t.getMessage());
        }
    }

    private List<String> sqliteLauseet() {
        ArrayList<String> lista = new ArrayList<>();

        // tietokantataulujen luomiseen tarvittavat komennot suoritusjärjestyksessä
        lista.add("CREATE TABLE Drinkki (id integer PRIMARY KEY, nimi varchar(255), ohje varchar(1500));");
        lista.add("CREATE TABLE Kategoria (id integer PRIMARY KEY, nimi varchar(255));");
        lista.add("CREATE TABLE RaakaAine (id integer PRIMARY KEY, nimi varchar(255));");
        lista.add("CREATE TABLE DrinkkiKategoria (drinkki_id integer, kategoria_id, nimi varchar(255), FOREIGN KEY (drinkki_id) REFERENCES Drinkki(id), FOREIGN KEY (kategoria_id) REFERENCES Kategoria(id));");
        lista.add("CREATE TABLE DrinkkiRaakaAine (raakaaine_id integer, drinkki_id integer, jarjestys integer, maara varchar(300), FOREIGN KEY (raakaaine_id) REFERENCES RaakaAine(id), FOREIGN KEY (drinkki_id) REFERENCES Drinkki(id));");
        lista.add("INSERT INTO Drinkki (nimi) VALUES ('Vodka Martini');");
        lista.add("INSERT INTO Drinkki (nimi) VALUES ('Valkovenäläinen');");
        lista.add("INSERT INTO Drinkki (nimi) VALUES ('Kelkka');");
        lista.add("INSERT INTO Kategoria (nimi) VALUES ('Alkoholiton');");
        lista.add("INSERT INTO Kategoria (nimi) VALUES ('Aperatiivi');");
        lista.add("INSERT INTO Kategoria (nimi) VALUES ('Cocktail');");
        lista.add("INSERT INTO Kategoria (nimi) VALUES ('Muu');");
        lista.add("INSERT INTO DrinkkiKategoria (kategoria_id, drinkki_id) VALUES (3, 1);");
        lista.add("INSERT INTO DrinkkiKategoria (kategoria_id, drinkki_id) VALUES (3, 2);");
        lista.add("INSERT INTO DrinkkiKategoria (kategoria_id, drinkki_id) VALUES (1, 3);");
        lista.add("INSERT INTO RaakaAine (nimi) VALUES ('Appelsiinimehu');");
        lista.add("INSERT INTO RaakaAine (nimi) VALUES ('Maito');");
        lista.add("INSERT INTO RaakaAine (nimi) VALUES ('Vodka');");
        lista.add("INSERT INTO RaakaAine (nimi) VALUES ('Valkoinen vermutti');");
        lista.add("INSERT INTO RaakaAine (nimi) VALUES ('Kaakaojauhe');");
        lista.add("INSERT INTO RaakaAine (nimi) VALUES ('Passionhedelmälikööri');");
        lista.add("INSERT INTO DrinkkiRaakaAine (raakaaine_id, drinkki_id,jarjestys, maara) VALUES (1, 3, 1, '10 cl');");
        lista.add("INSERT INTO DrinkkiRaakaAine (raakaaine_id, drinkki_id,jarjestys, maara) VALUES (2, 2, 1, '5 cl');");
        lista.add("INSERT INTO DrinkkiRaakaAine (raakaaine_id, drinkki_id,jarjestys, maara) VALUES (3, 1, 1, '5 cl');");
        lista.add("INSERT INTO DrinkkiRaakaAine (raakaaine_id, drinkki_id,jarjestys, maara) VALUES (3, 2, 2, '5 cl');");
        lista.add("INSERT INTO DrinkkiRaakaAine (raakaaine_id, drinkki_id,jarjestys, maara) VALUES (3, 3, 2, '5 cl');");
        
        
        return lista;
    }
}
