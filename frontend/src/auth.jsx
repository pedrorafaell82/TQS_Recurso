import React, { createContext, useContext, useEffect, useState } from "react";
import { api, clearAuth, getStoredAuth, setAuth as storeAuth } from "./api";

const AuthCtx = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  async function refreshMe() {
    try {
      const me = await api.me();
      setUser(me);
    } catch {
      setUser(null);
    } finally {
      setLoading(false);
    }
  }

  async function login(email, password) {
    storeAuth(email, password);
    await refreshMe();
  }

  function logout() {
    clearAuth();
    setUser(null);
  }

  useEffect(() => {
    const auth = getStoredAuth();
    if (!auth) {
      setLoading(false);
      return;
    }
    refreshMe();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  return (
    <AuthCtx.Provider value={{ user, loading, login, logout, refreshMe }}>
      {children}
    </AuthCtx.Provider>
  );
}

export function useAuth() {
  return useContext(AuthCtx);
}

export function hasRole(user, role) {
  return !!user?.roles?.includes(role);
}
