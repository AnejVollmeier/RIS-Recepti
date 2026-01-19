import { useState, useEffect } from "react";
import { useLocation } from "react-router-dom";
import api from "../../server/server";
import styles from "./list.module.css";
import NutritionalInfo from "../../components/NutritionalInfo/NutritionalInfo";

function List() {
  const [recepti, setRecepti] = useState([]);
  const [searchId, setSearchId] = useState("");
  const location = useLocation();
  const [editingId, setEditingId] = useState(null);
  const [ime, setIme] = useState("");
  const [opis, setOpis] = useState("");
  const [navodila, setNavodila] = useState("");
  const [sestavine, setSestavine] = useState([{ ime: "", kolicina: "" }]);
  const [searchName, setSearchName] = useState("");

  const [addSelectors, setAddSelectors] = useState({});
  const [porcijePoReceptu, setPorcijePoReceptu] = useState({});
  const [showNutrition, setShowNutrition] = useState({});

  const [comments, setComments] = useState({});
  const [showComments, setShowComments] = useState({});
  const [newComment, setNewComment] = useState({});

  // State for editing comments
  const [editingCommentId, setEditingCommentId] = useState(null);
  const [editCommentText, setEditCommentText] = useState("");

  const [preracunaniRecepti, setPreracunaniRecepti] = useState({});
  const [loadingPorcije, setLoadingPorcije] = useState({});

  useEffect(() => {
    if (recepti.length > 0 && location.hash) {
      const element = document.getElementById(location.hash.substring(1));
      if (element) {
        setTimeout(() => {
          element.scrollIntoView({ behavior: "smooth", block: "center" });
          element.classList.add(styles.highlighted);
          setTimeout(() => {
            element.classList.remove(styles.highlighted);
          }, 3000);
        }, 100);
      }
    }
  }, [recepti, location.hash]);

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

  // show selector:  fetch user's jedilniki and open selector for this recipe
  const openAddToJedilnikSelector = async (receptId) => {
    setAddSelectors((s) => ({
      ...s,
      [receptId]: { jedilniki: [], selectedJedId: "", loading: true },
    }));
    try {
      const userId = getCurrentUserId();
      let resp;
      if (userId) {
        try {
          resp = await api.get(`/api/jedilniki/uporabnik/${userId}`);
        } catch (err) {
          // fallback:  fetch all and filter by owner id client-side
          const all = await api.get("/api/jedilniki");
          const filtered = (all.data || []).filter(
            (j) => j.uporabnik && Number(j.uporabnik.id) === Number(userId),
          );
          const jeds = filtered || [];
          setAddSelectors((s) => ({
            ...s,
            [receptId]: {
              jedilniki: jeds,
              selectedJedId: jeds.length ? String(jeds[0].id) : "",
              loading: false,
            },
          }));
          return;
        }
      } else {
        resp = await api.get("/api/jedilniki");
      }

      const jeds = resp.data || [];
      setAddSelectors((s) => ({
        ...s,
        [receptId]: {
          jedilniki: jeds,
          selectedJedId: jeds.length ? String(jeds[0].id) : "",
          loading: false,
        },
      }));
    } catch (err) {
      console.error("Napaka pri pridobivanju jedilnikov:", err);
      const serverMsg = err?.response?.data || err?.message;
      alert("Napaka pri nalaganju tvojih jedilnikov: " + serverMsg);
      setAddSelectors((s) => ({
        ...s,
        [receptId]: { jedilniki: [], selectedJedId: "", loading: false },
      }));
    }
  };

  const cancelAddSelector = (receptId) =>
    setAddSelectors((s) => {
      const ns = { ...s };
      delete ns[receptId];
      return ns;
    });

  const confirmAddToJedilnik = async (receptId) => {
    const sel = addSelectors[receptId];
    const selectedId = sel?.selectedJedId;
    const jedId = Number(selectedId);
    if (!selectedId || Number.isNaN(jedId)) {
      alert("Najprej izberi jedilnik");
      return;
    }
    try {
      const jed = (sel?.jedilniki || []).find((j) => j.id === jedId);
      const payload = {
        receptId: receptId,
        datum: jed ? jed.datum : null,
        steviloOseb: jed ? jed.steviloOseb : null,
        alergenIds: [],
      };
      const resp = await api.post(`/api/jedilniki/${jedId}/recepti`, payload, {
        headers: { "Content-Type": "application/json" },
      });
      if (resp.status === 200 || resp.status === 201) {
        alert("Recept uspešno dodan v jedilnik");
        window.dispatchEvent(
          new CustomEvent("jedilnik-changed", { detail: resp.data }),
        );
        cancelAddSelector(receptId);
        return;
      }
    } catch (err) {
      const serverMsg = err?.response?.data || err?.message;
      alert("Napaka pri dodajanju:  " + JSON.stringify(serverMsg));
      // fallback
      try {
        const fallback = await api.post(
          `/api/jedilniki/${selectedId}/recepti/${receptId}`,
        );
        if (fallback.status === 200 || fallback.status === 201) {
          alert("Recept uspešno dodan v jedilnik");
          window.dispatchEvent(
            new CustomEvent("jedilnik-changed", { detail: fallback.data }),
          );
          cancelAddSelector(receptId);
          return;
        }
      } catch (err2) {
        alert("Napaka pri dodajanju recepta v jedilnik");
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
    setNavodila(recept.navodila || "");
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
      prev.map((s, i) => (i === index ? { ...s, [field]: value } : s)),
    );
  };

  // Funkcija za preračun preko backend API
  const getPorcijeLabel = (n) => {
    if (n === 1) return "porcijo";
    if (n === 2) return "porciji";
    if (n === 3 || n === 4) return "porcije";
    return "porcij";
  };

  const handlePorcijeChange = async (receptId, value) => {
    const num = parseInt(value);
    if (isNaN(num) || num < 1 || num > 20) return;

    setPorcijePoReceptu((prev) => ({ ...prev, [receptId]: num }));

    // Najdi originalni recept
    const originalRecept = recepti.find((r) => r.id === receptId);
    if (!originalRecept) return;

    // Če je število porcij enako originalnemu, zbriši cache
    if (num === originalRecept.steviloPorcij) {
      setPreracunaniRecepti((prev) => {
        const updated = { ...prev };
        delete updated[`${receptId}-${num}`];
        return updated;
      });
      return;
    }

    // Pokliči backend API za preračun
    setLoadingPorcije((prev) => ({ ...prev, [receptId]: true }));
    try {
      const resp = await api.get(`/api/recepti/${receptId}/porcije/${num}`);
      const cacheKey = `${receptId}-${num}`;
      setPreracunaniRecepti((prev) => ({
        ...prev,
        [cacheKey]: resp.data,
      }));
    } catch (err) {
      console.error("Napaka pri preračunu porcij:", err);
      // Če backend ne deluje, uporabi client-side fallback
    } finally {
      setLoadingPorcije((prev) => ({ ...prev, [receptId]: false }));
    }
  };

  // Funkcija za pridobitev sestavin (backend ali fallback)
  const getSestavineZaPrikaz = (recept, trenutnePorcije) => {
    const cacheKey = `${recept.id}-${trenutnePorcije}`;
    const preracunan = preracunaniRecepti[cacheKey];

    // Če imamo backend odgovor, uporabi ga
    if (preracunan && preracunan.sestavine) {
      return preracunan.sestavine.map((s) => ({
        naziv: s.naziv,
        kolicina: s.kolicina,
        originalaKolicina: s.originalaKolicina,
      }));
    }

    // Fallback: client-side preračun
    const originalnePorcije = recept.steviloPorcij || 1;
    const faktor = trenutnePorcije / originalnePorcije;

    return (recept.sestavine || []).map((sestavina) => {
      const kolicina = sestavina.kolicina || "";
      const stevilka = parseFloat(kolicina);

      let prilagojena = kolicina;
      if (!isNaN(stevilka)) {
        const novaVrednost = (stevilka * faktor).toFixed(1);
        prilagojena = kolicina.replace(stevilka.toString(), novaVrednost);
      }

      return {
        naziv: sestavina.naziv,
        kolicina: prilagojena,
        originalaKolicina: kolicina,
      };
    });
  };

  const handleUpdate = async (e) => {
    e.preventDefault();
    if (!editingId) return;
    const payload = {
      ime,
      opis,
      navodila,
      sestavine: sestavine.map((s) => ({ naziv: s.ime, kolicina: s.kolicina })),
    };
    try {
      const resp = await api.put(`/api/recepti/${editingId}`, payload);
      setRecepti((prev) =>
        prev.map((r) => (r.id === editingId ? resp.data : r)),
      );
      setEditingId(null);
      setIme("");
      setOpis("");
      setNavodila("");
      setSestavine([{ ime: "", kolicina: "" }]);
    } catch (error) {
      console.error("Napaka pri posodabljanju recepta:", error);
    }
  };

  const toggleNutrition = (receptId) => {
    setShowNutrition((prev) => ({
      ...prev,
      [receptId]: !prev[receptId],
    }));
  };

  const toggleComments = async (receptId) => {
    if (showComments[receptId]) {
      setShowComments((prev) => ({ ...prev, [receptId]: false }));
    } else {
      try {
        const resp = await api.get(`/api/komentarji/recept/${receptId}`);
        setComments((prev) => ({ ...prev, [receptId]: resp.data }));
        setShowComments((prev) => ({ ...prev, [receptId]: true }));
      } catch (err) {
        console.error("Napaka pri nalaganju komentarjev:", err);
        alert("Napaka pri nalaganju komentarjev.");
      }
    }
  };

  const handleCommentChange = (receptId, val) => {
    setNewComment((prev) => ({ ...prev, [receptId]: val }));
  };

  const submitComment = async (receptId) => {
    const userId = getCurrentUserId();
    if (!userId) {
      alert("Za oddajo komentarja se prosim prijavite.");
      return;
    }
    const text = newComment[receptId];
    if (!text || !text.trim()) return;

    try {
      const payload = {
        besedilo: text,
        receptId: parseInt(receptId),
        uporabnikId: parseInt(userId),
      };
      const resp = await api.post("/api/komentarji", payload);
      // Prepend new comment
      setComments((prev) => ({
        ...prev,
        [receptId]: [resp.data, ...(prev[receptId] || [])],
      }));
      setNewComment((prev) => ({ ...prev, [receptId]: "" }));
    } catch (err) {
      console.error("Napaka pri oddaji komentarja:", err);
      alert("Napaka pri oddaji komentarja.");
    }
  };

  const startEditComment = (comment) => {
    setEditingCommentId(comment.id);
    setEditCommentText(comment.besedilo);
  };

  const cancelEditComment = () => {
    setEditingCommentId(null);
    setEditCommentText("");
  };

  const saveEditComment = async (commentId, receptId) => {
    if (!editCommentText.trim()) return;
    try {
      const payload = {
        besedilo: editCommentText,
      };
      const resp = await api.put(`/api/komentarji/${commentId}`, payload);

      setComments((prev) => {
        const specificComments = prev[receptId] || [];
        return {
          ...prev,
          [receptId]: specificComments.map((c) =>
            c.id === commentId ? resp.data : c,
          ),
        };
      });
      cancelEditComment();
    } catch (err) {
      console.error("Napaka pri urejanju komentarja:", err);
      alert("Napaka pri urejanju komentarja.");
    }
  };

  const deleteComment = async (commentId, receptId) => {
    if (!window.confirm("Res želite izbrisati ta komentar?")) return;
    try {
      await api.delete(`/api/komentarji/${commentId}`);
      setComments((prev) => {
        const specificComments = prev[receptId] || [];
        return {
          ...prev,
          [receptId]: specificComments.filter((c) => c.id !== commentId),
        };
      });
    } catch (err) {
      console.error("Napaka pri brisanju komentarja:", err);
      alert("Napaka pri brisanju komentarja.");
    }
  };

  const handleRate = async (receptId, rating) => {
    const userId = getCurrentUserId();
    if (!userId) {
      alert("Za ocenjevanje se morate prijaviti.");
      return;
    }

    const payload = {
      vrednost: rating,
      uporabnikId: parseInt(userId),
      receptId: parseInt(receptId),
    };

    if (isNaN(payload.uporabnikId) || isNaN(payload.receptId)) {
      alert("Napaka: Neveljaven ID uporabnika ali recepta.");
      return;
    }

    try {
      await api.post("/api/ocene", payload);

      // Osveži recept da dobimo novo povprečje
      const resp = await api.get(`/api/recepti/${receptId}`);
      setRecepti((prev) =>
        prev.map((r) => (r.id === receptId ? resp.data : r)),
      );
    } catch (err) {
      console.error("Napaka pri ocenjevanju:", err);
      const msg = err.response?.data || "Napaka pri ocenjevanju.";
      alert(
        "Napaka pri ocenjevanju: " +
          (typeof msg === "string" ? msg : JSON.stringify(msg)),
      );
    }
  };

  return (
    <div className={styles.container}>
      <div className={styles.searchBar}>
        <input
          type="text"
          placeholder="Išči po imenu recepta..."
          className={styles.searchInput}
          value={searchName}
          onChange={(e) => setSearchName(e.target.value)}
        />
        <button type="button" className={`${styles.btn} ${styles.btnPrimary}`} onClick={handleShowAll}>
          Prikaži vse
        </button>
      </div>

      <div className={styles.recipeGrid}>
        {recepti
          .filter((r) =>
            r.ime.toLowerCase().includes(searchName.toLowerCase())
          )
          .map((recept) => {
            const originalnePorcije = recept.steviloPorcij || 1;
            const trenutnePorcije = porcijePoReceptu[recept.id] || originalnePorcije;
            const sestavineZaPrikaz = getSestavineZaPrikaz(recept, trenutnePorcije);
            const jeLoading = loadingPorcije[recept.id];

            return (
              <div key={recept.id} id={`recept-${recept.id}`} className={styles.recipeCard}>
                <div className={styles.cardHeader}>
                  <h2 className={styles.recipeTitle}>{recept.ime}</h2>
                  <div style={{ display: "flex", alignItems: "center", gap: "12px" }}>
                    <div className={styles.ratingDisplay}>
                      {[1, 2, 3, 4, 5].map((star) => (
                        <span key={star} style={{ color: star <= Math.round(recept.povprecnaOcena || 0) ? "#FFD700" : "#374151", fontSize: "1.1rem" }}>★</span>
                      ))}
                      <span style={{ marginLeft: "6px", color: "#94a3b8", fontSize: "0.85rem" }}>
                        {(recept.povprecnaOcena || 0).toFixed(1)}
                      </span>
                    </div>
                    <span className={styles.recipeId}>#{recept.id}</span>
                  </div>
                </div>
                
                <p className={styles.description}>{recept.opis}</p>

                <div className={styles.section}>
                  <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: "15px" }}>
                    <h3>Sestavine (za {trenutnePorcije} {getPorcijeLabel(trenutnePorcije)})</h3>
                    <div style={{ display: "flex", alignItems: "center", gap: "10px" }}>
                       <input 
                         type="range" 
                         min="1" max="20" 
                         value={trenutnePorcije} 
                         onChange={(e) => handlePorcijeChange(recept.id, e.target.value)}
                         style={{ width: "100px", accentColor: "hsl(208, 73%, 54%)" }}
                       />
                       <span style={{ color: "#fff", fontWeight: "600", width: "20px" }}>{trenutnePorcije}</span>
                    </div>
                  </div>
                  <ul className={styles.ingredientList}>
                    {sestavineZaPrikaz.map((s, i) => (
                      <li key={i} className={styles.ingredientItem}>
                        <span className={styles.ingredientName}>{s.naziv}</span>
                        <span className={styles.ingredientQty}>{s.kolicina}</span>
                      </li>
                    ))}
                  </ul>
                  {jeLoading && <p style={{ fontSize: "0.8rem", color: "hsl(208, 73%, 54%)", marginTop: "10px" }}>⏳ Preračunavam...</p>}
                </div>

                {recept.navodila && (
                  <div className={styles.section}>
                    <h3 style={{ marginBottom: "10px" }}>Postopek priprave</h3>
                    <p style={{ 
                      whiteSpace: "pre-line", 
                      fontSize: "0.95rem", 
                      lineHeight: "1.6", 
                      color: "var(--text-secondary)",
                      backgroundColor: "rgba(255,255,255,0.03)",
                      padding: "15px",
                      borderRadius: "8px",
                      border: "1px solid var(--border-color)"
                    }}>
                      {recept.navodila}
                    </p>
                  </div>
                )}

                <div className={styles.actions}>
                  <button className={`${styles.btn} ${styles.btnPrimary}`} onClick={() => toggleNutrition(recept.id)}>
                    {showNutrition[recept.id] ? "Skrij" : "Prikaži"} hranilne vrednosti
                  </button>
                  <button className={`${styles.btn} ${styles.btnSecondary}`} onClick={() => toggleComments(recept.id)}>
                    {showComments[recept.id] ? "Skrij" : "Prikaži"} komentarje
                  </button>
                  <button className={`${styles.btn} ${styles.btnSecondary}`} onClick={() => startEdit(recept)}>Uredi</button>
                  <button className={`${styles.btn} ${styles.btnDanger}`} onClick={() => izbrisiRecept(recept.id)}>Izbriši</button>

                  {addSelectors[recept.id] ? (
                    <div className={styles.jedilnikSelector}>
                      <select
                        className={styles.select}
                        value={addSelectors[recept.id].selectedJedId || ""}
                        onChange={(e) =>
                          setAddSelectors((s) => ({
                            ...s,
                            [recept.id]: { ...s[recept.id], selectedJedId: e.target.value },
                          }))
                        }
                      >
                        {(addSelectors[recept.id].jedilniki || []).map((j) => (
                          <option key={j.id} value={String(j.id)}>{j.naziv}</option>
                        ))}
                      </select>
                      <button className={styles.btn} onClick={() => confirmAddToJedilnik(recept.id)}>✓</button>
                      <button className={`${styles.btn} ${styles.btnDanger}`} onClick={() => cancelAddSelector(recept.id)}>✕</button>
                    </div>
                  ) : (
                    <button className={`${styles.btn} ${styles.btnSecondary}`} onClick={() => openAddToJedilnikSelector(recept.id)}>+ Jedilnik</button>
                  )}

                  {getCurrentUserId() && (
                    <div className={styles.ratingAction}>
                      <span className={styles.ratingActionLabel}>Oceni:</span>
                      {[1, 2, 3, 4, 5].map((star) => (
                        <button
                          key={star}
                          type="button"
                          className={styles.starBtn}
                          onClick={() => handleRate(recept.id, star)}
                        >★</button>
                      ))}
                    </div>
                  )}
                </div>

                {showNutrition[recept.id] && (
                  <div style={{ marginTop: "20px" }}>
                    <NutritionalInfo receptId={recept.id} defaultPorcije={trenutnePorcije} />
                  </div>
                )}

                {showComments[recept.id] && (
                  <div className={styles.commentsSection}>
                    <h3 className={styles.sectionTitle}>Komentarji in ocene</h3>
                    <ul className={styles.commentList}>
                      {(comments[recept.id] || []).length > 0 ? (
                        comments[recept.id].map((kom) => (
                          <li key={kom.id} className={styles.commentItem}>
                            <div className={styles.commentHeader}>
                              <div>
                                <span className={styles.commentUser}>{kom.uporabnik?.uporabniskoIme || "Neznan"}</span>
                                <span className={styles.commentDate}>
                                  {kom.createdAt ? new Date(kom.createdAt).toLocaleDateString() : "Pravkar"}
                                </span>
                              </div>
                              {getCurrentUserId() && String(kom.uporabnik?.id) === String(getCurrentUserId()) && (
                                <button 
                                  onClick={() => deleteComment(kom.id, recept.id)} 
                                  className={`${styles.btn} ${styles.btnDanger}`}
                                  style={{ padding: "4px 8px", fontSize: "0.75rem" }}
                                >
                                  Izbriši
                                </button>
                              )}
                            </div>
                            <div className={styles.commentBody}>{kom.besedilo}</div>
                          </li>
                        ))
                      ) : (
                        <p style={{ color: "#64748b", textAlign: "center", fontStyle: "italic", fontSize: "0.9rem" }}>Še ni komentarjev. Bodite prvi!</p>
                      )}
                    </ul>
                    {getCurrentUserId() && (
                      <div style={{ marginTop: "20px" }}>
                        <textarea
                          className={styles.commentTextarea}
                          placeholder="Delite svoje mnenje o tem obroku..."
                          value={newComment[recept.id] || ""}
                          onChange={(e) => handleCommentChange(recept.id, e.target.value)}
                        />
                        <div style={{ display: "flex", justifyContent: "flex-end" }}>
                          <button 
                            className={`${styles.btn} ${styles.btnPrimary}`} 
                            onClick={() => submitComment(recept.id)}
                            disabled={!newComment[recept.id]?.trim()}
                          >
                            Objavi komentar
                          </button>
                        </div>
                      </div>
                    )}
                  </div>
                )}
              </div>
            );
          })}
      </div>

      {editingId && (
        <div className={styles.editOverlay}>
          <div className={styles.editModal}>
            <h2 style={{ color: "#fff", marginBottom: "20px" }}>Posodobi recept</h2>
            <form onSubmit={handleUpdate}>
              <div className={styles.inputGroup}>
                <label>Ime recepta</label>
                <input className={styles.input} type="text" value={ime} onChange={(e) => setIme(e.target.value)} />
              </div>
              <div className={styles.inputGroup}>
                <label>Opis</label>
                <textarea className={styles.input} style={{ minHeight: "80px" }} value={opis} onChange={(e) => setOpis(e.target.value)} />
              </div>
              <div className={styles.inputGroup}>
                <label>Postopek priprave</label>
                <textarea className={styles.input} style={{ minHeight: "150px" }} value={navodila} onChange={(e) => setNavodila(e.target.value)} placeholder="Koraki priprave..." />
              </div>
              <div className={styles.inputGroup}>
                <label>Sestavine</label>
                <div style={{ maxHeight: "200px", overflowY: "auto", marginBottom: "10px" }}>
                  {sestavine.map((s, idx) => (
                    <div key={idx} style={{ display: "flex", gap: "8px", marginBottom: "8px" }}>
                      <input className={styles.input} style={{ flex: 2 }} placeholder="Ime" value={s.ime} onChange={(e) => handleSestavinaChange(idx, "ime", e.target.value)} />
                      <input className={styles.input} style={{ flex: 1 }} placeholder="Količina" value={s.kolicina} onChange={(e) => handleSestavinaChange(idx, "kolicina", e.target.value)} />
                    </div>
                  ))}
                </div>
                <div style={{ display: "flex", gap: "10px" }}>
                  <button type="button" className={`${styles.btn} ${styles.btnSecondary}`} onClick={handleAddInput}>+ Sestavina</button>
                  <button type="button" className={`${styles.btn} ${styles.btnDanger}`} onClick={handleRemoveInput}>- Odstrani zadnjo</button>
                </div>
              </div>
              <div style={{ display: "flex", gap: "15px", marginTop: "30px" }}>
                <button type="submit" className={`${styles.btn} ${styles.btnPrimary}`} style={{ flex: 1 }}>Posodobi Recept</button>
                <button type="button" className={`${styles.btn} ${styles.btnSecondary}`} style={{ flex: 1 }} onClick={() => setEditingId(null)}>Prekliči</button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}

export default List;
