package si.um.feri.ris.projekt.Recepti;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import si.um.feri.ris.projekt.Recepti.dao.JedilnikDao;
import si.um.feri.ris.projekt.Recepti.dao.ReceptiJpaDao;
import si.um.feri.ris.projekt.Recepti.dao.UporabnikDao;
import si.um.feri.ris.projekt.Recepti.vao.Jedilnik;
import si.um.feri.ris.projekt.Recepti.vao.Recepti;
import si.um.feri.ris.projekt.Recepti.vao.Uporabnik;
import si.um.feri.ris.projekt.Recepti.vao.Vloga;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * Preverja funkcionalnosti ustvarjanja, upravljanja receptov in iskanja jedilnikov.
 *
 */
@SpringBootTest
@Transactional
public class JedilnikTest {

    @Autowired
    private JedilnikDao jedilnikDao;

    @Autowired
    private UporabnikDao uporabnikDao;

    @Autowired
    private ReceptiJpaDao receptiDao;

    private Jedilnik testJedilnik;
    private Uporabnik testUporabnik;
    private Recepti testRecept;


    @BeforeEach
    public void setUp() {
        // Ustvari uporabnika
        testUporabnik = new Uporabnik();
        testUporabnik.setUporabniskoIme("jedilnikUser");
        testUporabnik.setEmail("jedilnik@example.com");
        testUporabnik.setGeslo("password123");
        testUporabnik.setVloga(Vloga.USER);
        testUporabnik = uporabnikDao.save(testUporabnik);

        // Ustvari recept
        testRecept = new Recepti();
        testRecept.setIme("Testni recept");
        testRecept.setOpis("Opis recepta");
        testRecept.setAvtor(testUporabnik);
        testRecept = receptiDao.save(testRecept);

        // Ustvari jedilnik
        testJedilnik = new Jedilnik();
        testJedilnik.setNaziv("Testni jedilnik");
        testJedilnik.setDatum(LocalDate.now());
        testJedilnik.setSteviloOseb(4);
        testJedilnik.setUporabnik(testUporabnik);
    }


    @Test
    @DisplayName("Test ustvarjanja novega jedilnika - pozitiven scenarij")
    public void testUstvariNovJedilnik() {
        // Act - Shrani jedilnik
        Jedilnik shranjeniJedilnik = jedilnikDao.save(testJedilnik);

        // Assert
        assertNotNull(shranjeniJedilnik, "Shranjen jedilnik ne sme biti null");
        assertNotNull(shranjeniJedilnik.getId(), "ID jedilnika mora biti nastavljen");
        assertEquals("Testni jedilnik", shranjeniJedilnik.getNaziv(),
                "Naziv jedilnika se mora ujemati");
        assertEquals(LocalDate.now(), shranjeniJedilnik.getDatum(),
                "Datum se mora ujemati");
        assertEquals(4, shranjeniJedilnik.getSteviloOseb(),
                "Število oseb se mora ujemati");
        assertEquals(testUporabnik.getId(), shranjeniJedilnik.getUporabnik().getId(),
                "Uporabnik se mora ujemati");
    }


    @Test
    @DisplayName("Test iskanja jedilnikov po uporabniku")
    public void testIskanjePoUporabniku() {
        // Arrange - Shrani več jedilnikov
        Jedilnik jedilnik1 = new Jedilnik("Jedilnik 1", LocalDate.now(), 2);
        jedilnik1.setUporabnik(testUporabnik);
        jedilnikDao.save(jedilnik1);

        Jedilnik jedilnik2 = new Jedilnik("Jedilnik 2", LocalDate.now().plusDays(1), 3);
        jedilnik2.setUporabnik(testUporabnik);
        jedilnikDao.save(jedilnik2);

        // Ustvari drugega uporabnika in njegov jedilnik
        Uporabnik drugUporabnik = new Uporabnik();
        drugUporabnik.setUporabniskoIme("drugUser");
        drugUporabnik.setEmail("drug@example.com");
        drugUporabnik.setGeslo("pass");
        drugUporabnik = uporabnikDao.save(drugUporabnik);

        Jedilnik jedilnik3 = new Jedilnik("Jedilnik drugih", LocalDate.now(), 5);
        jedilnik3.setUporabnik(drugUporabnik);
        jedilnikDao.save(jedilnik3);

        // Act
        List<Jedilnik> rezultati = jedilnikDao.findByUporabnik(testUporabnik);

        // Assert
        assertEquals(2, rezultati.size(), "Mora biti 2 jedilnika za testnega uporabnika");
        assertTrue(rezultati.stream().allMatch(j -> j.getUporabnik().getId().equals(testUporabnik.getId())),
                "Vsi jedilniki morajo biti tega uporabnika");
    }


    @Test
    @DisplayName("Test dodajanja recepta v jedilnik")
    public void testDodajanjeReceptaVJedilnik() {
        // Arrange
        Jedilnik shranjeniJedilnik = jedilnikDao.save(testJedilnik);

        // Act - Dodaj recept
        shranjeniJedilnik.addRecept(testRecept);
        Jedilnik posodobljenJedilnik = jedilnikDao.save(shranjeniJedilnik);

        // Assert
        assertEquals(1, posodobljenJedilnik.getRecepti().size(),
                "Jedilnik mora imeti 1 recept");
        assertTrue(posodobljenJedilnik.getRecepti().stream()
                .anyMatch(r -> r.getId() == testRecept.getId()),
                "Dodani recept mora biti v jedilniku");
    }
}

