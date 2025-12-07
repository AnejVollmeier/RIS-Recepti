import { Link } from "react-router-dom";
import "./Navbar.css";

function Navbar() {
  return (
    <nav className="navbar">
      <Link to="/" className="nav-link">
        Recepti
      </Link>
      <Link to="/jedilnik" className="nav-link">
        Jedilnik
      </Link>
      <Link to="/registracija" className="nav-link">
        Registracija
      </Link>
      <Link to="/prijava" className="nav-link">
        Prijava
      </Link>
    </nav>
  );
}

export default Navbar;
