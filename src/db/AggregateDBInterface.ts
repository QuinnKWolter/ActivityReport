import { DatabaseInterface } from './DatabaseInterface';
import { DatabaseConfig } from '../config';
import { User } from '../models/User';
import { LoggedActivity, LogType } from '../models/LoggedActivity';
import { Activity } from '../models/Activity';
import { PCEXActivity } from '../models/PCEXActivity';
import { csvFromArray, MG_ACTIVITYID_MAP } from '../common';

export class AggregateDBInterface extends DatabaseInterface {
  constructor(config: DatabaseConfig) {
    super(config);
  }

  async getActivity(
    grp: string,
    nonStudents: string[],
    nonSessions: string[],
    dateRange: string[]
  ): Promise<Map<string, User>> {
    const nonStudentsStr = csvFromArray(nonStudents);
    const nonSessionsStr = csvFromArray(nonSessions);

    const res = new Map<string, User>();
    const connection = await this.getConnection();

    try {
      let query = `
        SELECT user_id, action, session_id, datentime, UNIX_TIMESTAMP(datentime) as utimestamp 
        FROM ent_tracking 
        WHERE action NOT LIKE '%scroll%'
          AND session_id NOT LIKE '%test%' 
          AND session_id NOT LIKE '%TEST%'
          AND group_id = ?
      `;

      const params: any[] = [grp];

      if (nonStudentsStr) {
        query += ` AND user_id NOT IN (${nonStudentsStr})`;
      }
      if (nonSessionsStr) {
        query += ` AND session_id NOT IN (${nonSessionsStr})`;
      }
      if (dateRange[0] && dateRange[0].length > 0) {
        query += ` AND datentime > ?`;
        params.push(dateRange[0]);
      }
      if (dateRange[1] && dateRange[1].length > 0) {
        query += ` AND datentime < ?`;
        params.push(dateRange[1]);
      }
      query += ` ORDER BY user_id, datentime ASC`;

      console.log('AGGREGATE QUERY for getActivity():\n   ', query);
      const [rows] = await connection.execute(query, params) as any[];

      let currentUser: User | null = null;
      let count = 0;

      for (const row of rows) {
        count++;
        const login = row.user_id;
        const utimestamp = row.utimestamp;

        if (!currentUser) {
          currentUser = new User(-1, login);
        }
        if (currentUser.userLogin !== login) {
          res.set(currentUser.userLogin, currentUser);
          currentUser = new User(-1, login);
        }

        const allParameters = row.action || '';
        const params_parsed = this.processAllParameters(allParameters);
        const activityName = params_parsed[0];
        const targetName = params_parsed[1];
        const parentName = params_parsed[2];
        const topicName = params_parsed[3];
        const activityId = MG_ACTIVITYID_MAP[activityName] || -1;

        const act = new LoggedActivity(
          -1, // MASTERY_GRIDS
          row.session_id || 'NULL',
          activityId,
          activityName,
          targetName,
          parentName,
          topicName,
          -1.0,
          row.datentime,
          -1,
          '',
          -1,
          -1,
          utimestamp || -1,
          allParameters,
          LogType.AGGREGATE
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

  processAllParameters(allParameters: string): string[] {
    const res: string[] = ['', '', '', ''];
    const params = allParameters.split(',');
    for (const param of params) {
      const pair = param.split(':');
      if (pair.length === 3) {
        pair[1] = pair[1] + ':' + pair[2];
      }
      if (pair && pair.length >= 2) {
        const p = pair[0];
        const v = pair[1] || '';
        if (p === 'action') res[0] = v;
        if (p === 'activity-id' || p === 'cell-activity-id' || 
            p === 'activity-recommended-id' || p === 'cell-topic-id') {
          res[1] = v;
        }
        if (p === 'grid-name') res[2] = v;
        if (p === 'activity-topic-id' || p === 'cell-topic-id' || 
            p === 'activity-recommended-topic-id') {
          res[3] = v;
        }
      }
    }
    return res;
  }

  async getActivityTopicMap(grp: string): Promise<Map<string, Activity>> {
    const connection = await this.getConnection();
    try {
      const query = `
        SELECT DISTINCT(T.topic_name) as topic_name, C.content_name, C.provider_id, 
               T.order, TC.resource_id, TC.display_order
        FROM ent_topic T, ent_content C, rel_topic_content TC, ent_group G
        WHERE G.group_id = ?
          AND T.course_id = G.course_id
          AND TC.content_id = C.content_id
          AND TC.topic_id = T.topic_id
        ORDER BY T.order, TC.resource_id, TC.display_order
      `;
      const [rows] = await connection.execute(query, [grp]) as any[];

      console.log('AGGREGATE QUERY for getActivityTopicMap():\n   ', query);

      const res = new Map<string, Activity>();
      let contentOrder = 1;
      let topicOrder = 0;
      let previousTopic = '';

      for (const row of rows) {
        const topic = row.topic_name;
        const content = row.content_name;
        if (previousTopic !== topic) {
          previousTopic = topic;
          topicOrder++;
        }
        const a = new Activity(
          content,
          topic,
          row.provider_id,
          contentOrder,
          row.display_order,
          topicOrder
        );
        res.set(content, a);
        contentOrder++;
      }
      return res;
    } catch (error) {
      console.error('Error in getActivityTopicMap:', error);
      throw error;
    } finally {
      this.releaseConnection(connection);
    }
  }

  async getPCEXActivityTopicMap(grp: string): Promise<Map<string, PCEXActivity>> {
    const connection = await this.getConnection();
    try {
      const query = `
        SELECT DISTINCT(T.topic_name) as topic_name, C.content_name, C.provider_id, 
               T.order, TC.resource_id, TC.display_order,
               pcex_activity.act_name, pcex_activity.AppID
        FROM ent_topic T, ent_content C, rel_topic_content TC, ent_group G,
        (SELECT A1.activity AS set_name, A2.activity AS act_name, A2.AppID 
         FROM um2.ent_activity A1, um2.ent_activity A2, um2.rel_pcex_set_component AA1
         WHERE A1.AppID = 45 AND (A2.AppID = 46 OR A2.AppID = 47) 
           AND AA1.ParentActivityID = A1.ActivityID 
           AND AA1.ChildActivityID = A2.ActivityID) as pcex_activity
        WHERE G.group_id = ?
          AND T.course_id = G.course_id
          AND TC.content_id = C.content_id
          AND TC.topic_id = T.topic_id
          AND (pcex_activity.set_name = C.content_name OR pcex_activity.act_name = C.content_name)
        ORDER BY T.order, TC.resource_id, TC.display_order
      `;
      const [rows] = await connection.execute(query, [grp]) as any[];

      console.log('AGGREGATE QUERY for getPCEXActivityTopicMap():\n   ', query);

      const result = new Map<string, PCEXActivity>();
      let contentOrder = 1;
      let topicOrder = 0;
      let previousTopic = '';

      for (const row of rows) {
        const topic = row.topic_name;
        const content = row.content_name;
        const actName = row.act_name;
        const isChallenge = row.AppID === '47';

        if (previousTopic !== topic) {
          previousTopic = topic;
          topicOrder++;
        }

        const contentActivity = new PCEXActivity(
          actName,
          isChallenge,
          content,
          topic,
          row.provider_id,
          contentOrder,
          row.display_order,
          topicOrder
        );

        result.set(actName, contentActivity);
        contentOrder++;
      }
      return result;
    } catch (error) {
      console.error('Error in getPCEXActivityTopicMap:', error);
      throw error;
    } finally {
      this.releaseConnection(connection);
    }
  }

  async getPCEXActivitySet(): Promise<any[]> {
    const connection = await this.getConnection();
    try {
      const query = `
        SELECT 
          activity1.ActivityID AS ActivitySetID,
          COUNT(activity2.Activity) AS NumCh,
          GROUP_CONCAT(DISTINCT pcex_set.ChildActivityID SEPARATOR ',') as Activities
        FROM um2.rel_pcex_set_component AS pcex_set,
             um2.ent_activity AS activity1,
             um2.ent_activity AS activity2
        WHERE pcex_set.ParentActivityID = activity1.ActivityID
          AND pcex_set.ChildActivityID = activity2.ActivityID
          AND activity1.AppID = 45
          AND activity2.AppID = 47
        GROUP BY ActivitySetID
      `;
      const [rows] = await connection.execute(query) as any[];
      console.log('QUERY for getPCEXActivitySet():\n   ', query);
      return rows;
    } finally {
      this.releaseConnection(connection);
    }
  }

  async getUrlToActivityName(): Promise<Map<string, string>> {
    const connection = await this.getConnection();
    try {
      const query = `SELECT content_name, url FROM ent_content WHERE provider_id = 'sqlknot'`;
      const [rows] = await connection.execute(query) as any[];
      console.log('AGGREGATE QUERY:\n   ', query);

      const res = new Map<string, string>();
      for (const row of rows) {
        let url = row.url;
        if (!url.includes('cid') || !url.includes('tid')) {
          console.log(`ERROR: url doesn't contain cid or tid (${url})`);
        } else {
          const suburl = url.substring(url.indexOf('cid'));
          url = 'Topic' + suburl.substring(4, suburl.indexOf('&tid')) +
                '_Template' + suburl.substring(suburl.indexOf('tid') + 4, 
                suburl.indexOf('&', suburl.indexOf('tid')));
          if (suburl.includes('lang')) {
            url += '_' + suburl.substring(suburl.indexOf('lang=') + 5);
          }
        }
        const activity = row.content_name;
        res.set(url, activity);
      }
      return res;
    } catch (error) {
      console.error('Error in getUrlToActivityName:', error);
      throw error;
    } finally {
      this.releaseConnection(connection);
    }
  }
}

