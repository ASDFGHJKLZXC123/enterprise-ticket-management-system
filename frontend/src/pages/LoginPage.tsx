import { useState } from 'react';
import {
  Alert,
  Box,
  Button,
  Card,
  CardContent,
  Link,
  Stack,
  TextField,
  Typography,
} from '@mui/material';
import { Ticket as TicketIcon } from 'lucide-react';
import { Link as RouterLink, useLocation, useNavigate, Navigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';
import { extractErrorMessage } from '../api/client';

export default function LoginPage() {
  const { user, login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  if (user) {
    const from = (location.state as { from?: string } | null)?.from ?? '/dashboard';
    return <Navigate to={from} replace />;
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setSubmitting(true);
    try {
      await login({ email: email.trim(), password });
      const from = (location.state as { from?: string } | null)?.from ?? '/dashboard';
      navigate(from, { replace: true });
    } catch (err) {
      setError(extractErrorMessage(err, 'Invalid email or password'));
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <Box
      sx={{
        minHeight: '100vh',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        bgcolor: 'background.default',
        p: 2,
      }}
    >
      <Card variant="outlined" sx={{ width: 380 }}>
        <CardContent sx={{ p: 3 }}>
          <Stack spacing={2} component="form" onSubmit={handleSubmit}>
            <Stack direction="row" spacing={1} alignItems="center">
              <TicketIcon size={24} color="#1f6feb" />
              <Typography variant="h6" fontWeight={700}>
                Enterprise Tickets
              </Typography>
            </Stack>
            <Typography variant="body2" color="text.secondary">
              Sign in to manage support tickets.
            </Typography>
            {error && <Alert severity="error">{error}</Alert>}
            <TextField
              label="Email"
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              autoComplete="username"
              required
              fullWidth
              autoFocus
            />
            <TextField
              label="Password"
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              autoComplete="current-password"
              required
              fullWidth
            />
            <Button type="submit" variant="contained" disabled={submitting} fullWidth>
              {submitting ? 'Signing in…' : 'Sign in'}
            </Button>
            <Typography variant="body2" color="text.secondary" textAlign="center">
              Need an account?{' '}
              <Link component={RouterLink} to="/register" underline="hover">
                Register
              </Link>
            </Typography>
          </Stack>
        </CardContent>
      </Card>
    </Box>
  );
}
