import { GroupActivityService } from './GroupActivityService';
import { Um2DBInterface } from '../db/Um2DBInterface';
import { AggregateDBInterface } from '../db/AggregateDBInterface';
import { NON_STUDENTS, NON_SESSIONS } from '../common';
import { CsvOutputFormatter } from '../output/CsvOutputFormatter';
import { JsonOutputFormatter } from '../output/JsonOutputFormatter';

export interface RawActivityParams {
  groupIds: string[];
  header: boolean;
  delimiter: string;
  fromDate: string;
  toDate: string;
  filename: string;
  includeSvc: boolean;
  includeAllParameters: boolean;
  removeUsers: string[];
  excludeAppIds: string[];
  sessionate: boolean;
  minThreshold: number;
  timeLabels?: string;
  replaceExtTimes: boolean;
  jsonOutput: boolean;
  queryArchive: boolean;
}

export class RawActivityService {
  private groupActivityService: GroupActivityService;

  constructor(um2Db: Um2DBInterface, aggregateDb: AggregateDBInterface) {
    this.groupActivityService = new GroupActivityService(um2Db, aggregateDb);
  }

  async getRawActivity(params: RawActivityParams): Promise<string> {
    const nonStudents = [...NON_STUDENTS, ...params.removeUsers];
    const dateRange = [params.fromDate, params.toDate];

    const outputFormatter = params.jsonOutput
      ? new JsonOutputFormatter()
      : new CsvOutputFormatter(params.delimiter);

    let output = '';

    for (const groupId of params.groupIds) {
      const groupActivity = await this.groupActivityService.getGroupActivity(
        groupId,
        nonStudents,
        NON_SESSIONS,
        false,
        dateRange,
        params.queryArchive,
        params.sessionate,
        params.minThreshold
      );

      if (groupActivity.size === 0) {
        return 'no activity found';
      }

      if (params.header && output === '' && !params.jsonOutput) {
        output += outputFormatter.getHeader(params);
      }

      for (const user of groupActivity.values()) {
        for (const act of user.activity) {
          if (!params.excludeAppIds.includes(act.appId.toString())) {
            const formatted = outputFormatter.formatActivity(act, user, params);
            if (formatted) {
              output += formatted;
            }
          }
        }
      }
    }

    if (params.jsonOutput && outputFormatter instanceof JsonOutputFormatter) {
      return outputFormatter.getOutput();
    }

    return output;
  }
}

