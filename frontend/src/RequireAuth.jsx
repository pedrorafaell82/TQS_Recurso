import React from "react";
import { Navigate } from "react-router-dom";
import { useAuth, hasRole } from "./auth";

export default function RequireAuth({ children, role }) {
  const { user, loading } = useAuth();

  if (loading) return <div style={{ padding: 16 }}>Loading...</div>;
  if (!user) return <Navigate to="/login" replace />;

  if (role && !hasRole(user, role)) {
    return <div style={{ padding: 16 }}>Forbidden</div>;
  }
  return children;
}
