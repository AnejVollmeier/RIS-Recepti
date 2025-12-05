package si.um.feri.ris.projekt.Recepti.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import si.um.feri.ris.projekt.Recepti.dao.ReceptiJpaDao;
import si.um.feri.ris.projekt.Recepti.dao.UporabnikDao;
import si.um.feri.ris.projekt.Recepti.dao.VsecekDao;
import si.um.feri.ris.projekt.Recepti.vao.Recepti;
import si.um.feri.ris.projekt.Recepti.vao.Uporabnik;
import si.um.feri.ris.projekt.Recepti.vao.Vsecek;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/vsecki")
@CrossOrigin
public class VsecekRestController {

    @Autowired
    private VsecekDao vsecekDao;

    @Autowired
    private ReceptiJpaDao receptiDao;

    @Autowired
    private UporabnikDao uporabnikDao;

    @GetMapping
    public List<Vsecek> vrniVse() {
        return vsecekDao.findAll();
    }

    @GetMapping("/recept/{receptId}")
    public List<Vsecek> vrniZaRecept(@PathVariable int receptId) {
        return vsecekDao.findByReceptId(receptId);
    }

    @GetMapping("/recept/{receptId}/stevilo")
    public ResponseEntity<Integer> vrniSteviloVseckov(@PathVariable int receptId) {
        return ResponseEntity.ok(vsecekDao.countByReceptId(receptId));
    }

    @GetMapping("/uporabnik/{uporabnikId}")
    public List<Vsecek> vrniZaUporabnika(@PathVariable Long uporabnikId) {
        return vsecekDao.findByUporabnikId(uporabnikId);
    }

    @GetMapping("/preveri")
    public ResponseEntity<Boolean> preveriVsecek(
            @RequestParam Long uporabnikId, 
            @RequestParam int receptId) {
        Optional<Uporabnik> uporabnikOpt = uporabnikDao.findById(uporabnikId);
        Optional<Recepti> receptOpt = receptiDao.findById(receptId);
        
        if (uporabnikOpt.isEmpty() || receptOpt.isEmpty()) {
            return ResponseEntity.ok(false);
        }
        
        return ResponseEntity.ok(vsecekDao.existsByUporabnikAndRecept(
                uporabnikOpt.get(), receptOpt.get()));
    }

    @PostMapping("/toggle")
    public ResponseEntity<?> toggleVsecek(@RequestBody VsecekRequest request) {
        Optional<Recepti> receptOpt = receptiDao.findById(request.getReceptId());
        if (receptOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Recept ne obstaja");
        }

        Optional<Uporabnik> uporabnikOpt = uporabnikDao.findById(request.getUporabnikId());
        if (uporabnikOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Uporabnik ne obstaja");
        }

        Optional<Vsecek> obstojecVsecek = vsecekDao.findByUporabnikAndRecept(
                uporabnikOpt.get(), receptOpt.get());
        
        if (obstojecVsecek.isPresent()) {
            // Odstrani všeček
            vsecekDao.delete(obstojecVsecek.get());
            return ResponseEntity.ok().body("Všeček odstranjen");
        } else {
            // Dodaj všeček
            Vsecek vsecek = new Vsecek(uporabnikOpt.get(), receptOpt.get());
            Vsecek saved = vsecekDao.save(vsecek);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> izbrisi(@PathVariable Long id) {
        if (!vsecekDao.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        vsecekDao.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // DTO za toggle všečka
    public static class VsecekRequest {
        private Long uporabnikId;
        private int receptId;

        public Long getUporabnikId() { return uporabnikId; }
        public void setUporabnikId(Long uporabnikId) { this.uporabnikId = uporabnikId; }
        public int getReceptId() { return receptId; }
        public void setReceptId(int receptId) { this.receptId = receptId; }
    }
}

