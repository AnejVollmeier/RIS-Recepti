package si.um.feri. ris.projekt.Recepti. rest.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HranilneVrednostiDto {

    private Integer receptId;
    private String receptIme;
    private Integer originalneSteviloPorcij;
    private Integer zahtevanoSteviloPorcij;

    private SkupneVrednosti skupneVrednosti;
    private NaPorcijoVrednosti naPorcijo;
    private List<SestavinaHranilneVrednosti> sestavine;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SkupneVrednosti {
        private Double kalorije;
        private Double mascobe;
        private Double proteini;
        private Double ogljikoviHidrati;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NaPorcijoVrednosti {
        private Double kalorije;
        private Double mascobe;
        private Double proteini;
        private Double ogljikoviHidrati;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SestavinaHranilneVrednosti {
        private String naziv;
        private String kolicina;
        private Double kalorije;
        private Double mascobe;
        private Double proteini;
        private Double ogljikoviHidrati;
        private Boolean najdeno;
    }
}