package si.um.feri.ris.projekt.Recepti;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import si.um.feri.ris.projekt.Recepti.dao.UporabnikDao;
import si.um.feri.ris.projekt.Recepti.vao.Uporabnik;
import si.um.feri.ris.projekt.Recepti.vao.Vloga;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Preverja funkcionalnosti ustvarjanja, shranjevanja, iskanja in validacije uporabnikov.
 */
@SpringBootTest
@Transactional
public class UporabnikTest {

    @Autowired
    private UporabnikDao uporabnikDao;

    private Uporabnik testUporabnik;


    @BeforeEach
    public void setUp() {
        testUporabnik = new Uporabnik();
        testUporabnik.setUporabniskoIme("testUser");
        testUporabnik.setEmail("test@example.com");
        testUporabnik.setGeslo("password123");
        testUporabnik.setVloga(Vloga.USER);
        testUporabnik.setAvatar("avatar_url");
        testUporabnik.setBio("Test bio");
    }


    @Test
    @DisplayName("Test ustvarjanja novega uporabnika - pozitiven scenarij")
    public void testUstvariNovegaUporabnika() {
        // Act - Shrani uporabnika
        Uporabnik shranjeniUporabnik = uporabnikDao.save(testUporabnik);

        // Assert
        assertNotNull(shranjeniUporabnik, "Shranjen uporabnik ne sme biti null");
        assertNotNull(shranjeniUporabnik.getId(), "ID uporabnika mora biti nastavljen");
        assertEquals("testUser", shranjeniUporabnik.getUporabniskoIme(),
                "Uporabniško ime se mora ujemati");
        assertEquals("test@example.com", shranjeniUporabnik.getEmail(),
                "Email se mora ujemati");
        assertEquals("password123", shranjeniUporabnik.getGeslo(),
                "Geslo se mora ujemati");
        assertEquals(Vloga.USER, shranjeniUporabnik.getVloga(),
                "Vloga mora biti USER");
        assertEquals("avatar_url", shranjeniUporabnik.getAvatar(),
                "Avatar se mora ujemati");
        assertEquals("Test bio", shranjeniUporabnik.getBio(),
                "Bio se mora ujemati");
    }


    @Test
    @DisplayName("Test iskanja uporabnika po uporabniškem imenu")
    public void testIskanjePoUporabniskomImenu() {
        // Arrange - Shrani uporabnika
        Uporabnik shranjeniUporabnik = uporabnikDao.save(testUporabnik);

        // Act - Poišči uporabnika po imenu
        Optional<Uporabnik> najdeniUporabnik = uporabnikDao.findByUporabniskoIme("testUser");

        // Assert
        assertTrue(najdeniUporabnik.isPresent(), "Uporabnik mora biti najden");
        assertEquals(shranjeniUporabnik.getId(), najdeniUporabnik.get().getId(),
                "ID-ja se morata ujemati");
        assertEquals("test@example.com", najdeniUporabnik.get().getEmail(),
                "Emaila se morata ujemati");
    }


    @Test
    @DisplayName("Test preverjanja, ali je uporabnik admin")
    public void testIsAdminPozitivenScenarij() {
        // Arrange
        Uporabnik adminUporabnik = new Uporabnik("adminUser", "admin@example.com", "pass", Vloga.ADMIN);

        // Act
        boolean isAdmin = adminUporabnik.isAdmin();

        // Assert
        assertTrue(isAdmin, "Admin uporabnik mora biti admin");
    }
}

