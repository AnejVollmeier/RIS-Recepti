import axios from "axios";

const baseURL = import.meta?.env?.VITE_BASE_URL || "http://localhost:8080";
console.log("API baseURL:", baseURL);

const api = axios.create({
  baseURL,
  timeout: 120000,
  headers: {
    "Content-Type": "application/json",
    Accept: "application/json",
  },
});

// Attach Authorization header automatically when token is present in localStorage
api.interceptors.request.use((config) => {
  try {
    const token = localStorage.getItem("token");
    if (token) {
      config.headers = config.headers || {};
      config.headers["Authorization"] = `Bearer ${token}`;
    }
  } catch {
    // ignore (e.g., server-side rendering)
  }
  return config;
});

export default api;
