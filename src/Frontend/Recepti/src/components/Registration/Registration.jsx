import { useState } from "react";
import { useNavigate } from "react-router-dom";
import styles from "./Registration.module.css";

export default function Registration() {
  const [formData, setFormData] = useState({
    uporabniskoIme: "",
    email: "",
    geslo: "",
    potrdiGeslo: "",
  });
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value,
    });
    setError("");
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");

    // Validacija
    if (
      !formData.uporabniskoIme ||
      !formData.email ||
      !formData.geslo ||
      !formData.potrdiGeslo
    ) {
      setError("Vsa polja so obvezna");
      return;
    }

    if (formData.geslo !== formData.potrdiGeslo) {
      setError("Gesli se ne ujemata");
      return;
    }

    if (formData.geslo.length < 6) {
      setError("Geslo mora imeti vsaj 6 znakov");
      return;
    }

    setLoading(true);

    try {
      const response = await fetch("http://localhost:8080/api/uporabniki", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          uporabniskoIme: formData.uporabniskoIme,
          email: formData.email,
          geslo: formData.geslo,
        }),
      });

      if (!response.ok) {
        const errorText = await response.text();
        throw new Error(errorText || "Registracija ni uspela");
      }

      navigate("/prijava");
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className={styles.container}>
      <div className={styles.registrationBox}>
        <h1 className={styles.title}>Registracija</h1>
        <form onSubmit={handleSubmit} className={styles.form}>
          <div className={styles.inputGroup}>
            <label htmlFor="uporabniskoIme" className={styles.label}>
              Uporabniško ime
            </label>
            <input
              type="text"
              id="uporabniskoIme"
              name="uporabniskoIme"
              value={formData.uporabniskoIme}
              onChange={handleChange}
              className={styles.input}
              disabled={loading}
              required
            />
          </div>

          <div className={styles.inputGroup}>
            <label htmlFor="email" className={styles.label}>
              Email
            </label>
            <input
              type="email"
              id="email"
              name="email"
              value={formData.email}
              onChange={handleChange}
              className={styles.input}
              disabled={loading}
              required
            />
          </div>

          <div className={styles.inputGroup}>
            <label htmlFor="geslo" className={styles.label}>
              Geslo
            </label>
            <input
              type="password"
              id="geslo"
              name="geslo"
              value={formData.geslo}
              onChange={handleChange}
              className={styles.input}
              disabled={loading}
              required
            />
          </div>

          <div className={styles.inputGroup}>
            <label htmlFor="potrdiGeslo" className={styles.label}>
              Potrdi geslo
            </label>
            <input
              type="password"
              id="potrdiGeslo"
              name="potrdiGeslo"
              value={formData.potrdiGeslo}
              onChange={handleChange}
              className={styles.input}
              disabled={loading}
              required
            />
          </div>

          {error && <p className={styles.error}>{error}</p>}

          <button type="submit" className={styles.button} disabled={loading}>
            {loading ? "Registracija..." : "Registriraj se"}
          </button>
        </form>

        <p className={styles.loginLink}>
          Že imaš račun? <a href="/prijava">Prijavi se</a>
        </p>
      </div>
    </div>
  );
}
