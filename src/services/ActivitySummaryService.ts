import { GroupActivityService } from './GroupActivityService';
import { Um2DBInterface } from '../db/Um2DBInterface';
import { AggregateDBInterface } from '../db/AggregateDBInterface';
import { NON_STUDENTS, NON_SESSIONS } from '../common';

export interface ActivitySummaryParams {
  groupIds: string[];
  header: boolean;
  filename: string;
  users?: string[];
  fromDate: string;
  toDate: string;
  timebins?: number[];
  sessionate: boolean;
  minThreshold: number;
  queryArchive: boolean;
}

export class ActivitySummaryService {
  private groupActivityService: GroupActivityService;

  constructor(um2Db: Um2DBInterface, aggregateDb: AggregateDBInterface) {
    this.groupActivityService = new GroupActivityService(um2Db, aggregateDb);
  }

  async getActivitySummary(params: ActivitySummaryParams): Promise<string> {
    const nonStudents = [...NON_STUDENTS];
    const dateRange = [params.fromDate, params.toDate];
    let output = '';

    for (const groupId of params.groupIds) {
      const groupActivity = await this.groupActivityService.getGroupActivity(
        groupId,
        nonStudents,
        NON_SESSIONS,
        true,
        dateRange,
        params.queryArchive,
        params.sessionate,
        params.minThreshold,
        params.timebins || null
      );

      if (groupActivity.size === 0) {
        return 'no activity found';
      }

      if (params.header && output === '') {
        output += this.getHeader();
      }

      const users = params.users || Array.from(groupActivity.keys());
      for (const userLogin of users) {
        const user = groupActivity.get(userLogin);
        if (!user) {
          output += this.formatEmptyUser(userLogin, groupId);
        } else {
          output += this.formatUserSummary(user, groupId, params.timebins);
        }
      }
    }

    return output;
  }

  private getHeader(): string {
    // Simplified header - full implementation would include all summary fields
    return 'user,group,sessions_dist,question_attempts,question_attempts_success\n';
  }

  private formatUserSummary(user: any, groupId: string, timebins?: number[]): string {
    // Simplified - full implementation would include all summary metrics
    return `${user.userLogin},${groupId},${user.summary.sessions_dist || 0},${user.summary.question_attempts || 0},${user.summary.question_attempts_success || 0}\n`;
  }

  private formatEmptyUser(userLogin: string, groupId: string): string {
    return `${userLogin},${groupId},0,0,0\n`;
  }
}

