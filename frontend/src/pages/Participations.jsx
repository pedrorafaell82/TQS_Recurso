import React, { useEffect, useState } from "react";
import { api } from "../api";
import { useAuth, hasRole } from "../auth";

export default function Participations() {
  const { user } = useAuth();
  const [current, setCurrent] = useState([]);
  const [history, setHistory] = useState([]);
  const [err, setErr] = useState("");
  const [msg, setMsg] = useState("");

  async function load() {
    setErr(""); setMsg("");
    try {
      const [c, h] = await Promise.all([api.myParticipations(), api.myParticipationHistory()]);
      setCurrent(c);
      setHistory(h);
    } catch (e) {
      setErr(e.message || "Failed to load");
    }
  }

  useEffect(() => { load(); }, []);

  async function cancel(id) {
    setErr(""); setMsg("");
    try {
      await api.cancelParticipation(id);
      setMsg("Cancelled.");
      await load();
    } catch (e) {
      setErr(e.message || "Cancel failed");
    }
  }

  return (
    <div>
      <h2>My participations</h2>
      {err && <div style={{ color: "crimson" }}>{err}</div>}
      {msg && <div style={{ color: "green" }}>{msg}</div>}

      <h3>Current</h3>
      <ul>
        {current.map((p) => (
          <li key={p.id}>
            Opp #{p.opportunityId} — {p.status} — {p.createdAt}
            {hasRole(user, "VOLUNTEER") && p.status === "PENDING" && (
              <button onClick={() => cancel(p.id)} style={{ marginLeft: 8 }}>Cancel</button>
            )}
          </li>
        ))}
      </ul>

      <h3>History</h3>
      <ul>
        {history.map((p) => (
          <li key={p.id}>
            Opp #{p.opportunityId} — {p.status} — {p.createdAt}
          </li>
        ))}
      </ul>
    </div>
  );
}
