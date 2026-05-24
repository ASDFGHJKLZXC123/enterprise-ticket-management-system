import {
  AppBar,
  Avatar,
  Box,
  Chip,
  Divider,
  Drawer,
  List,
  ListItemButton,
  ListItemIcon,
  ListItemText,
  Toolbar,
  Tooltip,
  Typography,
} from '@mui/material';
import {
  LayoutDashboard,
  LogOut,
  PlusCircle,
  Ticket as TicketIcon,
  Users,
} from 'lucide-react';
import type { ReactNode } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';
import { ROLE_LABELS } from '../auth/permissions';
import type { Role } from '../types/user';

const DRAWER_WIDTH = 232;

interface NavItem {
  label: string;
  path: string;
  icon: ReactNode;
  visible: (role: Role) => boolean;
}

const NAV_ITEMS: NavItem[] = [
  { label: 'Dashboard', path: '/dashboard', icon: <LayoutDashboard size={18} />, visible: () => true },
  { label: 'Tickets', path: '/tickets', icon: <TicketIcon size={18} />, visible: () => true },
  {
    label: 'New Ticket',
    path: '/tickets/new',
    icon: <PlusCircle size={18} />,
    visible: () => true,
  },
  {
    label: 'Users',
    path: '/users',
    icon: <Users size={18} />,
    visible: (r) => r === 'ADMIN',
  },
];

export default function AppShell({ children }: { children: ReactNode }) {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  if (!user) return <>{children}</>;

  const initials = user.fullName
    .split(' ')
    .map((p) => p[0])
    .slice(0, 2)
    .join('')
    .toUpperCase();

  return (
    <Box sx={{ display: 'flex', minHeight: '100vh' }}>
      <AppBar
        position="fixed"
        color="inherit"
        elevation={0}
        sx={{ zIndex: (t) => t.zIndex.drawer + 1, borderBottom: 1, borderColor: 'divider' }}
      >
        <Toolbar variant="dense" sx={{ gap: 1 }}>
          <TicketIcon size={20} color="#1f6feb" />
          <Typography variant="subtitle1" sx={{ fontWeight: 700 }}>
            Enterprise Tickets
          </Typography>
          <Box sx={{ flexGrow: 1 }} />
          <Chip size="small" label={ROLE_LABELS[user.role]} variant="outlined" />
          <Tooltip title={user.email}>
            <Avatar sx={{ width: 30, height: 30, fontSize: 13, bgcolor: 'primary.main' }}>
              {initials}
            </Avatar>
          </Tooltip>
          <Tooltip title="Sign out">
            <ListItemButton
              onClick={logout}
              sx={{ flexGrow: 0, borderRadius: 1, px: 1 }}
              aria-label="Sign out"
            >
              <LogOut size={18} />
            </ListItemButton>
          </Tooltip>
        </Toolbar>
      </AppBar>

      <Drawer
        variant="permanent"
        sx={{
          width: DRAWER_WIDTH,
          flexShrink: 0,
          '& .MuiDrawer-paper': { width: DRAWER_WIDTH, boxSizing: 'border-box' },
        }}
      >
        <Toolbar variant="dense" />
        <Box sx={{ overflow: 'auto', py: 1 }}>
          <List dense>
            {NAV_ITEMS.filter((item) => item.visible(user.role)).map((item) => {
              const selected =
                location.pathname === item.path ||
                (item.path !== '/dashboard' && item.path !== '/tickets/new' &&
                  location.pathname.startsWith(item.path));
              return (
                <ListItemButton
                  key={item.path}
                  selected={selected}
                  onClick={() => navigate(item.path)}
                  sx={{ borderRadius: 1, mx: 1, mb: 0.25 }}
                >
                  <ListItemIcon sx={{ minWidth: 32 }}>{item.icon}</ListItemIcon>
                  <ListItemText primaryTypographyProps={{ variant: 'body2', fontWeight: 600 }}>
                    {item.label}
                  </ListItemText>
                </ListItemButton>
              );
            })}
          </List>
          <Divider sx={{ my: 1 }} />
          <Box sx={{ px: 2 }}>
            <Typography variant="caption" color="text.secondary">
              Signed in as
            </Typography>
            <Typography variant="body2" fontWeight={600} noWrap>
              {user.fullName}
            </Typography>
          </Box>
        </Box>
      </Drawer>

      <Box component="main" sx={{ flexGrow: 1, p: 2.5, width: `calc(100% - ${DRAWER_WIDTH}px)` }}>
        <Toolbar variant="dense" />
        {children}
      </Box>
    </Box>
  );
}
