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

  const [addSelectors, setAddSelectors] = useState({});

  useEffect(() => {
    const pridobiRecepte = async () => {
      try {
        const response = await api.get("/api/recepti");
        setRecepti(response.data);
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

  const getCurrentUserId = () => {
    try {
      const u = JSON.parse(localStorage.getItem("uporabnik"));
      if (u && (u.id || u.uporabnikId || u.userId)) {
        return u.id || u.uporabnikId || u.userId;
      }
    } catch (err) {
      // ignore parse errors
    }
    return null;
  };

  // show selector: fetch user's jedilniki and open selector for this recipe
  const openAddToJedilnikSelector = async (receptId) => {
    setAddSelectors(s => ({ ...s, [receptId]: { jedilniki: [], selectedJedId: '', loading: true } }));
    try {
      const userId = getCurrentUserId();
      let resp;
      if (userId) {
        try {
          resp = await api.get(`/api/jedilniki/uporabnik/${userId}`);
        } catch (err) {
          // fallback: fetch all and filter by owner id client-side
          const all = await api.get('/api/jedilniki');
          const filtered = (all.data || []).filter(j => j.uporabnik && Number(j.uporabnik.id) === Number(userId));
          const jeds = filtered || [];
          setAddSelectors(s => ({ ...s, [receptId]: { jedilniki: jeds, selectedJedId: jeds.length ? String(jeds[0].id) : '', loading: false } }));
          return;
        }
      } else {
        resp = await api.get('/api/jedilniki');
      }

      const jeds = resp.data || [];
      setAddSelectors(s => ({ ...s, [receptId]: { jedilniki: jeds, selectedJedId: jeds.length ? String(jeds[0].id) : '', loading: false } }));
    } catch (err) {
      console.error('Napaka pri pridobivanju jedilnikov:', err);
      const serverMsg = err?.response?.data || err?.message;
      alert('Napaka pri nalaganju tvojih jedilnikov: ' + serverMsg);
      setAddSelectors(s => ({ ...s, [receptId]: { jedilniki: [], selectedJedId: '', loading: false } }));
    }
  };

  const cancelAddSelector = (receptId) => setAddSelectors(s => { const ns = { ...s }; delete ns[receptId]; return ns; });

  const confirmAddToJedilnik = async (receptId) => {
    const sel = addSelectors[receptId];
    const selectedId = sel?.selectedJedId;
    const jedId = Number(selectedId);
    if (!selectedId || Number.isNaN(jedId)) {
      alert('Najprej izberi jedilnik');
      return;
    }
    try {
      const jed = (sel?.jedilniki || []).find(j => j.id === jedId);
      const payload = {
        receptId: receptId,
        datum: jed ? jed.datum : null,
        steviloOseb: jed ? jed.steviloOseb : null,
        alergenIds: []
      };
      const resp = await api.post(`/api/jedilniki/${jedId}/recepti`, payload, { headers: { 'Content-Type': 'application/json' } });
      if (resp.status === 200 || resp.status === 201) {
        alert('Recept uspešno dodan v jedilnik');
        window.dispatchEvent(new CustomEvent('jedilnik-changed', { detail: resp.data }));
        cancelAddSelector(receptId);
        return;
      }
    } catch (err) {
      const serverMsg = err?.response?.data || err?.message;
      alert('Napaka pri dodajanju: ' + JSON.stringify(serverMsg));
      // fallback
      try {
        const fallback = await api.post(`/api/jedilniki/${selectedId}/recepti/${receptId}`);
        if (fallback.status === 200 || fallback.status === 201) {
          alert('Recept uspešno dodan v jedilnik');
          window.dispatchEvent(new CustomEvent('jedilnik-changed', { detail: fallback.data }));
          cancelAddSelector(receptId);
          return;
        }
      } catch (err2) {
        alert('Napaka pri dodajanju recepta v jedilnik');
        cancelAddSelector(receptId);
        return;
      }
    }
  };

  const handleSearch = async () => {
    const idValue = searchId.trim();
    if (!idValue) return;

    const numId = parseInt(idValue);
    if (isNaN(numId)) {
      alert("Prosim vnesi veljavno številko.");
      return;
    }

    try {
      const resp = await api.get(`/api/recepti/${numId}`);
      setRecepti([resp.data]);
    } catch (err) {
      console.error("Napaka pri iskanju recepta:", err);
      if (err.response?.status === 404) {
        alert(`Recept z ID ${numId} ne obstaja.`);
      } else {
        alert("Napaka pri iskanju recepta.");
      }
    } finally {
      setSearchId("");
    }
  };

  const handleSearchByName = async () => {
    const q = searchName.trim();
    if (!q) return;
    try {
      const resp = await api.get("/api/recepti", { params: { ime: q } });
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
      const resp = await api.get(`/api/recepti`);
      setRecepti(resp.data || []);
      setSearchId("");
    } catch (err) {
      console.error("Napaka pri nalaganju vseh receptov:", err);
    }
  };

  const izbrisiRecept = async (id) => {
    try {
      await api.delete(`/api/recepti/${id}`);
      setRecepti((prev) => prev.filter((recept) => recept.id !== id));
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
      const resp = await api.put(`/api/recepti/${editingId}`, payload);
      setRecepti((prev) =>
        prev.map((r) => (r.id === editingId ? resp.data : r))
      );
      setEditingId(null);
      setIme("");
      setOpis("");
      setSestavine([{ ime: "", kolicina: "" }]);
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
                >
                  Uredi
                </button>
                <button
                  className={styles.deleteButton}
                  onClick={() => izbrisiRecept(recept.id)}
                  type="button"
                >
                  Izbriši
                </button>
                {addSelectors[recept.id] ? (
                  <div className={styles.jedilnikSelector}>
                    <select
                      value={addSelectors[recept.id].selectedJedId || ''}
                      onChange={(e) => setAddSelectors(s => ({...s, [recept.id]: {...s[recept.id], selectedJedId: e.target.value}}))}
                    >
                      {(addSelectors[recept.id].jedilniki || []).map(j => (
                        <option key={j.id} value={String(j.id)}>{j.naziv} ({j.datum})</option>
                      ))}
                    </select>
                    <button type="button" onClick={() => confirmAddToJedilnik(recept.id)}>Potrdi</button>
                    <button type="button" className={styles.deleteButton} onClick={() => cancelAddSelector(recept.id)}>Prekliči</button>
                  </div>
                ) : (
                  <button type="button" onClick={() => openAddToJedilnikSelector(recept.id)}>Dodaj v jedilnik</button>
                )}
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
