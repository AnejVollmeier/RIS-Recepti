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

            {/* Macro Summary Dashboard */}
            <div className={styles.macroDashboard}>
                <div className={`${styles.macroCard} ${styles.calories}`}>
                    <div className={styles.macroValue}>
                        {hranilneVrednosti.naPorcijo?.kalorije?.toFixed(0) || 0}
                    </div>
                    <div className={styles.macroLabel}>kcal / porcijo</div>
                </div>
                <div className={`${styles.macroCard} ${styles.proteins}`}>
                    <div className={styles.macroValue}>
                        {hranilneVrednosti.naPorcijo?.proteini?.toFixed(1) || 0}g
                    </div>
                    <div className={styles.macroLabel}>Proteini</div>
                </div>
                <div className={`${styles.macroCard} ${styles.carbs}`}>
                    <div className={styles.macroValue}>
                        {hranilneVrednosti.naPorcijo?.ogljikoviHidrati?.toFixed(1) || 0}g
                    </div>
                    <div className={styles.macroLabel}>OH</div>
                </div>
                <div className={`${styles.macroCard} ${styles.fat}`}>
                    <div className={styles.macroValue}>
                        {hranilneVrednosti.naPorcijo?.mascobe?.toFixed(1) || 0}g
                    </div>
                    <div className={styles.macroLabel}>Maščobe</div>
                </div>
            </div>

            {/* Daily Intake Progress Bar */}
            <div className={styles.dailyIntakeContainer}>
                <div className={styles.dailyIntakeHeader}>
                    <span>Dnevni vnos (2500 kcal)</span>
                    <span className={styles.percentageText}>{procentKalorijSkupaj}%</span>
                </div>
                <div className={styles.progressBarBg}>
                    <div 
                        className={styles.progressBarFill} 
                        style={{ width: `${Math.min(procentKalorijSkupaj, 100)}%` }}
                    ></div>
                </div>
                <p className={styles.helperText}>
                    Ta obrok predstavlja {procentKalorijSkupaj}% priporočenega dnevnega vnosa kalorij.
                </p>
            </div>

            {/* Toggle Button */}
            <button
                className={styles.toggleButton}
                onClick={() => setPokaziSestavine(!pokaziSestavine)}
            >
                {pokaziSestavine ? "Skrij podrobnosti po sestavinah" : "Prikaži podrobnosti po sestavinah"}
            </button>

            {/* Detailed view */}
            {pokaziSestavine && (
                <div className={styles.sestavineList}>
                    <h4 className={styles.sectionTitle}>Podrobna analiza (za {defaultPorcije} {defaultPorcije === 1 ? "porcijo" : "porcij"})</h4>
                    <div className={styles.sestavineGrid}>
                        {hranilneVrednosti.sestavine?.map((sestavina, index) => (
                            <div
                                key={index}
                                className={`${styles.sestavinaBox} ${!sestavina.najdeno ? styles.notFound : ""}`}
                            >
                                <div className={styles.sestavinaHeader}>
                                    <span className={styles.sestavinaIme}>{sestavina.naziv}</span>
                                    <span className={styles.sestavinaKolicina}>{sestavina.kolicina}</span>
                                </div>
                                {sestavina.najdeno ? (
                                    <div className={styles.sestavinaMacros}>
                                        <div className={styles.miniMacro}>
                                            <span className={styles.miniLabel}>Kal:</span>
                                            <span className={styles.miniValue}>{sestavina.kalorije?.toFixed(0)}</span>
                                        </div>
                                        <div className={styles.miniMacro}>
                                            <span className={styles.miniLabel}>P:</span>
                                            <span className={styles.miniValue}>{sestavina.proteini?.toFixed(1)}g</span>
                                        </div>
                                        <div className={styles.miniMacro}>
                                            <span className={styles.miniLabel}>OH:</span>
                                            <span className={styles.miniValue}>{sestavina.ogljikoviHidrati?.toFixed(1)}g</span>
                                        </div>
                                        <div className={styles.miniMacro}>
                                            <span className={styles.miniLabel}>M:</span>
                                            <span className={styles.miniValue}>{sestavina.mascobe?.toFixed(1)}g</span>
                                        </div>
                                    </div>
                                ) : (
                                    <div className={styles.noDataText}>Podatki niso na voljo</div>
                                )}
                            </div>
                        ))}
                    </div>
                </div>
            )}
        </div>
    );
}

export default NutritionalInfo;