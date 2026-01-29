import React, { useEffect, useState } from "react";
import { api } from "../api";

export default function AdminPanel() {
  const [rules, setRules] = useState(null);
  const [mult, setMult] = useState("1.0");
  const [rewards, setRewards] = useState([]);
  const [name, setName] = useState("");
  const [cost, setCost] = useState("1");
  const [err, setErr] = useState("");
  const [msg, setMsg] = useState("");
  const [userId, setUserId] = useState("");
  const [role, setRole] = useState("PROMOTER");

  async function load() {
    setErr(""); setMsg("");
    try {
      const [r, pr] = await Promise.all([api.adminListRewards(), api.getPointRules()]);
      setRewards(r);
      setRules(pr);
      setMult(String(pr.approvalMultiplier));
    } catch (e) {
      setErr(e.message || "Failed to load");
    }
  }

  useEffect(() => { load(); }, []);

  async function updateRules() {
    setErr(""); setMsg("");
    try {
      await api.updatePointRules(Number(mult));
      setMsg("Rules updated.");
      await load();
    } catch (e) {
      setErr(e.message || "Failed to update rules");
    }
  }

  async function createReward() {
    setErr(""); setMsg("");
    try {
      await api.adminCreateReward({ name, cost: Number(cost) });
      setName(""); setCost("1");
      setMsg("Reward created.");
      await load();
    } catch (e) {
      setErr(e.message || "Failed to create reward");
    }
  }

  async function toggleActive(r) {
    setErr(""); setMsg("");
    try {
      await api.adminUpdateReward(r.id, { active: !r.active });
      await load();
    } catch (e) {
      setErr(e.message || "Failed to update reward");
    }
  }

  return (
    <div>
      <h2>Admin</h2>
      {err && <div style={{ color: "crimson" }}>{err}</div>}
      {msg && <div style={{ color: "green" }}>{msg}</div>}

      <h3>Point rules</h3>
      <div>Current: {rules ? rules.approvalMultiplier : "—"}</div>
      <div style={{ display: "flex", gap: 8, alignItems: "center", marginTop: 8 }}>
        <input value={mult} onChange={(e) => setMult(e.target.value)} style={{ width: 120 }} />
        <button onClick={updateRules}>Update multiplier</button>
      </div>

      <h3 style={{ marginTop: 16 }}>Rewards</h3>

      <div style={{ display: "flex", gap: 8, marginBottom: 12 }}>
        <input placeholder="name" value={name} onChange={(e) => setName(e.target.value)} />
        <input placeholder="cost" value={cost} onChange={(e) => setCost(e.target.value)} style={{ width: 100 }} />
        <button onClick={createReward}>Create</button>
      </div>

      <ul>
        {rewards.map((r) => (
          <li key={r.id}>
            {r.name} — cost {r.cost} — active: {String(r.active)}
            <button onClick={() => toggleActive(r)} style={{ marginLeft: 8 }}>
              Toggle active
            </button>
          </li>
        ))}
      </ul>
      <h3>Promote user</h3>
        <div style={{ display: "flex", gap: 8, alignItems: "center", marginBottom: 12 }}>
        <input
            placeholder="user id (e.g. 3)"
            value={userId}
            onChange={(e) => setUserId(e.target.value)}
            style={{ width: 180 }}
        />
        <select value={role} onChange={(e) => setRole(e.target.value)}>
            <option value="PROMOTER">PROMOTER</option>
            <option value="VOLUNTEER">VOLUNTEER</option>
            <option value="ADMIN">ADMIN</option>
        </select>
        <button
            onClick={async () => {
            setErr(""); setMsg("");
            try {
                await api.setUserRoles(Number(userId), [role]);
                setMsg(`User ${userId} roles updated to ${role}`);
            } catch (e) {
                setErr(e.message || "Failed to update roles");
            }
            }}
        >
            Update roles
        </button>
        </div>
    </div>
  );
}
