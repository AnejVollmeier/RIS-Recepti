package si.um.feri.ris.projekt.Recepti.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import si.um.feri.ris.projekt.Recepti.dao.OcenaDao;
import si.um.feri.ris.projekt.Recepti.dao.ReceptiJpaDao;
import si.um.feri.ris.projekt.Recepti.dao.UporabnikDao;
import si.um.feri.ris.projekt.Recepti.vao.Ocena;
import si.um.feri.ris.projekt.Recepti.vao.Recepti;
import si.um.feri.ris.projekt.Recepti.vao.Uporabnik;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/ocene")
@CrossOrigin
public class OcenaRestController {

    @Autowired
    private OcenaDao ocenaDao;

    @Autowired
    private ReceptiJpaDao receptiDao;

    @Autowired
    private UporabnikDao uporabnikDao;

    @GetMapping
    public List<Ocena> vrniVse() {
        return ocenaDao.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Ocena> vrniEnega(@PathVariable Long id) {
        return ocenaDao.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/recept/{receptId}")
    public List<Ocena> vrniZaRecept(@PathVariable int receptId) {
        return ocenaDao.findByReceptId(receptId);
    }

    @GetMapping("/recept/{receptId}/povprecna")
    public ResponseEntity<Double> vrniPovprecnoOceno(@PathVariable int receptId) {
        Double povprecna = ocenaDao.povprecnaOcenaZaRecept(receptId);
        if (povprecna == null) {
            return ResponseEntity.ok(0.0);
        }
        return ResponseEntity.ok(povprecna);
    }

    @PostMapping
    public ResponseEntity<?> ustvariAliPosodobi(@RequestBody OcenaRequest request) {
        Optional<Recepti> receptOpt = receptiDao.findById(request.getReceptId());
        if (receptOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Recept ne obstaja");
        }

        Optional<Uporabnik> uporabnikOpt = uporabnikDao.findById(request.getUporabnikId());
        if (uporabnikOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Uporabnik ne obstaja");
        }

        if (request.getVrednost() < 1 || request.getVrednost() > 5) {
            return ResponseEntity.badRequest().body("Ocena mora biti med 1 in 5");
        }

        // Preveri, če uporabnik že ima oceno za ta recept
        Optional<Ocena> obstojecaOcena = ocenaDao.findByUporabnikAndRecept(
                uporabnikOpt.get(), receptOpt.get());
        
        Ocena ocena;
        if (obstojecaOcena.isPresent()) {
            // Posodobi obstoječo oceno
            ocena = obstojecaOcena.get();
            ocena.setVrednost(request.getVrednost());
        } else {
            // Ustvari novo oceno
            ocena = new Ocena(request.getVrednost(), uporabnikOpt.get(), receptOpt.get());
        }
        
        Ocena saved = ocenaDao.save(ocena);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> izbrisi(@PathVariable Long id) {
        if (!ocenaDao.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        ocenaDao.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // DTO za ustvarjanje ocene
    public static class OcenaRequest {
        private int vrednost;
        private Long uporabnikId;
        private int receptId;

        public int getVrednost() { return vrednost; }
        public void setVrednost(int vrednost) { this.vrednost = vrednost; }
        public Long getUporabnikId() { return uporabnikId; }
        public void setUporabnikId(Long uporabnikId) { this.uporabnikId = uporabnikId; }
        public int getReceptId() { return receptId; }
        public void setReceptId(int receptId) { this.receptId = receptId; }
    }
}

