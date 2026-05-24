import { Card, CardContent, Stack, Typography } from '@mui/material';
import type { ReactNode } from 'react';

interface DashboardCardProps {
  label: string;
  value: ReactNode;
  icon?: ReactNode;
  accent?: string;
}

export default function DashboardCard({ label, value, icon, accent }: DashboardCardProps) {
  return (
    <Card variant="outlined" sx={{ height: '100%' }}>
      <CardContent sx={{ py: 1.5, '&:last-child': { pb: 1.5 } }}>
        <Stack direction="row" justifyContent="space-between" alignItems="flex-start">
          <Stack spacing={0.5}>
            <Typography variant="caption" color="text.secondary" sx={{ textTransform: 'uppercase', letterSpacing: 0.5 }}>
              {label}
            </Typography>
            <Typography variant="h5" sx={{ color: accent }}>
              {value}
            </Typography>
          </Stack>
          {icon && <span style={{ color: accent, opacity: 0.8 }}>{icon}</span>}
        </Stack>
      </CardContent>
    </Card>
  );
}
