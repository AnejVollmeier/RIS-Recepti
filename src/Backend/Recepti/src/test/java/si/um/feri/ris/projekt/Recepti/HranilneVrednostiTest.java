package si.um.feri.ris.projekt.Recepti;

import org.junit.jupiter. api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import si.um.feri.ris.projekt.Recepti.dao.ReceptiJpaDao;
import si.um.feri.ris.projekt.Recepti.rest.dto.HranilneVrednostiDto;
import si.um.feri.ris.projekt. Recepti.vao.Recepti;
import si.um.feri.ris.projekt.Recepti.vao.Sestavine;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest. WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode. AFTER_EACH_TEST_METHOD)
public class HranilneVrednostiTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ReceptiJpaDao receptiDao;

    private Integer testReceptId;

    @BeforeEach
    @Transactional
    public void setup() {
        Recepti testRecept = new Recepti();
        testRecept.setIme("Test Recept - Omleta");
        testRecept.setOpis("Testna omleta za teste");
        testRecept.setSteviloPorcij(2);

        List<Sestavine> sestavine = new ArrayList<>();

        Sestavine jajca = new Sestavine();
        jajca.setNaziv("eggs");
        jajca.setKolicina("200g");
        jajca.setRecept(testRecept);
        sestavine.add(jajca);

        Sestavine mleko = new Sestavine();
        mleko.setNaziv("milk");
        mleko.setKolicina("50ml");
        mleko.setRecept(testRecept);
        sestavine.add(mleko);

        testRecept.setSestavine(sestavine);
        testRecept = receptiDao.save(testRecept);
        testReceptId = testRecept.getId();
    }

    /**
     * Test 1: Preveri REST API endpoint - osnovni primer
     */
    @Test
    public void testHranilneVrednostiEndpoint_VrneOK() {
        // Given
        String url = "/api/recepti/" + testReceptId + "/hranilne-vrednosti?porcije=2";

        // When
        ResponseEntity<HranilneVrednostiDto> response = restTemplate.getForEntity(
                url,
                HranilneVrednostiDto.class
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode(), "Status mora biti 200 OK");
        assertNotNull(response.getBody(), "Telo odgovora ne sme biti null");

        HranilneVrednostiDto dto = response.getBody();
        assertEquals(testReceptId, dto.getReceptId());
        assertNotNull(dto.getSkupneVrednosti());
        assertNotNull(dto.getNaPorcijo());

        // Preveri da ima sestavine
        assertNotNull(dto.getSestavine());
        assertEquals(2, dto.getSestavine().size());

        // Preveri da so kalorije > 0
        assertTrue(dto.getSkupneVrednosti().getKalorije() > 0);
    }


    /**
     * Test 2: Preveri REST API endpoint za neobstoječ recept
     */
    @Test
    public void testHranilneVrednostiEndpoint_NeobstojecRecept() {
        // Given
        int neobstojecId = 99999;
        String url = "/api/recepti/" + neobstojecId + "/hranilne-vrednosti?porcije=1";

        // When
        ResponseEntity<HranilneVrednostiDto> response = restTemplate.getForEntity(
                url,
                HranilneVrednostiDto.class
        );

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode(),
                "Status mora biti 404 NOT FOUND za neobstoječ recept");
    }

    /**
     * Test 3: Preveri da endpoint vrne vse potrebne podatke
     */
    @Test
    public void testHranilneVrednostiEndpoint_VsePolja() {
        // Given
        String url = "/api/recepti/" + testReceptId + "/hranilne-vrednosti?porcije=2";

        // When
        ResponseEntity<HranilneVrednostiDto> response = restTemplate.getForEntity(
                url,
                HranilneVrednostiDto.class
        );

        // Then
        HranilneVrednostiDto dto = response.getBody();
        assertNotNull(dto);

        // Preveri vse potrebne podatke
        assertNotNull(dto.getReceptIme());
        assertNotNull(dto.getOriginalneSteviloPorcij());
        assertNotNull(dto.getZahtevanoSteviloPorcij());

        // Skupne vrednosti
        assertNotNull(dto.getSkupneVrednosti().getKalorije());
        assertNotNull(dto.getSkupneVrednosti().getMascobe());
        assertNotNull(dto.getSkupneVrednosti().getProteini());
        assertNotNull(dto.getSkupneVrednosti().getOgljikoviHidrati());

        // Na porcijo
        assertNotNull(dto.getNaPorcijo().getKalorije());
        assertNotNull(dto.getNaPorcijo().getMascobe());
        assertNotNull(dto.getNaPorcijo().getProteini());
        assertNotNull(dto.getNaPorcijo().getOgljikoviHidrati());
    }

    /**
     * Test 4: Preveri da endpoint deluje brez parametra porcije (default=1)
     */
    @Test
    public void testHranilneVrednostiEndpoint_BrezParametra() {
        // Given - URL brez parametra porcije
        String url = "/api/recepti/" + testReceptId + "/hranilne-vrednosti";

        // When
        ResponseEntity<HranilneVrednostiDto> response = restTemplate.getForEntity(
                url,
                HranilneVrednostiDto.class
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        HranilneVrednostiDto dto = response.getBody();
        assertNotNull(dto);

        // Preveri da je uporabil default vrednost (1 ali originalno število)
        assertTrue(dto.getZahtevanoSteviloPorcij() >= 1);
    }
}