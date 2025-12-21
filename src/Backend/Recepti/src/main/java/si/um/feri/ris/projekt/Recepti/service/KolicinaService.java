package si.um.feri. ris.projekt. Recepti. service;

import org.springframework.stereotype.Service;
import si.um.feri. ris.projekt.Recepti.vao.MerskaEnota;

import java.text.DecimalFormat;
import java.util.regex. Matcher;
import java.util. regex.Pattern;

/**
 * Service za pretvorbo in izračun količin sestavin.
 */
@Service
public class KolicinaService {

    private static final Pattern KOLICINA_PATTERN = Pattern. compile("([0-9]+[.,]?[0-9]*)\\s*([a-zA-ZščćžŠČĆŽ]+)?");
    private static final DecimalFormat FORMAT = new DecimalFormat("#.##");

    /**
     * DTO za razčlenjeno količino.
     */
    public static class ParsedKolicina {
        public double vrednost;
        public MerskaEnota enota;
        public String originalniTekst;
        public boolean imaEnoto; // NOVO: ali ima eksplicitno enoto

        public ParsedKolicina(double vrednost, MerskaEnota enota, String originalniTekst, boolean imaEnoto) {
            this.vrednost = vrednost;
            this.enota = enota;
            this.originalniTekst = originalniTekst;
            this.imaEnoto = imaEnoto;
        }
    }

    /**
     * Razčleni String količine na številko in enoto.
     * Primeri vhodov: "200 g", "1.5 kg", "2 kos", "500ml", "4" (brez enote)
     */
    public ParsedKolicina parseKolicina(String kolicinaStr) {
        if (kolicinaStr == null || kolicinaStr.isBlank()) {
            return new ParsedKolicina(0, MerskaEnota.NEZNANO, kolicinaStr, false);
        }

        Matcher matcher = KOLICINA_PATTERN.matcher(kolicinaStr. trim());

        if (matcher.find()) {
            try {
                String vrednostStr = matcher.group(1).replace(',', '.');
                double vrednost = Double.parseDouble(vrednostStr);

                String enotaStr = matcher.groupCount() > 1 ? matcher.group(2) : "";

                // NOVO: preveri ali ima eksplicitno enoto
                boolean imaEnoto = enotaStr != null && !enotaStr.isBlank();
                MerskaEnota enota = imaEnoto ? MerskaEnota.fromSimbol(enotaStr) : MerskaEnota.NEZNANO;

                return new ParsedKolicina(vrednost, enota, kolicinaStr, imaEnoto);
            } catch (NumberFormatException e) {
                return new ParsedKolicina(0, MerskaEnota. NEZNANO, kolicinaStr, false);
            }
        }

        return new ParsedKolicina(0, MerskaEnota.NEZNANO, kolicinaStr, false);
    }

    /**
     * Pretvori količino iz ene enote v drugo.
     */
    public double pretvoriEnoto(double vrednost, MerskaEnota izEnote, MerskaEnota vEnoto) {
        if (izEnote == MerskaEnota.NEZNANO || vEnoto == MerskaEnota.NEZNANO) {
            return vrednost;
        }

        if (! izEnote.jeKompatibilnaNa(vEnoto)) {
            throw new IllegalArgumentException(
                    String.format("Nezdružljivi enoti: %s in %s", izEnote. getSimbol(), vEnoto.getSimbol())
            );
        }

        // Pretvori v bazno enoto, nato v ciljno enoto
        double vBazniEnoti = vrednost * izEnote.getFaktorPretvorbe();
        return vBazniEnoti / vEnoto.getFaktorPretvorbe();
    }

    /**
     * Izračuna novo količino glede na spremembo števila porcij.
     *
     * @param originalaKolicina Originalna količina kot String (npr. "200 g", "4")
     * @param originalePorcije Originalno število porcij
     * @param novePorcije Novo število porcij
     * @return Preračunana količina kot String
     */
    public String izracunajNovoKolicino(String originalaKolicina, int originalePorcije, int novePorcije) {
        if (originalePorcije <= 0 || novePorcije <= 0) {
            return originalaKolicina;
        }

        ParsedKolicina parsed = parseKolicina(originalaKolicina);

        if (parsed.vrednost == 0) {
            return originalaKolicina; // Ne moremo preračunati - ni številke
        }

        double faktor = (double) novePorcije / originalePorcije;
        double novaVrednost = parsed.vrednost * faktor;

        // Če nima enote (npr. "4"), samo vrni število
        if (! parsed.imaEnoto || parsed.enota == MerskaEnota.NEZNANO) {
            return formatSamaStevilka(novaVrednost);
        }

        // Ima enoto - uporabi optimizacijo
        MerskaEnota optimiranaEnota = optimizirajEnoto(novaVrednost, parsed.enota);

        if (optimiranaEnota != parsed.enota) {
            novaVrednost = pretvoriEnoto(novaVrednost, parsed.enota, optimiranaEnota);
        }

        return formatKolicina(novaVrednost, optimiranaEnota);
    }

    /**
     * Optimizira enoto za lažjo berljivost (npr. 1000g -> 1kg, 0.5kg -> 500g).
     * DVOSMERNA optimizacija - deluje navzgor IN navzdol.
     */
    private MerskaEnota optimizirajEnoto(double vrednost, MerskaEnota trenutnaEnota) {
        // Masa:  g <-> kg (dvosmerna optimizacija)
        if (trenutnaEnota == MerskaEnota.GRAM) {
            if (vrednost >= 1000) {
                return MerskaEnota.KILOGRAM; // 1000g -> 1kg
            }
        } else if (trenutnaEnota == MerskaEnota.KILOGRAM) {
            if (vrednost < 1) {
                return MerskaEnota. GRAM; // 0.5kg -> 500g
            }
        }

        // Volumen: ml <-> dl <-> l (dvosmerna optimizacija)
        if (trenutnaEnota == MerskaEnota.MILILITER) {
            if (vrednost >= 1000) {
                return MerskaEnota. LITER; // 1000ml -> 1l
            } else if (vrednost >= 100) {
                return MerskaEnota.DECILITER; // 500ml -> 5dl
            }
        } else if (trenutnaEnota == MerskaEnota.DECILITER) {
            if (vrednost >= 10) {
                return MerskaEnota.LITER; // 15dl -> 1.5l
            } else if (vrednost < 1) {
                return MerskaEnota. MILILITER; // 0.5dl -> 50ml
            }
        } else if (trenutnaEnota == MerskaEnota.LITER) {
            if (vrednost < 0.1) {
                return MerskaEnota.MILILITER; // 0.05l -> 50ml
            } else if (vrednost < 1) {
                return MerskaEnota.DECILITER; // 0.5l -> 5dl
            }
        }

        return trenutnaEnota; // Obdrži trenutno enoto
    }

    /**
     * Formatira količino v String z enoto.
     */
    private String formatKolicina(double vrednost, MerskaEnota enota) {
        String formatiranVrednost = FORMAT.format(vrednost);

        if (enota == MerskaEnota.NEZNANO || enota. getSimbol().isEmpty()) {
            return formatiranVrednost;
        }

        return formatiranVrednost + " " + enota.getSimbol();
    }

    /**
     * Za kosovne količine (npr. jajca, krompir)
     */
    private String formatSamaStevilka(double vrednost) {
        // Če je celo število, ne prikaži decimalnih mest
        if (vrednost == Math.floor(vrednost)) {
            return String.valueOf((int) vrednost);
        }
        // Sicer uporabi format z do 2 decimalkama
        return FORMAT.format(vrednost);
    }
}