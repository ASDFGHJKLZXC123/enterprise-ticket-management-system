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
import { Link as RouterLink, Navigate, useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';
import { extractErrorMessage } from '../api/client';

export default function RegisterPage() {
  const { user, register } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [fullName, setFullName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  if (user) {
    const from = (location.state as { from?: string } | null)?.from ?? '/dashboard';
    return <Navigate to={from} replace />;
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);

    if (password.length < 8) {
      setError('Password must be at least 8 characters.');
      return;
    }
    if (password !== confirmPassword) {
      setError('Passwords do not match.');
      return;
    }

    setSubmitting(true);
    try {
      await register({
        fullName: fullName.trim(),
        email: email.trim(),
        password,
      });
      const from = (location.state as { from?: string } | null)?.from ?? '/dashboard';
      navigate(from, { replace: true });
    } catch (err) {
      setError(extractErrorMessage(err, 'Registration failed'));
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
      <Card variant="outlined" sx={{ width: 420 }}>
        <CardContent sx={{ p: 3 }}>
          <Stack spacing={2} component="form" onSubmit={handleSubmit}>
            <Stack direction="row" spacing={1} alignItems="center">
              <TicketIcon size={24} color="#1f6feb" />
              <Typography variant="h6" fontWeight={700}>
                Enterprise Tickets
              </Typography>
            </Stack>
            <Typography variant="body2" color="text.secondary">
              Register for a requester account.
            </Typography>
            {error && <Alert severity="error">{error}</Alert>}
            <TextField
              label="Full name"
              value={fullName}
              onChange={(e) => setFullName(e.target.value)}
              autoComplete="name"
              required
              fullWidth
              autoFocus
            />
            <TextField
              label="Email"
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              autoComplete="username"
              required
              fullWidth
            />
            <TextField
              label="Password"
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              autoComplete="new-password"
              helperText="At least 8 characters"
              required
              fullWidth
            />
            <TextField
              label="Confirm password"
              type="password"
              value={confirmPassword}
              onChange={(e) => setConfirmPassword(e.target.value)}
              autoComplete="new-password"
              required
              fullWidth
            />
            <Button
              type="submit"
              variant="contained"
              disabled={submitting || !fullName.trim() || !email.trim() || password.length < 8 || password !== confirmPassword}
              fullWidth
            >
              {submitting ? 'Creating account…' : 'Create account'}
            </Button>
            <Typography variant="body2" color="text.secondary" textAlign="center">
              Already registered?{' '}
              <Link component={RouterLink} to="/login" underline="hover">
                Sign in
              </Link>
            </Typography>
          </Stack>
        </CardContent>
      </Card>
    </Box>
  );
}

