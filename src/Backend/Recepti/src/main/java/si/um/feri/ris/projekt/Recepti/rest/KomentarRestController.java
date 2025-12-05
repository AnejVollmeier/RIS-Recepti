package si.um.feri.ris.projekt.Recepti.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import si.um.feri.ris.projekt.Recepti.dao.KomentarDao;
import si.um.feri.ris.projekt.Recepti.dao.ReceptiJpaDao;
import si.um.feri.ris.projekt.Recepti.dao.UporabnikDao;
import si.um.feri.ris.projekt.Recepti.vao.Komentar;
import si.um.feri.ris.projekt.Recepti.vao.Recepti;
import si.um.feri.ris.projekt.Recepti.vao.Uporabnik;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/komentarji")
@CrossOrigin
public class KomentarRestController {

    @Autowired
    private KomentarDao komentarDao;

    @Autowired
    private ReceptiJpaDao receptiDao;

    @Autowired
    private UporabnikDao uporabnikDao;

    @GetMapping
    public List<Komentar> vrniVse() {
        return komentarDao.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Komentar> vrniEnega(@PathVariable Long id) {
        return komentarDao.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/recept/{receptId}")
    public List<Komentar> vrniZaRecept(@PathVariable int receptId) {
        return komentarDao.findByReceptId(receptId);
    }

    @GetMapping("/uporabnik/{uporabnikId}")
    public List<Komentar> vrniZaUporabnika(@PathVariable Long uporabnikId) {
        return komentarDao.findByUporabnikId(uporabnikId);
    }

    @PostMapping
    public ResponseEntity<?> ustvari(@RequestBody KomentarRequest request) {
        Optional<Recepti> receptOpt = receptiDao.findById(request.getReceptId());
        if (receptOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Recept ne obstaja");
        }

        Optional<Uporabnik> uporabnikOpt = uporabnikDao.findById(request.getUporabnikId());
        if (uporabnikOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Uporabnik ne obstaja");
        }

        Komentar komentar = new Komentar(request.getBesedilo(), uporabnikOpt.get(), receptOpt.get());
        Komentar saved = komentarDao.save(komentar);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> posodobi(@PathVariable Long id, @RequestBody KomentarRequest request) {
        Optional<Komentar> komentarOpt = komentarDao.findById(id);
        if (komentarOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Komentar komentar = komentarOpt.get();
        komentar.uredi(request.getBesedilo());
        return ResponseEntity.ok(komentarDao.save(komentar));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> izbrisi(@PathVariable Long id) {
        if (!komentarDao.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        komentarDao.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // DTO za ustvarjanje/urejanje komentarja
    public static class KomentarRequest {
        private String besedilo;
        private Long uporabnikId;
        private int receptId;

        public String getBesedilo() { return besedilo; }
        public void setBesedilo(String besedilo) { this.besedilo = besedilo; }
        public Long getUporabnikId() { return uporabnikId; }
        public void setUporabnikId(Long uporabnikId) { this.uporabnikId = uporabnikId; }
        public int getReceptId() { return receptId; }
        public void setReceptId(int receptId) { this.receptId = receptId; }
    }
}

