import { useState, useEffect, useCallback } from "react";
import { Link } from "react-router-dom";
import api from "../server/server";
import pageStyles from "./Jedilnik.module.css";
import formStyles from "../components/Form/form.module.css";
import listStyles from "../components/List/list.module.css";

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

  const getOsebeLabel = (n) => {
    if (n === 1) return "oseba";
    if (n === 2) return "osebi";
    if (n === 3 || n === 4) return "osebe";
    return "oseb";
  };

  return (
    <div className={pageStyles.pageContainer}>
      <header className={pageStyles.header}>
        <h1 className={pageStyles.title}>Moji Jedilniki</h1>
        <p className={pageStyles.subtitle}>Načrtujte svoje obroke in organizirajte tedenski meni</p>
      </header>

      <div className={pageStyles.contentWrapper}>
        <aside className={pageStyles.sidebar}>
          <div className={pageStyles.formBox}>
            <h2 className={pageStyles.formTitle}>Nov Jedilnik</h2>
            <form onSubmit={handleCreate} className={formStyles.form}>
              <div className={formStyles.inputGroup}>
                <label>Naziv</label>
                <input 
                  className={formStyles.input}
                  value={naziv} 
                  onChange={(e) => setNaziv(e.target.value)} 
                  placeholder="npr. Tedenski meni"
                  required 
                />
              </div>
              <div className={formStyles.inputGroup}>
                <label>Datum</label>
                <input 
                  type="date" 
                  className={formStyles.input}
                  value={datum} 
                  onChange={(e) => setDatum(e.target.value)} 
                  required 
                />
              </div>
              <div className={formStyles.inputGroup}>
                <label>Število oseb</label>
                <input 
                  type="number" 
                  className={formStyles.input}
                  min={1} 
                  value={steviloOseb} 
                  onChange={(e) => setSteviloOseb(parseInt(e.target.value))} 
                  required 
                />
              </div>
              <button 
                type="submit" 
                className={`${formStyles.btn} ${formStyles.btnPrimary}`} 
                disabled={loading}
              >
                {loading ? "Ustvarjam..." : "Ustvari Jedilnik"}
              </button>
            </form>
          </div>
        </aside>

        <main className={pageStyles.mainContent}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 24 }}>
            <h2 style={{ margin: 0, fontWeight: 800 }}>Vsi jedilniki</h2>
            <button 
              onClick={fetchMyJedilniki} 
              className={`${listStyles.btn} ${listStyles.btnPrimary}`}
              style={{ padding: '8px 20px', fontSize: '0.8rem' }}
            >
              Osveži Seznam
            </button>
          </div>

          {jedilniki.length === 0 ? (
            <div className={pageStyles.emptyState}>
              <p>Ni najdenih jedilnikov. Ustvarite prvega v obrazcu na levi!</p>
            </div>
          ) : (
            <div className={listStyles.recipeGrid}>
              {jedilniki.map((j) => (
                <div key={j.id} className={pageStyles.jedilnikCard}>
                  <div className={pageStyles.jedilnikHeader}>
                    <div>
                      <h3 className={pageStyles.jedilnikTitle}>{j.naziv}</h3>
                      <p style={{ color: 'var(--text-secondary)', fontSize: '0.9rem', marginTop: 4 }}>
                        {new Date(j.datum).toLocaleDateString()}
                      </p>
                    </div>
                    <span className={pageStyles.jedilnikBadge}>
                      👥 {j.steviloOseb} {getOsebeLabel(j.steviloOseb)}
                    </span>
                  </div>

                  <div className={listStyles.ingredientsList} style={{ marginTop: 20 }}>
                    <h4 style={{ fontSize: '0.85rem', textTransform: 'uppercase', letterSpacing: '0.05em', color: 'var(--primary)', marginBottom: 12 }}>
                      Recepti v jedilniku
                    </h4>
                    {(!j.recepti || j.recepti.length === 0) ? (
                      <p style={{ fontStyle: 'italic', color: 'var(--text-muted)', fontSize: '0.9rem' }}>
                        Jedilnik je še prazen. Dodajte recepte na glavni strani.
                      </p>
                    ) : (
                      <ul className={pageStyles.recipeList}>
                        {j.recepti.map((r) => (
                          <li key={r.id} className={pageStyles.recipeItem}>
                            <Link to={`/#recept-${r.id}`} className={pageStyles.recipeName}>
                              {r.ime}
                            </Link>
                            <button
                              onClick={() => handleRemoveRecept(j.id, r.id)}
                              className={`${listStyles.btn} ${listStyles.btnDanger}`}
                              style={{ padding: '4px 10px', fontSize: '0.75rem', borderRadius: '8px' }}
                            >
                              Odstrani
                            </button>
                          </li>
                        ))}
                      </ul>
                    )}
                  </div>

                  <div className={listStyles.actions}>
                    <button 
                      onClick={() => handleDelete(j.id)} 
                      className={`${listStyles.btn} ${listStyles.btnDanger}`}
                      style={{ width: '100%' }}
                    >
                       Izbriši jedilnik
                    </button>
                  </div>
                </div>
              ))}
            </div>
          )}
        </main>
      </div>
    </div>
  );
}

export default Jedilnik;
