import { useState } from "react";
import styles from "./Login.module.css";

export default function Login() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");

    try {
      const response = await fetch("/api/login", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({ email, password }),
      });

      if (!response.ok) {
        throw new Error("Prijava ni uspela");
      }

      const data = await response.json();
      localStorage.setItem("token", data.token);
      window.location.href = "/";
    } catch (err) {
      setError(err.message);
    }
  };

  return (
    <div className={styles.container}>
      <div className={styles.loginBox}>
        <h1 className={styles.title}>Prijava</h1>
        <form onSubmit={handleSubmit} className={styles.form}>
          <div className={styles.inputGroup}>
            <label htmlFor="email" className={styles.label}>
              Email
            </label>
            <input
              type="email"
              id="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              className={styles.input}
              required
            />
          </div>
          <div className={styles.inputGroup}>
            <label htmlFor="password" className={styles.label}>
              Geslo
            </label>
            <input
              type="password"
              id="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              className={styles.input}
              required
            />
          </div>
          {error && <p className={styles.error}>{error}</p>}
          <button type="submit" className={styles.button}>
            Prijavi se
          </button>
        </form>
        <p className={styles.registerLink}>
          Še nimaš računa? <a href="/register">Registriraj se</a>
        </p>
      </div>
    </div>
  );
}
