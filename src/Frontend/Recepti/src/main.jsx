import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
import "./index.css";
import Navbar from "./components/Navbar/Navbar.jsx";
import Recepti from "./pages/Recepti.jsx";
import Jedilnik from "./pages/Jedilnik.jsx";
import Registracija from "./pages/Registracija.jsx";
import Prijava from "./pages/Prijava.jsx";

createRoot(document.getElementById("root")).render(
  <StrictMode>
    <Router>
      <Navbar />
      <Routes>
        <Route path="/" element={<Recepti />} />
        <Route path="/jedilnik" element={<Jedilnik />} />
        <Route path="/registracija" element={<Registracija />} />
        <Route path="/prijava" element={<Prijava />} />
      </Routes>
    </Router>
  </StrictMode>
);
