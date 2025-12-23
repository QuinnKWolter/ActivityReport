import { User } from '../models/User';
import { LoggedActivity } from '../models/LoggedActivity';
import { Activity } from '../models/Activity';
import { PCEXActivity } from '../models/PCEXActivity';
import { Um2DBInterface } from '../db/Um2DBInterface';
import { AggregateDBInterface } from '../db/AggregateDBInterface';
import { NON_STUDENTS, NON_SESSIONS } from '../common';

export class GroupActivityService {
  private um2Db: Um2DBInterface;
  private aggregateDb: AggregateDBInterface;

  constructor(um2Db: Um2DBInterface, aggregateDb: AggregateDBInterface) {
    this.um2Db = um2Db;
    this.aggregateDb = aggregateDb;
  }

  async getGroupActivity(
    grp: string,
    nonStudents: string[],
    nonSessions: string[],
    getSummary: boolean,
    dateRange: string[],
    queryArchive: boolean,
    resetSessions: boolean = false,
    sessionMinThreshold: number = 90,
    timeBins: number[] | null = null
  ): Promise<Map<string, User>> {
    try {
      // Get topic maps
      const topicMap = await this.aggregateDb.getActivityTopicMap(grp);
      let topicMap0: Map<string, Activity> | null = null;
      if (grp === 'ASUFALL2014') {
        topicMap0 = await this.aggregateDb.getActivityTopicMap('IS172014Spring');
      }
      if (!topicMap || topicMap.size === 0) {
        // Fallback logic
        const progressorMap = await this.aggregateDb.getActivityTopicMap('progressor_plus');
        const aggMap = await this.aggregateDb.getActivityTopicMap('IS172014Spring');
        for (const [key, value] of aggMap) {
          if (!topicMap.has(key)) {
            topicMap.set(key, value);
          }
        }
      }

      const pcexActivityTopicMap = await this.aggregateDb.getPCEXActivityTopicMap(grp);
      const pcexActivitySet = await this.aggregateDb.getPCEXActivitySet();
      const um2ActivityNameMap = await this.um2Db.getActivityName();
      const sqlknotUrlToActivityNameMap = await this.aggregateDb.getUrlToActivityName();

      // Get activities from both databases
      const grpActivityAggregate = await this.aggregateDb.getActivity(
        grp,
        nonStudents,
        nonSessions,
        dateRange
      );
      console.log('Finish getting activities from aggregate!');

      const grpActivityUm2 = await this.um2Db.getActivity(
        grp,
        nonStudents,
        nonSessions,
        topicMap,
        topicMap0,
        pcexActivityTopicMap,
        um2ActivityNameMap,
        sqlknotUrlToActivityNameMap,
        dateRange,
        queryArchive
      );
      console.log('Finish getting activities from um2!');

      // Merge activities
      const grpActivity = this.mergeActivity(grpActivityUm2, grpActivityAggregate);
      console.log('Finish merging!');

      // Process each user
      for (const user of grpActivity.values()) {
        if (resetSessions) {
          user.generateSessionIds(sessionMinThreshold);
        }
        user.computeActivityTimes();
        user.computeAttemptNo();
        // PCEX set completion rate computation would go here
      }

      return grpActivity;
    } catch (error) {
      console.error('Error in getGroupActivity:', error);
      throw error;
    }
  }

  private mergeActivity(
    grpActivityUm2: Map<string, User>,
    grpActivityAggregate: Map<string, User>
  ): Map<string, User> {
    const res = new Map<string, User>(grpActivityUm2);

    for (const [key, user] of grpActivityAggregate) {
      if (res.has(key)) {
        const existingUser = res.get(key)!;
        existingUser.activity.push(...user.activity);
        existingUser.activity.sort((a, b) => a.compareTo(b));
      } else {
        res.set(key, user);
      }
    }

    // Sort all activities by date
    for (const user of res.values()) {
      user.activity.sort((a, b) => a.compareTo(b));
    }

    return res;
  }
}

