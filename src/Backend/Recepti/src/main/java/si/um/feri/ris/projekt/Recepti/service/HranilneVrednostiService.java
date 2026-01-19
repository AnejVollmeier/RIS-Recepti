package si.um.feri.ris.projekt.Recepti.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import si.um.feri.ris.projekt.Recepti.dao.ReceptiJpaDao;
import si.um.feri.ris.projekt.Recepti.rest.dto.HranilneVrednostiDto;
import si.um.feri.ris.projekt.Recepti.rest.dto.NutritionData;
import si.um.feri.ris.projekt.Recepti.vao.Recepti;
import si.um.feri.ris.projekt.Recepti.vao.Sestavine;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

@Service
public class HranilneVrednostiService {

    private static final Logger log = Logger.getLogger(HranilneVrednostiService.class.getName());
    private final ExecutorService executor = Executors.newFixedThreadPool(8);

    @Autowired
    private ReceptiJpaDao receptiDao;

    @Autowired
    private NutritionApiService nutritionApiService;

    public HranilneVrednostiDto izracunajHranilneVrednosti(int receptId, int zahtevanoSteviloPorcij) {

        Optional<Recepti> receptOpt = receptiDao.findById(receptId);
        if (receptOpt.isEmpty()) {
            log.warning("Recept z ID " + receptId + " ne obstaja");
            return null;
        }

        Recepti recept = receptOpt.get();
        int originalneSteviloPorcij = recept.getSteviloPorcij();

        log.info("Izračunavam hranilne vrednosti za recept:  " + recept.getIme());

        List<Sestavine> sestavine = recept.getSestavine();
        List<HranilneVrednostiDto.SestavinaHranilneVrednosti> sestavineList = new ArrayList<>();

        // Process ingredients in parallel
        List<CompletableFuture<HranilneVrednostiDto.SestavinaHranilneVrednosti>> futures = new ArrayList<>();

        for (Sestavine sestavina : sestavine) {
            String naziv = sestavina.getNaziv();
            String kolicina = sestavina.getKolicina();

            // Check if already cached
            boolean cached = Boolean.TRUE.equals(sestavina.getHranilneNajdene())
                    && sestavina.getKalorije() != null;

            if (cached) {
                futures.add(CompletableFuture.completedFuture(
                        new HranilneVrednostiDto.SestavinaHranilneVrednosti(
                                naziv, kolicina,
                                sestavina.getKalorije(),
                                sestavina.getMascobe(),
                                sestavina.getProteini(),
                                sestavina.getOgljikoviHidrati(),
                                true)));
            } else {
                // Fetch in parallel
                futures.add(CompletableFuture.supplyAsync(() -> {
                    log.info("Obdelujem sestavino: " + naziv + " (" + kolicina + ")");
                    NutritionData nutritionData = nutritionApiService.searchByName(naziv);

                    if (nutritionData != null && nutritionData.isNajdeno()) {
                        NutritionData calculated = nutritionApiService.calculateForQuantity(nutritionData, kolicina);

                        sestavina.setKalorije(calculated.getKalorije());
                        sestavina.setMascobe(calculated.getMascobe());
                        sestavina.setProteini(calculated.getProteini());
                        sestavina.setOgljikoviHidrati(calculated.getOgljikoviHidrati());
                        sestavina.setHranilneNajdene(true);

                        return new HranilneVrednostiDto.SestavinaHranilneVrednosti(
                                naziv, kolicina,
                                calculated.getKalorije(),
                                calculated.getMascobe(),
                                calculated.getProteini(),
                                calculated.getOgljikoviHidrati(),
                                true);
                    } else {
                        sestavina.setHranilneNajdene(false);
                        return new HranilneVrednostiDto.SestavinaHranilneVrednosti(
                                naziv, kolicina, 0.0, 0.0, 0.0, 0.0, false);
                    }
                }, executor));
            }
        }

        // Wait for all to complete
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        double skupneKalorije = 0.0;
        double skupneMascobe = 0.0;
        double skupniProteini = 0.0;
        double skupniOgljikiviHidrati = 0.0;

        for (CompletableFuture<HranilneVrednostiDto.SestavinaHranilneVrednosti> future : futures) {
            try {
                HranilneVrednostiDto.SestavinaHranilneVrednosti result = future.get();
                sestavineList.add(result);
                skupneKalorije += (result.getKalorije() != null ? result.getKalorije() : 0.0);
                skupneMascobe += (result.getMascobe() != null ? result.getMascobe() : 0.0);
                skupniProteini += (result.getProteini() != null ? result.getProteini() : 0.0);
                skupniOgljikiviHidrati += (result.getOgljikoviHidrati() != null ? result.getOgljikoviHidrati() : 0.0);
            } catch (Exception e) {
                log.severe("Napaka pri obdelavi sestavine: " + e.getMessage());
            }
        }

        // SHRANI RECEPT (in vse sestavine)
        receptiDao.save(recept);
        log.info("Hranilne vrednosti shranjene v bazo za recept: " + recept.getIme());

        // FIX: Preračunaj na porcijo glede na ORIGINALNO število porcij
        double kalorijeNaPorcijo = skupneKalorije / originalneSteviloPorcij;
        double mascobeNaPorcijo = skupneMascobe / originalneSteviloPorcij;
        double proteiniNaPorcijo = skupniProteini / originalneSteviloPorcij;
        double ogljikoviHidratiNaPorcijo = skupniOgljikiviHidrati / originalneSteviloPorcij;

        // Izračunaj skupne vrednosti za ZAHTEVANO število porcij
        double skupneKalorijeZahtevano = kalorijeNaPorcijo * zahtevanoSteviloPorcij;
        double skupneMascobeZahtevano = mascobeNaPorcijo * zahtevanoSteviloPorcij;
        double skupniProteiniZahtevano = proteiniNaPorcijo * zahtevanoSteviloPorcij;
        double skupniOgljikoviHidratiZahtevano = ogljikoviHidratiNaPorcijo * zahtevanoSteviloPorcij;

        HranilneVrednostiDto dto = new HranilneVrednostiDto();
        dto.setReceptId(receptId);
        dto.setReceptIme(recept.getIme());
        dto.setOriginalneSteviloPorcij(originalneSteviloPorcij);
        dto.setZahtevanoSteviloPorcij(zahtevanoSteviloPorcij);

        dto.setSkupneVrednosti(new HranilneVrednostiDto.SkupneVrednosti(
                Math.round(skupneKalorijeZahtevano * 10.0) / 10.0,
                Math.round(skupneMascobeZahtevano * 10.0) / 10.0,
                Math.round(skupniProteiniZahtevano * 10.0) / 10.0,
                Math.round(skupniOgljikoviHidratiZahtevano * 10.0) / 10.0));

        dto.setNaPorcijo(new HranilneVrednostiDto.NaPorcijoVrednosti(
                Math.round(kalorijeNaPorcijo * 10.0) / 10.0,
                Math.round(mascobeNaPorcijo * 10.0) / 10.0,
                Math.round(proteiniNaPorcijo * 10.0) / 10.0,
                Math.round(ogljikoviHidratiNaPorcijo * 10.0) / 10.0));

        dto.setSestavine(sestavineList);

        log.info("Izračun končan - Skupne kalorije (" + zahtevanoSteviloPorcij + " porcij): " + skupneKalorijeZahtevano
                + " kcal");

        return dto;
    }
}