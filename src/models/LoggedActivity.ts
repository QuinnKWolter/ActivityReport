import { AppId, APP_MAP } from '../common';

export enum LogType {
  AGGREGATE = 'AGGREGATE',
  UM = 'UM',
  PCEX = 'PCEX',
  PCEX_CONTROL = 'PCEX_CONTROL',
}

export class LoggedActivity {
  logType: LogType;
  appId: number = -1;
  session: string = 'NULL';
  activityId: number = -1;
  parent: number = -1; // DEPRECATED
  sessionActNo: number = 0;
  attemptNo: number = 0;
  activityName: string = '';
  targetName: string = '';
  parentName: string = '';
  topicName: string = '';
  result: number = -1;
  date: Date;
  dateStr: string;
  time: number = -1; // time in seconds
  dateNS: number = -1;
  svc: string = '';
  allParameters: string = '';
  labelTime: string = '';
  actLabel: string = '';
  actLabelReadable: string = '';
  activityCourseOrder: number = -1;
  topicOrder: number = -1;
  unixTimestamp: number = -1;
  difficulty: number = -1;
  params: Record<string, string> = {};

  constructor(
    appId: number,
    session: string,
    activityId: number,
    activityName: string,
    targetName: string,
    parentName: string,
    topicName: string,
    result: number,
    dateStr: string,
    dateNS: number,
    svc: string,
    activityCourseOrder: number,
    topicOrder: number,
    unixTimestamp: number,
    allParameters: string,
    logType: LogType
  ) {
    this.appId = appId;
    this.session = session;
    this.activityId = activityId;
    this.activityName = activityName;
    this.targetName = targetName;
    this.parentName = parentName;
    this.topicName = topicName;
    this.result = result;
    this.activityCourseOrder = activityCourseOrder;
    this.topicOrder = topicOrder;
    this.unixTimestamp = unixTimestamp;
    this.logType = logType;
    this.date = new Date(dateStr);
    this.dateStr = dateStr;
    this.dateNS = dateNS;
    this.svc = svc;
    this.allParameters = allParameters;
    this.labelTime = '';
    this.actLabel = '';
    this.actLabelReadable = '';
    this.processAllParameters();
    
    try {
      this.difficulty = this.params['difficulty'] ? parseFloat(this.params['difficulty']) : -1;
    } catch (e) {
      this.difficulty = -1;
    }
  }

  processAllParameters(): void {
    this.params = {};
    let params = this.allParameters;
    
    const codIndex = params.indexOf('cod=');
    if (codIndex > 0) {
      params = params.substring(0, codIndex);
    }

    const paramPairs = params.split(',');
    for (const param of paramPairs) {
      const pair = param.split(':');
      if (pair.length === 3) {
        pair[1] = pair[1] + ':' + pair[2];
      }
      if (pair && pair.length >= 2) {
        const p = pair[0];
        const v = pair[1] || '';
        this.params[p] = v;
      }
    }
  }

  getLabel(): string {
    return APP_MAP[this.appId] || 'OTHER';
  }

  compareTo(other: LoggedActivity): number {
    if (this.date > other.date) return 1;
    if (this.date < other.date) return -1;
    return 0;
  }
}

