import React, { useEffect, useState } from "react";
import { api } from "../api";
import { useAuth, hasRole } from "../auth";

export default function Rewards() {
  const { user } = useAuth();
  const [balance, setBalance] = useState(null);
  const [rewards, setRewards] = useState([]);
  const [history, setHistory] = useState([]);
  const [err, setErr] = useState("");
  const [msg, setMsg] = useState("");

  async function load() {
    setErr(""); setMsg("");
    try {
      const [b, r, h] = await Promise.all([
        api.pointsBalance(),
        api.listRewards(),
        api.myRewardsHistory(),
      ]);
      setBalance(b.balance);
      setRewards(r);
      setHistory(h);
    } catch (e) {
      setErr(e.message || "Failed to load");
    }
  }

  useEffect(() => { load(); }, []);

  async function redeem(id) {
    setErr(""); setMsg("");
    try {
      await api.redeemReward(id);
      setMsg("Redeemed!");
      await load();
    } catch (e) {
      setErr(e.message || "Redeem failed");
    }
  }

  return (
    <div>
      <h2>Rewards</h2>
      {err && <div style={{ color: "crimson" }}>{err}</div>}
      {msg && <div style={{ color: "green" }}>{msg}</div>}

      <div style={{ marginBottom: 12 }}>
        Balance: <b>{balance ?? "—"}</b>
      </div>

      <h3>Available rewards</h3>
      <ul>
        {rewards.map((r) => (
          <li key={r.id}>
            {r.name} — cost {r.cost}
            {user && hasRole(user, "VOLUNTEER") && (
              <button onClick={() => redeem(r.id)} style={{ marginLeft: 8 }}>Redeem</button>
            )}
          </li>
        ))}
      </ul>

      <h3>My reward history</h3>
      <ul>
        {history.map((h) => (
          <li key={h.transactionId}>
            {h.rewardName} — cost {h.cost} — {h.createdAt}
          </li>
        ))}
      </ul>
    </div>
  );
}
