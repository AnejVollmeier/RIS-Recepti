package si.um. feri.ris.projekt. Recepti.vao;

/**
 * Enum za merske enote sestavin.
 * Vsaka enota ima bazno enoto in faktor za pretvorbo.
 */
public enum MerskaEnota {
    // Masa
    GRAM("g", "g", 1.0),
    KILOGRAM("kg", "g", 1000.0),
    MILIGRAM("mg", "g", 0.001),

    // Volumen
    MILILITER("ml", "ml", 1.0),
    DECILITER("dl", "ml", 100.0),
    LITER("l", "ml", 1000.0),

    // Kosovne enote
    KOS("kos", "kos", 1.0),
    SKODELICA("skodelica", "ml", 240.0),
    ZLICA("žlica", "ml", 15.0),
    ZLICKA("žlička", "ml", 5.0),

    // Posebne enote
    CSEMZ("csemz", "g", 5.0), // čajna žlička ~5g
    PEST("pest", "g", 10.0), // približno
    ŠČEP("ščep", "g", 1.0),

    // Default
    NEZNANO("", "", 1.0);

    private final String simbol;
    private final String baznaEnota;
    private final double faktorPretvorbe;

    MerskaEnota(String simbol, String baznaEnota, double faktorPretvorbe) {
        this.simbol = simbol;
        this. baznaEnota = baznaEnota;
        this.faktorPretvorbe = faktorPretvorbe;
    }

    public String getSimbol() {
        return simbol;
    }

    public String getBaznaEnota() {
        return baznaEnota;
    }

    public double getFaktorPretvorbe() {
        return faktorPretvorbe;
    }

    /**
     * Poišče mersko enoto po simbolu (case-insensitive).
     */
    public static MerskaEnota fromSimbol(String simbol) {
        if (simbol == null || simbol.isBlank()) {
            return NEZNANO;
        }

        String normalized = simbol.trim().toLowerCase();

        for (MerskaEnota enota : values()) {
            if (enota.getSimbol().equalsIgnoreCase(normalized)) {
                return enota;
            }
        }

        // Poskusi še alternatine oznake
        return switch (normalized) {
            case "kilogram", "kilograma" -> KILOGRAM;
            case "gram", "grama" -> GRAM;
            case "liter", "litra" -> LITER;
            case "ml.", "mililitre", "mililitri" -> MILILITER;
            case "dl.", "deciliter" -> DECILITER;
            case "kosa", "kosi" -> KOS;
            case "žlice" -> ZLICA;
            case "žličke", "zlicke" -> ZLICKA;
            default -> NEZNANO;
        };
    }

    /**
     * Preveri, ali sta dve enoti kompatibilni za pretvorbo.
     */
    public boolean jeKompatibilnaNa(MerskaEnota druga) {
        if (this == NEZNANO || druga == NEZNANO) {
            return false;
        }
        return this.baznaEnota.equals(druga.baznaEnota);
    }
}