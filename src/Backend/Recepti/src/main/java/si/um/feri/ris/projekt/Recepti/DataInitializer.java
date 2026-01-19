package si.um.feri.ris.projekt.Recepti;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;
import si.um.feri.ris.projekt.Recepti.dao.ReceptiJpaDao;
import si.um.feri.ris.projekt.Recepti.dao.UporabnikDao;
import si.um.feri.ris.projekt.Recepti.vao.*;

@Component
public class DataInitializer implements CommandLineRunner {

    private final ReceptiJpaDao receptiRepository;
    private final UporabnikDao uporabnikRepository;

    public DataInitializer(ReceptiJpaDao receptiRepository, UporabnikDao uporabnikRepository) {
        this.receptiRepository = receptiRepository;
        this.uporabnikRepository = uporabnikRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        // Only seed if no recipes exist
        if (receptiRepository.count() == 0) {

            // 1. Ustvari uporabnike
            Uporabnik userDemo = getOrCreateUser("demo", "demo@student.um.si", "test1234");
            Uporabnik userAna = getOrCreateUser("ana", "ana@example.com", "ana123");
            Uporabnik userMarko = getOrCreateUser("marko", "marko@example.com", "marko123");
            Uporabnik userMaja = getOrCreateUser("maja", "maja@example.com", "maja123");

            // 2. Ustvari recepte z komentarji in ocenami

            Recepti r1 = createRecept("Domače Palačinke",
                    "Najboljše puhaste palačinke po babičinem receptu.",
                    "1. Zmešaj jajca in sladkor.\n2. Dodaj moko in mleko.\n3. Peci na vroči ponvi.",
                    4,
                    userDemo,
                    new Sestavine("Bela moka", "250g"),
                    new Sestavine("Jajca L", "2"),
                    new Sestavine("Mleko 3.5%", "500ml"),
                    new Sestavine("Sol", "ščepec"));

            addKomentar(r1, userAna, "Odličen recept! Moji otroci jih obožujejo.");
            addKomentar(r1, userMarko, "Malo preveč moke za moj okus, ampak vseeno dobre.");
            addKomentar(r1, userDemo, "Hvala vsem! Poskusite dodati še malo vanilije.");

            addOcena(r1, userAna, 5);
            addOcena(r1, userMarko, 4);
            addOcena(r1, userMaja, 5);
            receptiRepository.save(r1);

            Recepti r2 = createRecept("Testenine Carbonara",
                    "Originalna italijanska carbonara brez smetane.",
                    "1. Skuhaj testenine al dente.\n2. Prepraži guanciale.\n3. Zmešaj jajca in pecorino.\n4. Vse skupaj zmešaj na zmanjšanem ognju.",
                    4,
                    userDemo,
                    new Sestavine("Spaghetti", "400g"),
                    new Sestavine("Guanciale", "150g"),
                    new Sestavine("Pecorino Romano", "100g"),
                    new Sestavine("Jajca", "3"));

            addKomentar(r2, userMaja, "Končno pravi recept brez smetane! Bravissimo!");
            addKomentar(r2, userAna, "Jaz dodam še malo česna, pa je perfektno.");
            addOcena(r2, userMaja, 5);
            addOcena(r2, userMarko, 5);
            addOcena(r2, userAna, 4);
            receptiRepository.save(r2);

            Recepti r3 = createRecept("Zelenjavna Juha",
                    "Zdrava in lahka juha za hladne dni.",
                    "1. Nareži zelenjavo.\n2. Prepraži na olju.\n3. Zalij z vodo in kuhaj 20 min.",
                    4,
                    userAna,
                    new Sestavine("Korenje", "3"),
                    new Sestavine("Krompir", "2"),
                    new Sestavine("Por", "1"),
                    new Sestavine("Čebula", "1"));

            addKomentar(r3, userDemo, "Zelo preprosto in okusno.");
            addKomentar(r3, userMarko, "Dodal sem še malo cvetače, super je izpadlo.");
            addOcena(r3, userDemo, 4);
            addOcena(r3, userMarko, 5);
            receptiRepository.save(r3);

            Recepti r4 = createRecept("Pica Margherita",
                    "Domača pica kot iz krušne peči.",
                    "1. Zamesi testo in pusti vzhajati.\n2. Razvaljaj in obloži s pelati in mocarelo.\n3. Peci na najvišji temperaturi 10 min.",
                    2,
                    userMarko,
                    new Sestavine("Moka za pico", "500g"),
                    new Sestavine("Kvas", "1 vrečka"),
                    new Sestavine("Pelati", "400g"),
                    new Sestavine("Mozzarella", "200g"));

            addKomentar(r4, userAna, "Testo je super, hrustljavo!");
            addOcena(r4, userAna, 5);
            receptiRepository.save(r4);

            Recepti r5 = createRecept("Rižota z Gamberi",
                    "Kremna morska rižota polnega okusa.",
                    "1. Na olju popraži česen in gambere.\n2. Dodaj riž in zalij z belim vinom.\n3. Postopoma dodajaj ribjo osnovo.\n4. Na koncu vmešaj maslo.",
                    2,
                    userMaja,
                    new Sestavine("Riž Arborio", "200g"),
                    new Sestavine("Gamberi", "300g"),
                    new Sestavine("Belo vino", "100ml"),
                    new Sestavine("Maslo", "30g"));

            addKomentar(r5, userDemo, "Uf, tole pa izgleda kot v restavraciji!");
            addKomentar(r5, userMarko, "Sem dodal še malo žafrana, vrhunsko.");
            addOcena(r5, userDemo, 5);
            addOcena(r5, userMarko, 5);
            receptiRepository.save(r5);

            Recepti r6 = createRecept("Jabolčni Zavitek",
                    "Klasičen štrudelj po babičino.",
                    "1. Olupi in naribaj jabolka.\n2. Dodaj cimet in sladkor.\n3. Zavij v vlečeno testo.\n4. Peci 45 min na 180 stopinjah.",
                    6,
                    userAna,
                    new Sestavine("Jabolka", "1kg"),
                    new Sestavine("Vlečeno testo", "500g"),
                    new Sestavine("Cimet", "1 žlička"),
                    new Sestavine("Sladkor", "100g"));

            addKomentar(r6, userMaja, "Prav asocira na otroštvo!");
            addOcena(r6, userMaja, 5);
            receptiRepository.save(r6);

            System.out.println("DEMO Podatki so bili uspešno inicializirani!");
        }
    }

    private Uporabnik getOrCreateUser(String username, String email, String password) {
        return uporabnikRepository.findByUporabniskoIme(username)
                .orElseGet(() -> {
                    Uporabnik u = new Uporabnik();
                    u.setUporabniskoIme(username);
                    u.setEmail(email);
                    u.setGeslo(password);
                    u.setVloga(Vloga.USER);
                    return uporabnikRepository.save(u);
                });
    }

    private Recepti createRecept(String ime, String opis, String navodila, int porcije, Uporabnik avtor,
            Sestavine... sestavine) {
        Recepti r = new Recepti();
        r.setIme(ime);
        r.setOpis(opis);
        r.setNavodila(navodila);
        r.setSteviloPorcij(porcije);
        r.setAvtor(avtor);
        for (Sestavine s : sestavine) {
            r.addSestavina(s);
        }
        return r;
    }

    private void addKomentar(Recepti r, Uporabnik u, String besedilo) {
        Komentar k = new Komentar(besedilo, u, r);
        r.addKomentar(k);
    }

    private void addOcena(Recepti r, Uporabnik u, int vrednost) {
        Ocena o = new Ocena(vrednost, u, r);
        r.addOcena(o);
    }
}
