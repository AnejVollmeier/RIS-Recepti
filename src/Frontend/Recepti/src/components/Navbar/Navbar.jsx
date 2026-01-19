import "./Navbar.css";
import { Link, useNavigate, useLocation } from "react-router-dom";
import { useState, useEffect } from "react";

function Navbar() {
    const [uporabnik, setUporabnik] = useState(null);
    const [theme, setTheme] = useState(localStorage.getItem("theme") || "dark");
    const navigate = useNavigate();
    const location = useLocation();

    useEffect(() => {
        document.documentElement.setAttribute("data-theme", theme);
        localStorage.setItem("theme", theme);
    }, [theme]);

    useEffect(() => {
        const storedUser = localStorage.getItem("uporabnik");
        if (storedUser) {
            try {
                setUporabnik(JSON.parse(storedUser));
            } catch (error) {
                console.error("Napaka pri branju uporabnika:", error);
            }
        }
    }, [location]); // Refresh on navigation

    const handleOdjava = () => {
        localStorage.removeItem("token");
        localStorage.removeItem("uporabnik");
        setUporabnik(null);
        navigate("/");
    };

    const isActive = (path) => location.pathname === path ? "active" : "";

    return (
        <nav className="navbar">
            <div className="nav-links-container">
                <Link to="/" className={`nav-link ${isActive("/")}`}>
                    Recepti
                </Link>
                <Link to="/jedilnik" className={`nav-link ${isActive("/jedilnik")}`}>
                    Jedilnik
                </Link>
                <button 
                  className="theme-toggle" 
                  onClick={() => setTheme(theme === "dark" ? "light" : "dark")}
                  title="Preklopi temo"
                >
                  {theme === "dark" ? "☀️" : "🌙"}
                </button>
            </div>

            <div className="user-section">
                {!uporabnik ? (
                    <div className="auth-buttons">
                        <Link to="/prijava" className={`nav-link ${isActive("/prijava")}`}>
                            Prijava
                        </Link>
                        <Link to="/registracija" className={`nav-link register-btn ${isActive("/registracija")}`}>
                            Registracija
                        </Link>
                    </div>
                ) : (
                    <>
                        <div className="user-info">
                            <div className="user-avatar">
                                {uporabnik.uporabniskoIme?.charAt(0).toUpperCase()}
                            </div>
                            <span>{uporabnik.uporabniskoIme}</span>
                        </div>
                        <button onClick={handleOdjava} className="logout-button">
                            Odjava
                        </button>
                    </>
                )}
            </div>
        </nav>
    );
}

export default Navbar;
