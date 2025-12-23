import express, { Request, Response } from 'express';
import cors from 'cors';
import helmet from 'helmet';
import { config } from './config';
import { rawActivityRouter } from './routes/rawActivity';
import { activitySummaryRouter } from './routes/activitySummary';
import { getSequencesRouter } from './routes/getSequences';
import { fixTrackingRouter } from './routes/fixTracking';

const app = express();

// Middleware
app.use(helmet());
app.use(cors());
app.use(express.json());
app.use(express.urlencoded({ extended: true }));

// Routes
app.use('/ActivityReport', rawActivityRouter);
app.use('/ActivitySummary', activitySummaryRouter);
app.use('/GetSequences', getSequencesRouter);
app.use('/FixTracking', fixTrackingRouter);

// Health check
app.get('/health', (req: Request, res: Response) => {
  res.json({ status: 'ok' });
});

// Start server
app.listen(config.port, () => {
  console.log(`ActivityReport service running on port ${config.port}`);
});

export default app;

