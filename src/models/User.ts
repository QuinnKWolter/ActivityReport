import { LoggedActivity } from './LoggedActivity';
import { Sequence } from './Sequence';
import { AppId, isContent } from '../common';

export const EARLY_ATT_TH = 15;

export class User {
  userId: number;
  userLogin: string;
  activity: LoggedActivity[] = [];
  sequences: Sequence[] | null = null;
  summary: Record<string, number> = {};

  constructor(userId: number, userLogin: string) {
    this.userId = userId;
    this.userLogin = userLogin;
    this.activity = [];
    this.sequences = null;
  }

  addLoggedActivity(act: LoggedActivity): void {
    this.activity.push(act);
  }

  generateSessionIds(thresholdMins: number): void {
    if (!this.activity || this.activity.length === 0) return;

    let sessionId = 0;
    let previousAct = this.activity[0];
    previousAct.session = sessionId.toString();

    for (let i = 1; i < this.activity.length; i++) {
      const currentAct = this.activity[i];
      const diff = currentAct.date.getTime() - previousAct.date.getTime();
      if (diff > thresholdMins * 60 * 1000) {
        sessionId++;
      }
      currentAct.session = sessionId.toString();
      previousAct = currentAct;
    }
  }

  computeActivityTimes(): void {
    if (!this.activity || this.activity.length === 0) return;

    let previousAct = this.activity[0];
    previousAct.time = 0.0;

    for (let i = 1; i < this.activity.length; i++) {
      const currentAct = this.activity[i];

      if (currentAct.appId === AppId.MASTERY_GRIDS && currentAct.activityName === 'activity-done') {
        continue;
      }

      let duration = 0.0;
      if (currentAct.session === previousAct.session) {
        if (currentAct.appId === AppId.WEBEX) {
          if (i + 1 < this.activity.length) {
            const nextAct = this.activity[i + 1];
            if (currentAct.session === nextAct.session && 
                (nextAct.appId === AppId.WEBEX || nextAct.appId === AppId.MASTERY_GRIDS)) {
              duration = (nextAct.date.getTime() - currentAct.date.getTime()) / 1000.0;
            }
          }
        } else {
          duration = (currentAct.date.getTime() - previousAct.date.getTime()) / 1000.0;
        }
      }

      currentAct.time = duration;
      previousAct = currentAct;
    }
  }

  computeAttemptNo(): void {
    if (!this.activity || this.activity.length === 0) return;

    const activityAttemptNoMap = new Map<number, number>();

    for (const act of this.activity) {
      if (act.logType === 'UM') {
        const current = activityAttemptNoMap.get(act.activityId) || 0;
        activityAttemptNoMap.set(act.activityId, current + 1);
        act.attemptNo = activityAttemptNoMap.get(act.activityId)!;
      }
    }
  }
}

