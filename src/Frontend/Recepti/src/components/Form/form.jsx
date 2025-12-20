import { useState } from "react";
import api from "../../server/server";
import styles from "./form.module.css";

function Form() {
  const [ime, setIme] = useState("");
  const [opis, setOpis] = useState("");
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
      setSestavine([{ ime: "", kolicina: "" }]);
      setSteviloPorcij(1);
    } catch (err) {
      console.error("Napaka pri pošiljanju:", err);
    }
  };

  return (
    <>
      <div className={styles.formcontainer}>
        <h1>Dodaj recept</h1>
        <div className={styles.onlyformContainer}>
          <form onSubmit={dodajRecept}>
            <div className={styles.inputGroup}>
              <label>Ime recepta:</label>
              <input
                type="text"
                name="ime"
                value={ime}
                onChange={(e) => setIme(e.target.value)}
              />
            </div>
            <div className={styles.inputGroup}>
              <label>Opis:</label>
              <textarea
                name="opis"
                value={opis}
                onChange={(e) => setOpis(e.target.value)}
              ></textarea>
            </div>
            <div className={styles.inputGroup}>
              <label>Število porcij: {steviloPorcij}</label>
              <div
                style={{ display: "flex", gap: "10px", alignItems: "center" }}
              >
                <input
                  type="range"
                  min="1"
                  max="20"
                  value={steviloPorcij}
                  onChange={(e) => handlePorcijeChange(e.target.value)}
                  style={{
                    flex: 1,
                    accentColor: "#007bff",
                  }}
                />
                <input
                  type="number"
                  min="1"
                  max="20"
                  value={steviloPorcij}
                  onChange={(e) => handlePorcijeChange(e.target.value)}
                  style={{ width: "60px" }}
                />
              </div>
            </div>
            <label>Sestavine</label>
            <div>
              {sestavine.map((s, idx) => (
                <div key={idx} style={{ marginBottom: 8 }}>
                  <input
                    className={styles.sestavineInput}
                    type="text"
                    name={`sestavine-${idx}`}
                    placeholder="Ime sestavine"
                    value={s.ime}
                    onChange={(e) =>
                      handleSestavinaChange(idx, "ime", e.target.value)
                    }
                  />
                  <input
                    className={styles.sestavineInput}
                    type="text"
                    name={`kolicina-${idx}`}
                    placeholder="Količina"
                    value={s.kolicina}
                    onChange={(e) =>
                      handleSestavinaChange(idx, "kolicina", e.target.value)
                    }
                  />
                </div>
              ))}
              <div className={styles.buttonGroup}>
                <button type="button" onClick={handleAddInput}>
                  +
                </button>
                <button
                  className={styles.minusButton}
                  type="button"
                  onClick={handleRemoveInput}
                >
                  -
                </button>
              </div>
            </div>
            <button type="submit">Dodaj recept</button>
          </form>
        </div>
      </div>
    </>
  );
}

export default Form;
