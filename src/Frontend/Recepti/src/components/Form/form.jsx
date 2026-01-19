import { useState } from "react";
import api from "../../server/server";
import styles from "./form.module.css";

function Form() {
  const [ime, setIme] = useState("");
  const [opis, setOpis] = useState("");
  const [navodila, setNavodila] = useState("");
  const [sestavine, setSestavine] = useState([{ ime: "", kolicina: "" }]);
  const [steviloPorcij, setSteviloPorcij] = useState(1);

  const handleAddInput = (e) => {
    e?.preventDefault();
    setSestavine((prev) => [...prev, { ime: "", kolicina: "" }]);
  };

  const handleRemoveInput = (e) => {
    e?.preventDefault();
    setSestavine((prev) => (prev.length > 1 ? prev.slice(0, -1) : prev));
  };

  const handleSestavinaChange = (index, field, value) => {
    setSestavine((prev) =>
      prev.map((s, i) => (i === index ? { ...s, [field]: value } : s))
    );
  };

  const handlePorcijeChange = (value) => {
    const num = parseInt(value);
    if (!isNaN(num) && num >= 1 && num <= 20) {
      setSteviloPorcij(num);
    } else if (value === "") {
      setSteviloPorcij("");
    }
  };

  const dodajRecept = async (e) => {
    e.preventDefault();

    if (!steviloPorcij || steviloPorcij < 1 || steviloPorcij > 20) {
      alert("Prosim vnesi število porcij med 1 in 20");
      return;
    }

    const payload = {
      ime,
      opis,
      navodila,
      steviloPorcij,
      sestavine: sestavine.map((s) => ({ naziv: s.ime, kolicina: s.kolicina })),
    };
    try {
      const result = await api.post("/api/recepti", payload);
      try {
        window.dispatchEvent(
          new CustomEvent("recept-added", { detail: result.data })
        );
      } catch {
        // ignore dispatch errors
      }
      setIme("");
      setOpis("");
      setNavodila("");
      setSestavine([{ ime: "", kolicina: "" }]);
      setSteviloPorcij(1);
    } catch (err) {
      console.error("Napaka pri pošiljanju:", err);
    }
  };

  return (
    <div className={styles.formContainer}>
      <h2 className={styles.title}>Novi Recept</h2>
      <form onSubmit={dodajRecept}>
        <div className={styles.inputGroup}>
          <label>Ime recepta</label>
          <input
            className={styles.input}
            type="text"
            value={ime}
            onChange={(e) => setIme(e.target.value)}
            placeholder="npr. Goveji golaž"
            required
          />
        </div>
        
        <div className={styles.inputGroup}>
          <label>Kratek opis</label>
          <textarea
            className={`${styles.input} ${styles.textarea}`}
            style={{ minHeight: "80px" }}
            value={opis}
            onChange={(e) => setOpis(e.target.value)}
            placeholder="Kratek povzetek recepta..."
            required
          ></textarea>
        </div>

        <div className={styles.inputGroup}>
          <label>Postopek priprave</label>
          <textarea
            className={`${styles.input} ${styles.textarea}`}
            style={{ minHeight: "150px" }}
            value={navodila}
            onChange={(e) => setNavodila(e.target.value)}
            placeholder="Napišite korake priprave..."
            required
          ></textarea>
        </div>

        <div className={styles.inputGroup}>
          <label>
            Število porcij <span className={styles.portionBadge}>{steviloPorcij}</span>
          </label>
          <div style={{ display: "flex", gap: "12px", alignItems: "center" }}>
            <input
              type="range"
              min="1"
              max="20"
              value={steviloPorcij}
              onChange={(e) => handlePorcijeChange(e.target.value)}
              style={{ flex: 1, accentColor: "hsl(208, 73%, 54%)" }}
            />
            <input
              className={styles.input}
              type="number"
              min="1"
              max="20"
              value={steviloPorcij}
              onChange={(e) => handlePorcijeChange(e.target.value)}
              style={{ width: "70px", textAlign: "center" }}
            />
          </div>
        </div>

        <h3 className={styles.sectionTitle}>Sestavine</h3>
        {sestavine.map((s, idx) => (
          <div key={idx} className={styles.sestavinaRow}>
            <input
              className={styles.input}
              type="text"
              placeholder="Sestavina"
              value={s.ime}
              onChange={(e) => handleSestavinaChange(idx, "ime", e.target.value)}
              required
            />
            <input
              className={styles.input}
              type="text"
              placeholder="Kol."
              value={s.kolicina}
              onChange={(e) => handleSestavinaChange(idx, "kolicina", e.target.value)}
              style={{ width: "100px" }}
              required
            />
          </div>
        ))}

        <div className={styles.btnGroup}>
          <button 
            type="button" 
            className={`${styles.btn} ${styles.btnSecondary}`}
            onClick={handleAddInput}
          >
            + Dodaj
          </button>
          {sestavine.length > 1 && (
            <button
              type="button"
              className={`${styles.btn} ${styles.btnDanger}`}
              onClick={handleRemoveInput}
            >
              Odstrani
            </button>
          )}
        </div>

        <button type="submit" className={`${styles.btn} ${styles.btnPrimary}`}>
          Objavi Recept
        </button>
      </form>
    </div>
  );
}

export default Form;
