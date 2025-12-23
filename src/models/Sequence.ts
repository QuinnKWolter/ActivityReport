import { LoggedActivity } from './LoggedActivity';

export class Sequence {
  activity: LoggedActivity[] = [];
  sequenceId: number;
  groupingId: string = '';

  constructor(sequenceId: number) {
    this.sequenceId = sequenceId;
    this.activity = [];
    this.groupingId = '';
  }

  toString(delimiter1: string, delimiter2: string): string[] {
    const r: string[] = ['', ''];
    for (const a of this.activity) {
      r[0] += a.actLabel + delimiter1;
      r[1] += a.actLabelReadable + delimiter2;
    }
    if (r[0].length > 0) {
      r[0] = r[0].substring(0, r[0].length - delimiter1.length);
    }
    if (r[1].length > 0) {
      r[1] = r[1].substring(0, r[1].length - delimiter2.length);
    }
    return r;
  }

  totalTime(): number {
    let t = 0.0;
    if (this.activity) {
      for (const a of this.activity) {
        t += a.time;
      }
    }
    return t;
  }

  size(): number {
    return this.activity ? this.activity.length : 0;
  }

  getFirstDate(): string {
    if (this.activity && this.activity.length > 0) {
      return this.activity[0].dateStr;
    }
    return '';
  }

  getLastDate(): string {
    if (this.activity && this.activity.length > 0) {
      return this.activity[this.activity.length - 1].dateStr;
    }
    return '';
  }
}

