import { useState, useEffect } from "react";
import api from "../../server/server";
import styles from "./list.module.css";
import NutritionalInfo from "../../components/NutritionalInfo/NutritionalInfo";

function List() {
    const [recepti, setRecepti] = useState([]);
    const [searchId, setSearchId] = useState("");
    const [editingId, setEditingId] = useState(null);
    const [ime, setIme] = useState("");
    const [opis, setOpis] = useState("");
    const [sestavine, setSestavine] = useState([{ ime: "", kolicina: "" }]);
    const [searchName, setSearchName] = useState("");

    const [addSelectors, setAddSelectors] = useState({});
    const [porcijePoReceptu, setPorcijePoReceptu] = useState({});
    const [showNutrition, setShowNutrition] = useState({});

    const [comments, setComments] = useState({});
    const [showComments, setShowComments] = useState({});
    const [newComment, setNewComment] = useState({});

    const [preracunaniRecepti, setPreracunaniRecepti] = useState({});
    const [loadingPorcije, setLoadingPorcije] = useState({});

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
            if (e?. detail) {
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
                    resp = await api. get(`/api/jedilniki/uporabnik/${userId}`);
                } catch (err) {
                    // fallback:  fetch all and filter by owner id client-side
                    const all = await api.get("/api/jedilniki");
                    const filtered = (all.data || []).filter(
                        (j) => j.uporabnik && Number(j.uporabnik.id) === Number(userId)
                    );
                    const jeds = filtered || [];
                    setAddSelectors((s) => ({
                        ...s,
                        [receptId]: {
                            jedilniki: jeds,
                            selectedJedId:  jeds.length ?  String(jeds[0].id) : "",
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
            const serverMsg = err?. response?.data || err?.message;
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
        const selectedId = sel?. selectedJedId;
        const jedId = Number(selectedId);
        if (! selectedId || Number. isNaN(jedId)) {
            alert("Najprej izberi jedilnik");
            return;
        }
        try {
            const jed = (sel?.jedilniki || []).find((j) => j.id === jedId);
            const payload = {
                receptId: receptId,
                datum: jed ?  jed.datum : null,
                steviloOseb: jed ? jed.steviloOseb : null,
                alergenIds: [],
            };
            const resp = await api.post(`/api/jedilniki/${jedId}/recepti`, payload, {
                headers: { "Content-Type": "application/json" },
            });
            if (resp.status === 200 || resp.status === 201) {
                alert("Recept uspešno dodan v jedilnik");
                window.dispatchEvent(
                    new CustomEvent("jedilnik-changed", { detail: resp.data })
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
                    `/api/jedilniki/${selectedId}/recepti/${receptId}`
                );
                if (fallback.status === 200 || fallback.status === 201) {
                    alert("Recept uspešno dodan v jedilnik");
                    window.dispatchEvent(
                        new CustomEvent("jedilnik-changed", { detail:  fallback.data })
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
        const idValue = searchId. trim();
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
            const resp = await api.get("/api/recepti", { params:  { ime: q } });
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
            kolicina: s. kolicina || "",
        }));
        setSestavine(localSest. length ?  localSest :  [{ ime: "", kolicina: "" }]);
        const el = document.querySelector(`.${styles.updateContainer}`);
        if (el) el.scrollIntoView({ behavior: "smooth" });
    };

    const handleAddInput = (e) => {
        e?. preventDefault();
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

    // Funkcija za preračun preko backend API
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
            return preracunan.sestavine. map((s) => ({
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
            if (! isNaN(stevilka)) {
                const novaVrednost = (stevilka * faktor).toFixed(1);
                prilagojena = kolicina. replace(stevilka. toString(), novaVrednost);
            }

            return {
                naziv: sestavina.naziv,
                kolicina: prilagojena,
                originalaKolicina: kolicina,
            };
        });
    };

    const handleUpdate = async (e) => {
        e. preventDefault();
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

    const toggleNutrition = (receptId) => {
        setShowNutrition((prev) => ({
            ...prev,
            [receptId]: ! prev[receptId],
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

    return (
        <>
            <div className={styles. listContainer}>
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
                    {recepti.map((recept) => {
                        const originalnePorcije = recept.steviloPorcij || 1;
                        const trenutnePorcije =
                            porcijePoReceptu[recept.id] || originalnePorcije;
                        const sestavineZaPrikaz = getSestavineZaPrikaz(
                            recept,
                            trenutnePorcije
                        );
                        const jeLoading = loadingPorcije[recept.id];

                        return (
                            <div key={recept.id} className={styles.receptCard}>
                                <h2>{recept.ime}</h2>
                                <p>{recept.opis}</p>

                                <div className={styles.porcijeContainer}>
                                    <label>
                                        <strong>
                                            Število porcij:  {trenutnePorcije}
                                            {trenutnePorcije !== originalnePorcije && (
                                                <span style={{ fontSize: "0.9em", color: "#666" }}>
                          {" "}
                                                    (original: {originalnePorcije})
                        </span>
                                            )}
                                        </strong>
                                    </label>
                                    <div
                                        style={{
                                            display: "flex",
                                            gap: "10px",
                                            alignItems: "center",
                                            marginTop: "8px",
                                        }}
                                    >
                                        <input
                                            type="range"
                                            min="1"
                                            max="20"
                                            value={trenutnePorcije}
                                            onChange={(e) =>
                                                handlePorcijeChange(recept.id, e.target.value)
                                            }
                                            style={{
                                                flex: 1,
                                                accentColor: "#007bff",
                                            }}
                                        />
                                        <input
                                            type="number"
                                            min="1"
                                            max="20"
                                            value={trenutnePorcije}
                                            onChange={(e) =>
                                                handlePorcijeChange(recept. id, e.target.value)
                                            }
                                            style={{ width: "60px" }}
                                        />
                                    </div>
                                </div>

                                <h3>
                                    Sestavine:
                                    {jeLoading && (
                                        <span
                                            style={{
                                                fontSize: "0.8em",
                                                color: "#007bff",
                                                marginLeft: "10px",
                                            }}
                                        >
                      ⏳ Preračunavam...
                    </span>
                                    )}
                                </h3>
                                <ul>
                                    {sestavineZaPrikaz.map((sestavina, index) => (
                                        <li key={index}>
                                            <strong>{sestavina.naziv}:</strong> {sestavina.kolicina}
                                            {sestavina.kolicina !== sestavina.originalaKolicina && (
                                                <span
                                                    style={{
                                                        fontSize: "0.85em",
                                                        color: "#666",
                                                        marginLeft:  "8px",
                                                    }}
                                                >
                          (original: {sestavina.originalaKolicina})
                        </span>
                                            )}
                                        </li>
                                    ))}
                                </ul>
                                <NutritionalInfo
                                    receptId={recept.id}
                                    defaultPorcije={trenutnePorcije}
                                    compact={true}
                                />

                                <div className={styles.buttonGroup}>
                                    <button type="button" onClick={() => startEdit(recept)}>
                                        Uredi
                                    </button>
                                    <button
                                        className={styles.deleteButton}
                                        onClick={() => izbrisiRecept(recept. id)}
                                        type="button"
                                    >
                                        Izbriši
                                    </button>
                                    <button
                                        type="button"
                                        onClick={() => toggleNutrition(recept. id)}
                                    >
                                        {showNutrition[recept.id] ?  "Skrij" : "Prikaži"} hranilne vrednosti
                                    </button>

                                    <button
                                        type="button"
                                        onClick={() => toggleComments(recept.id)}
                                    >
                                        {showComments[recept.id] ? "Skrij" : "Prikaži"} komentarje
                                    </button>

                                    {addSelectors[recept.id] ?  (
                                        <div className={styles.jedilnikSelector}>
                                            <select
                                                value={addSelectors[recept.id]. selectedJedId || ""}
                                                onChange={(e) =>
                                                    setAddSelectors((s) => ({
                                                        ...s,
                                                        [recept. id]: {
                                                            ... s[recept.id],
                                                            selectedJedId: e.target.value,
                                                        },
                                                    }))
                                                }
                                            >
                                                {(addSelectors[recept.id].jedilniki || []).map((j) => (
                                                    <option key={j.id} value={String(j.id)}>
                                                        {j.naziv} ({j.datum})
                                                    </option>
                                                ))}
                                            </select>
                                            <button
                                                type="button"
                                                onClick={() => confirmAddToJedilnik(recept.id)}
                                            >
                                                Potrdi
                                            </button>
                                            <button
                                                type="button"
                                                className={styles.deleteButton}
                                                onClick={() => cancelAddSelector(recept.id)}
                                            >
                                                Prekliči
                                            </button>
                                        </div>
                                    ) : (
                                        <button
                                            type="button"
                                            onClick={() => openAddToJedilnikSelector(recept. id)}
                                        >
                                            Dodaj v jedilnik
                                        </button>
                                    )}
                                </div>
                                {showComments[recept.id] && (
                                    <div className={styles.commentsSection} style={{ marginTop: "15px", borderTop: "1px solid #ccc", paddingTop: "10px" }}>
                                        <h4>Komentarji</h4>
                                        {comments[recept.id] && comments[recept.id].length > 0 ? (
                                            <ul style={{ listStyle: "none", padding: 0 }}>
                                                {comments[recept.id].map((kom) => (
                                                    <li key={kom.id} style={{ marginBottom: "10px", padding: "8px", backgroundColor: "#f9f9f9", borderRadius: "5px", color: "black" }}>
                                                        <div style={{ fontWeight: "bold", fontSize: "0.9em" }}>
                                                            {kom.uporabnik ? kom.uporabnik.uporabniskoIme : "Neznan uporabnik"}
                                                            <span style={{ fontWeight: "normal", color: "#888", marginLeft: "10px", fontSize: "0.8em" }}>
                                                                {kom.createdAt ? new Date(kom.createdAt).toLocaleDateString() : ""}
                                                            </span>
                                                        </div>
                                                        <div style={{ marginTop: "5px" }}>{kom.besedilo}</div>
                                                    </li>
                                                ))}
                                            </ul>
                                        ) : (
                                            <p style={{ fontStyle: "italic", color: "#666" }}>Ni komentarjev.</p>
                                        )}
                                        
                                        {getCurrentUserId() ? (
                                            <div style={{ marginTop: "10px" }}>
                                                <textarea
                                                    style={{ width: "100%", padding: "8px", borderRadius: "4px", border: "1px solid #ccc" }}
                                                    rows="3"
                                                    placeholder="Dodaj komentar..."
                                                    value={newComment[recept.id] || ""}
                                                    onChange={(e) => handleCommentChange(recept.id, e.target.value)}
                                                ></textarea>
                                                <button
                                                    type="button"
                                                    style={{ marginTop: "5px", padding: "5px 15px", backgroundColor: "#28a745", color: "white", border: "none", borderRadius: "4px", cursor: "pointer" }}
                                                    onClick={() => submitComment(recept.id)}
                                                >
                                                    Pošlji komentar
                                                </button>
                                            </div>
                                        ) : (
                                            <div style={{ marginTop: "10px", color: "#666", fontSize: "0.9em" }}>
                                                Za komentiranje se morate prijaviti.
                                            </div>
                                        )}
                                    </div>
                                )}
                            </div>
                        );
                    })}
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
                                            handleSestavinaChange(idx, "kolicina", e. target.value)
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