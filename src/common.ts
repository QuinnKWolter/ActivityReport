export const MAX_ACTIVITY_TIME = 600.0; // in seconds
export const MIN_MOUSEOVER_TIME = 1;

export enum AppId {
  QUIZPACK = 2,
  WEBEX = 3,
  KNOWLEDGE_SEA = 5,
  KT = 8,
  SQLTUTOR = 19,
  QUIZGUIDE = 20,
  SQLKNOT = 23,
  QUIZJET = 25,
  ANIMATED_EXAMPLE = 35,
  MASTERY_GRIDS = -1,
  QUIZPET = 41,
  PARSONS = 38,
  LESSLET = 37,
  PCRS = 44,
  PCEX_EXAMPLE = 46,
  PCEX_CHALLENGE = 47,
  DBQA = 53,
}

export const APP_MAP: Record<number, string> = {
  [AppId.QUIZPACK]: 'QUIZPACK',
  [AppId.WEBEX]: 'WEBEX',
  [AppId.KNOWLEDGE_SEA]: 'KNOWLEDGE_SEA',
  [AppId.KT]: 'KT',
  [AppId.SQLTUTOR]: 'SQLTUTOR',
  [AppId.QUIZGUIDE]: 'QUIZGUIDE',
  [AppId.SQLKNOT]: 'SQLKNOT',
  [AppId.QUIZJET]: 'QUIZJET',
  [AppId.ANIMATED_EXAMPLE]: 'ANIMATED_EXAMPLE',
  [AppId.LESSLET]: 'LESSLET',
  [AppId.PARSONS]: 'PARSONS',
  [AppId.QUIZPET]: 'QUIZPET',
  [AppId.PCRS]: 'PCRS',
  [AppId.PCEX_EXAMPLE]: 'PCEX_EXAMPLE',
  [AppId.PCEX_CHALLENGE]: 'PCEX_CHALLENGE',
  [AppId.DBQA]: 'DBQA',
  [AppId.MASTERY_GRIDS]: 'MASTERY_GRIDS',
};

export const MG_ACTIVITYID_MAP: Record<string, number> = {
  '': 990000001,
  'app-start': 990000002,
  'data-load-start': 990000003,
  'data-load-end': 990000004,
  'app-ready': 990000005,
  'group-set': 990000006,
  'grid-activity-cell-select': 990000007,
  'activity-open': 990000008,
  'activity-reload': 990000009,
  'activity-done': 990000010,
  'activity-close': 990000011,
  'activity-load-recommended': 990000012,
  'activity-load-original': 990000013,
  'load-others-list': 990000014,
  'resource-set': 990000015,
  'activity-feedback-set-difficulty': 990000016,
  'grid-topic-cell-select': 990000017,
  'comparison-mode-set': 990000018,
};

export const NON_STUDENTS = [
  'anonymous_user', 'fedor.bakalov', 'nkresl', 'maccloud',
  'moeslein', 'mliang', 'pjcst19', 'fseels', 'r.hosseini', 'ltaylor',
  'peterb', 'shoha99', 'jennifer', 'dguerra', 'demo01', 'demo02', 'demo03',
  'ddicheva', 'bcaldwell', 'sibelsomyurek', 'somyurek', 'jdg60', 'demo04', 'kerttupollari',
  'yuh43', 'alto15instructor', 'johnramirez', 'regan', 'sherry', 'billlaboon', 'daqing',
  'rafael.araujo', 'prmenon', 'aaltoinstructor', 'tmprinstructor', 'dmb72', 'test0002', 'test0003',
  'experimental_test1', 'test0001', 'akhuseyinoglu', 'tanja.mitrovic', 'cht77', 'jiangqiang', 'ykortsarts', 'mab650',
  'arl122', 'arunb', 'rah225', 'ruhendrawan', 'moh70', 'ras555', 'qkw3', 'jab464',
];

export const NON_SESSIONS = [
  'null', 'undefined', 'xxx', 'aaaaa', 'bbbbb', 'fffff',
  'XXXX', 'xxxx', 'XXXXX', 'xxxxx', 'xxxyyy', 'YYYYY', 'YYYY',
];

export function isContent(appId: number): boolean {
  return [
    AppId.QUIZJET, AppId.QUIZPET, AppId.PARSONS, AppId.QUIZPACK,
    AppId.SQLKNOT, AppId.SQLTUTOR, AppId.WEBEX, AppId.ANIMATED_EXAMPLE,
    AppId.LESSLET, AppId.PCRS, AppId.PCEX_EXAMPLE, AppId.PCEX_CHALLENGE, AppId.DBQA,
  ].includes(appId);
}

export function csvFromArray(values: string[]): string | null {
  if (!values || values.length === 0) {
    return null;
  }
  return values.map(v => `'${v}'`).join(',');
}

export function formatDecimal(value: number): string {
  return value.toFixed(2);
}

export function compareStringDates(date1: string, date2: string): number {
  const d1 = new Date(date1);
  const d2 = new Date(date2);
  if (d1 < d2) return -1;
  if (d1 > d2) return 1;
  return 0;
}

export function replaceNewLines(s: string): string {
  return s.replace(/\n/g, '\\n');
}

