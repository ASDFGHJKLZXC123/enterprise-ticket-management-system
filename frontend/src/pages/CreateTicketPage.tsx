import { useState } from 'react';
import { Alert, Breadcrumbs, Card, CardContent, Link, Stack, Typography } from '@mui/material';
import { useNavigate } from 'react-router-dom';
import TicketForm from '../components/TicketForm';
import { ticketsApi } from '../api/tickets';
import { extractErrorMessage } from '../api/client';
import type { CreateTicketRequest } from '../types/ticket';

export default function CreateTicketPage() {
  const navigate = useNavigate();
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  const handleSubmit = async (payload: CreateTicketRequest) => {
    setError(null);
    setSubmitting(true);
    try {
      const created = await ticketsApi.create(payload);
      navigate(`/tickets/${created.id}`, { replace: true });
    } catch (err) {
      setError(extractErrorMessage(err));
      setSubmitting(false);
    }
  };

  return (
    <Stack spacing={2}>
      <Breadcrumbs>
        <Link component="button" underline="hover" onClick={() => navigate('/tickets')}>
          Tickets
        </Link>
        <Typography color="text.primary">New</Typography>
      </Breadcrumbs>
      <Typography variant="h5">Create ticket</Typography>
      {error && <Alert severity="error">{error}</Alert>}
      <Card variant="outlined">
        <CardContent>
          <TicketForm
            submitting={submitting}
            onSubmit={handleSubmit}
            onCancel={() => navigate('/tickets')}
          />
        </CardContent>
      </Card>
    </Stack>
  );
}
