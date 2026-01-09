package si.um.feri.ris.projekt.Recepti.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import si.um.feri.ris.projekt.Recepti.dao.ReceptiJpaDao;
import si.um.feri.ris.projekt.Recepti.rest.dto.NutritionData;
import si.um.feri.ris.projekt.Recepti.service.KolicinaService;
import si.um.feri.ris.projekt.Recepti.service.NutritionApiService;
import si.um.feri.ris.projekt.Recepti.vao.Recepti;
import si.um.feri.ris.projekt.Recepti.vao.Sestavine;
import si.um.feri.ris.projekt. Recepti.rest.dto. HranilneVrednostiDto;
import si.um.feri. ris.projekt.Recepti. service.HranilneVrednostiService;

import java.net.URI;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/recepti")
@CrossOrigin
public class ReceptiRestController {

    @Autowired
    ReceptiJpaDao dao;

    @Autowired
    KolicinaService kolicinaService;

    @Autowired
    private HranilneVrednostiService hranilneVrednostiService;

    @Autowired
    NutritionApiService nutritionApiService;  // ← DODANO

    @GetMapping
    public Iterable<Recepti> findAll(@RequestParam(value = "ime", required = false) String ime) {
        if (ime != null && !ime.isBlank()) {
            return dao.findByImeContainingIgnoreCase(ime);
        }
        return dao.findAll();
    }

    @GetMapping("/{idRecepta}")
    public ResponseEntity<Recepti> findById(@PathVariable("idRecepta") int idRecepta) {
        return dao.findById(idRecepta)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{idRecepta}/porcije/{steviloPorcij}")
    public ResponseEntity<?> findByIdZaPorcije(
            @PathVariable("idRecepta") int idRecepta,
            @PathVariable("steviloPorcij") int steviloPorcij) {

        if (steviloPorcij <= 0) {
            return ResponseEntity.badRequest().body("Število porcij mora biti pozitivno");
        }

        Optional<Recepti> receptOpt = dao.findById(idRecepta);
        if (receptOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Recepti recept = receptOpt.get();
        List<Recepti.SestavinaDto> preracunaneSestavine =
                recept.getSestavineZaPorcije(steviloPorcij, kolicinaService);

        // Ustvari DTO odgovor
        ReceptZaPorcijoDto response = new ReceptZaPorcijoDto();
        response.id = recept.getId();
        response.ime = recept.getIme();
        response.opis = recept.getOpis();
        response.navodila = recept.getNavodila();
        response.originalneSteviloPorcij = recept.getSteviloPorcij();
        response.zahtevanoSteviloPorcij = steviloPorcij;
        response.sestavine = preracunaneSestavine;

        return ResponseEntity.ok(response);
    }

    @GetMapping("/test-nutrition")
    public ResponseEntity<NutritionData> testNutrition(
            @RequestParam String ime,
            @RequestParam(required = false) String kolicina) {

        NutritionData data = nutritionApiService.searchByName(ime);

        // Izračunaj vrednosti za podano količino ali 100g če količina ni podana
        if (data != null && data.isNajdeno()) {
            String kolicinaZaIzracun = (kolicina != null && !kolicina.trim().isEmpty()) ? kolicina : "100g";
            data = nutritionApiService.calculateForQuantity(data, kolicinaZaIzracun);
        }

        return ResponseEntity.ok(data);
    }

    @GetMapping("/test-nutrition-quantity")
    public ResponseEntity<NutritionData> testNutritionWithQuantity(
            @RequestParam String ime,
            @RequestParam String kolicina) {

        // Najprej poišči osnovne podatke
        NutritionData data = nutritionApiService.searchByName(ime);

        // Potem izračunaj za količino
        NutritionData calculated = nutritionApiService.calculateForQuantity(data, kolicina);

        return ResponseEntity.ok(calculated);
    }

    @PostMapping
    public ResponseEntity<Recepti> addNew(@RequestBody Recepti r) {
        if (r.getSestavine() != null) {
            for (Sestavine s : r.getSestavine()) s.setRecept(r);
        }
        Recepti saved = dao.save(r);
        return ResponseEntity. created(URI.create("/api/recepti/" + saved.getId())).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Recepti> updateById(@PathVariable("id") int id, @RequestBody Recepti incoming) {
        Optional<Recepti> existing = dao.findById(id);
        if (existing.isEmpty()) return ResponseEntity.notFound().build();

        Recepti r = existing.get();
        r.setIme(incoming.getIme());
        r.setOpis(incoming.getOpis());
        r.setNavodila(incoming.getNavodila());
        r.setSlikaUrl(incoming.getSlikaUrl());
        r.setSteviloPorcij(incoming.getSteviloPorcij());
        r.setSestavine(incoming. getSestavine());

        Recepti saved = dao.save(r);
        return ResponseEntity. ok(saved);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable("id") int id) {
        if (! dao.existsById(id)) return ResponseEntity.notFound().build();
        dao.deleteById(id);
        return ResponseEntity. noContent().build();
    }

    // DTO za odgovor
    public static class ReceptZaPorcijoDto {
        public int id;
        public String ime;
        public String opis;
        public String navodila;
        public int originalneSteviloPorcij;
        public int zahtevanoSteviloPorcij;
        public List<Recepti.SestavinaDto> sestavine;

        // Getters in Setters za JSON serializacijo
        public int getId() { return id; }
        public void setId(int id) { this.id = id; }

        public String getIme() { return ime; }
        public void setIme(String ime) { this.ime = ime; }

        public String getOpis() { return opis; }
        public void setOpis(String opis) { this.opis = opis; }

        public String getNavodila() { return navodila; }
        public void setNavodila(String navodila) { this.navodila = navodila; }

        public int getOriginalneSteviloPorcij() { return originalneSteviloPorcij; }
        public void setOriginalneSteviloPorcij(int originalneSteviloPorcij) {
            this.originalneSteviloPorcij = originalneSteviloPorcij;
        }

        public int getZahtevanoSteviloPorcij() { return zahtevanoSteviloPorcij; }
        public void setZahtevanoSteviloPorcij(int zahtevanoSteviloPorcij) {
            this.zahtevanoSteviloPorcij = zahtevanoSteviloPorcij;
        }

        public List<Recepti.SestavinaDto> getSestavine() { return sestavine; }
        public void setSestavine(List<Recepti.SestavinaDto> sestavine) {
            this.sestavine = sestavine;
        }
    }

    /**
     * Vrne hranilne vrednosti za recept
     */
    @GetMapping("/{id}/hranilne-vrednosti")
    public ResponseEntity<HranilneVrednostiDto> getHranilneVrednosti(
            @PathVariable("id") int id,
            @RequestParam(name = "porcije", defaultValue = "1") int porcije) {

        if (porcije < 1) {
            porcije = 1;
        }

        HranilneVrednostiDto vrednosti = hranilneVrednostiService. izracunajHranilneVrednosti(id, porcije);

        if (vrednosti == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(vrednosti);
    }
}