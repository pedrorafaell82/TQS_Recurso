import React, { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { api } from "../api";

export default function Opportunities() {
  const [items, setItems] = useState([]);
  const [q, setQ] = useState("");
  const [minPoints, setMinPoints] = useState("");
  const [maxPoints, setMaxPoints] = useState("");
  const [active, setActive] = useState("true");
  const [err, setErr] = useState("");

  async function load() {
    setErr("");
    try {
      const params = {};
      if (q) params.q = q;
      if (minPoints) params.minPoints = minPoints;
      if (maxPoints) params.maxPoints = maxPoints;
      if (active !== "") params.active = active;
      const data = await api.listOpportunities(params);
      setItems(data);
    } catch (e) {
      setErr(e.message || "Failed to load opportunities");
    }
  }

  useEffect(() => { load(); }, []); // initial

  return (
    <div>
      <h2>Opportunities</h2>

      <div style={{ display: "flex", gap: 8, flexWrap: "wrap", marginBottom: 12 }}>
        <input placeholder="search..." value={q} onChange={(e) => setQ(e.target.value)} />
        <input placeholder="minPoints" value={minPoints} onChange={(e) => setMinPoints(e.target.value)} />
        <input placeholder="maxPoints" value={maxPoints} onChange={(e) => setMaxPoints(e.target.value)} />
        <select value={active} onChange={(e) => setActive(e.target.value)}>
          <option value="true">active</option>
          <option value="false">inactive</option>
          <option value="">all</option>
        </select>
        <button onClick={load}>Apply</button>
      </div>

      {err && <div style={{ color: "crimson", marginBottom: 12 }}>{err}</div>}

      <ul>
        {items.map((o) => (
          <li key={o.id}>
            <Link to={`/opportunities/${o.id}`}>{o.title}</Link>{" "}
            — {o.points} pts — {o.date}
          </li>
        ))}
      </ul>
    </div>
  );
}
