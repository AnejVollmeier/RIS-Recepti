import { useState, useEffect } from "react";
import api from "../../server/server";
import styles from "./list.module.css";

function List() {
  const [recepti, setRecepti] = useState([]);
  const [searchId, setSearchId] = useState("");
  const [editingId, setEditingId] = useState(null);
  const [ime, setIme] = useState("");
  const [opis, setOpis] = useState("");
  const [sestavine, setSestavine] = useState([{ ime: "", kolicina: "" }]);
  const [searchName, setSearchName] = useState("");

  useEffect(() => {
    const pridobiRecepte = async () => {
      try {
        const response = await api.get("/recepti");
        setRecepti(response.data);
        console.log("Pridobljeni recepti:", response.data);
      } catch (error) {
        console.error("Napaka pri pridobivanju receptov:", error);
      }
    };
    pridobiRecepte();
    const onAdded = (e) => {
      if (e?.detail) {
        setRecepti((prev) => [...prev, e.detail]);
      }
    };
    window.addEventListener("recept-added", onAdded);
    return () => window.removeEventListener("recept-added", onAdded);
  }, []);

  const handleSearch = async () => {
    if (!searchId) return;
    try {
      const resp = await api.get(`/recepti/${searchId}`);
      if (resp && resp.data) {
        setRecepti([resp.data]);
      } else {
        setRecepti([]);
      }
      setSearchId("");
    } catch (err) {
      console.error("Napaka pri iskanju recepta:", err);
      setRecepti([]);
      setSearchId("");
    }
  };

  const handleSearchByName = async () => {
    const q = searchName.trim();
    if (!q) return;
    try {
      const resp = await api.get("/recepti", { params: { ime: q } });
      setRecepti(resp.data || []);
    } catch (err) {
      console.error("Napaka pri iskanju po imenu:", err);
      setRecepti([]);
    } finally {
      setSearchName("");
    }
  };

  const handleShowAll = async () => {
    try {
      const resp = await api.get(`/recepti`);
      setRecepti(resp.data || []);
      setSearchId("");
    } catch (err) {
      console.error("Napaka pri nalaganju vseh receptov:", err);
    }
  };

  const izbrisiRecept = async (id) => {
    try {
      await api.delete(`/recepti/${id}`);
      setRecepti((prev) => prev.filter((recept) => recept.id !== id));
      console.log(`Recept z ID ${id} je bil izbrisan.`);
    } catch (error) {
      console.error("Napaka pri brisanju recepta:", error);
    }
  };

  const startEdit = (recept) => {
    setEditingId(recept.id);
    setIme(recept.ime || "");
    setOpis(recept.opis || "");
    const localSest = (recept.sestavine || []).map((s) => ({
      ime: s.naziv || "",
      kolicina: s.kolicina || "",
    }));
    setSestavine(localSest.length ? localSest : [{ ime: "", kolicina: "" }]);
    const el = document.querySelector(`.${styles.updateContainer}`);
    if (el) el.scrollIntoView({ behavior: "smooth" });
  };

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

  const handleUpdate = async (e) => {
    e.preventDefault();
    if (!editingId) return;
    const payload = {
      ime,
      opis,
      sestavine: sestavine.map((s) => ({ naziv: s.ime, kolicina: s.kolicina })),
    };
    try {
      const resp = await api.put(`/recepti/${editingId}`, payload);
      setRecepti((prev) =>
        prev.map((r) => (r.id === editingId ? resp.data : r))
      );
      setEditingId(null);
      setIme("");
      setOpis("");
      setSestavine([{ ime: "", kolicina: "" }]);
      console.log("Recept posodobljen:", resp.data);
    } catch (error) {
      console.error("Napaka pri posodabljanju recepta:", error);
    }
  };

  return (
    <>
      <div className={styles.listContainer}>
        <h1>Poišči specifičen recept</h1>
        <div className={styles.searchContainer}>
          <input
            type="number"
            value={searchId}
            onChange={(e) => setSearchId(e.target.value)}
            placeholder="Vnesi ID recepta"
          />
          <button type="button" onClick={handleSearch}>
            Išči
          </button>
          <input
            type="text"
            value={searchName}
            onChange={(e) => setSearchName(e.target.value)}
            placeholder="Vnesi ime recepta"
          />
          <button type="button" onClick={handleSearchByName}>
            Išči
          </button>

          <button type="button" onClick={handleShowAll}>
            Prikaži vse
          </button>
        </div>
        <div className={styles.onlyListContainer}>
          {recepti.map((recept) => (
            <div key={recept.id} className={styles.receptCard}>
              <h2>{recept.ime}</h2>
              <p>{recept.opis}</p>
              <h3>Sestavine:</h3>
              <ul>
                {recept.sestavine.map((sestavina, index) => (
                  <li key={index}>
                    {sestavina.naziv}: {sestavina.kolicina}
                  </li>
                ))}
              </ul>
              <div className={styles.buttonGroup}>
                <button
                  type="button"
                  onClick={() => startEdit(recept)}
                  style={{ color: "#fff" }}
                >
                  Uredi
                </button>
                <button
                  className={styles.deleteButton}
                  onClick={() => izbrisiRecept(recept.id)}
                  type="button"
                  style={{ color: "#fff" }}
                >
                  Izbriši
                </button>
              </div>
            </div>
          ))}
        </div>
      </div>
      <div className={styles.updateContainer}>
        <h1>Posodobi recept</h1>
        <div className={styles.onlyUpdateContainer}>
          <form onSubmit={handleUpdate}>
            <div className={styles.updateInputGroup}>
              <label>Ime recepta:</label>
              <input
                type="text"
                name="ime"
                value={ime}
                onChange={(e) => setIme(e.target.value)}
              />
            </div>
            <div className={styles.updateInputGroup}>
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
                    className={styles.updateSestavineInput}
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
              <div className={styles.updateButtonGroup}>
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
            <button type="submit" disabled={!editingId}>
              Posodobi recept
            </button>
          </form>
        </div>
      </div>
    </>
  );
}

export default List;
