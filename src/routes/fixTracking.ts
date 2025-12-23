import { Router, Request, Response } from 'express';
import { FixTrackingService } from '../services/FixTrackingService';
import { config } from '../config';
import { Um2DBInterface } from '../db/Um2DBInterface';

const router = Router();
const um2Db = new Um2DBInterface(config.um2);
const service = new FixTrackingService(um2Db);

const handleRequest = async (req: Request, res: Response) => {
  try {
    const archive = req.query.archive !== undefined;
    const result = await service.getFixTrackingScripts(archive);

    res.setHeader('Content-Type', 'text/plain');
    res.setHeader('Content-Disposition', 'attachment;filename=update_bad_rows2.txt');
    res.send(result);
  } catch (error) {
    console.error('Error in FixTracking:', error);
    res.status(500).send('Internal server error');
  }
};

router.get('/', handleRequest);
router.post('/', handleRequest);

export { router as fixTrackingRouter };

