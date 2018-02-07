package tikape.runko.domain;

public class Kategoria {
    private Integer id;
    private String nimi;
    private List<Drinkki> drinkit;

    public Kategoria(Integer id, String nimi) {
        this.id = id;
        this.nimi = nimi;
        this.drinkit = new ArrayList<>();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNimi() {
        return nimi;
    }

    public void setNimi(String nimi) {
        this.nimi = nimi;
    }
    
    public List<Drinkki> getDrinkit() {
        return this.drinkit;
    }
    
    public void lisaaDrinkki(Drinkki drinkki) {
        this.drinkit.add(drinkki);
    }
}
