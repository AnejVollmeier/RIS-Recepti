package si.um.feri. ris.projekt. Recepti;

import org.junit. jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation. Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import si.um.feri. ris.projekt.Recepti.dao.OcenaDao;
import si. um.feri.ris. projekt.Recepti.dao.ReceptiJpaDao;
import si.um.feri.ris.projekt. Recepti.dao.UporabnikDao;
import si.um.feri.ris.projekt.Recepti.vao. Ocena;
import si.um.feri.ris.projekt.Recepti.vao. Recepti;
import si.um. feri.ris.projekt. Recepti.vao. Uporabnik;
import si.um.feri. ris.projekt.Recepti.vao. Vloga;

import java.util.Optional;

import static org.junit. jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class OceneTest {

    @Autowired
    private OcenaDao ocenaDao;

    @Autowired
    private UporabnikDao uporabnikDao;

    @Autowired
    private ReceptiJpaDao receptiDao;

    private Uporabnik testUporabnik;
    private Recepti testRecept;

    @BeforeEach
    public void setUp() {
        testUporabnik = new Uporabnik();
        testUporabnik. setUporabniskoIme("testUser");
        testUporabnik.setEmail("test@example.com");
        testUporabnik.setGeslo("password123");
        testUporabnik.setVloga(Vloga.USER);
        testUporabnik = uporabnikDao.save(testUporabnik);

        testRecept = new Recepti();
        testRecept.setIme("Testni recept");
        testRecept. setOpis("Opis testnega recepta");
        testRecept. setNavodila("Navodila za pripravo");
        testRecept = receptiDao.save(testRecept);
    }

    @Test
    @DisplayName("Test ustvarjanja veljavne ocene - pozitiven scenarij")
    public void testUstvariVeljavnoOceno() {
        int vrednostOcene = 3;

        Ocena ocena = new Ocena(vrednostOcene, testUporabnik, testRecept);
        Ocena shranjenaOcena = ocenaDao.save(ocena);

        assertNotNull(shranjenaOcena, "Shranjena ocena ne sme biti null");
        assertNotNull(shranjenaOcena.getId(), "ID shranjene ocene mora biti nastavljen");
        assertEquals(vrednostOcene, shranjenaOcena.getVrednost(),
                "Vrednost ocene se mora ujemati s podano vrednostjo");
        assertEquals(testUporabnik. getId(), shranjenaOcena.getUporabnik().getId(),
                "Uporabnik ocene se mora ujemati s testnim uporabnikom");
        assertEquals(testRecept.getId(), shranjenaOcena.getRecept().getId(),
                "Recept ocene se mora ujemati s testnim receptom");
        assertNotNull(shranjenaOcena.getCreatedAt(),
                "Časovni žig ustvarjanja mora biti nastavljen");

        // Dodatna validacija - preveri, ali lahko ponovno preberemo oceno iz baze
        Optional<Ocena> najdenaOcena = ocenaDao.findById(shranjenaOcena.getId());
        assertTrue(najdenaOcena.isPresent(), "Ocena mora biti najdena v bazi");
        assertEquals(vrednostOcene, najdenaOcena.get().getVrednost(),
                "Vrednost prebrane ocene se mora ujemati");
    }

    @Test
    @DisplayName("Test izračuna povprečne ocene recepta - pozitiven scenarij")
    public void testPovprecnaOcenaRecepta() {
        Uporabnik uporabnik1 = new Uporabnik();
        uporabnik1.setUporabniskoIme("user1");
        uporabnik1.setEmail("user1@example.com");
        uporabnik1.setGeslo("pass1");
        uporabnik1.setVloga(Vloga.USER);
        uporabnik1 = uporabnikDao.save(uporabnik1);

        Uporabnik uporabnik2 = new Uporabnik();
        uporabnik2.setUporabniskoIme("user2");
        uporabnik2.setEmail("user2@example.com");
        uporabnik2.setGeslo("pass2");
        uporabnik2.setVloga(Vloga.USER);
        uporabnik2 = uporabnikDao.save(uporabnik2);

        Uporabnik uporabnik3 = new Uporabnik();
        uporabnik3.setUporabniskoIme("user3");
        uporabnik3.setEmail("user3@example.com");
        uporabnik3.setGeslo("pass3");
        uporabnik3.setVloga(Vloga.USER);
        uporabnik3 = uporabnikDao.save(uporabnik3);

        // Act - Ustvari ocene
        Ocena ocena1 = new Ocena(5, uporabnik1, testRecept);
        Ocena ocena2 = new Ocena(3, uporabnik2, testRecept);
        Ocena ocena3 = new Ocena(4, uporabnik3, testRecept);

        ocenaDao.save(ocena1);
        ocenaDao.save(ocena2);
        ocenaDao.save(ocena3);

        Double pricakovaPovprecnaOcena = 4.0;

        Double dejanskaPovprecnaOcena = ocenaDao.povprecnaOcenaZaRecept(testRecept.getId());

        assertNotNull(dejanskaPovprecnaOcena,
                "Povprečna ocena ne sme biti null");
        assertEquals(pricakovaPovprecnaOcena, dejanskaPovprecnaOcena, 0.01,
                "Povprečna ocena mora biti 4.0");

        var ocenoRecepta = ocenaDao.findByReceptId(testRecept.getId());
        assertEquals(3, ocenoRecepta. size(),
                "Recept mora imeti točno 3 ocene");
    }
}