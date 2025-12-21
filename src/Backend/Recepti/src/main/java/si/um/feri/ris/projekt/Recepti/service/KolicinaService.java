package si. um.feri.ris. projekt.Recepti.service;

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

        public ParsedKolicina(double vrednost, MerskaEnota enota, String originalniTekst) {
            this.vrednost = vrednost;
            this.enota = enota;
            this.originalniTekst = originalniTekst;
        }
    }

    /**
     * Razčleni String količine na številko in enoto.
     * Primeri vhodov:  "200 g", "1.5 kg", "2 kos", "500ml"
     */
    public ParsedKolicina parseKolicina(String kolicinaStr) {
        if (kolicinaStr == null || kolicinaStr.isBlank()) {
            return new ParsedKolicina(0, MerskaEnota.NEZNANO, kolicinaStr);
        }

        Matcher matcher = KOLICINA_PATTERN.matcher(kolicinaStr. trim());

        if (matcher.find()) {
            try {
                String vrednostStr = matcher.group(1).replace(',', '.');
                double vrednost = Double.parseDouble(vrednostStr);

                String enotaStr = matcher.groupCount() > 1 ? matcher.group(2) : "";
                MerskaEnota enota = MerskaEnota.fromSimbol(enotaStr);

                return new ParsedKolicina(vrednost, enota, kolicinaStr);
            } catch (NumberFormatException e) {
                return new ParsedKolicina(0, MerskaEnota.NEZNANO, kolicinaStr);
            }
        }

        return new ParsedKolicina(0, MerskaEnota.NEZNANO, kolicinaStr);
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
                    String.format("Nezdružljivi enoti:  %s in %s", izEnote.getSimbol(), vEnoto.getSimbol())
            );
        }

        // Pretvori v bazno enoto, nato v ciljno enoto
        double vBazniEnoti = vrednost * izEnote.getFaktorPretvorbe();
        return vBazniEnoti / vEnoto.getFaktorPretvorbe();
    }

    /**
     * Izračuna novo količino glede na spremembo števila porcij.
     *
     * @param originalaKolicina Originalna količina kot String (npr. "200 g")
     * @param originalePorcije Originalno število porcij
     * @param novePorcije Novo število porcij
     * @return Preračunana količina kot String
     */
    public String izracunajNovoKolicino(String originalaKolicina, int originalePorcije, int novePorcije) {
        if (originalePorcije <= 0 || novePorcije <= 0) {
            return originalaKolicina;
        }

        ParsedKolicina parsed = parseKolicina(originalaKolicina);

        if (parsed.enota == MerskaEnota. NEZNANO || parsed.vrednost == 0) {
            return originalaKolicina; // Ne moremo preračunati
        }

        double faktor = (double) novePorcije / originalePorcije;
        double novaVrednost = parsed.vrednost * faktor;

        // Avtomatska pretvorba v večjo enoto, če je smiselno
        MerskaEnota optimiranaEnota = optimizirajEnoto(novaVrednost, parsed.enota);

        if (optimiranaEnota != parsed.enota) {
            novaVrednost = pretvoriEnoto(novaVrednost, parsed.enota, optimiranaEnota);
        }

        return formatKolicina(novaVrednost, optimiranaEnota);
    }

    /**
     * Optimira enoto za lažjo berljivost (npr. 1000g -> 1kg).
     */
    private MerskaEnota optimizirajEnoto(double vrednost, MerskaEnota trenutnaEnota) {
        // Masa:  g -> kg
        if (trenutnaEnota == MerskaEnota.GRAM && vrednost >= 1000) {
            return MerskaEnota.KILOGRAM;
        }

        // Volumen: ml -> dl -> l
        if (trenutnaEnota == MerskaEnota. MILILITER) {
            if (vrednost >= 1000) {
                return MerskaEnota.LITER;
            } else if (vrednost >= 100) {
                return MerskaEnota.DECILITER;
            }
        }

        if (trenutnaEnota == MerskaEnota.DECILITER && vrednost >= 10) {
            return MerskaEnota.LITER;
        }

        return trenutnaEnota;
    }

    /**
     * Formatira količino v String.
     */
    private String formatKolicina(double vrednost, MerskaEnota enota) {
        String formatiranVrednost = FORMAT.format(vrednost);

        if (enota == MerskaEnota.NEZNANO || enota. getSimbol().isEmpty()) {
            return formatiranVrednost;
        }

        return formatiranVrednost + " " + enota.getSimbol();
    }
}