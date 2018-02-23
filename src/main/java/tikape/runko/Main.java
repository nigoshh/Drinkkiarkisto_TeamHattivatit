package tikape.runko;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import spark.ModelAndView;
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
        database.init();

//        // herokun portille
//        if (System.getenv("PORT") != null) {
//            Spark.port(Integer.valueOf(System.getenv("PORT")));
//        }
        DrinkkiDao drinkkiDao = new DrinkkiDao(database);
        KategoriaDao kategoriaDao = new KategoriaDao(database);
        RaakaAineDao raakaAineDao = new RaakaAineDao(database);

        get("/", (req, res) -> {
            HashMap map = new HashMap<>();
            return new ModelAndView(map, "index");
        }, new ThymeleafTemplateEngine());

        get("/drinkit", (req, res) -> {
            HashMap map = new HashMap<>();
            map.put("drinkit", drinkkiDao.findAll());

            return new ModelAndView(map, "drinkit");
        }, new ThymeleafTemplateEngine());
        
        get("/drinkit/kategoriat/:kategoria", (req, res) -> {
            HashMap map = new HashMap<>();
            map.put("drinkit", drinkkiDao.findAllbyCategory(req.params("kategoria")));

            return new ModelAndView(map, "drinkit");
        }, new ThymeleafTemplateEngine());
        
        get("/drinkit/poista/:id", (req, res) -> {
            HashMap map = new HashMap<>();
            
            drinkkiDao.delete(Integer.parseInt(req.params("id")));
            
            res.redirect("/drinkit");

            return new ModelAndView(map, "drinkki");
        }, new ThymeleafTemplateEngine());
        
        get("/drinkit/:drinkkiId/poistaRaaka-aine/:raId", (req, res) -> {
            HashMap map = new HashMap<>();
            
            int drinkkiId = Integer.parseInt(req.params("drinkkiId"));
            int raId = Integer.parseInt(req.params("raId"));
            
            drinkkiDao.poistaRaakaAine(drinkkiId, raId);
            
            res.redirect("/drinkit/" + req.params("drinkkiId") + "/ok");

            return new ModelAndView(map, "drinkki");
        }, new ThymeleafTemplateEngine());
        
        get("/raaka-aineet/poista/:id", (req, res) -> {
            HashMap map = new HashMap<>();
            
            raakaAineDao.delete(Integer.parseInt(req.params("id")));
            
            res.redirect("/raaka-aineet/ok");

            return new ModelAndView(map, "raaka-aineet");
        }, new ThymeleafTemplateEngine());

        get("/lisaa/:virhe", (req, res) -> {
            HashMap map = new HashMap<>();
            map.put("lisaa", drinkkiDao.findAll());
            
            String virhe = "";
            if (req.params("virhe").equals("v")) {
                virhe += "Virhe! Ole hyvä ja täytä Drinkin nimi-kenttä";
            }
            map.put("virhe", virhe);

            return new ModelAndView(map, "lisaa");
        }, new ThymeleafTemplateEngine());

        post("/lisaa/:virhe", (req, res) -> {
            boolean virhe = false;
            
            String nimi = req.queryParams("nimi");
            if (nimi.isEmpty()) {
                virhe = true;
            }
            
            String redirUrl = "/";
            if (virhe) {
                redirUrl += "lisaa/v";
            } else {
                drinkkiDao.save(new Drinkki(0, nimi, ""));

                String kategoria = req.queryParams("kategoria");
                Drinkki lisatty = drinkkiDao.findOnebyName(nimi);
                kategoriaDao.lisaaDrinkki(kategoriaDao.findOnebyName(kategoria), lisatty);
                
                redirUrl += "drinkit/" + lisatty.getId() + "/ok";
            }
            
            res.redirect(redirUrl);
            return "";
        });
        
        get("/drinkit/:id/:virhe", (req, res) -> {
            HashMap map = new HashMap<>();
            
            int drinkkiId = Integer.parseInt(req.params("id"));
            map.put("drinkki", drinkkiDao.findOne(drinkkiId));
            
            String virhe = "";
            
            if (req.params("virhe").equals("v")) {
                virhe += "Virhe! Ole hyvä ja täytä Määrä ja Järjestys -kentät"
                        + " (Järjestys-kenttään pelkästään kokonaislukuja)";
            }
            
            map.put("virhe", virhe);

            List<RaakaAineDrinkissa> raakaAineetDrinkissa =
                    raakaAineDao.findAllInDrink(drinkkiId);

            List<RaakaAine> raakaAineetEiDrinkissa = raakaAineDao.findAllNotInDrink(drinkkiId);

            map.put("raakaAineetEiDrinkissa", raakaAineetEiDrinkissa);
            map.put("raakaAineetDrinkissa", raakaAineetDrinkissa);

            return new ModelAndView(map, "drinkki");
        }, new ThymeleafTemplateEngine());

        post("/drinkit/:id/:virhe", (req, res) -> {
            
            boolean virhe = false;

            String nimi = req.queryParams("nimi");
            if (nimi.isEmpty()) {
                virhe = true;
            }
            
            int jarjestys = 0;
            if (isInteger(req.queryParams("jarjestys"))) {
                jarjestys = Integer.parseInt(req.queryParams("jarjestys"));
            } else {
                virhe = true;
            }
            
            String maara = req.queryParams("maara");
            if (maara.isEmpty()) {
                virhe = true;
            }
            
            String redirUrl = "/drinkit/" + req.params("id") + "/";
            
            if (virhe) {
                redirUrl += "v";
            } else {
                drinkkiDao.lisaaRaakaAine(drinkkiDao.findOne(Integer.parseInt(req.params("id"))),
                    raakaAineDao.findOnebyName(nimi), jarjestys, maara);
                redirUrl += "ok";
            }

            res.redirect(redirUrl);
            return "";
            });
        
        get("/raaka-aineet/:virhe", (req, res) -> {
            HashMap map = new HashMap<>();
            map.put("raakaAineet", raakaAineDao.findAll());
            map.put("tilasto", raakaAineDao.findStatistics());
            
            String virhe = "";
            if (req.params("virhe").equals("v")) {
                virhe += "Virhe! Ole hyvä ja täytä Raaka-aineen nimi-kenttä";
            }
            map.put("virhe", virhe);

            return new ModelAndView(map, "raaka-aineet");
        }, new ThymeleafTemplateEngine());
        
        post("/raaka-aineet/:virhe", (req, res) -> {
            boolean virhe = false;
            
            String nimi = req.queryParams("nimi");
            if (nimi.isEmpty()) {
                virhe = true;
            }
            
            String redirUrl = "/raaka-aineet/";
            
            if (virhe) {
                redirUrl += "v";
            } else {
                RaakaAine raakaAine = new RaakaAine(0, nimi);
                raakaAineDao.save(raakaAine);
                redirUrl += "ok";
            }
            
            res.redirect(redirUrl);
            return "";
        });
    }

    public static boolean isInteger(String str) {
        if (str == null) {
            return false;
        }
        int length = str.length();
        if (length == 0) {
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
