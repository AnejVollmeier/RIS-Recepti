import { useState } from "react";
import api from "../../server/server";
import styles from "./form.module.css";

function Form() {
  const [ime, setIme] = useState("");
  const [opis, setOpis] = useState("");
  const [sestavine, setSestavine] = useState([{ ime: "", kolicina: "" }]);

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

  const dodajRecept = async (e) => {
    e.preventDefault();
    const payload = {
      ime,
      opis,
      sestavine: sestavine.map((s) => ({ naziv: s.ime, kolicina: s.kolicina })),
    };
    console.log("Pošiljam payload:", payload);
    try {
      const result = await api.post("/api/recepti", payload);
      console.log("Odgovor:", result.data);
      try {
        window.dispatchEvent(
          new CustomEvent("recept-added", { detail: result.data })
        );
      } catch (e) {}
      setIme("");
      setOpis("");
      setSestavine([{ ime: "", kolicina: "" }]);
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
