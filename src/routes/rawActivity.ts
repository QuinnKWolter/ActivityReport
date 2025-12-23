import { Router, Request, Response } from 'express';
import { RawActivityService } from '../services/RawActivityService';
import { config } from '../config';
import { Um2DBInterface } from '../db/Um2DBInterface';
import { AggregateDBInterface } from '../db/AggregateDBInterface';

const router = Router();
const um2Db = new Um2DBInterface(config.um2);
const aggregateDb = new AggregateDBInterface(config.aggregate);
const service = new RawActivityService(um2Db, aggregateDb);

const handleRequest = async (req: Request, res: Response) => {
  try {
    const groupIds = req.query.grp ? (req.query.grp as string).split(/\s*[,\t]+\s*/) : null;
    if (!groupIds || groupIds.length === 0) {
      return res.status(400).send('group identifier not provided or invalid');
    }

    const params = {
      groupIds,
      header: req.query.header === 'yes',
      delimiter: (req.query.delimiter as string) || config.delimiter,
      fromDate: (req.query.fromDate as string) || '',
      toDate: (req.query.toDate as string) || '',
      filename: (req.query.filename as string) || `${groupIds.join('_')}_raw_activity.txt`,
      includeSvc: req.query.svc === 'yes',
      includeAllParameters: req.query.allparameters === 'yes',
      removeUsers: req.query.removeUsr ? (req.query.removeUsr as string).split(/\s*[,\t]+\s*/) : [],
      excludeAppIds: req.query.excludeApp ? (req.query.excludeApp as string).split(/\s*[,\t]+\s*/) : [],
      sessionate: req.query.sessionate !== undefined,
      minThreshold: req.query.minthreshold ? parseInt(req.query.minthreshold as string, 10) : 90,
      timeLabels: req.query.timelabels as string | undefined,
      replaceExtTimes: req.query.replaceexttimes !== undefined,
      jsonOutput: req.query.jsonOutput === 'yes',
      queryArchive: req.query.queryArchive !== 'no',
    };

    const result = await service.getRawActivity(params);

    res.setHeader('Content-Type', params.jsonOutput ? 'application/json' : 'text/plain');
    res.setHeader('Content-Disposition', `attachment;filename=${params.filename}`);
    res.send(result);
  } catch (error) {
    console.error('Error in RawActivity:', error);
    res.status(500).send('Internal server error');
  }
};

router.get('/', handleRequest);
router.post('/', handleRequest);

export { router as rawActivityRouter };

