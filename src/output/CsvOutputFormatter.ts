import { LoggedActivity } from '../models/LoggedActivity';
import { User } from '../models/User';
import { RawActivityParams } from '../services/RawActivityService';
import { formatDecimal, replaceNewLines } from '../common';

export class CsvOutputFormatter {
  private delimiter: string;

  constructor(delimiter: string) {
    this.delimiter = delimiter;
  }

  getHeader(params: RawActivityParams): string {
    const cols = [
      'user',
      'group',
      'session',
      'appid',
      'applabel',
      'activityname',
      'targetname',
      'parentname',
      'topicname',
      'result',
      'datestring',
      'durationseconds',
    ];

    if (params.timeLabels) {
      cols.push('timelabel');
    }
    if (params.includeSvc) {
      cols.push('svc');
    }
    if (params.includeAllParameters) {
      cols.push('allparameters');
    }

    return cols.join(this.delimiter) + '\n';
  }

  formatActivity(act: LoggedActivity, user: User, params: RawActivityParams): string {
    const values = [
      user.userLogin,
      '', // group will be set by caller
      act.session,
      act.appId.toString(),
      act.getLabel(),
      act.activityName,
      act.targetName,
      act.parentName,
      act.topicName,
      act.result.toString(),
      act.dateStr,
      formatDecimal(act.time),
    ];

    if (params.timeLabels) {
      values.push(act.labelTime || '');
    }
    if (params.includeSvc) {
      values.push(act.svc || '');
    }
    if (params.includeAllParameters) {
      values.push(`"${replaceNewLines(act.allParameters)}"`);
    }

    return values.join(this.delimiter) + '\n';
  }
}

