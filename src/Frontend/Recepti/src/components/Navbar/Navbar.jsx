import { Link, useNavigate } from "react-router-dom";
import { useState, useEffect } from "react";
import "./Navbar.css";

function Navbar() {
  const [uporabnik, setUporabnik] = useState(null);
  const navigate = useNavigate();

  useEffect(() => {
    // Preveri če je uporabnik prijavljen
    const storedUser = localStorage.getItem("uporabnik");
    if (storedUser) {
      try {
        setUporabnik(JSON.parse(storedUser));
      } catch (error) {
        console.error("Napaka pri branju uporabnika:", error);
      }
    }
  }, []);

  const handleOdjava = () => {
    // Sprazni storage
    localStorage.removeItem("token");
    localStorage.removeItem("uporabnik");
    setUporabnik(null);
    // Preusmeri na recepti stran
    navigate("/");
  };

  return (
    <nav className="navbar">
      <Link to="/" className="nav-link">
        Recepti
      </Link>
      <Link to="/jedilnik" className="nav-link">
        Jedilnik
      </Link>
      {!uporabnik ? (
        <>
          <Link to="/registracija" className="nav-link">
            Registracija
          </Link>
          <Link to="/prijava" className="nav-link">
            Prijava
          </Link>
        </>
      ) : (
        <>
          <span className="user-info">{uporabnik.uporabniskoIme}</span>
          <button onClick={handleOdjava} className="nav-link logout-button">
            Odjava
          </button>
        </>
      )}
    </nav>
  );
}

export default Navbar;
