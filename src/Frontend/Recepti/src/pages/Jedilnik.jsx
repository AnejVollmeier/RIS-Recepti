import { useState, useEffect, useCallback } from "react";
import api from "../server/server";
import styles from "../components/List/list.module.css";

function Jedilnik() {
  const [jedilniki, setJedilniki] = useState([]);
  const [naziv, setNaziv] = useState("");
  const [datum, setDatum] = useState("");
  const [steviloOseb, setSteviloOseb] = useState(1);
  const [loading, setLoading] = useState(false);

  const resolveUserId = useCallback(async () => {
    try {
      const u = JSON.parse(localStorage.getItem("uporabnik"));
      if (!u) return null;
      if (u.id || u.uporabnikId || u.userId) return u.id || u.uporabnikId || u.userId;
      const username = u.uporabniskoIme || u.username || u.email;
      if (username) {
        try {
          const resp = await api.get(`/api/uporabniki/uporabniskoIme/${encodeURIComponent(username)}`);
          return resp.data?.id || null;
        } catch (err) {
          console.debug('Error resolving user by username', err);
          return null;
        }
      }
    } catch (err) {
      console.debug('resolveUserId parse error', err);
    }
    return null;
  }, []);

  const fetchMyJedilniki = useCallback(async () => {
    try {
      const userId = await resolveUserId();
      let resp;
      if (userId) {
        resp = await api.get(`/api/jedilniki/uporabnik/${userId}`);
      } else {
        resp = await api.get("/api/jedilniki");
      }
      setJedilniki(resp.data || []);
    } catch (err) {
      console.error("Napaka pri nalaganju jedilnikov:", err);
      setJedilniki([]);
    }
  }, [resolveUserId]);

  useEffect(() => {
    fetchMyJedilniki();
    const onJedilnikChanged = () => {
      fetchMyJedilniki();
    };
    window.addEventListener("jedilnik-changed", onJedilnikChanged);
    return () => window.removeEventListener("jedilnik-changed", onJedilnikChanged);
  }, [fetchMyJedilniki]);

  const handleCreate = async (e) => {
    e.preventDefault();
    setLoading(true);
    try {
      const payload = { naziv, datum, steviloOseb };
      const userId = await resolveUserId();
      if (userId) payload.uporabnikId = userId;

      const resp = await api.post("/api/jedilniki", payload);
      setJedilniki((prev) => [...prev, resp.data]);
      setNaziv("");
      setDatum("");
      setSteviloOseb(1);
    } catch (err) {
      console.error("Napaka pri ustvarjanju jedilnika:", err);
      alert(err?.response?.data || "Napaka pri ustvarjanju jedilnika");
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async (id) => {
    if (!confirm("Ali res želite izbrisati ta jedilnik?")) return;
    try {
      await api.delete(`/api/jedilniki/${id}`);
      setJedilniki((prev) => prev.filter((j) => j.id !== id));
    } catch (err) {
      console.error("Napaka pri brisanju jedilnika:", err);
      alert("Neuspešno brisanje jedilnika");
    }
  };

  const handleRemoveRecept = async (jedilnikId, receptId) => {
    try {
      const resp = await api.delete(`/api/jedilniki/${jedilnikId}/recepti/${receptId}`);
      if (resp?.data) {
        setJedilniki((prev) => prev.map((j) => (j.id === jedilnikId ? resp.data : j)));
      } else {
        fetchMyJedilniki();
      }
    } catch (err) {
      console.error("Napaka pri odstranjevanju recepta:", err);
      alert(err?.response?.data || "Napaka pri odstranjevanju recepta");
    }
  };

  return (
    <div className={styles.listContainer}>
      <h1 className="page-title">Moji jedilniki</h1>

      <button onClick={fetchMyJedilniki} style={{ marginBottom: 16, background: '#3498db', color: '#fff', border: 'none', padding: '8px 16px', borderRadius: 4, cursor: 'pointer' }}>
        Osveži seznam
      </button>

      <section className={styles.updateContainer} style={{ marginBottom: 24 }}>
        <h2>Ustvari nov jedilnik</h2>
        <div className={styles.onlyUpdateContainer}>
          <form onSubmit={handleCreate}>
            <div className={styles.updateInputGroup}>
              <label>Naziv: </label>
              <input value={naziv} onChange={(e) => setNaziv(e.target.value)} required />
            </div>
            <div className={styles.updateInputGroup}>
              <label>Datum: </label>
              <input type="date" value={datum} onChange={(e) => setDatum(e.target.value)} required />
            </div>
            <div className={styles.updateInputGroup}>
              <label>Število oseb: </label>
              <input type="number" min={1} value={steviloOseb} onChange={(e) => setSteviloOseb(parseInt(e.target.value))} required />
            </div>
            <button type="submit" disabled={loading}>{loading ? "Ustvarjam..." : "Ustvari"}</button>
          </form>
        </div>
      </section>

      <section className={styles.onlyListContainer}>
        <h2>Seznam jedilnikov</h2>
        {jedilniki.length === 0 && <p>Ni najdenih jedilnikov.</p>}
        {jedilniki.map((j) => (
          <div key={j.id} className={styles.receptCard}>
            <h3>{j.naziv}</h3>
            <p>Datum: {j.datum}</p>
            <p>Število oseb: {j.steviloOseb}</p>
            <div>
              <strong>Recepti v jedilniku:</strong>
              {(!j.recepti || j.recepti.length === 0) ? (
                <p style={{ fontStyle: 'italic', color: '#888' }}>Ni receptov. Dodaj recepte na strani Recepti.</p>
              ) : (
                <ul>
                  {j.recepti.map((r) => (
                    <li key={r.id}>
                      {r.ime}
                      <button
                        onClick={() => handleRemoveRecept(j.id, r.id)}
                        style={{ marginLeft: 8, background: '#e74c3c', color: '#fff', border: 'none', borderRadius: 4, padding: '2px 8px', cursor: 'pointer' }}
                      >
                        Odstrani
                      </button>
                    </li>
                  ))}
                </ul>
              )}
            </div>

            <div className={styles.buttonGroup} style={{ marginTop: 8 }}>
              <button onClick={() => handleDelete(j.id)} style={{ background: '#c0392b', color: '#fff' }}>Izbriši jedilnik</button>
            </div>
          </div>
        ))}
      </section>
    </div>
  );
}

export default Jedilnik;
