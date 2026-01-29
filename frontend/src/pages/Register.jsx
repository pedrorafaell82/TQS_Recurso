import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import { api, setAuth } from "../api";
import { useAuth } from "../auth";

export default function Register() {
  const nav = useNavigate();
  const { refreshMe } = useAuth();
  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("strongPass1");
  const [err, setErr] = useState("");

  async function onSubmit(e) {
    e.preventDefault();
    setErr("");
    try {
      await api.register({ name, email, password });
      // auto-login after register
      setAuth(email, password);
      await refreshMe();
      nav("/");
    } catch (e2) {
      setErr(e2.message || "Register failed");
    }
  }

  return (
    <div>
      <h2>Register</h2>
      <form onSubmit={onSubmit} style={{ display: "grid", gap: 8, maxWidth: 360 }}>
        <input placeholder="name" value={name} onChange={(e) => setName(e.target.value)} />
        <input placeholder="email" value={email} onChange={(e) => setEmail(e.target.value)} />
        <input placeholder="password" type="password" value={password} onChange={(e) => setPassword(e.target.value)} />
        <button type="submit">Create account</button>
        {err && <div style={{ color: "crimson" }}>{err}</div>}
      </form>
    </div>
  );
}
