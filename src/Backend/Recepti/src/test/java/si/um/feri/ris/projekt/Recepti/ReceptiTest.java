package si.um.feri.ris.projekt.Recepti;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import si.um.feri.ris.projekt.Recepti.dao.ReceptiJpaDao;
import si.um.feri.ris.projekt.Recepti.dao.UporabnikDao;
import si.um.feri.ris.projekt.Recepti.vao.Recepti;
import si.um.feri.ris.projekt.Recepti.vao.Uporabnik;
import si.um.feri.ris.projekt.Recepti.vao.Vloga;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Preverja funkcionalnosti ustvarjanja, shranjevanja, iskanja in upravljanja receptov.
 *
 */
@SpringBootTest
@Transactional
public class ReceptiTest {

    @Autowired
    private ReceptiJpaDao receptiDao;

    @Autowired
    private UporabnikDao uporabnikDao;

    private Recepti testRecept;
    private Uporabnik testAvtor;

    @BeforeEach
    public void setUp() {
        // Ustvari avtorja
        testAvtor = new Uporabnik();
        testAvtor.setUporabniskoIme("receptiAutor");
        testAvtor.setEmail("autor@example.com");
        testAvtor.setGeslo("password123");
        testAvtor.setVloga(Vloga.USER);
        testAvtor = uporabnikDao.save(testAvtor);

        // Ustvari recept
        testRecept = new Recepti();
        testRecept.setIme("Testni recept");
        testRecept.setOpis("Opis testnega recepta");
        testRecept.setNavodila("Koraki priprave recepta");
        testRecept.setSlikaUrl("https://example.com/slika.jpg");
        testRecept.setAvtor(testAvtor);
    }


    @Test
    @DisplayName("Test ustvarjanja novega recepta - pozitiven scenarij")
    public void testUstvariNovRecept() {
        // Act - Shrani recept
        Recepti shranjeniRecept = receptiDao.save(testRecept);

        // Assert
        assertNotNull(shranjeniRecept, "Shranjen recept ne sme biti null");
        assertNotNull(shranjeniRecept.getId(), "ID recepta mora biti nastavljen");
        assertEquals("Testni recept", shranjeniRecept.getIme(),
                "Ime recepta se mora ujemati");
        assertEquals("Opis testnega recepta", shranjeniRecept.getOpis(),
                "Opis recepta se mora ujemati");
        assertEquals("Koraki priprave recepta", shranjeniRecept.getNavodila(),
                "Navodila se morajo ujemati");
        assertEquals("https://example.com/slika.jpg", shranjeniRecept.getSlikaUrl(),
                "URL slike se mora ujemati");
        assertEquals(testAvtor.getId(), shranjeniRecept.getAvtor().getId(),
                "Avtor recepta se mora ujemati");
        assertNotNull(shranjeniRecept.getCreatedAt(),
                "Časovni žig ustvarjanja mora biti nastavljen");
    }


    @Test
    @DisplayName("Test iskanja recepta po ID-u")
    public void testIskanjePoID() {
        // Arrange - Shrani recept
        Recepti shranjeniRecept = receptiDao.save(testRecept);
        int receptId = shranjeniRecept.getId();

        // Act - Poišči recept
        Optional<Recepti> najdeniRecept = receptiDao.findById(receptId);

        // Assert
        assertTrue(najdeniRecept.isPresent(), "Recept mora biti najden");
        assertEquals("Testni recept", najdeniRecept.get().getIme(),
                "Ime se mora ujemati");
        assertEquals(receptId, najdeniRecept.get().getId(),
                "ID-ja se morata ujemati");
    }

    @Test
    @DisplayName("Test iskanja receptov po imenu")
    public void testIskanjePoImenu() {
        // Arrange - Shrani več receptov
        Recepti recept1 = new Recepti();
        recept1.setIme("Torta Čokolada");
        recept1.setOpis("Čokoladna torta");
        recept1.setAvtor(testAvtor);
        receptiDao.save(recept1);

        Recepti recept2 = new Recepti();
        recept2.setIme("Čokolada Frnikole");
        recept2.setOpis("Frnikole");
        recept2.setAvtor(testAvtor);
        receptiDao.save(recept2);

        Recepti recept3 = new Recepti();
        recept3.setIme("Piškoti Sladki");
        recept3.setOpis("Sladki piškoti");
        recept3.setAvtor(testAvtor);
        receptiDao.save(recept3);

        // Act - Poišči recepte s "čokolada" v imenu (case-insensitive)
        List<Recepti> rezultati = receptiDao.findByImeContainingIgnoreCase("čokolada");

        // Assert
        assertEquals(2, rezultati.size(), "Morala bi biti 2 recepta s 'čokolada' v imenu");
        assertTrue(rezultati.stream().anyMatch(r -> r.getIme().equals("Torta Čokolada")),
                "Recept 'Torta Čokolada' mora biti v rezultatih");
        assertTrue(rezultati.stream().anyMatch(r -> r.getIme().equals("Čokolada Frnikole")),
                "Recept 'Čokolada Frnikole' mora biti v rezultatih");
    }
}

