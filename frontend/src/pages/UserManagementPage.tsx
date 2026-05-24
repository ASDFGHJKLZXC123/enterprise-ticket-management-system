import { useEffect, useState } from 'react';
import {
  Alert,
  Box,
  Button,
  Card,
  CardContent,
  Chip,
  CircularProgress,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  MenuItem,
  Stack,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableRow,
  TextField,
  Typography,
} from '@mui/material';
import { Plus } from 'lucide-react';
import { usersApi } from '../api/users';
import { extractErrorMessage } from '../api/client';
import { ROLE_LABELS } from '../auth/permissions';
import { useAuth } from '../auth/AuthContext';
import type { CreateUserRequest, Role, User } from '../types/user';

const ROLES: Role[] = ['ADMIN', 'MANAGER', 'AGENT', 'REQUESTER'];

const EMPTY_FORM: CreateUserRequest = { email: '', password: '', fullName: '', role: 'REQUESTER' };

export default function UserManagementPage() {
  const { user: currentUser } = useAuth();
  const [users, setUsers] = useState<User[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [actionError, setActionError] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);

  const [dialogOpen, setDialogOpen] = useState(false);
  const [form, setForm] = useState<CreateUserRequest>(EMPTY_FORM);

  const load = () => {
    setLoading(true);
    setError(null);
    usersApi
      .list()
      .then(setUsers)
      .catch((err) => setError(extractErrorMessage(err)))
      .finally(() => setLoading(false));
  };

  useEffect(load, []);

  const runAction = async (fn: () => Promise<unknown>): Promise<boolean> => {
    setActionError(null);
    setBusy(true);
    try {
      await fn();
      load();
      return true;
    } catch (err) {
      setActionError(extractErrorMessage(err));
      return false;
    } finally {
      setBusy(false);
    }
  };

  const handleCreate = async () => {
    const ok = await runAction(() => usersApi.create(form));
    if (!ok) {
      return;
    }
      setDialogOpen(false);
      setForm(EMPTY_FORM);
  };

  if (loading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', py: 8 }}>
        <CircularProgress />
      </Box>
    );
  }

  if (error) return <Alert severity="error">{error}</Alert>;

  const formValid =
    form.email.trim() && form.password.length >= 8 && form.fullName.trim();

  return (
    <Stack spacing={2}>
      <Stack direction="row" justifyContent="space-between" alignItems="center">
        <Typography variant="h5">Users</Typography>
        <Button variant="contained" startIcon={<Plus size={16} />} onClick={() => setDialogOpen(true)}>
          New user
        </Button>
      </Stack>

      {actionError && <Alert severity="error" onClose={() => setActionError(null)}>{actionError}</Alert>}

      <Card variant="outlined">
        <CardContent sx={{ p: 0, '&:last-child': { pb: 0 } }}>
          <Table size="small">
            <TableHead>
              <TableRow>
                <TableCell sx={{ fontWeight: 700 }}>Name</TableCell>
                <TableCell sx={{ fontWeight: 700 }}>Email</TableCell>
                <TableCell sx={{ fontWeight: 700 }}>Role</TableCell>
                <TableCell sx={{ fontWeight: 700 }}>Status</TableCell>
                <TableCell sx={{ fontWeight: 700 }} align="right">
                  Actions
                </TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {users.map((u) => {
                const isSelf = u.id === currentUser?.id;
                return (
                  <TableRow key={u.id} hover>
                    <TableCell>{u.fullName}</TableCell>
                    <TableCell>{u.email}</TableCell>
                    <TableCell sx={{ minWidth: 150 }}>
                      <TextField
                        select
                        size="small"
                        value={u.role}
                        disabled={busy || isSelf}
                        onChange={(e) =>
                          runAction(() => usersApi.update(u.id, { role: e.target.value as Role }))
                        }
                        sx={{ minWidth: 130 }}
                      >
                        {ROLES.map((r) => (
                          <MenuItem key={r} value={r}>
                            {ROLE_LABELS[r]}
                          </MenuItem>
                        ))}
                      </TextField>
                    </TableCell>
                    <TableCell>
                      <Chip
                        size="small"
                        label={u.active ? 'Active' : 'Inactive'}
                        color={u.active ? 'success' : 'default'}
                        variant="outlined"
                      />
                    </TableCell>
                    <TableCell align="right">
                      {u.active ? (
                        <Button
                          size="small"
                          color="warning"
                          disabled={busy || isSelf}
                          onClick={() => runAction(() => usersApi.deactivate(u.id))}
                        >
                          Deactivate
                        </Button>
                      ) : (
                        <Button
                          size="small"
                          color="success"
                          disabled={busy}
                          onClick={() => runAction(() => usersApi.reactivate(u.id))}
                        >
                          Reactivate
                        </Button>
                      )}
                    </TableCell>
                  </TableRow>
                );
              })}
            </TableBody>
          </Table>
        </CardContent>
      </Card>

      <Dialog open={dialogOpen} onClose={() => setDialogOpen(false)} fullWidth maxWidth="xs">
        <DialogTitle>Create user</DialogTitle>
        <DialogContent>
          <Stack spacing={2} sx={{ mt: 1 }}>
            <TextField
              label="Full name"
              value={form.fullName}
              onChange={(e) => setForm({ ...form, fullName: e.target.value })}
              fullWidth
              required
            />
            <TextField
              label="Email"
              type="email"
              value={form.email}
              onChange={(e) => setForm({ ...form, email: e.target.value })}
              fullWidth
              required
            />
            <TextField
              label="Password"
              type="password"
              value={form.password}
              onChange={(e) => setForm({ ...form, password: e.target.value })}
              helperText="At least 8 characters"
              fullWidth
              required
            />
            <TextField
              select
              label="Role"
              value={form.role}
              onChange={(e) => setForm({ ...form, role: e.target.value as Role })}
              fullWidth
            >
              {ROLES.map((r) => (
                <MenuItem key={r} value={r}>
                  {ROLE_LABELS[r]}
                </MenuItem>
              ))}
            </TextField>
          </Stack>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDialogOpen(false)}>Cancel</Button>
          <Button variant="contained" onClick={handleCreate} disabled={busy || !formValid}>
            Create
          </Button>
        </DialogActions>
      </Dialog>
    </Stack>
  );
}
