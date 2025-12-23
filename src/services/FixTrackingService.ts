import { Um2DBInterface } from '../db/Um2DBInterface';

export class FixTrackingService {
  private um2Db: Um2DBInterface;

  constructor(um2Db: Um2DBInterface) {
    this.um2Db = um2Db;
  }

  async getFixTrackingScripts(archive: boolean): Promise<string> {
    const activities = await this.um2Db.getActivityIdMap();
    const actSubIds = await this.um2Db.getActSubIdMap();
    const users = await this.um2Db.getLoginUserIdMap();
    const data = await this.um2Db.getBadTrackedActivity(archive);

    let output = '';

    for (const row of data) {
      const params = row[2].split(';');
      let appid = -1;
      let groupid = -1;
      let userid = -1;
      let activityid = -1;

      const parsedParams: Record<string, string> = {};
      for (const param of params) {
        const pair = param.split('=');
        if (pair.length === 2) {
          parsedParams[pair[0]] = pair[1];
        }
      }

      if (parsedParams['app']) {
        appid = parseInt(parsedParams['app'], 10);
      }
      if (parsedParams['grp']) {
        groupid = users.get(parsedParams['grp']) || -1;
      }
      if (parsedParams['usr']) {
        userid = users.get(parsedParams['usr']) || -1;
      }
      if (parsedParams['act'] && parsedParams['sub']) {
        activityid = actSubIds.get(`${parsedParams['act']}_${parsedParams['sub']}`) || -1;
      } else if (parsedParams['act']) {
        activityid = activities.get(parsedParams['act']) || -1;
      }

      if (appid > 0 && groupid > 0 && userid > 0 && activityid > 0) {
        const table = archive ? 'archive_user_activity' : 'ent_user_activity';
        output += `UPDATE ${table} SET appid = ${appid}, groupid = ${groupid}, userid = ${userid}, activityid = ${activityid} WHERE id = ${row[8]};\n`;
      }
    }

    return output;
  }
}

