package tikape.runko;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import spark.ModelAndView;
import spark.Spark;
import static spark.Spark.*;
import spark.template.thymeleaf.ThymeleafTemplateEngine;
import tikape.runko.database.Database;
import tikape.runko.database.DrinkkiDao;
import tikape.runko.database.KategoriaDao;
import tikape.runko.database.RaakaAineDao;
import tikape.runko.domain.Drinkki;
import tikape.runko.domain.Kategoria;
import tikape.runko.domain.RaakaAine;
import tikape.runko.domain.RaakaAineDrinkissa;

public class Main {

    public static void main(String[] args) throws Exception {
        Database database = new Database("jdbc:sqlite:drinkit.db");

        // herokun portille
        if (System.getenv("PORT") != null) {
            Spark.port(Integer.valueOf(System.getenv("PORT")));
        }
        DrinkkiDao drinkkiDao = new DrinkkiDao(database);
        KategoriaDao kategoriaDao = new KategoriaDao(database);
        RaakaAineDao raakaAineDao = new RaakaAineDao(database);

        get("/", (req, res) -> {
            HashMap map = new HashMap<>();
            return new ModelAndView(map, "index");
        }, new ThymeleafTemplateEngine());

        get("/drinkit/:viesti", (req, res) -> {
            HashMap map = new HashMap<>();
            map.put("drinkit", drinkkiDao.findAll());
            
            String onnistumisenViesti = "";
            if (req.params("viesti").equals("p")) {
                onnistumisenViesti += "Drinkin poistaminen onnistui!";
            }
            map.put("onnistui", onnistumisenViesti);

            return new ModelAndView(map, "drinkit");
        }, new ThymeleafTemplateEngine());

        get("/drinkit/kategoriat/:kategoria", (req, res) -> {
            HashMap map = new HashMap<>();
            map.put("drinkit", drinkkiDao.findAllbyCategory(req.params("kategoria")));

            map.put("kategoria", req.params("kategoria"));

            return new ModelAndView(map, "drinkit");
        }, new ThymeleafTemplateEngine());

        get("/drinkit/poista/:id", (req, res) -> {
            HashMap map = new HashMap<>();

            drinkkiDao.delete(Integer.parseInt(req.params("id")));

            res.redirect("/drinkit/p");

            return new ModelAndView(map, "drinkki");
        }, new ThymeleafTemplateEngine());

        get("/drinkit/:drinkkiId/poistaRaaka-aine/:raId", (req, res) -> {
            HashMap map = new HashMap<>();

            int drinkkiId = Integer.parseInt(req.params("drinkkiId"));
            int raId = Integer.parseInt(req.params("raId"));

            drinkkiDao.poistaRaakaAine(drinkkiId, raId);

            res.redirect("/drinkit/" + req.params("drinkkiId") + "/p");

            return new ModelAndView(map, "drinkki");
        }, new ThymeleafTemplateEngine());

        get("/drinkit/:drinkkiId/poistaKategoria/:kId", (req, res) -> {
            HashMap map = new HashMap<>();

            int drinkkiId = Integer.parseInt(req.params("drinkkiId"));
            int kId = Integer.parseInt(req.params("kId"));

            kategoriaDao.poistaDrinkki(kId, drinkkiId);

            res.redirect("/drinkit/" + req.params("drinkkiId") + "/p");

            return new ModelAndView(map, "drinkki");
        }, new ThymeleafTemplateEngine());

        get("/raaka-aineet/poista/:id", (req, res) -> {
            HashMap map = new HashMap<>();

            int raId = Integer.parseInt(req.params("id"));

            String redirUrl = "/raaka-aineet/";

            if (raakaAineDao.onkoKaytossa(raId)) {
                redirUrl += "s";
            } else {
                raakaAineDao.delete(raId);
                redirUrl += "p";
            }

            res.redirect(redirUrl);

            return new ModelAndView(map, "raaka-aineet");
        }, new ThymeleafTemplateEngine());

        get("/lisaa/:virhe", (req, res) -> {
            HashMap map = new HashMap<>();
            map.put("lisaa", drinkkiDao.findAll());

            String virheviesti = "";
            if (req.params("virhe").equals("v")) {
                virheviesti += "Virhe! Ole hyvä ja täytä Drinkin nimi-kenttä";
            } else if (req.params("virhe").equals("d")) {
                virheviesti += "Virhe! Drinkin nimi on jo käytössä; "
                        + "ole hyvä ja valitse toinen nimi";
            }
            map.put("virhe", virheviesti);

            return new ModelAndView(map, "lisaa");
        }, new ThymeleafTemplateEngine());

        post("/lisaa/:virhe", (req, res) -> {
            String virhe = "";

            String nimi = req.queryParams("nimi").trim();
            if (nimi.isEmpty()) {
                virhe += "v";
            } else if (drinkkiDao.findOnebyName(nimi) != null) {
                virhe += "d";
            }

            String redirUrl = "/";

            if (!virhe.isEmpty()) {
                redirUrl += "lisaa/" + virhe;
            } else {
                drinkkiDao.save(new Drinkki(0, nimi, ""));

                Drinkki lisatty = drinkkiDao.findOnebyName(nimi);
                redirUrl += "drinkit/" + lisatty.getId() + "/ok";
            }

            res.redirect(redirUrl);
            return "";
        });

        get("/drinkit/:id/:virhe", (req, res) -> {
            HashMap map = new HashMap<>();

            int drinkkiId = Integer.parseInt(req.params("id"));
            map.put("drinkki", drinkkiDao.findOne(drinkkiId));

            String virheviesti = "";
            String onnistumisenViesti = "";
            
            String virhe = req.params("virhe");
            if (virhe.equals("v")) {
                virheviesti += "Virhe! Ole hyvä ja täytä Määrä ja Järjestysnumero -kentät"
                        + " (Järjestysnumero-kenttään pelkästään kokonaislukuja"
                        + " välillä [-99999999, 999999999])";
            } else if (virhe.equals("l")) {
                onnistumisenViesti += "Tiedon lisääminen onnistui!";
            } else if (virhe.equals("p")) {
                onnistumisenViesti += "Tiedon poistaminen onnistui!";
            }

            map.put("virhe", virheviesti);
            map.put("onnistui", onnistumisenViesti);

            List<RaakaAineDrinkissa> raakaAineetDrinkissa
                    = raakaAineDao.findAllInDrink(drinkkiId);

            List<RaakaAine> raakaAineetEiDrinkissa = raakaAineDao.findAllNotInDrink(drinkkiId);

            map.put("raakaAineetEiDrinkissa", raakaAineetEiDrinkissa);
            map.put("raakaAineetDrinkissa", raakaAineetDrinkissa);

            List<Kategoria> drinkkiEiKategorioissa = kategoriaDao.findAllNotInDrink(drinkkiId);
            List<Kategoria> drinkinKategoriat = kategoriaDao.findAllInDrink(drinkkiId);

            map.put("drinkkiEiKategorioissa", drinkkiEiKategorioissa);
            map.put("DrinkinKategoriat", drinkinKategoriat);

            return new ModelAndView(map, "drinkki");
        }, new ThymeleafTemplateEngine());

        post("/drinkit/:id/:virhe", (req, res) -> {

            if (req.params("virhe").equals("kategoria")) {
                String kategoria = req.queryParams("kategoria");
                Drinkki drinkki = drinkkiDao.findOne(Integer.parseInt(req.params("id")));
                kategoriaDao.lisaaDrinkki(kategoriaDao.findOnebyName(kategoria), drinkki);

                res.redirect("/drinkit/" + req.params("id") + "/l");

            } else if (req.params("virhe").equals("ohje")) {
                String ohje = req.queryParams("ohje").trim();
                drinkkiDao.lisaaOhje(ohje, Integer.parseInt(req.params("id")));

                res.redirect("/drinkit/" + req.params("id") + "/l");
            } else {
                boolean virhe = false;

                String nimi = req.queryParams("nimi").trim();
                if (nimi.isEmpty()) {
                    virhe = true;
                }

                int jarjestys = 0;
                String j = req.queryParams("jarjestys").trim();
                if (isIntegerAndShort(j)) {
                    jarjestys = Integer.parseInt(j);
                } else {
                    virhe = true;
                }

                String maara = req.queryParams("maara").trim();
                if (maara.isEmpty()) {
                    virhe = true;
                }

                String redirUrl = "/drinkit/" + req.params("id") + "/";

                if (virhe) {
                    redirUrl += "v";
                } else {
                    drinkkiDao.lisaaRaakaAine(drinkkiDao.findOne(Integer.parseInt(req.params("id"))),
                            raakaAineDao.findOnebyName(nimi), jarjestys, maara);
                    redirUrl += "l";
                }

                res.redirect(redirUrl);
            }
            return "";
        });

        get("/raaka-aineet/:virhe", (req, res) -> {
            HashMap map = new HashMap<>();
            map.put("raakaAineet", raakaAineDao.findAll());
            map.put("tilasto", raakaAineDao.findStatistics());

            String virheviesti = "";
            String onnistumisenViesti = "";
            
            String virhe = req.params("virhe");
            if (virhe.equals("v")) {
                virheviesti += "Virhe! Ole hyvä ja täytä Raaka-aineen nimi-kenttä";
            } else if (virhe.equals("d")) {
                virheviesti += "Virhe! Raaka-aineen nimi on jo olemassa; "
                        + "ole hyvä ja valitse toinen raaka-aineen nimi";
            } else if (virhe.equals("s")) {
                virheviesti += "Virhe! Et voi poistaa raaka-ainetta joka on käytössä";
            } else if (virhe.equals("l")) {
                onnistumisenViesti += "Tiedon lisääminen onnistui!";
            } else if (virhe.equals("p")) {
                onnistumisenViesti += "Tiedon poistaminen onnistui!";
            }
            
            map.put("virhe", virheviesti);
            map.put("onnistui", onnistumisenViesti);

            return new ModelAndView(map, "raaka-aineet");
        }, new ThymeleafTemplateEngine());

        post("/raaka-aineet/:virhe", (req, res) -> {
            String virhe = "";

            String nimi = req.queryParams("nimi").trim();
            if (nimi.isEmpty()) {
                virhe += "v";
            } else if (raakaAineDao.findOnebyName(nimi) != null) {
                virhe += "d";
            }

            String redirUrl = "/raaka-aineet/";

            if (!virhe.isEmpty()) {
                redirUrl += virhe;
            } else {
                RaakaAine raakaAine = new RaakaAine(0, nimi);
                raakaAineDao.save(raakaAine);
                redirUrl += "l";
            }

            res.redirect(redirUrl);
            return "";
        });
    }

    public static boolean isIntegerAndShort(String str) {
        if (str == null) {
            return false;
        }
        int length = str.length();
        if (length == 0 || length > 9) {
            return false;
        }
        int i = 0;
        if (str.charAt(0) == '-') {
            if (length == 1) {
                return false;
            }
            i = 1;
        }
        for (; i < length; i++) {
            char c = str.charAt(i);
            if (c < '0' || c > '9') {
                return false;
            }
        }
        return true;
    }
}
