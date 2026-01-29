import React, { useEffect, useMemo, useState } from "react";
import { api } from "../api";

const emptyForm = {
  title: "",
  description: "",
  date: "", // yyyy-mm-dd
  durationHours: 1,
  points: 1,
};

export default function PromoterPanel() {
  const [items, setItems] = useState([]);
  const [activeFilter, setActiveFilter] = useState("true");

  const [form, setForm] = useState(emptyForm);
  const [editingId, setEditingId] = useState(null);
  const [partId, setPartId] = useState("");
  const [err, setErr] = useState("");
  const [msg, setMsg] = useState("");

  async function load() {
    setErr(""); setMsg("");
    try {
      const data = await api.listOpportunities({ active: activeFilter });
      setItems(data);
    } catch (e) {
      setErr(e.message || "Failed to load opportunities");
    }
  }

  useEffect(() => { load(); }, [activeFilter]);

  function onChange(field, value) {
    setForm((f) => ({ ...f, [field]: value }));
  }

  const canSubmit = useMemo(() => {
    return (
      form.title.trim().length > 0 &&
      form.description.trim().length > 0 &&
      /^\d{4}-\d{2}-\d{2}$/.test(form.date) &&
      Number(form.durationHours) > 0 &&
      Number(form.points) > 0
    );
  }, [form]);

  async function submit(e) {
    e.preventDefault();
    setErr(""); setMsg("");

    const payload = {
      title: form.title.trim(),
      description: form.description.trim(),
      date: form.date,
      durationHours: Number(form.durationHours),
      points: Number(form.points),
    };

    try {
      if (editingId) {
        await api.updateOpportunity(editingId, payload);
        setMsg(`Opportunity #${editingId} updated.`);
      } else {
        const created = await api.createOpportunity(payload);
        setMsg(`Opportunity created (id: ${created?.id ?? "?"}).`);
      }
      setForm(emptyForm);
      setEditingId(null);
      await load();
    } catch (e2) {
      setErr(e2.message || "Failed to submit");
    }
  }

  function startEdit(o) {
    setMsg(""); setErr("");
    setEditingId(o.id);
    setForm({
      title: o.title ?? "",
      description: o.description ?? "",
      date: o.date ?? "",
      durationHours: o.durationHours ?? 1,
      points: o.points ?? 1,
    });
    window.scrollTo({ top: 0, behavior: "smooth" });
  }

  function cancelEdit() {
    setEditingId(null);
    setForm(emptyForm);
  }

  async function deactivate(id) {
    setErr(""); setMsg("");
    try {
      await api.deactivateOpportunity(id);
      setMsg(`Opportunity #${id} deactivated.`);
      await load();
    } catch (e2) {
      setErr(e2.message || "Failed to deactivate");
    }
  }

  return (
    <div>
      <h2>Promoter</h2>

      {err && <div style={{ color: "crimson", marginBottom: 12 }}>{err}</div>}
      {msg && <div style={{ color: "green", marginBottom: 12 }}>{msg}</div>}

      <h3>{editingId ? `Edit opportunity #${editingId}` : "Create opportunity"}</h3>

      <form onSubmit={submit} style={{ display: "grid", gap: 8, maxWidth: 520, marginBottom: 18 }}>
        <input
          placeholder="Title"
          value={form.title}
          onChange={(e) => onChange("title", e.target.value)}
        />
        <textarea
          placeholder="Description"
          value={form.description}
          onChange={(e) => onChange("description", e.target.value)}
          rows={3}
        />
        <div style={{ display: "flex", gap: 8, flexWrap: "wrap" }}>
          <div>
            <div style={{ fontSize: 12, opacity: 0.8 }}>Date</div>
            <input
              type="date"
              value={form.date}
              onChange={(e) => onChange("date", e.target.value)}
            />
          </div>
          <div>
            <div style={{ fontSize: 12, opacity: 0.8 }}>Duration (hours)</div>
            <input
              type="number"
              min="1"
              value={form.durationHours}
              onChange={(e) => onChange("durationHours", e.target.value)}
              style={{ width: 140 }}
            />
          </div>
          <div>
            <div style={{ fontSize: 12, opacity: 0.8 }}>Points</div>
            <input
              type="number"
              min="1"
              value={form.points}
              onChange={(e) => onChange("points", e.target.value)}
              style={{ width: 120 }}
            />
          </div>
        </div>

        <div style={{ display: "flex", gap: 8 }}>
          <button type="submit" disabled={!canSubmit}>
            {editingId ? "Save changes" : "Create"}
          </button>
          {editingId && (
            <button type="button" onClick={cancelEdit}>
              Cancel
            </button>
          )}
        </div>
      </form>

      <h3>My opportunities (browse)</h3>
      <div style={{ display: "flex", gap: 8, alignItems: "center", marginBottom: 10 }}>
        <span>Show:</span>
        <select value={activeFilter} onChange={(e) => setActiveFilter(e.target.value)}>
          <option value="true">Active</option>
          <option value="false">Inactive</option>
          <option value="">All</option>
        </select>
        <button onClick={load}>Refresh</button>
      </div>

      <ul style={{ paddingLeft: 16 }}>
        {items.map((o) => (
          <li key={o.id} style={{ marginBottom: 10 }}>
            <div>
              <b>#{o.id}</b> — {o.title} ({o.points} pts) — {o.date}
            </div>
            <div style={{ opacity: 0.9 }}>{o.description}</div>
            <div style={{ marginTop: 6, display: "flex", gap: 8 }}>
              <button onClick={() => startEdit(o)}>Edit</button>
              <button onClick={() => deactivate(o.id)}>Deactivate</button>
            </div>
          </li>
        ))}
      </ul>
      <h3 style={{ marginTop: 20 }}>Approve / Reject participation</h3>
        <div style={{ display: "flex", gap: 8, alignItems: "center", flexWrap: "wrap" }}>
        <input
            placeholder="participation id (e.g. 12)"
            value={partId}
            onChange={(e) => setPartId(e.target.value)}
            style={{ width: 220 }}
        />
        <button
            onClick={async () => {
            setErr(""); setMsg("");
            try {
                await api.approveParticipation(Number(partId));
                setMsg(`Participation #${partId} approved.`);
                setPartId("");
            } catch (e2) {
                setErr(e2.message || "Approve failed");
            }
            }}
            disabled={!partId}
        >
            Approve
        </button>

        <button
            onClick={async () => {
            setErr(""); setMsg("");
            try {
                await api.rejectParticipation(Number(partId));
                setMsg(`Participation #${partId} rejected.`);
                setPartId("");
            } catch (e2) {
                setErr(e2.message || "Reject failed");
            }
            }}
            disabled={!partId}
        >
            Reject
        </button>
        </div>

        <div style={{ marginTop: 8, fontSize: 13, opacity: 0.85 }}>
        Tip: o volunteer consegue ver o participationId em <b>My participations</b> (estado PENDING) e pode copiar esse ID para aqui.
        </div>

    </div>
  );
}
