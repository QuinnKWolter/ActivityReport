import { Router, Request, Response } from 'express';
import { ActivitySummaryService } from '../services/ActivitySummaryService';
import { config } from '../config';
import { Um2DBInterface } from '../db/Um2DBInterface';
import { AggregateDBInterface } from '../db/AggregateDBInterface';

const router = Router();
const um2Db = new Um2DBInterface(config.um2);
const aggregateDb = new AggregateDBInterface(config.aggregate);
const service = new ActivitySummaryService(um2Db, aggregateDb);

const handleRequest = async (req: Request, res: Response) => {
  try {
    const groupIds = req.query.grp ? (req.query.grp as string).split(/\s*[,\t]+\s*/) : null;
    if (!groupIds || groupIds.length === 0) {
      return res.status(400).send('group identifier not provided or invalid');
    }

    const params = {
      groupIds,
      header: req.query.header === 'yes',
      filename: (req.query.filename as string) || `${groupIds.join('_')}_summary.txt`,
      users: req.query.usr ? (req.query.usr as string).split(/\s*[,\t]+\s*/) : undefined,
      fromDate: (req.query.fromDate as string) || '',
      toDate: (req.query.toDate as string) || '',
      timebins: req.query.timebins ? (req.query.timebins as string).split(',').map(t => parseInt(t, 10)) : undefined,
      sessionate: req.query.sessionate !== undefined,
      minThreshold: req.query.minthreshold ? parseInt(req.query.minthreshold as string, 10) : 90,
      queryArchive: req.query.queryArchive !== 'no',
    };

    const result = await service.getActivitySummary(params);

    res.setHeader('Content-Type', 'text/plain');
    res.setHeader('Content-Disposition', `attachment;filename=${params.filename}`);
    res.send(result);
  } catch (error) {
    console.error('Error in ActivitySummary:', error);
    res.status(500).send('Internal server error');
  }
};

router.get('/', handleRequest);
router.post('/', handleRequest);

export { router as activitySummaryRouter };

