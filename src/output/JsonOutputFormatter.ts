import { LoggedActivity } from '../models/LoggedActivity';
import { User } from '../models/User';
import { RawActivityParams } from '../services/RawActivityService';
import { formatDecimal } from '../common';

export class JsonOutputFormatter {
  private activities: any[] = [];

  getHeader(params: RawActivityParams): string {
    return '';
  }

  formatActivity(act: LoggedActivity, user: User, params: RawActivityParams): string {
    const activity: any = {
      user: user.userLogin,
      session: act.session,
      appid: act.appId,
      applabel: act.getLabel(),
      activityname: act.activityName,
      targetname: act.targetName,
      parentname: act.parentName,
      topicname: act.topicName,
      result: act.result,
      datestring: act.dateStr,
      durationseconds: parseFloat(formatDecimal(act.time)),
    };

    if (params.timeLabels) {
      activity.timelabel = act.labelTime || '';
    }
    if (params.includeSvc) {
      activity.svc = act.svc || '';
    }
    if (params.includeAllParameters) {
      activity.allparameters = act.allParameters;
    }

    this.activities.push(activity);
    return '';
  }

  getOutput(): string {
    return JSON.stringify(this.activities, null, 2);
  }
}

