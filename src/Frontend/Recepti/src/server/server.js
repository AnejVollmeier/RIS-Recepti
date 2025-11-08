import axios from "axios";

const baseURL = import.meta?.env?.VITE_BASE_URL || "http://localhost:8080";
console.log("API baseURL:", baseURL);

const api = axios.create({
  baseURL,
  timeout: 30000,
  headers: {
    "Content-Type": "application/json",
    Accept: "application/json",
  },
});

export default api;
