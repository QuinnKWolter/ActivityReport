import { GroupActivityService } from './GroupActivityService';
import { Um2DBInterface } from '../db/Um2DBInterface';
import { AggregateDBInterface } from '../db/AggregateDBInterface';
import { NON_STUDENTS, NON_SESSIONS } from '../common';

export interface GetSequencesParams {
  groupIds: string[];
  mode: number;
  include: number;
  extended: boolean;
  pexspam: boolean;
  labelmap: boolean;
  half: number;
  header: boolean;
  delimiter: string;
  filename: string;
  users?: string[];
  fromDate: string;
  toDate: string;
  includeSvc: boolean;
  includeAllParameters: boolean;
  removeUsers: string[];
  timeLabels?: string;
  replaceExtTimes: boolean;
  markRepetition: boolean;
  markRepetitionSeq: boolean;
  queryArchive: boolean;
}

export class GetSequencesService {
  private groupActivityService: GroupActivityService;

  constructor(um2Db: Um2DBInterface, aggregateDb: AggregateDBInterface) {
    this.groupActivityService = new GroupActivityService(um2Db, aggregateDb);
  }

  async getSequences(params: GetSequencesParams): Promise<string> {
    const nonStudents = [...NON_STUDENTS, ...params.removeUsers];
    const dateRange = [params.fromDate, params.toDate];
    let output = '';

    for (const groupId of params.groupIds) {
      const groupActivity = await this.groupActivityService.getGroupActivity(
        groupId,
        nonStudents,
        NON_SESSIONS,
        false,
        dateRange,
        params.queryArchive,
        false,
        90
      );

      if (groupActivity.size === 0) {
        return 'no activity found';
      }

      // Sequence generation logic would go here
      // This is a simplified stub
      output += `Sequences for group ${groupId}\n`;
    }

    return output;
  }
}

