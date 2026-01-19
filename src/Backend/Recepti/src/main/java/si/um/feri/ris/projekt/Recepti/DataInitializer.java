package si.um.feri.ris.projekt.Recepti;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import si.um.feri.ris.projekt.Recepti.dao.JedilnikDao;
import si.um.feri.ris.projekt.Recepti.dao.ReceptiJpaDao;
import si.um.feri.ris.projekt.Recepti.dao.UporabnikDao;
import si.um.feri.ris.projekt.Recepti.vao.*;

@Component
public class DataInitializer implements CommandLineRunner {

    private final ReceptiJpaDao receptiRepository;
    private final UporabnikDao uporabnikRepository;
    private final JedilnikDao jedilnikRepository;

    public DataInitializer(ReceptiJpaDao receptiRepository, UporabnikDao uporabnikRepository,
            JedilnikDao jedilnikRepository) {
        this.receptiRepository = receptiRepository;
        this.uporabnikRepository = uporabnikRepository;
        this.jedilnikRepository = jedilnikRepository;
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
                    "Najboljše puhaste palačinke po babičinem receptu. Idealne za nedeljski zajtrk.",
                    "1. V večji posodi zmešajte jajca, ščepec soli in žlico sladkorja.\n" +
                            "2. Postopoma dodajajte moko in mleko, medtem ko nenehno stepate z metlico, da dobite gladko maso brez grudic.\n"
                            +
                            "3. Maso pustite počivati vsaj 15 minut na sobni temperaturi.\n" +
                            "4. Ponev za palačinke rahlo namastite in močno segrejte.\n" +
                            "5. Z zajemalko vlijte maso in jo enakomerno razporedite po ponvi.\n" +
                            "6. Pecite približno 1-2 minuti na vsaki strani, dokler palačinka ne postane zlato-rjava.",
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
                    "Originalna italijanska carbonara brez smetane. Skrivnost je v kakovostnih sestavinah.",
                    "1. Velik lonec vode osolite in zavrite. Dodajte špagete in jih skuhajte 'al dente'.\n" +
                            "2. Medtem ko se testenine kuhajo, narežite guanciale na majhne koščke in ga v ponvi prepražite na zmernem ognju, da postane hrustljav.\n"
                            +
                            "3. V manjši posodi zmešajte celo jajce, rumenjaka in nariban pecorino romano s sveže mletim poprom.\n"
                            +
                            "4. Ko so testenine kuhane, jih z zajemalko prenesite neposredno v ponev h guancialu.\n" +
                            "5. Ponev odstavite z ognja! To je ključno. Prilijte jajčno mešanico in nekaj žlic vode od kuhanja testenin.\n"
                            +
                            "6. Hitro in neprestano mešajte, da se ustvari kremna omaka, ne da bi jajca zakrknila.",
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
                    "Bogata in hranljiva zelenjavna juha, pripravljena iz svežih sezonskih sestavin.",
                    "1. Čebulo in por očistite ter drobno sesekljajte. Korenje in krompir olupite ter narežite na enakomerne kocke.\n"
                            +
                            "2. V velikem loncu segrejte malo oljčnega olja in na njem počasi pražite čebulo in por, dokler ne ovenita.\n"
                            +
                            "3. Dodajte preostalo zelenjavo in vse skupaj pražite še 5 minut, da se sprostijo arome.\n"
                            +
                            "4. Zalijte z 1,5 litra vode ali zelenjavne osnove, posolite in popoprajte po okusu.\n" +
                            "5. Ko juha zavre, zmanjšajte ogenj in kuhajte približno 20-25 minut, da se zelenjava zmehča.\n"
                            +
                            "6. Po želji lahko del juhe pretlačite s paličnim mešalnikom za bolj kremno teksturo.",
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
                    "Domača pica s hrustljavo skorjico in sočnim nadevom, narejena po tradicionalnem postopku.",
                    "1. V posodi zmešajte moko, kvas in ščepec soli. Postopoma dodajajte mlačno vodo in zamesite gladko testo.\n"
                            +
                            "2. Testo gnetite vsaj 10 minut, nato ga pokrijte in pustite vzhajati na toplem mestu približno 1 uro.\n"
                            +
                            "3. Medtem ko testo vzhaja, pripravite paradižnikovo omako iz pelatov, ki jih malo posolite in dodate baziliko.\n"
                            +
                            "4. Pečico segrejte na najvišjo možno temperaturo (navadno 250°C ali več). Če imate kamen za peko, ga vstavite v pečico.\n"
                            +
                            "5. Vzhajano testo razvlecite z rokami v krog, nanesite paradižnikovo omako, dodajte natrgano mocarelo in pokapljajte z oljčnim oljem.\n"
                            +
                            "6. Pecite 7-10 minut, dokler robovi niso zlato rjavi in sir mehurčkast.",
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
                    "Prefinjena in kremna rižota, ki vas bo popeljala naravnost na obalo Italije.",
                    "1. V široki ponvi segrejte malo oljčnega olja in na hitro prepražite gambere. Odstavite jih na krožnik.\n"
                            +
                            "2. V isti ponvi prepražite drobno sesekljano šalotko in strok česna, da zadiši.\n" +
                            "3. Dodajte riž Arborio in ga pražite kakšno minuto, da postane prosojen.\n" +
                            "4. Prilijte suho belo vino in počakajte, da popolnoma izpari.\n" +
                            "5. Nato postopoma, zajemalko za zajemalko, dodajajte vročo ribjo ali zelenjavno osnovo. Med dodajanjem nenehno mešajte.\n"
                            +
                            "6. Ko je riž kuhan 'al dente', vmešajte kocko hladnega masla, nariban parmezan in pripravljene gambere.\n"
                            +
                            "7. Pustite počivati 2 minuti, preden postrežete.",
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
                    "Tradicionalni jabolčni zavitek s hrustljavim testom in sočnim nadevom, kot so ga pripravljale naše babice.",
                    "1. Jabolka olupite, jim odstranite peščišča in jih naribajte na tanke rezine ali lističe.\n" +
                            "2. Naribanim jabolkom dodajte sladkor, cimet, vanilijev sladkor in po želji pesti rozin ter mletih orehov.\n"
                            +
                            "3. List vlečenega testa pokapljajte s stopljenim maslom, nanj položite naslednji list in postopek ponovite.\n"
                            +
                            "4. Jabolčni nadev enakomerno razporedite po spodnji tretjini testa, robove pa zavihajte navznoter.\n"
                            +
                            "5. Zavitek previdno zrolajte in ga položite v pekač, obložen s papirjem za peko.\n" +
                            "6. Premažite ga z raztopljenim maslom in pecite v ogreti pečici na 180°C približno 40-45 minut, da postane zlato rjav.\n"
                            +
                            "7. Pred serviranjem ga potresite s sladkorjem v prahu.",
                    6,
                    userAna,
                    new Sestavine("Jabolka", "1kg"),
                    new Sestavine("Vlečeno testo", "500g"),
                    new Sestavine("Cimet", "1 žlička"),
                    new Sestavine("Sladkor", "100g"));

            addKomentar(r6, userMaja, "Prav asocira na otroštvo!");
            addOcena(r6, userMaja, 5);
            receptiRepository.save(r6);

            // 3. Ustvari demo jedilnike
            Jedilnik j1 = new Jedilnik("Študentski teden", LocalDate.now().plusDays(1), 2);
            j1.setUporabnik(userDemo);
            j1.addRecept(r1);
            j1.addRecept(r2);
            j1.addRecept(r4);
            jedilnikRepository.save(j1);

            Jedilnik j2 = new Jedilnik("Romantična večerja", LocalDate.now().plusDays(3), 2);
            j2.setUporabnik(userMaja);
            j2.addRecept(r5);
            j2.addRecept(r6);
            jedilnikRepository.save(j2);

            Jedilnik j3 = new Jedilnik("Nedeljsko kosilo", LocalDate.now().plusDays(6), 4);
            j3.setUporabnik(userAna);
            j3.addRecept(r3);
            j3.addRecept(r1);
            jedilnikRepository.save(j3);

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
