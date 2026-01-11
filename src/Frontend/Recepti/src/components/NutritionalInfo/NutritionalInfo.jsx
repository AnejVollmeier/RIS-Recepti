import { useState, useEffect } from "react";
import api from "../../server/server";
import styles from "./NutritionalInfo.module.css";

function NutritionalInfo({ receptId, defaultPorcije = 1 }) {
    const [hranilneVrednosti, setHranilneVrednosti] = useState(null);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);
    const [pokaziSestavine, setPokaziSestavine] = useState(false);

    const DNEVNI_VNOS_KALORIJ = 2500;

    useEffect(() => {
        if (receptId) {
            fetchHranilneVrednosti(defaultPorcije);
        }
    }, [receptId, defaultPorcije]);

    const fetchHranilneVrednosti = async (steviloPorcij) => {
        setLoading(true);
        setError(null);
        try {
            const response = await api.get(
                `/api/recepti/${receptId}/hranilne-vrednosti?porcije=${steviloPorcij}`
            );
            setHranilneVrednosti(response.data);
        } catch (err) {
            console.error("Napaka pri pridobivanju hranilnih vrednosti:", err);
            setError("Ni mogoče pridobiti hranilnih vrednosti");
        } finally {
            setLoading(false);
        }
    };

    const izracunajProcent = (vrednost, dnevniVnos) => {
        if (! vrednost || !dnevniVnos) return 0;
        return ((vrednost / dnevniVnos) * 100).toFixed(0);
    };

    if (loading) return <div className={styles.loading}>Nalaganje... </div>;
    if (error) return <div className={styles.error}>{error}</div>;
    if (!hranilneVrednosti) return null;

    const procentKalorijSkupaj = izracunajProcent(
        hranilneVrednosti.skupneVrednosti?.kalorije,
        DNEVNI_VNOS_KALORIJ
    );

    return (
        <div className={styles.container}>
            <h3 className={styles.title}>Hranilne vrednosti</h3>

            {/* Prikaz % dnevnega vnosa */}
            <div className={styles.dnevniVnos}>
                Skupaj: {hranilneVrednosti.skupneVrednosti?.kalorije?. toFixed(1) || 0} kcal
                ({procentKalorijSkupaj}% dnevnega vnosa)
            </div>

            {/* Vrednosti na porcijo */}
            <div className={styles.section}>
                <h4>Na 1 porcijo</h4>
                <div className={styles.grid}>
                    <div className={styles.item}>
                        <span className={styles.label}>Kalorije:</span>
                        <span className={styles.value}>
                            {hranilneVrednosti.naPorcijo?.kalorije?.toFixed(1) || 0} kcal
                        </span>
                    </div>
                    <div className={styles.item}>
                        <span className={styles.label}>Maščobe:</span>
                        <span className={styles.value}>
                            {hranilneVrednosti.naPorcijo?.mascobe?.toFixed(1) || 0} g
                        </span>
                    </div>
                    <div className={styles.item}>
                        <span className={styles.label}>Proteini:</span>
                        <span className={styles.value}>
                            {hranilneVrednosti.naPorcijo?.proteini?.toFixed(1) || 0} g
                        </span>
                    </div>
                    <div className={styles.item}>
                        <span className={styles. label}>Ogljikovi hidrati:</span>
                        <span className={styles.value}>
                            {hranilneVrednosti.naPorcijo?.ogljikoviHidrati?.toFixed(1) || 0} g
                        </span>
                    </div>
                </div>
            </div>

            {/* Skupne vrednosti */}
            <div className={styles.section}>
                <h4>Skupaj (za {defaultPorcije} {defaultPorcije === 1 ?  "porcijo" : defaultPorcije === 2 ? "porciji" : "porcij"})</h4>
                <div className={styles.grid}>
                    <div className={styles.item}>
                        <span className={styles.label}>Kalorije:</span>
                        <span className={styles.value}>
                            {hranilneVrednosti.skupneVrednosti?.kalorije?. toFixed(1) || 0} kcal
                        </span>
                    </div>
                    <div className={styles.item}>
                        <span className={styles.label}>Maščobe:</span>
                        <span className={styles.value}>
                            {hranilneVrednosti.skupneVrednosti?.mascobe?.toFixed(1) || 0} g
                        </span>
                    </div>
                    <div className={styles.item}>
                        <span className={styles.label}>Proteini:</span>
                        <span className={styles.value}>
                            {hranilneVrednosti.skupneVrednosti?.proteini?.toFixed(1) || 0} g
                        </span>
                    </div>
                    <div className={styles.item}>
                        <span className={styles.label}>Ogljikovi hidrati:</span>
                        <span className={styles.value}>
                            {hranilneVrednosti.skupneVrednosti?.ogljikoviHidrati?.toFixed(1) || 0} g
                        </span>
                    </div>
                </div>
            </div>

            {/* Gumb za prikaz sestavin */}
            <button
                className={styles.toggleButton}
                onClick={() => setPokaziSestavine(!pokaziSestavine)}
            >
                {pokaziSestavine ? "Skrij" : "Prikaži"} podrobnosti po sestavinah
            </button>

            {/* Seznam sestavin */}
            {pokaziSestavine && (
                <div className={styles.sestavineList}>
                    <h4>Hranilne vrednosti po sestavinah:</h4>
                    {hranilneVrednosti.sestavine?.map((sestavina, index) => (
                        <div
                            key={index}
                            className={`${styles.sestavinaCard} ${
                                ! sestavina.najdeno ? styles. niNajdeno : ""
                            }`}
                        >
                            <div className={styles.sestavinaHeader}>
                                <strong>{sestavina.naziv}</strong>
                                <span className={styles.kolicina}>({sestavina.kolicina})</span>
                            </div>
                            {sestavina.najdeno ?  (
                                <div className={styles.sestavinaGrid}>
                                    <span>Kalorije:  {sestavina.kalorije?.toFixed(1) || 0} kcal</span>
                                    <span>Maščobe: {sestavina.mascobe?.toFixed(1) || 0} g</span>
                                    <span>Proteini: {sestavina.proteini?.toFixed(1) || 0} g</span>
                                    <span>Ogljikovi hidrati: {sestavina.ogljikoviHidrati?.toFixed(1) || 0} g</span>
                                </div>
                            ) : (
                                <p className={styles.niNajdenoText}>
                                    Podatki niso na voljo
                                </p>
                            )}
                        </div>
                    ))}
                </div>
            )}
        </div>
    );
}

export default NutritionalInfo;