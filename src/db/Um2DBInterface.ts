import { DatabaseInterface } from './DatabaseInterface';
import { DatabaseConfig } from '../config';
import { User } from '../models/User';
import { LoggedActivity, LogType } from '../models/LoggedActivity';
import { Activity } from '../models/Activity';
import { PCEXActivity } from '../models/PCEXActivity';
import { csvFromArray, compareStringDates } from '../common';
import { AppId } from '../common';

export class Um2DBInterface extends DatabaseInterface {
  constructor(config: DatabaseConfig) {
    super(config);
  }

  async getActivity(
    grp: string,
    nonStudents: string[],
    nonSessions: string[],
    topicMap: Map<string, Activity> | null,
    topicMap0: Map<string, Activity> | null,
    pcexActivityTopicMap: Map<string, PCEXActivity> | null,
    activityNameMap: Map<string, string> | null,
    sqlknotUrlToActivityNameMap: Map<string, string> | null,
    dateRange: string[],
    queryArchive: boolean
  ): Promise<Map<string, User>> {
    const nonStudentsStr = csvFromArray(nonStudents);
    const nonSessionsStr = csvFromArray(nonSessions);

    const res = new Map<string, User>();
    const connection = await this.getConnection();

    try {
      const table = queryArchive ? 'archive_user_activity' : 'ent_user_activity';
      let query = `
        SELECT 
          UA.AppID, UA.UserID, U.Login, UA.ActivityID, UA.Result, UA.\`Session\`, 
          UA.DateNTime, UA.DateNTimeNS, UNIX_TIMESTAMP(UA.DateNTime) as utimestamp, 
          UA.SVC, UA.AllParameters 
        FROM ent_user U, ${table} UA 
        WHERE 
          UA.GroupID = (SELECT UserID FROM ent_user WHERE IsGroup = 1 AND Login = ?)
          AND U.UserID = UA.UserID
      `;

      const params: any[] = [grp];

      if (nonStudentsStr) {
        query += ` AND U.Login NOT IN (${nonStudentsStr})`;
        query += ` AND U.Login NOT IN (SELECT DISTINCT(user_id) FROM aggregate.ent_non_student)`;
      }
      if (nonSessionsStr) {
        query += ` AND UA.\`Session\` NOT IN (${nonSessionsStr})`;
      }
      query += ` AND UA.\`Session\` NOT LIKE '%TEST%' AND UA.\`Session\` NOT LIKE '%test%'`;
      query += ` AND UA.UserID NOT IN (SELECT UserID FROM rel_user_user WHERE GroupID = 68)`;
      query += ` AND UA.AllParameters NOT LIKE '%usr=undefined%' AND UA.AllParameters NOT LIKE '%sid=undefined%'`;

      if (dateRange[0] && dateRange[0].length > 0) {
        query += ` AND datentime > ?`;
        params.push(dateRange[0]);
      }
      if (dateRange[1] && dateRange[1].length > 0) {
        query += ` AND datentime < ?`;
        params.push(dateRange[1]);
      }
      query += ` ORDER BY UA.UserID, UA.DateNTime ASC`;

      console.log('UM QUERY for getActivity():\n    ', query);
      const [rows] = await connection.execute(query, params) as any[];

      let currentUser: User | null = null;
      let count = 0;

      for (const row of rows) {
        count++;
        const userId = row.UserID;
        const login = row.Login;
        const utimestamp = row.utimestamp;

        if (!currentUser) {
          currentUser = new User(userId, login);
        }
        if (currentUser.userId !== userId) {
          res.set(currentUser.userLogin, currentUser);
          currentUser = new User(userId, login);
        }

        const appId = row.AppID;
        let allParameters = row.AllParameters || '';
        let activityName = '';
        let parentName = '';
        let topicName = '';
        let actOrderInCourse = -1;
        let topicOrderInCourse = -1;

        // Process parameters based on app type
        if ([AppId.WEBEX, AppId.QUIZJET, AppId.ANIMATED_EXAMPLE, AppId.KT, AppId.SQLKNOT,
             AppId.SQLTUTOR, AppId.LESSLET, AppId.PARSONS, AppId.QUIZPET, AppId.PCRS,
             AppId.PCEX_EXAMPLE, AppId.PCEX_CHALLENGE, AppId.DBQA].includes(appId)) {
          
          const codIndex = allParameters.indexOf('cod=');
          if (codIndex > 0) {
            allParameters = allParameters.substring(0, codIndex);
          }

          const allParams = allParameters.split(';');
          for (const param of allParams) {
            if (param.trim().length > 4) {
              const paramKey = param.trim().substring(0, 3);
              const value = param.trim().substring(4);

              if (paramKey === 'act') {
                switch (appId) {
                  case AppId.WEBEX:
                  case AppId.ANIMATED_EXAMPLE:
                  case AppId.LESSLET:
                  case AppId.PCRS:
                    parentName = value;
                    if (topicMap && topicMap.has(value)) {
                      const a = topicMap.get(value)!;
                      actOrderInCourse = a.orderInCourse;
                      topicOrderInCourse = a.topicOrderInCourse;
                      topicName = a.getFirstTopic();
                    }
                    if (grp === 'ASUFALL2014' && 
                        compareStringDates(row.DateNTime, '2014-09-26 00:00:00.000') < 0 &&
                        topicMap0 && topicMap0.has(value)) {
                      const a = topicMap0.get(value)!;
                      actOrderInCourse = a.orderInCourse;
                      topicOrderInCourse = a.topicOrderInCourse;
                      topicName = a.getFirstTopic();
                    }
                    break;
                  case AppId.PCEX_EXAMPLE:
                    parentName = value;
                    if (pcexActivityTopicMap && pcexActivityTopicMap.has(value)) {
                      const activity = pcexActivityTopicMap.get(value)!;
                      actOrderInCourse = activity.orderInCourse;
                      topicOrderInCourse = activity.topicOrderInCourse;
                      topicName = activity.getFirstTopic();
                    }
                    break;
                  case AppId.QUIZJET:
                    if (!topicName) {
                      if (activityNameMap && activityNameMap.has(value)) {
                        activityName = activityNameMap.get(value)!;
                        parentName = activityName;
                        if (topicMap && topicMap.has(activityName)) {
                          topicName = topicMap.get(activityName)!.getFirstTopic();
                        }
                        if (grp === 'ASUFALL2014' && 
                            compareStringDates(row.DateNTime, '2014-09-26 00:00:00.000') < 0 &&
                            topicMap0 && topicMap0.has(value)) {
                          topicName = topicMap0.get(value)!.getFirstTopic();
                        }
                      } else {
                        topicName = value;
                      }
                    }
                    break;
                  case AppId.SQLKNOT:
                    if (value.includes('Topic') || value === 'sqllab') {
                      topicName = value;
                    }
                    break;
                  case AppId.KT:
                    activityName = value;
                    break;
                }
              } else if (paramKey === 'sub') {
                switch (appId) {
                  case AppId.WEBEX:
                  case AppId.ANIMATED_EXAMPLE:
                  case AppId.LESSLET:
                    activityName = value;
                    break;
                  case AppId.QUIZJET:
                  case AppId.QUIZPET:
                  case AppId.PARSONS:
                  case AppId.DBQA:
                  case AppId.PCRS:
                    if (!activityName) {
                      activityName = value;
                      parentName = activityName;
                      if (!topicName && topicMap && topicMap.has(activityName)) {
                        const a = topicMap.get(value)!;
                        actOrderInCourse = a.orderInCourse;
                        topicOrderInCourse = a.topicOrderInCourse;
                        topicName = a.getFirstTopic();
                      }
                      if (grp === 'ASUFALL2014' && 
                          compareStringDates(row.DateNTime, '2014-09-26 00:00:00.000') < 0 &&
                          topicMap0 && topicMap0.has(value)) {
                        const a = topicMap0.get(value)!;
                        actOrderInCourse = a.orderInCourse;
                        topicOrderInCourse = a.topicOrderInCourse;
                        topicName = a.getFirstTopic();
                      }
                    }
                    break;
                }
              }
            }
          }
        }

        const act = new LoggedActivity(
          appId,
          row.Session || 'NULL',
          row.ActivityID || -1,
          activityName,
          '',
          parentName,
          topicName,
          row.Result || -1,
          row.DateNTime,
          row.DateNTimeNS || -1,
          row.SVC || '',
          actOrderInCourse,
          topicOrderInCourse,
          utimestamp || -1,
          allParameters,
          LogType.UM
        );

        currentUser.addLoggedActivity(act);
      }

      if (currentUser) {
        res.set(currentUser.userLogin, currentUser);
      }

      console.log(`#activities=${count}`);
      return res;
    } catch (error) {
      console.error('Error in getActivity:', error);
      throw error;
    } finally {
      this.releaseConnection(connection);
    }
  }

  async getActivityName(): Promise<Map<string, string>> {
    const connection = await this.getConnection();
    try {
      const query = 'SELECT ActivityID, Activity FROM ent_activity WHERE AppID IN (3, 23, 35, 37, 38, 44, 45, 46, 47, 53)';
      const [rows] = await connection.execute(query) as any[];
      console.log('UM QUERY:\n   ', query);
      const map = new Map<string, string>();
      for (const row of rows) {
        map.set(row.ActivityID.toString(), row.Activity);
      }
      return map;
    } finally {
      this.releaseConnection(connection);
    }
  }

  async getActivityIdMap(): Promise<Map<string, number>> {
    const connection = await this.getConnection();
    try {
      const query = 'SELECT Activity, ActivityID FROM ent_activity';
      const [rows] = await connection.execute(query) as any[];
      const map = new Map<string, number>();
      for (const row of rows) {
        map.set(row.Activity, row.ActivityID);
      }
      return map;
    } finally {
      this.releaseConnection(connection);
    }
  }

  async getActSubIdMap(): Promise<Map<string, number>> {
    // Note: ent_act_sub table does not exist in the current schema
    // This method is kept for compatibility but returns empty map
    return new Map<string, number>();
  }

  async getLoginUserIdMap(): Promise<Map<string, number>> {
    const connection = await this.getConnection();
    try {
      const query = 'SELECT Login, UserID, GroupID FROM ent_user WHERE IsGroup = 0';
      const [rows] = await connection.execute(query) as any[];
      const map = new Map<string, number>();
      for (const row of rows) {
        map.set(row.Login, row.GroupID);
      }
      return map;
    } finally {
      this.releaseConnection(connection);
    }
  }

  async getBadTrackedActivity(archive: boolean): Promise<string[][]> {
    const connection = await this.getConnection();
    try {
      const table = archive ? 'archive_user_activity' : 'ent_user_activity';
      const query = `
        SELECT DateNTime, DateNTimeNS, AllParameters, id 
        FROM ${table} 
        WHERE AppID = 1 AND ActivityID = 1 AND GroupID = 1 AND UserID = 2
      `;
      const [rows] = await connection.execute(query) as any[];
      return rows.map((row: any) => [
        row.DateNTime,
        row.DateNTimeNS,
        row.AllParameters,
        '', '', '', '', row.id,
      ]);
    } finally {
      this.releaseConnection(connection);
    }
  }
}

