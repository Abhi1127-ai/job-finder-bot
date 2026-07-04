import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import Layout from "./components/Layout";
import Login from "./pages/Login";
import DashBoard from "./pages/DashBoard";
import Jobs from "./pages/Jobs";
import Alerts from "./pages/Alerts";
import Settings from "./pages/Settings";
import Resume from "./pages/Resume";

function RequireAuth({ children }) {
  const token = localStorage.getItem("token");
  return token ? children : <Navigate to="/login" replace />;
}

export default function App() {
  return (
      <BrowserRouter>
        <Routes>
          <Route path="/login" element={<Login />} />
          <Route
              path="/"
              element={
                <RequireAuth>
                  <Layout />
                </RequireAuth>
              }
          >
            <Route index element={<DashBoard />} />
            <Route path="jobs" element={<Jobs />} />
            <Route path="alerts" element={<Alerts />} />
            <Route path="settings" element={<Settings />} />
            <Route path="resume" element={<Resume />} />
          </Route>
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </BrowserRouter>
  );
}
