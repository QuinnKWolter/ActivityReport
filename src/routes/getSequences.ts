import { Router, Request, Response } from 'express';
import { GetSequencesService } from '../services/GetSequencesService';
import { config } from '../config';
import { Um2DBInterface } from '../db/Um2DBInterface';
import { AggregateDBInterface } from '../db/AggregateDBInterface';

const router = Router();
const um2Db = new Um2DBInterface(config.um2);
const aggregateDb = new AggregateDBInterface(config.aggregate);
const service = new GetSequencesService(um2Db, aggregateDb);

const handleRequest = async (req: Request, res: Response) => {
  try {
    const groupIds = req.query.grp ? (req.query.grp as string).split(/\s*[,\t]+\s*/) : null;
    if (!groupIds || groupIds.length === 0) {
      return res.status(400).send('group identifier not provided or invalid');
    }

    const params = {
      groupIds,
      mode: req.query.mode ? parseInt(req.query.mode as string, 10) : 0,
      include: req.query.include ? parseInt(req.query.include as string, 10) : 0,
      extended: req.query.extended !== undefined,
      pexspam: req.query.pexspam !== undefined,
      labelmap: req.query.labelmap !== undefined,
      half: req.query.half ? parseInt(req.query.half as string, 10) : 0,
      header: req.query.header === 'yes',
      delimiter: (req.query.delimiter as string) || config.delimiter,
      filename: (req.query.filename as string) || `${groupIds.join('_')}_sequences.txt`,
      users: req.query.usr ? (req.query.usr as string).split(/\s*[,\t]+\s*/) : undefined,
      fromDate: (req.query.fromDate as string) || '',
      toDate: (req.query.toDate as string) || '',
      includeSvc: req.query.svc === 'yes',
      includeAllParameters: req.query.allparameters === 'yes',
      removeUsers: req.query.removeUsr ? (req.query.removeUsr as string).split(/\s*[,\t]+\s*/) : [],
      timeLabels: req.query.timelabels as string | undefined,
      replaceExtTimes: req.query.replaceexttimes !== undefined,
      markRepetition: req.query.markrepetition !== undefined,
      markRepetitionSeq: req.query.markrepetitionseq !== undefined,
      queryArchive: req.query.queryArchive !== 'no',
    };

    const result = await service.getSequences(params);

    res.setHeader('Content-Type', 'text/plain');
    res.setHeader('Content-Disposition', `attachment;filename=${params.filename}`);
    res.send(result);
  } catch (error) {
    console.error('Error in GetSequences:', error);
    res.status(500).send('Internal server error');
  }
};

router.get('/', handleRequest);
router.post('/', handleRequest);

export { router as getSequencesRouter };

