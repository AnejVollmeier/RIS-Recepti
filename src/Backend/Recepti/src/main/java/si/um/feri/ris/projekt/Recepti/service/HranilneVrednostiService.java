package si.um.feri. ris.projekt.Recepti.service;

import org.springframework.beans.factory.annotation. Autowired;
import org.springframework.stereotype.Service;
import si.um.feri.ris.projekt.Recepti.dao.ReceptiJpaDao;
import si.um.feri.ris.projekt. Recepti.rest.dto.HranilneVrednostiDto;
import si.um.feri.ris.projekt. Recepti.rest.dto.NutritionData;
import si.um.feri.ris.projekt. Recepti.vao.Recepti;
import si.um.feri. ris.projekt.Recepti.vao. Sestavine;

import java.util. ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

@Service
public class HranilneVrednostiService {

    private static final Logger log = Logger.getLogger(HranilneVrednostiService. class.getName());

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

        List<HranilneVrednostiDto.SestavinaHranilneVrednosti> sestavineList = new ArrayList<>();

        double skupneKalorije = 0.0;
        double skupneMascobe = 0.0;
        double skupniProteini = 0.0;
        double skupniOgljikiviHidrati = 0.0;

        for (Sestavine sestavina : recept.getSestavine()) {
            String naziv = sestavina.getNaziv();
            String kolicina = sestavina.getKolicina();

            log.info("Obdelujem sestavino: " + naziv + " (" + kolicina + ")");

            NutritionData nutritionData = nutritionApiService.searchByName(naziv);

            if (nutritionData != null && nutritionData.isNajdeno()) {
                NutritionData calculated = nutritionApiService.calculateForQuantity(nutritionData, kolicina);

                // SHRANI V SESTAVINO
                sestavina.setKalorije(calculated.getKalorije());
                sestavina.setMascobe(calculated.getMascobe());
                sestavina.setProteini(calculated.getProteini());
                sestavina.setOgljikoviHidrati(calculated.getOgljikoviHidrati());
                sestavina.setHranilneNajdene(true);
                log.info("Shranjujem hranilne vrednosti za:  " + naziv);

                sestavineList.add(new HranilneVrednostiDto. SestavinaHranilneVrednosti(
                        naziv,
                        kolicina,
                        calculated.getKalorije(),
                        calculated.getMascobe(),
                        calculated.getProteini(),
                        calculated.getOgljikoviHidrati(),
                        true
                ));

                skupneKalorije += (calculated.getKalorije() != null ?  calculated.getKalorije() : 0.0);
                skupneMascobe += (calculated.getMascobe() != null ? calculated.getMascobe() : 0.0);
                skupniProteini += (calculated.getProteini() != null ? calculated.getProteini() : 0.0);
                skupniOgljikiviHidrati += (calculated.getOgljikoviHidrati() != null ? calculated.getOgljikoviHidrati() : 0.0);

            } else {
                log. warning("Hranilni podatki niso najdeni za:  " + naziv);

                sestavina.setHranilneNajdene(false);

                sestavineList.add(new HranilneVrednostiDto. SestavinaHranilneVrednosti(
                        naziv,
                        kolicina,
                        0.0,
                        0.0,
                        0.0,
                        0.0,
                        false
                ));
            }
        }

        // SHRANI RECEPT (in vse sestavine)
        receptiDao.save(recept);
        log.info("Hranilne vrednosti shranjene v bazo za recept: " + recept.getIme());

        double kalorijeNaPorcijo = skupneKalorije / zahtevanoSteviloPorcij;
        double mascobeNaPorcijo = skupneMascobe / zahtevanoSteviloPorcij;
        double proteiniNaPorcijo = skupniProteini / zahtevanoSteviloPorcij;
        double ogljikoviHidratiNaPorcijo = skupniOgljikiviHidrati / zahtevanoSteviloPorcij;

        HranilneVrednostiDto dto = new HranilneVrednostiDto();
        dto.setReceptId(receptId);
        dto.setReceptIme(recept.getIme());
        dto.setOriginalneSteviloPorcij(originalneSteviloPorcij);
        dto.setZahtevanoSteviloPorcij(zahtevanoSteviloPorcij);

        dto.setSkupneVrednosti(new HranilneVrednostiDto.SkupneVrednosti(
                Math.round(skupneKalorije * 10.0) / 10.0,
                Math.round(skupneMascobe * 10.0) / 10.0,
                Math.round(skupniProteini * 10.0) / 10.0,
                Math.round(skupniOgljikiviHidrati * 10.0) / 10.0
        ));

        dto.setNaPorcijo(new HranilneVrednostiDto.NaPorcijoVrednosti(
                Math.round(kalorijeNaPorcijo * 10.0) / 10.0,
                Math.round(mascobeNaPorcijo * 10.0) / 10.0,
                Math.round(proteiniNaPorcijo * 10.0) / 10.0,
                Math.round(ogljikoviHidratiNaPorcijo * 10.0) / 10.0
        ));

        dto.setSestavine(sestavineList);

        log.info("Izračun končan - Skupne kalorije: " + skupneKalorije + " kcal");

        return dto;
    }
}