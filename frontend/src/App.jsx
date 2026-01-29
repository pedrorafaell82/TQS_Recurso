import React from "react";
import { Link, Route, Routes } from "react-router-dom";
import RequireAuth from "./RequireAuth.jsx";
import { useAuth, hasRole } from "./auth";

import Login from "./pages/Login";
import Register from "./pages/Register";
import Opportunities from "./pages/Opportunities.jsx";
import OpportunityDetails from "./pages/OpportunityDetails";
import Participations from "./pages/Participations";
import Rewards from "./pages/Rewards";
import AdminPanel from "./pages/AdminPanel";
import PromoterPanel from "./pages/PromoterPanel";

export default function App() {
  const { user, logout } = useAuth();

  return (
    <div style={{ maxWidth: 1000, margin: "0 auto", padding: 16 }}>
      <header style={{ display: "flex", gap: 12, alignItems: "center", marginBottom: 16 }}>
        <Link to="/">Opportunities</Link>
        {user && <Link to="/participations">My participations</Link>}
        {user && <Link to="/rewards">Rewards</Link>}
        {hasRole(user, "PROMOTER") && <Link to="/promoter">Promoter</Link>}
        {hasRole(user, "ADMIN") && <Link to="/admin">Admin</Link>}
        <div style={{ marginLeft: "auto" }}>
          {user ? (
            <>
              <span style={{ marginRight: 12 }}>
                {user.email} ({user.roles?.join(", ")})
              </span>
              <button onClick={logout}>Logout</button>
            </>
          ) : (
            <>
              <Link to="/login" style={{ marginRight: 8 }}>Login</Link>
              <Link to="/register">Register</Link>
            </>
          )}
        </div>
      </header>

      <Routes>
        <Route path="/" element={<Opportunities />} />
        <Route path="/opportunities/:id" element={<OpportunityDetails />} />
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />

        <Route
          path="/participations"
          element={
            <RequireAuth>
              <Participations />
            </RequireAuth>
          }
        />
        <Route
          path="/rewards"
          element={
            <RequireAuth>
              <Rewards />
            </RequireAuth>
          }
        />
        <Route
          path="/admin"
          element={
            <RequireAuth role="ADMIN">
              <AdminPanel />
            </RequireAuth>
          }
        />
        <Route
          path="/promoter"
          element={
            <RequireAuth role="PROMOTER">
              <PromoterPanel />
            </RequireAuth>
          }
        />

        <Route path="*" element={<div>Not found</div>} />
      </Routes>
    </div>
  );
}
