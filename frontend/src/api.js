const API_BASE = import.meta.env.VITE_API_BASE_URL;

function getAuthHeader() {
  const raw = sessionStorage.getItem("auth");
  if (!raw) return {};
  const { email, password } = JSON.parse(raw);
  const token = btoa(`${email}:${password}`);
  return { Authorization: `Basic ${token}` };
}

export function setAuth(email, password) {
  sessionStorage.setItem("auth", JSON.stringify({ email, password }));
}

export function clearAuth() {
  sessionStorage.removeItem("auth");
}

export function getStoredAuth() {
  const raw = sessionStorage.getItem("auth");
  return raw ? JSON.parse(raw) : null;
}

async function request(path, { method = "GET", body, headers } = {}) {
  const res = await fetch(`${API_BASE}${path}`, {
    method,
    headers: {
      "Content-Type": "application/json",
      ...getAuthHeader(),
      ...(headers ?? {}),
    },
    body: body ? JSON.stringify(body) : undefined,
  });

  if (res.status === 204) return null;

  const text = await res.text();
  const data = text ? JSON.parse(text) : null;

  if (!res.ok) {
    const msg = data?.error || data?.message || `Request failed (${res.status})`;
    const err = new Error(msg);
    err.status = res.status;
    err.data = data;
    throw err;
  }
  return data;
}

export const api = {
  // auth/current user
  me: () => request("/api/me"),
  register: (payload) => request("/api/auth/register", { method: "POST", body: payload }),

  // opportunities
  listOpportunities: (params = {}) => {
    const qs = new URLSearchParams(params).toString();
    return request(`/api/opportunities${qs ? `?${qs}` : ""}`);
  },
  getOpportunity: (id) => request(`/api/opportunities/${id}`),
  createOpportunity: (payload) => request("/api/opportunities", { method: "POST", body: payload }),
  updateOpportunity: (id, payload) => request(`/api/opportunities/${id}`, { method: "PUT", body: payload }),
  deactivateOpportunity: (id) => request(`/api/opportunities/${id}/deactivate`, { method: "PATCH" }),
  enroll: (id) => request(`/api/opportunities/${id}/enroll`, { method: "POST" }),

  // participations
  myParticipations: () => request("/api/participations/me"),
  myParticipationHistory: () => request("/api/participations/me/history"),
  approveParticipation: (id) => request(`/api/participations/${id}/approve`, { method: "POST" }),
  rejectParticipation: (id) => request(`/api/participations/${id}/reject`, { method: "POST" }),
  cancelParticipation: (id) => request(`/api/participations/${id}/cancel`, { method: "POST" }),

  // points
  pointsBalance: () => request("/api/points/balance"),

  // rewards
  listRewards: () => request("/api/rewards"),
  redeemReward: (id) => request(`/api/rewards/${id}/redeem`, { method: "POST" }),
  myRewardsHistory: () => request("/api/rewards/me/history"),

  // admin: users/roles, rewards, point rules
  setUserRoles: (userId, roles) =>
    request(`/api/admin/users/${userId}/roles`, { method: "PUT", body: { roles } }),

  adminListRewards: () => request("/api/admin/rewards"),
  adminCreateReward: (payload) => request("/api/admin/rewards", { method: "POST", body: payload }),
  adminUpdateReward: (id, payload) => request(`/api/admin/rewards/${id}`, { method: "PATCH", body: payload }),

  getPointRules: () => request("/api/admin/points/rules"),
  updatePointRules: (approvalMultiplier) =>
    request("/api/admin/points/rules", { method: "PUT", body: { approvalMultiplier } }),
};
