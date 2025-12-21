package si.um.feri. ris.projekt.Recepti;

import org.junit.jupiter. api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation. Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import si.um.feri.ris.projekt.Recepti.dao.ReceptiJpaDao;
import si.um.feri.ris.projekt.Recepti.dao.UporabnikDao;
import si.um.feri.ris.projekt.Recepti.service.KolicinaService;
import si.um.feri. ris.projekt.Recepti.vao.*;

import java.util.List;

import static org. junit.jupiter.api.Assertions.*;

/**
 * Unit testi za izračun količin sestavin glede na število porcij.
 * Preverja pravilnost preračuna količin pri različnih merskih enotah.
 *
 * @author Anej Vollmeier
 */
@SpringBootTest
@Transactional
public class KolicinaTest {

    @Autowired
    private KolicinaService kolicinaService;

    @Autowired
    private ReceptiJpaDao receptiDao;

    @Autowired
    private UporabnikDao uporabnikDao;

    private Recepti testRecept;
    private Uporabnik testAvtor;

    @BeforeEach
    public void setUp() {
        // Ustvari testnega avtorja
        testAvtor = new Uporabnik();
        testAvtor.setUporabniskoIme("kolicinaTestUser");
        testAvtor.setEmail("kolicina@test.com");
        testAvtor.setGeslo("test123");
        testAvtor. setVloga(Vloga.USER);
        testAvtor = uporabnikDao.save(testAvtor);

        // Ustvari osnovni recept za 4 porcije
        testRecept = new Recepti();
        testRecept.setIme("Testni recept za porcije");
        testRecept.setOpis("Recept za testiranje količin");
        testRecept. setNavodila("Koraki priprave");
        testRecept. setSteviloPorcij(4);
        testRecept. setAvtor(testAvtor);
    }

    @Test
    @DisplayName("Test izračuna količin za podvojitev porcij (4 -> 8)")
    public void testPodvojitvePorcij() {
        // Arrange - Dodaj sestavine za 4 porcije
        testRecept.addSestavina(new Sestavine("Moka", "500 g"));
        testRecept.addSestavina(new Sestavine("Mleko", "2 dl"));
        testRecept.addSestavina(new Sestavine("Jajca", "2"));
        testRecept = receptiDao.save(testRecept);

        // Act - Izračunaj za 8 porcij (2x več)
        List<Recepti.SestavinaDto> rezultat = testRecept.getSestavineZaPorcije(8, kolicinaService);

        // Assert
        assertNotNull(rezultat, "Rezultat ne sme biti null");
        assertEquals(3, rezultat.size(), "Morajo biti 3 sestavine");

        // Preveri moko (500g * 2 = 1000g -> 1 kg)
        Recepti.SestavinaDto mokaDto = najdiSestavino(rezultat, "Moka");
        assertEquals("1 kg", mokaDto.getKolicina(), "Moka mora biti 1 kg");

        // Preveri mleko (2dl * 2 = 4dl)
        Recepti.SestavinaDto mlekoDto = najdiSestavino(rezultat, "Mleko");
        assertEquals("4 dl", mlekoDto.getKolicina(), "Mleko mora biti 4 dl");

        // Preveri jajca (2 * 2 = 4)
        Recepti.SestavinaDto jajcaDto = najdiSestavino(rezultat, "Jajca");
        assertEquals("4", jajcaDto.getKolicina(), "Jajca morajo biti 4");
    }

    @Test
    @DisplayName("Test izračuna količin za razpolovitev porcij (4 -> 2)")
    public void testRazpolovitvePorcij() {
        // Arrange
        testRecept.addSestavina(new Sestavine("Olje", "1 dl"));
        testRecept.addSestavina(new Sestavine("Sol", "8 g"));
        testRecept = receptiDao.save(testRecept);

        // Act - Izračunaj za 2 porciji (1/2)
        List<Recepti. SestavinaDto> rezultat = testRecept.getSestavineZaPorcije(2, kolicinaService);

        // Assert
        assertEquals(2, rezultat.size(), "Morata biti 2 sestavini");

        // Preveri olje (1dl / 2 = 0.5dl -> 50ml)
        Recepti.SestavinaDto oljeDto = najdiSestavino(rezultat, "Olje");
        assertEquals("50 ml", oljeDto.getKolicina(), "Olje mora biti 50 ml");

        // Preveri sol (8g / 2 = 4g)
        Recepti.SestavinaDto solDto = najdiSestavino(rezultat, "Sol");
        assertEquals("4 g", solDto.getKolicina(), "Sol mora biti 4 g");
    }

    @Test
    @DisplayName("Test izračuna za 1 porcijo (4 -> 1)")
    public void testEnaPorcija() {
        // Arrange
        testRecept. addSestavina(new Sestavine("Riž", "400 g"));
        testRecept.addSestavina(new Sestavine("Voda", "800 ml"));
        testRecept = receptiDao.save(testRecept);

        // Act - Izračunaj za 1 porcijo (1/4)
        List<Recepti.SestavinaDto> rezultat = testRecept. getSestavineZaPorcije(1, kolicinaService);

        // Assert
        assertEquals(2, rezultat.size(), "Morata biti 2 sestavini");

        // Preveri riž (400g / 4 = 100g)
        Recepti.SestavinaDto rizDto = najdiSestavino(rezultat, "Riž");
        assertEquals("100 g", rizDto.getKolicina(), "Riž mora biti 100 g");

        // Preveri vodo (800ml / 4 = 200ml -> 2dl zaradi optimizacije)
        Recepti.SestavinaDto vodaDto = najdiSestavino(rezultat, "Voda");
        assertEquals("2 dl", vodaDto.getKolicina(), "Voda mora biti 2 dl (optimizirano iz 200ml)");
    }

    @Test
    @DisplayName("Test optimizacije enot pri večanju porcij (g -> kg)")
    public void testOptimizacijaEnot() {
        // Arrange
        testRecept.addSestavina(new Sestavine("Maslo", "250 g"));
        testRecept = receptiDao.save(testRecept);

        // Act - Izračunaj za 16 porcij (4x več:  250g * 4 = 1000g -> 1kg)
        List<Recepti.SestavinaDto> rezultat = testRecept.getSestavineZaPorcije(16, kolicinaService);

        // Assert
        Recepti.SestavinaDto masloDto = najdiSestavino(rezultat, "Maslo");
        assertEquals("1 kg", masloDto.getKolicina(),
                "1000g mora biti avtomatsko pretvorjeno v 1 kg");
    }

    @Test
    @DisplayName("Test optimizacije enot pri manjšanju porcij (kg -> g)")
    public void testOptimizacijaEnoteNavzdol() {
        // Arrange
        testRecept.addSestavina(new Sestavine("Moka", "2 kg"));
        testRecept = receptiDao.save(testRecept);

        // Act - Izračunaj za 1 porcijo (2kg / 4 = 0.5kg -> 500g)
        List<Recepti. SestavinaDto> rezultat = testRecept.getSestavineZaPorcije(1, kolicinaService);

        // Assert
        Recepti.SestavinaDto mokaDto = najdiSestavino(rezultat, "Moka");
        assertEquals("500 g", mokaDto.getKolicina(),
                "0.5kg mora biti avtomatsko pretvorjeno v 500 g");
    }

    @Test
    @DisplayName("Test izračuna z decimalnimi števili")
    public void testDecimalneKolicine() {
        // Arrange
        testRecept.addSestavina(new Sestavine("Sladkor", "150 g"));
        testRecept = receptiDao.save(testRecept);

        // Act - Izračunaj za 6 porcij (150g * 1.5 = 225g)
        List<Recepti. SestavinaDto> rezultat = testRecept.getSestavineZaPorcije(6, kolicinaService);

        // Assert
        Recepti.SestavinaDto sladkorDto = najdiSestavino(rezultat, "Sladkor");
        assertEquals("225 g", sladkorDto.getKolicina(), "Sladkor mora biti 225 g");
    }

    @Test
    @DisplayName("Test ohranjanja istega števila porcij")
    public void testIstaSteviloPorcij() {
        // Arrange
        testRecept.addSestavina(new Sestavine("Sestavina", "300 g"));
        testRecept = receptiDao.save(testRecept);

        // Act - Ohrani 4 porcije
        List<Recepti.SestavinaDto> rezultat = testRecept.getSestavineZaPorcije(4, kolicinaService);

        // Assert
        Recepti.SestavinaDto dto = najdiSestavino(rezultat, "Sestavina");
        assertEquals("300 g", dto.getKolicina(),
                "Količina mora ostati enaka pri istem številu porcij");
    }

    @Test
    @DisplayName("Test z ničelnimi ali negativnimi porcijami - robni primer")
    public void testNeveljavnePorcije() {
        // Arrange
        testRecept. addSestavina(new Sestavine("Test", "100 g"));
        testRecept = receptiDao.save(testRecept);

        // Act - Poskusi z 0 porcijami
        List<Recepti.SestavinaDto> rezultat = testRecept.getSestavineZaPorcije(0, kolicinaService);

        // Assert
        Recepti. SestavinaDto dto = najdiSestavino(rezultat, "Test");
        assertEquals("100 g", dto.getKolicina(),
                "Pri neveljavnem številu porcij mora vrniti originalno količino");
    }

    @Test
    @DisplayName("Test z več različnimi enotami naenkrat")
    public void testMesaneEnote() {
        // Arrange
        testRecept.addSestavina(new Sestavine("Moka", "500 g"));
        testRecept. addSestavina(new Sestavine("Mleko", "3 dl"));
        testRecept. addSestavina(new Sestavine("Sladkor", "100 g"));
        testRecept. addSestavina(new Sestavine("Maslo", "1 kg"));
        testRecept. addSestavina(new Sestavine("Voda", "500 ml"));
        testRecept. addSestavina(new Sestavine("Jajca", "3"));
        testRecept. addSestavina(new Sestavine("Sol", "5 g"));
        testRecept = receptiDao.save(testRecept);

        // Act - Podvoji na 8 porcij
        List<Recepti.SestavinaDto> rezultat = testRecept.getSestavineZaPorcije(8, kolicinaService);

        // Assert
        assertEquals(7, rezultat.size(), "Mora biti 7 sestavin");

        // Preveri vse pravilno podvojene
        assertEquals("1 kg", najdiSestavino(rezultat, "Moka").getKolicina());
        assertEquals("6 dl", najdiSestavino(rezultat, "Mleko").getKolicina());
        assertEquals("200 g", najdiSestavino(rezultat, "Sladkor").getKolicina());
        assertEquals("2 kg", najdiSestavino(rezultat, "Maslo").getKolicina());
        assertEquals("1 l", najdiSestavino(rezultat, "Voda").getKolicina());
        assertEquals("6", najdiSestavino(rezultat, "Jajca").getKolicina());
        assertEquals("10 g", najdiSestavino(rezultat, "Sol").getKolicina());
    }

    @Test
    @DisplayName("Test z zelo velikim povečanjem porcij (4 -> 100)")
    public void testVelikoStPorcij() {
        // Arrange
        testRecept.addSestavina(new Sestavine("Sol", "4 g"));
        testRecept = receptiDao.save(testRecept);

        // Act - 25x več porcij (4g * 25 = 100g)
        List<Recepti. SestavinaDto> rezultat = testRecept.getSestavineZaPorcije(100, kolicinaService);

        // Assert
        Recepti. SestavinaDto solDto = najdiSestavino(rezultat, "Sol");
        assertEquals("100 g", solDto.getKolicina(), "Sol mora biti 100 g");
    }

    @Test
    @DisplayName("Test KolicinaService.izracunajNovoKolicino direktno")
    public void testIzracunajNovoKolicinoService() {
        // Test različnih scenarijev direktno na service

        // Podvojitev
        String rezultat1 = kolicinaService.izracunajNovoKolicino("200 g", 2, 4);
        assertEquals("400 g", rezultat1, "200g za 2 porciji -> 400g za 4 porcije");

        // Razpolovitev (DecimalFormat uporablja vejico, ne pike!)
        String rezultat2 = kolicinaService.izracunajNovoKolicino("500 ml", 4, 2);
        assertEquals("2,5 dl", rezultat2, "500ml za 4 porcije -> 250ml (2,5dl) za 2 porciji");

        // Brez enote
        String rezultat3 = kolicinaService.izracunajNovoKolicino("4", 4, 8);
        assertEquals("8", rezultat3, "4 kosi za 4 porcije -> 8 kosov za 8 porcij");

        // Optimizacija g -> kg
        String rezultat4 = kolicinaService.izracunajNovoKolicino("500 g", 2, 4);
        assertEquals("1 kg", rezultat4, "500g * 2 = 1000g -> 1kg");

        // Optimizacija kg -> g
        String rezultat5 = kolicinaService.izracunajNovoKolicino("1 kg", 4, 2);
        assertEquals("500 g", rezultat5, "1kg / 2 = 0. 5kg -> 500g");
    }

    @Test
    @DisplayName("Test parseKolicina metode")
    public void testParseKolicina() {
        // Test parsiranja različnih formatov
        KolicinaService. ParsedKolicina parsed1 = kolicinaService.parseKolicina("200 g");
        assertEquals(200.0, parsed1.vrednost, 0.01, "Vrednost mora biti 200");
        assertEquals(MerskaEnota.GRAM, parsed1.enota, "Enota mora biti gram");
        assertTrue(parsed1.imaEnoto, "Mora imeti enoto");

        KolicinaService.ParsedKolicina parsed2 = kolicinaService.parseKolicina("2.5 kg");
        assertEquals(2.5, parsed2.vrednost, 0.01, "Vrednost mora biti 2.5");
        assertEquals(MerskaEnota.KILOGRAM, parsed2.enota, "Enota mora biti kilogram");

        KolicinaService.ParsedKolicina parsed3 = kolicinaService.parseKolicina("4");
        assertEquals(4.0, parsed3.vrednost, 0.01, "Vrednost mora biti 4");
        assertFalse(parsed3.imaEnoto, "Ne sme imeti enote");

        KolicinaService.ParsedKolicina parsed4 = kolicinaService.parseKolicina("500ml");
        assertEquals(500.0, parsed4.vrednost, 0.01, "Vrednost mora biti 500");
        assertEquals(MerskaEnota. MILILITER, parsed4.enota, "Enota mora biti mililiter");
    }

    // ===== HELPER METODE =====

    private Recepti. SestavinaDto najdiSestavino(List<Recepti.SestavinaDto> seznam, String naziv) {
        return seznam.stream()
                .filter(s -> s.getNaziv().equals(naziv))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Sestavina '" + naziv + "' ni najdena"));
    }
}