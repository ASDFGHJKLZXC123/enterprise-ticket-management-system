import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom';
import { AuthProvider } from './auth/AuthContext';
import ProtectedRoute from './components/ProtectedRoute';
import AppShell from './components/AppShell';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import DashboardPage from './pages/DashboardPage';
import TicketListPage from './pages/TicketListPage';
import TicketDetailPage from './pages/TicketDetailPage';
import CreateTicketPage from './pages/CreateTicketPage';
import UserManagementPage from './pages/UserManagementPage';

/** Wraps a page in the authenticated shell + route guard. */
function Shell({ children, roles }: { children: React.ReactNode; roles?: ('ADMIN' | 'MANAGER' | 'AGENT' | 'REQUESTER')[] }) {
  return (
    <ProtectedRoute roles={roles}>
      <AppShell>{children}</AppShell>
    </ProtectedRoute>
  );
}

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />

          <Route path="/dashboard" element={<Shell><DashboardPage /></Shell>} />
          <Route path="/tickets" element={<Shell><TicketListPage /></Shell>} />
          <Route path="/tickets/new" element={<Shell><CreateTicketPage /></Shell>} />
          <Route path="/tickets/:id" element={<Shell><TicketDetailPage /></Shell>} />
          <Route
            path="/users"
            element={
              <Shell roles={['ADMIN']}>
                <UserManagementPage />
              </Shell>
            }
          />

          <Route path="/" element={<Navigate to="/dashboard" replace />} />
          <Route path="*" element={<Navigate to="/dashboard" replace />} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
}
