import React, { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import { api } from "../api";
import { useAuth, hasRole } from "../auth";

export default function OpportunityDetails() {
  const { id } = useParams();
  const { user } = useAuth();
  const [opp, setOpp] = useState(null);
  const [msg, setMsg] = useState("");
  const [err, setErr] = useState("");

  useEffect(() => {
    (async () => {
      try {
        setErr("");
        const data = await api.getOpportunity(id);
        setOpp(data);
      } catch (e) {
        setErr(e.message || "Failed to load");
      }
    })();
  }, [id]);

  async function enroll() {
    setMsg(""); setErr("");
    try {
      await api.enroll(id);
      setMsg("Enrolled (PENDING).");
    } catch (e) {
      setErr(e.message || "Enroll failed");
    }
  }

  if (err) return <div style={{ color: "crimson" }}>{err}</div>;
  if (!opp) return <div>Loading...</div>;

  return (
    <div>
      <h2>{opp.title}</h2>
      <div>{opp.description}</div>
      <div>Date: {opp.date}</div>
      <div>Duration: {opp.durationHours}h</div>
      <div>Points: {opp.points}</div>

      {user && hasRole(user, "VOLUNTEER") && (
        <button onClick={enroll} style={{ marginTop: 12 }}>Enroll</button>
      )}

      {msg && <div style={{ color: "green", marginTop: 12 }}>{msg}</div>}
      {err && <div style={{ color: "crimson", marginTop: 12 }}>{err}</div>}
    </div>
  );
}
